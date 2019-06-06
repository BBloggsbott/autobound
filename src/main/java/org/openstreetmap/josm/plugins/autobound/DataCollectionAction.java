package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Action class to collect imagery data
 */
public class DataCollectionAction extends MapMode implements ActionListener {

    private NetworkUtils networkUtils = null;

    /**
     * Create a new DataCollectionAction object
     * @param dataCollectionServerUrl URL to the server that saves data
     */
    public DataCollectionAction(String dataCollectionServerUrl){
        super(tr("Data Collect Action"), "autobound",
                tr("Collect and save data for AutoBound"), Shortcut.registerShortcut("mapmode:autobounddatacollect", tr("Mode: {0}", tr("AutoBoundDataCollect mode")), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                ImageProvider.getCursor("normal", "autobound"));
        try{
            networkUtils = new NetworkUtils(dataCollectionServerUrl);
        } catch (MalformedURLException mue){
            Logging.error("Malformed Data Collection Server Url");
        }
    }

    //TODO : Add ProgressMonitor
    @Override
    public void actionPerformed(ActionEvent e) {
        if(networkUtils != null){
            OsmDataLayer dataLayer = MainApplication.getLayerManager().getActiveDataLayer();
            DataSet dataset = dataLayer.getDataSet();
            ArrayList<Way> buildings = DataUtils.getBuildingsFromDataSet(dataset);
            BufferedImage image;
            ProjectionBounds bounds;
            JSONObject data;
            String response;
            int successfulSaves = 0, failedSaves = 0;

            try{
                for (Way building : buildings){
                    bounds = MapUtils.getProjectionBoundsForImage(MapUtils.getBoundsForWay(building));
                    image = MapUtils.getSatelliteImage(bounds);
                    data = DataUtils.createJSON(image, bounds, building, MapUtils.getDist100Pixel());
                    response = networkUtils.sendToServer(data);
                    if(response.equalsIgnoreCase("success")){
                        successfulSaves +=1;
                    } else {
                        failedSaves += 1;
                    }
                }
            } catch (IOException ioe){
                Logging.error("Error While communicating with server");
            }
            JOptionPane.showMessageDialog(
                    MainApplication.getMainFrame(),
                    tr("Successfully saved "+successfulSaves+" images out of "+(successfulSaves+failedSaves)+" images."),
                    tr("Save Complete"),
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            Logging.error("Network not configured.");
        }
    }
}
