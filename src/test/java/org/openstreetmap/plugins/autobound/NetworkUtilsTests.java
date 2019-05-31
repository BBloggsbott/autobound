package org.openstreetmap.plugins.autobound;

import org.json.JSONObject;
import org.junit.Test;
import org.openstreetmap.josm.plugins.autobound.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NetworkUtilsTests {

    @Test
    public void generateNodesTest(){
        NetworkUtils networkUtils = null;
        JSONObject jsonObject = new JSONObject();
        try{
            networkUtils = new NetworkUtils("http://localhost:5000/generateNodesTest/");
            jsonObject.put("verification","generateNodesTest");
            String response = networkUtils.generateNodes(jsonObject);
            System.out.println(response);
            assertEquals("message exchange works", response);
        } catch(MalformedURLException mue){
            mue.printStackTrace();
            fail("Malformed URL");
        } catch(IOException ioe){
            ioe.printStackTrace();
            fail("IOException during network communication");
        }
    }

}
