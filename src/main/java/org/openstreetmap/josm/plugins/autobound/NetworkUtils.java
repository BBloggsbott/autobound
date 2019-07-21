package org.openstreetmap.josm.plugins.autobound;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.openstreetmap.josm.tools.Logging;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Scanner;

/**
 * Provide Networking utilities for the plugin.
 * @author bbloggsbott
 */
public class NetworkUtils {

    /**
     * Object that holds the URL of the AutoBound Server.
     */
    private final String serverUrl;

    /**
     * Creates the new object for NetworkUtils.
     * @param serverUrl The url to send and receive data for AutoBound.
     * @throws MalformedURLException Thrown when the serverUrlis malformed.
     */
    public NetworkUtils(String serverUrl) throws MalformedURLException {
        this.serverUrl = serverUrl;
    }

    /**
     * Generates the nodes by sending data to the server and returning the response from the server.
     * @param jsonData JSONObject containing the data to be sent to the server.
     * @return The response form the server.
     * @throws IOException Thrown when an IOException is encountered.
     */
    public String sendToServer(JSONObject jsonData) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(serverUrl);
        String responseString = null;

        NameValuePair data = new BasicNameValuePair("data", jsonData.toString());
        httppost.setEntity(new StringEntity(jsonData.toString()));

        Logging.info("Sending data to server at "+serverUrl);
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        Logging.info("Response Received");

        if(entity != null){
            InputStream instream = entity.getContent();
            Scanner s = new Scanner(instream).useDelimiter("\n");
            responseString="";
            while(s.hasNext()){
                responseString += s.next();
            }
        }
        return responseString;
    }
}
