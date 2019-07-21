package org.openstreetmap.josm.plugins.autobound;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONObject;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.projection.CustomProjection;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.*;
import org.openstreetmap.josm.gui.SelectionManager.SelectionEnded;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
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
     *
     * @param mapFrame The MapFrame, whose autobound mode should be enabled.
     */
    public AutoBoundAction(MapFrame mapFrame, String serverUrl) {
        super(tr("AutoBound Action"), "autobound",
                tr("Generate nodes using AutoBound"), Shortcut.registerShortcut("mapmode:autobound", tr("Mode: {0}", tr("AutoBound mode")), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                ImageProvider.getCursor("normal", "autobound"));
        this.selectionManager = new SelectionManager(this, true, mapFrame.mapView);
        this.serverUrl = serverUrl;
    }

    @Override
    public void selectionEnded(Rectangle r, MouseEvent e) {
        MapView mv = MainApplication.getMap().mapView;
        MapViewState.MapViewRectangle selectedArea = mv.getState().getViewArea(r);
        ProjectionBounds selectedInEastNorth = selectedArea.getProjectionBounds();
        Bounds bounds = selectedArea.getLatLonBoundsBox();
        BufferedImage image = MapUtils.getSatelliteImage(selectedInEastNorth);
        String response=null;
        NetworkUtils networkUtils=null;
        DataSet dataset = null;
        try {
            networkUtils = new NetworkUtils(serverUrl);
            JSONObject data = DataUtils.createJson(image, bounds.getMinLat(), bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon());
            response = networkUtils.sendToServer(data);
            dataset = DataUtils.xmlToDataSet(DataUtils.responseToInputStream(response));
        } catch (MalformedURLException mue) {
            Logging.error("Malformed AutoBound server URL");
            return;
        } catch (IOException ioe) {
            Logging.error("Error while communicating with the server");
            return;
        } catch (IllegalDataException ide){
            Logging.error("Error while parsing response from server");
            ide.printStackTrace();
        }
        MapUtils.addDataSetToDataLayer(dataset);
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
