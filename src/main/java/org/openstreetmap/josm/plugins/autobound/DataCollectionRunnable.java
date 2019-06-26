package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.JOptionPane;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static org.openstreetmap.josm.tools.I18n.tr;

public class DataCollectionRunnable implements Runnable {

    private Thread downloadThread;
    private final NetworkUtils networkUtils;
    private static int threadNo = 0;
    private PleaseWaitProgressMonitor progressMonitor;

    DataCollectionRunnable(NetworkUtils networkUtils){
        this.networkUtils = networkUtils;
        this.progressMonitor = new PleaseWaitProgressMonitor("Downloading Data");
    }

    @Override
    public void run() {
        OsmDataLayer dataLayer = MainApplication.getLayerManager().getActiveDataLayer();
        DataSet dataset = dataLayer.getDataSet();
        ArrayList<Way> buildings = DataUtils.getBuildingsFromDataSet(dataset);
        int successfulSaves = 0;
        int totalBuildings = buildings.size();
        progressMonitor.beginTask("Downloading Data", totalBuildings);
        progressMonitor.appendLogMessage("Found "+totalBuildings+" buildings.");
        ProjectionBounds bounds;
        BufferedImage image;
        JSONObject data;
        String response;
        try{
            for (Way building : buildings){
                progressMonitor.setCustomText("Saving Image "+(successfulSaves+1));
                bounds = MapUtils.getProjectionBoundsForImage(MapUtils.getBoundsForWay(building));
                image = MapUtils.getSatelliteImage(bounds);
                if(image == null){
                    progressMonitor.appendLogMessage("Could not get satellite Image");
                    break;
                }
                data = DataUtils.createJSON(image, bounds, building, MapUtils.getDist100Pixel());
                response = networkUtils.sendToServer(data);
                if(response.equalsIgnoreCase("success")){
                    progressMonitor.appendLogMessage("Saved building "+(successfulSaves+1));
                    successfulSaves +=1;
                    if(!progressMonitor.isCanceled()){
                        progressMonitor.worked(1);
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException ioe){
            Logging.error("Error While communicating with server");
        } finally {
            progressMonitor.finishTask();
            progressMonitor.close();
        }
        JOptionPane.showMessageDialog(
                MainApplication.getMainFrame(),
                tr("Successfully saved "+successfulSaves+" images out of "+totalBuildings+" images."),
                tr("Save Complete"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void start (){
        if (downloadThread == null){
            downloadThread = new Thread(this, "AutoBound Data Collector "+threadNo);
            threadNo+=1;
            downloadThread.start();
        }
    }
}
