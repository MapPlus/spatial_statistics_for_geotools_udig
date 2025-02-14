/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2020, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.process.spatialstatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.geotools.api.data.Parameter;
import org.geotools.api.util.InternationalString;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.process.Process;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.util.KVP;
import org.geotools.util.logging.Logging;

/**
 * SmoothProcessFactory
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class SmoothProcessFactory extends SpatialStatisticsProcessFactory {
    protected static final Logger LOGGER = Logging.getLogger(SmoothProcessFactory.class);

    private static final String PROCESS_NAME = "Smooth";

    /*
     * Smooth(SimpleFeatureCollection features, Double fit): SimpleFeatureCollection
     */

    public SmoothProcessFactory() {
        super(new NameImpl(NAMESPACE, PROCESS_NAME));
    }

    @Override
    public Process create() {
        return new SmoothProcess(this);
    }

    @Override
    public InternationalString getTitle() {
        return getResource("Smooth.title");
    }

    @Override
    public InternationalString getDescription() {
        return getResource("Smooth.description");
    }

    /** features */
    public static final Parameter<SimpleFeatureCollection> features = new Parameter<SimpleFeatureCollection>(
            "features", SimpleFeatureCollection.class, getResource("Smooth.features.title"),
            getResource("Smooth.features.description"), true, 1, 1, null,
            new KVP(Params.FEATURES, Params.Polyline));

    /** fit */
    public static final Parameter<Double> fit = new Parameter<Double>("fit", Double.class,
            getResource("Smooth.fit.title"), getResource("Smooth.fit.description"), false, 0, 1,
            Double.valueOf(0.3d), null);

    @Override
    protected Map<String, Parameter<?>> getParameterInfo() {
        HashMap<String, Parameter<?>> parameterInfo = new LinkedHashMap<String, Parameter<?>>();
        parameterInfo.put(features.key, features);
        parameterInfo.put(fit.key, fit);
        return parameterInfo;
    }

    /** result */
    public static final Parameter<SimpleFeatureCollection> RESULT = new Parameter<SimpleFeatureCollection>(
            "result", SimpleFeatureCollection.class, getResource("Smooth.result.title"),
            getResource("Smooth.result.description"), true, 1, 1, null, null);

    static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    static {
        resultInfo.put(RESULT.key, RESULT);
    }

    @Override
    protected Map<String, Parameter<?>> getResultInfo(Map<String, Object> parameters)
            throws IllegalArgumentException {
        return Collections.unmodifiableMap(resultInfo);
    }

}
