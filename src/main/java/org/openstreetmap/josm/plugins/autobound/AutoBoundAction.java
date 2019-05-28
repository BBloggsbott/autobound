package org.openstreetmap.josm.plugins.autobound;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.*;
import org.openstreetmap.josm.gui.SelectionManager.SelectionEnded;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.imageio.ImageIO;

/**
 * Enable AutoBound functionality within MapFrame.
 *
 * @author bbloggsbott
 */

public class AutoBoundAction extends MapMode implements SelectionEnded {

    /**
     * Manager that manages the selection rectangle with the aspect ration of the MapView
     */
    private final SelectionManager selectionManager;
    private final String serverUrl;

    /**
     * Construct an AutoBoundAction without a Label
     *
     * @param mapFrame The MapFrame, whose autobound mode should be enabled.
     */
    //TODO : Reduce icon size for cursor
    public AutoBoundAction(MapFrame mapFrame, String serverUrl) {
        super(tr("AutoBound Action"), "autobound",
                tr("Generate nodes using AutoBound"), Shortcut.registerShortcut("mapmode:autobound", tr("Mode: {0}", tr("AutoBound mode")), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                ImageProvider.getCursor("normal", "autobound"));
        this.selectionManager = new SelectionManager(this, true, mapFrame.mapView);
        this.serverUrl = serverUrl;
    }

    //TODO : Modify selectionEnded method by getting MapView inside selection, encoding it and passing it to the server
    @Override
    public void selectionEnded(Rectangle r, MouseEvent e) {
        MapView mapView = MainApplication.getMap().mapView;
        /*MapViewState mapViewState = mapView.getState();
        MapViewState.MapViewRectangle viewArea = mapViewState.getViewArea();
        Bounds latLongBounds = viewArea.getLatLonBoundsBox();   //Use this to calculate offsets

        //Get Satellite Image and encode it
        List<Layer> layers = MainApplication.getLayerManager().getVisibleLayersInZOrder();
        Layer lastBackgroundLayer = null;

        for (Layer layer : layers) {       //Iterate over all layers to select the last background layer
            if (layer.isBackgroundLayer()) {
                lastBackgroundLayer = layer;
            }
        }*/

        try {
            Dimension d = mapView.getSize();
            Rectangle rect = new Rectangle(d);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rect);     //Causes program to crash with exit code 134
            //ImageIO.write(image, ".jpg", new File("temp_mapView.jpg"));
        } catch (AWTException awe) {
            Logging.error("AWTException when creating Robot object");
        }

    }

    @Override
    public void enterMode() {
        super.enterMode();
        selectionManager.register(MainApplication.getMap().mapView, false);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        selectionManager.unregister(MainApplication.getMap().mapView);
    }

    @Override
    public String getModeHelpText() {
        return tr("Left Click and drag to select area for to detect buildings and generate boundaries");
    }
}
