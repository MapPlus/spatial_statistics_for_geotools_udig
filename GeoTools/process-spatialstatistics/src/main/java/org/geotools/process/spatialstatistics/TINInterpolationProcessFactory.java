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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.Process;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.enumeration.RasterPixelType;
import org.geotools.util.KVP;
import org.geotools.util.logging.Logging;

/**
 * TINInterpolationProcessFactory
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class TINInterpolationProcessFactory extends SpatialStatisticsProcessFactory {
    protected static final Logger LOGGER = Logging.getLogger(TINInterpolationProcessFactory.class);

    private static final String PROCESS_NAME = "TINInterpolation";

    /*
     * TINInterpolation(SimpleFeatureCollection inputFeatures, Expression inputField, RasterPixelType pixelType, Double cellSize, ReferencedEnvelope
     * extent): GridCoverage2D
     */

    public TINInterpolationProcessFactory() {
        super(new NameImpl(NAMESPACE, PROCESS_NAME));
    }

    @Override
    public Process create() {
        return new TINInterpolationProcess(this);
    }

    @Override
    public InternationalString getTitle() {
        return getResource("TINInterpolation.title");
    }

    @Override
    public InternationalString getDescription() {
        return getResource("TINInterpolation.description");
    }

    /** inputFeatures */
    public static final Parameter<SimpleFeatureCollection> inputFeatures = new Parameter<SimpleFeatureCollection>(
            "inputFeatures", SimpleFeatureCollection.class,
            getResource("TINInterpolation.inputFeatures.title"),
            getResource("TINInterpolation.inputFeatures.description"), true, 1, 1, null,
            new KVP(Params.FEATURES, Params.Point));

    /** inputField */
    public static final Parameter<String> inputField = new Parameter<String>("inputField",
            String.class, getResource("TINInterpolation.inputField.title"),
            getResource("TINInterpolation.inputField.description"), true, 1, 1, null,
            new KVP(Params.FIELD, "inputFeatures.Number"));

    /** pixelType */
    public static final Parameter<RasterPixelType> pixelType = new Parameter<RasterPixelType>(
            "pixelType", RasterPixelType.class, getResource("TINInterpolation.pixelType.title"),
            getResource("TINInterpolation.pixelType.description"), false, 0, 1,
            RasterPixelType.FLOAT, null);

    /** cellSize */
    public static final Parameter<Double> cellSize = new Parameter<Double>("cellSize", Double.class,
            getResource("TINInterpolation.cellSize.title"),
            getResource("TINInterpolation.cellSize.description"), false, 0, 1, Double.valueOf(0.0),
            null);

    /** extent */
    public static final Parameter<ReferencedEnvelope> extent = new Parameter<ReferencedEnvelope>(
            "extent", ReferencedEnvelope.class, getResource("TINInterpolation.extent.title"),
            getResource("TINInterpolation.extent.description"), false, 0, 1, null, null);

    @Override
    protected Map<String, Parameter<?>> getParameterInfo() {
        HashMap<String, Parameter<?>> parameterInfo = new LinkedHashMap<String, Parameter<?>>();
        parameterInfo.put(inputFeatures.key, inputFeatures);
        parameterInfo.put(inputField.key, inputField);
        parameterInfo.put(pixelType.key, pixelType);

        parameterInfo.put(cellSize.key, cellSize);
        parameterInfo.put(extent.key, extent);
        return parameterInfo;
    }

    /** result */
    public static final Parameter<GridCoverage2D> RESULT = new Parameter<GridCoverage2D>("result",
            GridCoverage2D.class, getResource("TINInterpolation.result.title"),
            getResource("TINInterpolation.result.description"));

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
