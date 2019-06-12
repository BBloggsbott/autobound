package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
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

    public DataCollectionRunnable(NetworkUtils networkUtils){
        this.networkUtils = networkUtils;
    }

    @Override
    public void run() {
        OsmDataLayer dataLayer = MainApplication.getLayerManager().getActiveDataLayer();
        DataSet dataset = dataLayer.getDataSet();
        ArrayList<Way> buildings = DataUtils.getBuildingsFromDataSet(dataset);
        int successfulSaves = 0;
        int totalBuildings = buildings.size();
        ProjectionBounds bounds;
        BufferedImage image;
        JSONObject data;
        String response;
        try{
            for (Way building : buildings){
                bounds = MapUtils.getProjectionBoundsForImage(MapUtils.getBoundsForWay(building));
                image = MapUtils.getSatelliteImage(bounds);
                if(image == null){
                    break;
                }
                data = DataUtils.createJSON(image, bounds, building, MapUtils.getDist100Pixel());
                response = networkUtils.sendToServer(data);
                if(response.equalsIgnoreCase("success")){
                    successfulSaves +=1;
                }
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
    }

    public void start (){
        if (downloadThread == null){
            downloadThread = new Thread(this, "AutoBound Data Collector "+threadNo);
            threadNo+=1;
            downloadThread.start();
        }
    }
}
