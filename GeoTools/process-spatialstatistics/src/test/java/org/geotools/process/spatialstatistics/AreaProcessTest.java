package org.geotools.process.spatialstatistics;

import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.junit.Test;

public class AreaProcessTest extends SpatialStatisticsTestCase {

    @Test
    public void test() throws Exception {
        SimpleFeatureSource source = dataStore.getFeatureSource("polygon");
        assertTrue(source.getCount(Query.ALL) > 0);

        Map<String, Object> input = new HashMap<String, Object>();
        input.put(AreaProcessFactory.inputFeatures.key, source.getFeatures());

        // direct
        org.geotools.process.Process process = new AreaProcess(null);
        Map<String, Object> resultMap = process.execute(input, null);
        Object result = resultMap.get(AreaProcessFactory.RESULT.key);
        assertNotNull("result area = ", result);

        // process factory
        AreaProcessFactory factory = new AreaProcessFactory();
        process = factory.create();
        resultMap = process.execute(input, null);
        result = resultMap.get(AreaProcessFactory.RESULT.key);
        assertNotNull("result area = ", result);
    }
}
