package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.actions.JosmAction;import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Action class to collect imagery data
 */
public class DataCollectionAction extends JosmAction {

    private NetworkUtils networkUtils = null;

    /**
     * Create a new DataCollectionAction object
     * @param dataCollectionServerUrl URL to the server that saves data
     */
    public DataCollectionAction(String dataCollectionServerUrl){
        super(tr("Collect Data - AutoBound"), "autobound",
                tr("Collect the data of every building in the Data Layer and send them to the Data Collection server for processing and saving"),
                Shortcut.registerShortcut("autobound:collectdata", tr("Tools: {0}", tr("Collect Data - AutoBound")),
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE), true);
        try{
            networkUtils = new NetworkUtils(dataCollectionServerUrl);
        } catch (MalformedURLException mue){
            Logging.error("Malformed Data Collection Server Url");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(networkUtils != null){
            DataCollectionRunnable downloader = new DataCollectionRunnable(networkUtils);
            downloader.start();
        } else {
            Logging.error("Network not configured.");
        }
    }
}
