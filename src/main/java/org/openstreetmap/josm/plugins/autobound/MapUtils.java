package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;

import java.awt.image.BufferedImage;

public class MapUtils {

    //TODO : Modify to get the last background layer for satellite image.
    public static BufferedImage getSatelliteImage(){
        MapView mapView = MainApplication.getMap().mapView;
        BufferedImage image = new BufferedImage(mapView.getWidth(), mapView.getHeight(), BufferedImage.TYPE_INT_RGB);
        mapView.paintAll(image.getGraphics());
        return image;
    }
}
