package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MainLayerManager;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Logging;

import java.awt.image.BufferedImage;
import java.util.List;

public class MapUtils {

    //TODO : Modify to get the last background layer for satellite image.
    public static BufferedImage getSatelliteImage(){
        MapView mapView = MainApplication.getMap().mapView;
        BufferedImage image = new BufferedImage(mapView.getWidth(), mapView.getHeight(), BufferedImage.TYPE_INT_RGB);
        mapView.paintAll(image.getGraphics());
        return image;
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
