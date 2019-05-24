package org.openstreetmap.josm.plugins.autobound;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.data.preferences.StringProperty;

public class AutoBound extends Plugin {
    static StringProperty server_url = new StringProperty("autoboundServerUrl", "https://localhost:5000");
    public AutoBound(PluginInformation info) {
        super(info);
    }
}
