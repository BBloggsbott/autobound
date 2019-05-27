package org.openstreetmap.josm.plugins.autobound;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.SelectionManager;
import org.openstreetmap.josm.gui.SelectionManager.SelectionEnded;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

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
     * @param mapFrame The MapFrame, whose autobound mode should be enabled.
     */
    //TODO : Modify Icon names
    public AutoBoundAction(MapFrame mapFrame, String serverUrl){
        super(tr("AutoBound Action"), "zoom",
                tr("Generate nodes using AutoBound"), Shortcut.registerShortcut("mapmode:autobound", tr("Mode: {0}", tr("AutoBound mode")),KeyEvent.CHAR_UNDEFINED,Shortcut.NONE),
                ImageProvider.getCursor("normal","zoom"));
        this.selectionManager = new SelectionManager(this, true, mapFrame.mapView);
        this.serverUrl = serverUrl;
    }

    //TODO : Modify selectionEnded method by getting MapView inside selection, encoding it and passing it to the server
    @Override
    public void selectionEnded(Rectangle r, MouseEvent e) {
        System.out.println("[INFO] x1:"+r.x+" y1:"+r.y+" x2:"+(r.x+r.width)+" y2:"+(r.y+r.height));
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
