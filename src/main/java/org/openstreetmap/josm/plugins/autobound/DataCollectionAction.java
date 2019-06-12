package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Action class to collect imagery data
 */
public class DataCollectionAction extends JosmAction {

    private NetworkUtils networkUtils = null;

    /**
     * Create a new DataCollectionAction object
     * @param dataCollectionServerUrl URL to the server that saves data
     */
    public DataCollectionAction(String dataCollectionServerUrl){
        super(tr("Collect Data - AutoBound"), "autobound",
                tr("Collect the data of every building in the Data Layer and send them to the Data Collection server for processing and saving"),
                Shortcut.registerShortcut("autobound:collectdata", tr("Tools: {0}", tr("Collect Data - AutoBound")),
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE), true);
        try{
            networkUtils = new NetworkUtils(dataCollectionServerUrl);
        } catch (MalformedURLException mue){
            Logging.error("Malformed Data Collection Server Url");
        }
    }

    //TODO : Add ProgressMonitor
    @Override
    public void actionPerformed(ActionEvent e) {
        PleaseWaitProgressMonitor progressMonitor = new PleaseWaitProgressMonitor("Collecting Data");
        if(networkUtils != null){
            progressMonitor.setCancelable(false);

            OsmDataLayer dataLayer = MainApplication.getLayerManager().getActiveDataLayer();
            DataSet dataset = dataLayer.getDataSet();
            ArrayList<Way> buildings = DataUtils.getBuildingsFromDataSet(dataset);
            BufferedImage image;
            ProjectionBounds bounds;
            JSONObject data;
            String response;
            int successfulSaves = 0, totalBuildings = buildings.size();
            progressMonitor.appendLogMessage("Found "+totalBuildings+" buildings.");
            progressMonitor.doBeginTask();
            progressMonitor.setTicks(totalBuildings);


            try{
                for (Way building : buildings){
                    bounds = MapUtils.getProjectionBoundsForImage(MapUtils.getBoundsForWay(building));
                    image = MapUtils.getSatelliteImage(bounds);
                    if(image == null){
                        progressMonitor.appendLogMessage("Could not find imagery");
                        progressMonitor.close();
                        break;
                    }
                    data = DataUtils.createJSON(image, bounds, building, MapUtils.getDist100Pixel());
                    response = networkUtils.sendToServer(data);
                    if(response.equalsIgnoreCase("success")){
                        successfulSaves +=1;
                    }
                    progressMonitor.worked(successfulSaves);
                }
            } catch (IOException ioe){
                Logging.error("Error While communicating with server");
            }
            JOptionPane.showMessageDialog(
                    MainApplication.getMainFrame(),
                    tr("Successfully saved "+successfulSaves+" images out of "+totalBuildings+" images."),
                    tr("Save Complete"),
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            Logging.error("Network not configured.");
        }
        progressMonitor.doFinishTask();
        progressMonitor.close();
    }
}
