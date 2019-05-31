package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

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
     * @param offset_x longitude of the top left corner of the selection area.
     * @param offset_y lattitude of the top left corner of the selection area.
     * @return JSONObject with the Base64 encoded image and the two offset values
     */
    public static JSONObject createJson(BufferedImage image, double offset_x, double offset_y){
        JSONObject data = new JSONObject();
        data.put("image",encodeImage(image));
        data.put("offset_x",offset_x);
        data.put("offset_y",offset_y);
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
}
