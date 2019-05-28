package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.data.preferences.StringProperty;

public class AutoBound extends Plugin {
    static StringProperty serverUrl = new StringProperty("autoboundServerUrl", "https://localhost:5000");
    public AutoBound(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        MainMenu mainMenu = MainApplication.getMenu();
        if(newFrame != null)
            newFrame.addMapMode(new IconToggleButton(new AutoBoundAction(newFrame, serverUrl.get())));
    }
}
