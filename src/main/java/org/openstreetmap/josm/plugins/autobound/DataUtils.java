package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.tools.Logging;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Class with methods that help process data.
 * @author bbloggsbott
 */

public class DataUtils {

    /**
     * Create a new object for DataUtils
     */
    public DataUtils(){}

    /**
     * Create JSON object that is to be passed to the server
     * @param image The buffered image of the satellite image in the area selected by the user.
     * @param min_lat Minimum latitude of the selected are
     * @param min_lon Minimum longitude of the selected area
     * @param max_lat Maximum latitude of the selected are
     * @param max_lon Maximum longitude of the selected area
     * @return JSONObject with the Base64 encoded image and the two offset values
     */
    public static JSONObject createJson(BufferedImage image, double min_lat, double min_lon, double max_lat, double max_lon){
        JSONObject data = new JSONObject();
        data.put("image",encodeImage(image));
        data.put("min_lat",min_lat);
        data.put("min_lon",min_lon);
        data.put("max_lat",max_lat);
        data.put("max_lon",max_lon);
        return data;
    }

    public static JSONObject createJSON(BufferedImage image, ProjectionBounds imageBounds, Way way, double dist100pixel){
        JSONObject data = new JSONObject();
        JSONObject wayJson = new JSONObject();
        JSONObject nodeJson;
        List<JSONObject> nodeList = new ArrayList<>();
        List<Node> nodes = way.getNodes();
        int nodeCount = 0;
        EastNorth en;
        long timestamp = System.currentTimeMillis() / 1000L;


        for (Node node : nodes){
            en = node.getEastNorth();
            nodeJson = new JSONObject();
            nodeJson.put("east",en.east());
            nodeJson.put("north", en.north());
            nodeList.add(nodeJson);
        }

        data.put("timestamp", timestamp);

        data.put("image", DataUtils.encodeImage(image));

        en = imageBounds.getMin();
        data.put("minEast", en.east());
        data.put("minNorth", en.north());

        en = imageBounds.getMax();
        data.put("maxEast", en.east());
        data.put("maxNorth", en.north());

        wayJson.put("id", way.getId());
        wayJson.put("nodes", nodeList.toArray());
        data.put("way", wayJson);

        data.put("dist100pixel", dist100pixel);

        return data;
    }

    /**
     * Method to Base64 encode an Image
     * @param image The image to be encoded
     * @return Base64 encoded image as a String
     */
    public static String encodeImage(BufferedImage image){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String encodedImage;
        try{
            ImageIO.write(image, "png", os);
            encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
        }
        catch(final IOException ioe){
            throw new UncheckedIOException(ioe);
        }
        return encodedImage;
    }


    /**
     * Convert xml from an input stream to a Dataset
     * @param source InputStream to read data from
     * @return DataSet parsed from the input stream
     * @throws IllegalDataException
     */
    public static DataSet xmlToDataSet(InputStream source) throws IllegalDataException {
        PleaseWaitProgressMonitor progressMonitor = new PleaseWaitProgressMonitor("Parsing dataset from XML");
        DataSet dataset = OsmReader.parseDataSet(source, progressMonitor);
        progressMonitor.close();
        return dataset;
    }

    /**
     * Convert a String to InputStream
     * @param response Response String to be converted
     * @return InputStream
     */
    public static InputStream responseToInputStream(String response){
        return new ByteArrayInputStream(response.getBytes());
    }


    /**
     * Get all the Ways that have the tag building=* in them
     * @param dataset The dataset to extract buildings from
     * @return
     */
    public static ArrayList<Way> getBuildingsFromDataSet(DataSet dataset){
        Collection<Way> ways = dataset.getWays();
        ArrayList<Way> buildings = new ArrayList<>(ways.stream().filter(way -> way.hasTag("building")).collect(Collectors.toList()));
        return buildings;
    }

}
