package org.openstreetmap.josm.plugins.autobound;

import org.json.JSONObject;
import org.openstreetmap.josm.tools.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    private final URL url;

    public NetworkUtils(String serverUrl) throws MalformedURLException {
        this.url = new URL(serverUrl);
    }

    public String generateNodes(JSONObject data) throws IOException {
        int httpResult;
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("data", data.toString());
        connection.setRequestProperty("Content-Type", "application/json");

        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(data.toString());
        out.close();
        httpResult = connection.getResponseCode();
        Logging.info("[INFO] Response Code from AutoBound server: "+httpResult);
        return readResponse(httpResult, connection);
    }

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
                Logging.error("[INFO] Error on posting "+connection.getResponseMessage());
            }
        }
        catch(IOException ioe){
            Logging.error("[INFO] Error in Response" + ioe.getMessage());
        }
        return response;
    }
}
