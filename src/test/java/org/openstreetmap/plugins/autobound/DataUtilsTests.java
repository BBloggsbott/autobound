package org.openstreetmap.plugins.autobound;

import org.json.JSONObject;
import org.junit.Test;
import org.openstreetmap.josm.plugins.autobound.DataUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

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


}
