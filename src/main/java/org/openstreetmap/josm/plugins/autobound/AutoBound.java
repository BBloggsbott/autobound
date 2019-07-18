package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.data.preferences.StringProperty;

/**
 * Class to create an instance of the Action class, add it to the menu.
 */
public class AutoBound extends Plugin {
    static StringProperty serverUrl = new StringProperty("autoboundServerUrl", "http://localhost:5000");
    static StringProperty dataCollectionServerUrl = new StringProperty("dataCollectionServerUrl","http://localhost:5000/dataCollector");
    static IntegerProperty delayProperty = new IntegerProperty("AutoBound imagery load delay", 5);
    public AutoBound(PluginInformation info) {
        super(info);
        MainMenu menu = MainApplication.getMenu();
        menu.add(menu.toolsMenu, new DataCollectionAction(dataCollectionServerUrl.get()));
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if(newFrame != null) {
            newFrame.addMapMode(new IconToggleButton(new AutoBoundAction(newFrame, serverUrl.get())));
            MapUtils.setDelay(delayProperty.get());
        }
    }
}
