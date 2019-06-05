package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.tools.Logging;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

    public String addDataToUrl(JSONObject data, String serverUrl){
        String newServerUrl = serverUrl;
        if(!serverUrl.substring(serverUrl.length()-1).equalsIgnoreCase("/")){
            newServerUrl = serverUrl + "?data=" + data.toString();
        }
        return newServerUrl;
    }

    /**
     * Generates the nodes by sending data to the server and returning the response from the server.
     * @param data JSONObject containing the data to be sent to the server.
     * @return The response form the server.
     * @throws IOException Thrown when an IOException is encountered.
     */
    public String sendToServer(JSONObject data) throws IOException {
        int httpResult;
        URL url = new URL(addDataToUrl(data, serverUrl));
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);

        Logging.info("Sending data to AutoBound server at "+url);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(data.toString());
        out.close();
        httpResult = connection.getResponseCode();
        Logging.info("Response Code from AutoBound server: "+httpResult);
        return readResponse(httpResult, connection);
    }

    /**
     * Reads and returns the response from the server.
     * @param httpResult The response code from the server.
     * @param connection The HttpURLConnection object with the connection established to the server
     * @return The response from the server.
     */
    public String readResponse(int httpResult, HttpURLConnection connection){
        String response = null, line = null;
        StringBuilder sb;
        try{
            if(httpResult==HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                sb = new StringBuilder();
                while((line=br.readLine())!=null){
                    sb.append(line);
                }
                br.close();
                response = sb.toString();
            }
            else {
                Logging.error("Error on posting "+connection.getResponseMessage());
            }
        }
        catch(IOException ioe){
            Logging.error("Error in Response" + ioe.getMessage());
        }
        return response;
    }

}
