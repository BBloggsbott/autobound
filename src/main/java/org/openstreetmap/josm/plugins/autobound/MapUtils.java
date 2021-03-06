package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
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
import java.util.concurrent.TimeUnit;

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
    private static int delay;

    static void setDelay(int delaySecs){
        delay = delaySecs;
    }

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

            try{
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException ie) {
                Logging.error(ie);
            }
            // Actual Drawing
            BufferedImage image = new BufferedImage((int) toPaint.getInView().getWidth(), (int) toPaint.getInView().getHeight(), BufferedImage.TYPE_BYTE_INDEXED);

            Graphics2D graphics = image.createGraphics();
            // Move so that the image matches the region we are painting
            graphics.translate(-toPaint.getInView().getMinX(), -toPaint.getInView().getMinY());
            painter.paint(new MapViewGraphics(fakeMapView, graphics, toPaint));


            graphics.dispose();
            painter.detachFromMapView(new MapViewPaintable.MapViewEvent(fakeMapView, false));

            reset.run();

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

    /**
     * Return the imagery inside a given ProjectionBounds
     * @param bounds ProjectionBounds to get imagery or
     * @return Imagery inside the ProjectionBounds
     */
    public static BufferedImage getSatelliteImage(ProjectionBounds bounds){
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
            fakeMapView.zoomTo(bounds);
            try{
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                Logging.error("Interrupted Exception while waiting for image to render");
                e.printStackTrace();
            }

            //Find selected area in fakes map view space
            MapViewState.MapViewRectangle toPaint = fakeMapView.getState().getPointFor(bounds.getMin())
                    .rectTo(fakeMapView.getState().getPointFor(bounds.getMax()));

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

    /**
     * Get the bounds in east north form for a way
     * @param way Way to calculate bounds for
     * @return ProjectionBounds
     */
    public static ProjectionBounds getBoundsForWay(Way way){
        BoundingXYVisitor visitor = new BoundingXYVisitor();
        visitor.visit(way);
        return visitor.getBounds();
    }

    public static ProjectionBounds getProjectionBoundsForImage(ProjectionBounds bounds){
        EastNorth min = bounds.getMin();
        EastNorth max = bounds.getMax();
        EastNorth center = bounds.getCenter();

        // padding min east by half the size of the building
        double newMinEast = min.east() - (center.east() - min.east());
        // padding min north by half the size of the building
        double newMinNorth = min.north() - (center.north() - min.north());
        // padding max east by half the size of the building
        double newMaxEast = max.east() + (max.east() - center.east());
        // padding max north by half the size of the building
        double newMaxNorth = max.north() + (max.north() - center.north());

        ProjectionBounds newBounds = new ProjectionBounds(newMinEast, newMinNorth, newMaxEast, newMaxNorth);
        return newBounds;
    }
}
