package org.openstreetmap.plugins.autobound;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.autobound.DataUtils;
import org.openstreetmap.josm.plugins.autobound.MapUtils;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class MapUtilsTest {

    @Rule
    public JOSMTestRules test = new JOSMTestRules().preferences().fakeAPI();

    @Test
    public void addDataSetToDataLayerTest(){
        DataSet dataset = null;
        Boolean foundLayer = false;
        try{
            dataset = DataUtils.xmlToDataSet(DataUtils.responseToInputStream("<?xml version='1.0' encoding='UTF-8'?><osm version='0.6' generator='AutoBoundTest'><bounds minlat='51.5076478723889' minlon='-0.127989783553507' maxlat='51.5077445145483' maxlon='-0.127774884645096' origin='AutoBoundTest' /><node id='26821100' timestamp='2009-02-16T21:34:57+00:00' visible='true' lat='51.5077286' lon='-0.1279688' version='1'/></osm>"));
        } catch(IllegalDataException ide){
            ide.printStackTrace();
            fail("XML error");
        }

        MapUtils.addDataSetToDataLayer(dataset);
        List<Layer> layers = MainApplication.getLayerManager().getLayers();
        for (Layer layer: layers){
            if (layer.getName().equalsIgnoreCase("AutoBoundData")){
                foundLayer=true;
                Node nodeExpected = (Node)dataset.getNodes().toArray()[0];
                OsmDataLayer dataLayer = (OsmDataLayer)layer;
                Node nodeTest = (Node)dataLayer.getDataSet().getNodes().toArray()[0];
                assertEquals(nodeExpected.getCoor().lat(), nodeTest.getCoor().lat(), 0);
                assertEquals(nodeExpected.getCoor().lon(), nodeTest.getCoor().lon(), 0);
            }
        }
        assertTrue(foundLayer);
    }
}
