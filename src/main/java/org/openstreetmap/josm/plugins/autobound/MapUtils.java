package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapViewState;
import org.openstreetmap.josm.gui.layer.*;
import org.openstreetmap.josm.gui.layer.imagery.TileCoordinateConverter;
import org.openstreetmap.josm.gui.layer.imagery.TileSourceDisplaySettings;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for Map related operations
 *
 * @author bbloggsbott
 * @author michaelzangl
 */
import static org.openstreetmap.josm.tools.I18n.tr;

public class MapUtils {

    /**
     * Stores the number of meters in 100 pixel for the image generated when getSatelliteImage is called
     */
    private static double dist100Pixel = -1.0;

    /**
     * Extract and return the imagery of the selected area in the map view
     * @param r The selected area
     * @return Imagery of the selected area
     */
    public static BufferedImage getSatelliteImage(Rectangle r){
        MapView mv = MainApplication.getMap().mapView;
        MapViewState.MapViewRectangle selectedArea = mv.getState().getViewArea(r);
        ProjectionBounds selectedInEastNorth = selectedArea.getProjectionBounds();

        Optional<Layer> layerOpt = MainApplication.getLayerManager().getLayers().stream()
                .filter(l -> l.isBackgroundLayer() && l.isVisible())
                .findFirst();
        if(layerOpt.isPresent()) {
            Layer layer = layerOpt.get();

            MainLayerManager layersToRender = new MainLayerManager();
            layersToRender.addLayer(layer);
            MapView fakeMapView = new MapView(layersToRender, null) {
                {
                    setBounds(0, 0, 1024, 1024);   // < max rendering size
                    updateLocationState();
                }

                @Override
                protected boolean isVisibleOnScreen() {
                    return true;
                }

                @Override
                public Point getLocationOnScreen() {
                    return new Point(0, 0);
                }
            };
            Runnable reset = () -> {
            };
            if (layer instanceof AbstractTileSourceLayer) {
                try {
                    Field coordinateConverterField = AbstractTileSourceLayer.class.getDeclaredField("coordinateConverter");
                    coordinateConverterField.setAccessible(true);
                    Field tileSourceField = TileCoordinateConverter.class.getDeclaredField("tileSource");
                    Field settingsField = TileCoordinateConverter.class.getDeclaredField("settings");
                    TileCoordinateConverter oldConverter = (TileCoordinateConverter) coordinateConverterField.get(layer);
                    tileSourceField.setAccessible(true);
                    settingsField.setAccessible(true);
                    TileCoordinateConverter newConverter = new TileCoordinateConverter(fakeMapView,
                            (TileSource) tileSourceField.get(oldConverter),
                            (TileSourceDisplaySettings) settingsField.get(oldConverter));
                    coordinateConverterField.set(layer, newConverter);
                    reset = () -> {
                        try {
                            coordinateConverterField.set(layer, oldConverter);
                        } catch (IllegalAccessException iae) {
                            Logging.warn(iae);
                        }
                    };
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    Logging.error("ex");
                }
            }
            MapViewPaintable.LayerPainter painter = layer.attachToMapView(new MapViewPaintable.MapViewEvent(fakeMapView, false));
            //This will not exactly zoom to that area, but instead to an area close to it depending on the native scale of the background layer.
            fakeMapView.zoomTo(selectedArea.getCornerBounds());
            //Find selected area in fakes map view space
            MapViewState.MapViewRectangle toPaint = fakeMapView.getState().getPointFor(selectedInEastNorth.getMin())
                    .rectTo(fakeMapView.getState().getPointFor(selectedInEastNorth.getMax()));

            // Actual Drawing
            BufferedImage image = new BufferedImage((int) toPaint.getInView().getWidth(), (int) toPaint.getInView().getHeight(), BufferedImage.TYPE_BYTE_INDEXED);

            Graphics2D graphics = image.createGraphics();
            // Move so that the image matches the region we are painting
            graphics.translate(-toPaint.getInView().getMinX(), -toPaint.getInView().getMinY());
            painter.paint(new MapViewGraphics(fakeMapView, graphics, toPaint));

            graphics.dispose();
            painter.detachFromMapView(new MapViewPaintable.MapViewEvent(fakeMapView, false));

            reset.run();

            dist100Pixel = fakeMapView.getDist100Pixel();
            return image;
        } else {
            JOptionPane.showMessageDialog(
                    MainApplication.getMainFrame(),
                    tr("No imagery layer found"),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE
            );
        }
        return null;
    }


    public static double getDist100Pixel(){
        return dist100Pixel;
    }

    /**
     * Add a dataset to the AutBound data layer
     * @param from Dataset to add primitives from
     */
    public static void addDataSetToDataLayer(DataSet from){
        MainLayerManager mainLayerManager = MainApplication.getLayerManager();
        List<OsmDataLayer> layers = mainLayerManager.getLayersOfType(OsmDataLayer.class);
        for (OsmDataLayer layer: layers){
            if(layer.getName().equalsIgnoreCase("AutoBoundData")){
                Logging.info("AutoBoundData layer exists. Merging data");
                layer.mergeFrom(from);
                Logging.info("Merged Data");
                return;
            }
        }
        OsmDataLayer newLayer = new OsmDataLayer(from,"AutoBoundData", null);
        mainLayerManager.addLayer(newLayer);
    }
}
