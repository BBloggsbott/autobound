package org.openstreetmap.plugins.autobound;

import org.json.JSONObject;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.autobound.DataUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DataUtilsTests {

    @Test
    public void encodeImageTest(){
        BufferedImage image = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String encodedImage;
        try{
            image = ImageIO.read(getClass().getClassLoader().getResource("images/testimage.png"));
            ImageIO.write(image, "png", os);
        } catch(IOException ioe){
            ioe.printStackTrace();
            fail("IO error");
        }
        encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
        assertEquals(encodedImage, DataUtils.encodeImage(image));
    }

    @Test
    public void createJsonTest(){
        int offset_x = 10;
        int offset_y = 8;
        JSONObject data = new JSONObject();
        BufferedImage image = null;
        try{
            image = ImageIO.read(getClass().getClassLoader().getResource("images/testimage.png"));
        } catch(IOException ioe){
            ioe.printStackTrace();
            fail("IO error");
        }
        data.put("image",DataUtils.encodeImage(image));
        data.put("offset_x",offset_x);
        data.put("offset_y",offset_y);
        JSONObject testJson = DataUtils.createJson(image, offset_x, offset_y);
        assertEquals(data.getInt("offset_x"), testJson.getInt("offset_x"));
        assertEquals(data.getInt("offset_y"), testJson.getInt("offset_y"));
        assertEquals(data.getString("image"), testJson.getString("image"));
    }

    @Test
    public void xmlToDataSetTest(){
        DataSet dataset=null;
        try{
            dataset = DataUtils.xmlToDataSet(DataUtils.responseToInputStream("<?xml version='1.0' encoding='UTF-8'?><osm version='0.6' generator='AutoBoundTest'><bounds minlat='51.5076478723889' minlon='-0.127989783553507' maxlat='51.5077445145483' maxlon='-0.127774884645096' origin='AutoBoundTest' /><node id='26821100' timestamp='2009-02-16T21:34:57+00:00' visible='true' lat='51.5077286' lon='-0.1279688' version='1'/><osm>"));
        } catch(IllegalDataException ide){
            ide.printStackTrace();
            fail("XML error");
        }
        Collection<Node> nodes = dataset.getNodes();
        for (Node node: nodes){
            assertEquals(51.5077286, node.getCoor().lat(),0.0);
            assertEquals(-0.1279688, node.getCoor().lon(),0.0);
        }
    }

}
