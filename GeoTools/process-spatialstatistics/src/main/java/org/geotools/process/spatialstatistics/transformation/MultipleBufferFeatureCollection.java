/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.spatialstatistics.transformation;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.SubFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.core.UnitConverter;
import org.geotools.process.spatialstatistics.enumeration.DistanceUnit;
import org.geotools.process.spatialstatistics.util.GeodeticBuilder;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import si.uom.SI;

/**
 * Creates a new features of buffer features using a set of buffer distances.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class MultipleBufferFeatureCollection extends GXTSimpleFeatureCollection {
    protected static final Logger LOGGER = Logging.getLogger(MultipleBufferFeatureCollection.class);

    static final String bufferField = "distance";

    private double[] distances;

    private Boolean outsideOnly = Boolean.TRUE;

    private SimpleFeatureType schema;

    public MultipleBufferFeatureCollection(SimpleFeatureCollection delegate, double[] distances,
            Boolean outsideOnly) {
        this(delegate, distances, DistanceUnit.Default, outsideOnly);
    }

    public MultipleBufferFeatureCollection(SimpleFeatureCollection delegate, double[] distances,
            DistanceUnit distanceUnit, Boolean outsideOnly) {
        super(delegate);

        String typeName = delegate.getSchema().getTypeName();
        this.schema = FeatureTypes.build(delegate.getSchema(), typeName, Polygon.class);
        this.schema = FeatureTypes.add(schema, bufferField, Double.class, 19);

        CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();

        // sort ascending
        // 250, 500, 750, 1000
        Arrays.sort(distances);

        // reverse distances for inside ring(ascending -> descending)
        // 1000, 750, 500, 250
        int arrLen = distances.length;
        double[] reverseDistances = new double[arrLen];
        for (int i = 0; i < arrLen; i++) {
            reverseDistances[i] = distances[(arrLen - 1) - i];
        }

        // apply distance unit
        if (distanceUnit == DistanceUnit.Default) {
            this.distances = reverseDistances;
        } else {
            double[] converted = reverseDistances.clone();

            Unit<Length> targetUnit = UnitConverter.getLengthUnit(crs);
            if (UnitConverter.isGeographicCRS(crs)) {
                for (int i = 0; i < converted.length; i++) {
                    converted[i] = UnitConverter.convertDistance(converted[i], distanceUnit,
                            SI.METRE);
                }
            } else {
                for (int i = 0; i < converted.length; i++) {
                    converted[i] = UnitConverter.convertDistance(converted[i], distanceUnit,
                            targetUnit);
                }
            }
            this.distances = converted;
        }
        this.outsideOnly = outsideOnly;
    }

    @Override
    public SimpleFeatureIterator features() {
        return new BufferedFeatureIterator(delegate.features(), getSchema(), distances,
                outsideOnly);
    }

    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        if (filter == Filter.INCLUDE) {
            return this;
        }
        return new SubFeatureCollection(this, filter);
    }

    @Override
    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope bounds = delegate.getBounds();
        bounds.expandBy(distances[0]);
        return bounds;
    }

    @Override
    public int size() {
        return delegate.size() * distances.length;
    }

    static class BufferedFeatureIterator implements SimpleFeatureIterator {
        private SimpleFeatureIterator delegate;

        private double[] distances;

        private Boolean outsideOnly = Boolean.TRUE;

        private int bufferIndex = 0;

        private int featureID = 0;

        private SimpleFeatureBuilder builder;

        private SimpleFeature nextFeature = null;

        private SimpleFeature origFeature = null;

        private String typeName;

        private boolean isGeographicCRS = false;

        private GeodeticBuilder geodetic;

        private int quadrantSegments = 24;

        public BufferedFeatureIterator(SimpleFeatureIterator delegate, SimpleFeatureType schema,
                double[] distances, Boolean outsideOnly) {
            this.delegate = delegate;

            this.bufferIndex = 0;
            this.distances = distances;
            this.outsideOnly = outsideOnly;
            this.builder = new SimpleFeatureBuilder(schema);
            this.typeName = schema.getTypeName();

            CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
            this.isGeographicCRS = UnitConverter.isGeographicCRS(crs);
            if (isGeographicCRS) {
                geodetic = new GeodeticBuilder(crs);
                geodetic.setQuadrantSegments(quadrantSegments);
            }
        }

        public void close() {
            delegate.close();
        }

        public boolean hasNext() {
            while ((nextFeature == null && delegate.hasNext())
                    || (nextFeature == null && !delegate.hasNext() && bufferIndex > 0)) {
                if (bufferIndex == 0) {
                    origFeature = delegate.next();
                }

                // buffer geometry
                Geometry orig = (Geometry) origFeature.getDefaultGeometry();

                Geometry buffered = orig;
                boolean isCuttable = outsideOnly && bufferIndex < distances.length - 1;

                if (isGeographicCRS) {
                    try {
                        buffered = geodetic.buffer(orig, distances[bufferIndex]);
                        if (isCuttable) {
                            Geometry before = geodetic.buffer(orig, distances[bufferIndex + 1]);
                            buffered = buffered.difference(before);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINER, e.getMessage(), e);
                    }
                } else {
                    buffered = orig.buffer(distances[bufferIndex], quadrantSegments);
                    if (isCuttable) {
                        Geometry before = orig.buffer(distances[bufferIndex + 1], quadrantSegments);
                        buffered = buffered.difference(before);
                    }
                }

                // create feature
                nextFeature = builder.buildFeature(buildID(typeName, ++featureID));
                transferAttribute(origFeature, nextFeature);
                nextFeature.setDefaultGeometry(buffered);
                nextFeature.setAttribute(bufferField, distances[bufferIndex]);

                builder.reset();
                bufferIndex++;

                if (bufferIndex >= distances.length) {
                    bufferIndex = 0;
                    origFeature = null;
                }
            }
            return nextFeature != null;
        }

        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("hasNext() returned false!");
            }
            SimpleFeature result = nextFeature;
            nextFeature = null;
            return result;
        }
    }
}
