package org.openstreetmap.plugins.autobound;

import org.json.JSONObject;
import org.junit.Test;
import org.openstreetmap.josm.plugins.autobound.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

/**
 * Class to test NetworkUtils. AutoBound server must be running for these test to pass.
 */
public class NetworkUtilsTests {

    /**
     * Test the method to generate nodes.
     */
    @Test
    public void generateNodesTest(){
        NetworkUtils networkUtils = null;
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("verification","generateNodesTest");
            networkUtils = new NetworkUtils("http://localhost:5000/generateNodesTest");
            String response = networkUtils.sendToServer(jsonObject);
            System.out.println(response);
            assertEquals("message exchange works", response);
            jsonObject = new JSONObject();
            jsonObject.put("verification","invalid");
            response = networkUtils.sendToServer(jsonObject);
            assertNotEquals("message exchange error", response);
        } catch(MalformedURLException mue){
            mue.printStackTrace();
            fail("Malformed URL");
        } catch(IOException ioe){
            ioe.printStackTrace();
            fail("IOException during network communication");
        }
    }

}
