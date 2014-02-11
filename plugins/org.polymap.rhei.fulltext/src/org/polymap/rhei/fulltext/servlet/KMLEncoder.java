/* 
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.fulltext.servlet;

import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_GEOM;
import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_SRS;
import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_TITLE;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Encoder;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.util.Geometries;

/**
 * This stream encodes {@link SimpleFeature} and {@link Document} objects into
 * KML stream. Later this may also extend to be a {@link PipelineProcessor}
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KMLEncoder
        extends DataOutputStream
        implements ObjectOutput {

    private static final Log log = LogFactory.getLog( KMLEncoder.class );

    private static final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory( null );

    private String                      typeName = "Orte";
    
    /** The CRS used for encoding KML. */
    private CoordinateReferenceSystem   worldCRS = CRS.decode( "EPSG:4326" );  //DefaultGeographicCRS.WGS84;


    public KMLEncoder( OutputStream out ) throws Exception {
        super( out );

        OutputStreamWriter writer = new OutputStreamWriter( this, "UTF-8" );
        writer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
        writer.append( "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" );
        writer.append( "<Document>\n" );
        writer.flush();
    }

    
    @Override
    public void close() throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter( this, "UTF-8" );
        writer.append( "</Document>\n" );
        writer.append( "</kml>\n" );
        writer.flush();
        super.close();
    }


    public void writeObject( Object obj )
            throws IOException {
        try {
            if (obj instanceof JSONObject) {
                encodeFeature( (JSONObject)obj );
            }
            else {
                throw new RuntimeException( "Unsupported object type: " + obj );
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e.getMessage(), e );
        }
    }

    
    public synchronized void flush() throws IOException {
        super.flush();
    }


    protected void encodeFeature( JSONObject obj ) throws Exception {
        // transform geometry
        String title = obj.getString( FIELD_TITLE );
        Geometry geom = (Geometry)obj.get( FIELD_GEOM );
        String srs = obj.getString( FIELD_SRS );
        geom = Geometries.transform( geom, Geometries.crs( srs ), worldCRS );
        
        // Placemark
        OutputStreamWriter writer = new OutputStreamWriter( this, "UTF-8" );
        writer.append( "<Placemark>\n" );
        writer.append( "   <name>" ).append( title ).append( "</name>\n" );

//        // fields
//        for (String field : obj.getFieldNames()) {
//            String value = obj.getField( field );
//            if (value != null && value.length() > 0) {
//                String fieldName = StringUtils.capitalize( field );
//                writer.append( "   <" ).append( fieldName ).append( ">" )
//                        .append( value )
//                        .append( "</" ).append( fieldName ).append( ">\n" );
//            }
//        }
        writer.flush();

        // encode geometry
        Encoder kmlEncoder = new Encoder( new KMLConfiguration() );
        kmlEncoder.setIndenting( true );
        kmlEncoder.setEncoding( Charset.forName( "UTF-8" ) );
        kmlEncoder.setNamespaceAware( false );
        kmlEncoder.setOmitXMLDeclaration( true );
        kmlEncoder.encode( geom, KML.Geometry, this );
        
        writer = new OutputStreamWriter( this, "UTF-8" );
        writer.write( "</Placemark>\n" );
        writer.flush();
    }
    
}
