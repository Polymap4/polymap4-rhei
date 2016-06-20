/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.fulltext.model2;

import static org.polymap.rhei.fulltext.model2.DuplicateHandler.CONCAT;
import static org.polymap.rhei.fulltext.model2.FieldNameProvider.STANDARD;

import java.util.Date;
import java.util.Locale;

import java.text.DateFormat;
import java.text.NumberFormat;

import org.json.JSONObject;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;

import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.runtime.CompositeStateVisitor;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityFeatureTransformer
        extends CompositeStateVisitor<RuntimeException>
        implements FeatureTransformer<Entity,JSONObject> {

    private static Log log = LogFactory.getLog( EntityFeatureTransformer.class );

    /**
     * The locale used for {@link NumberFormat} and {@link DateFormat}. Defaults to
     * {@link Locale#getDefault()}.
     */
    @Mandatory
    public Config2<EntityFeatureTransformer,Locale>     locale;
    
    /**
     * True specifies that only Properties annotated as {@link Queryable} are
     * indexed.
     */
    @Mandatory
    @DefaultBoolean( false )
    public Config2<EntityFeatureTransformer,Boolean>    honorQueryableAnnotation;
    
    /**
     * By default this is {@link StandardFieldNameProvider}. Change this to affect
     * subsequent call of {@link #putValue(Property, String)}.
     */
    @Mandatory
    public Config2<EntityFeatureTransformer,FieldNameProvider> fieldNameProvider;
    
    /**
     * By default this is set to {@link #CONCAT}. Change this to affect subsequent
     * calls of {@link #putValue(Property, String)}.
     */
    @Mandatory
    public Config2<EntityFeatureTransformer,DuplicateHandler> duplicateHandler;
    

    private Lazy<NumberFormat>          nf = new PlainLazyInit( () -> NumberFormat.getInstance( locale.get() ) );

    private Lazy<FastDateFormat>        df = new PlainLazyInit( () -> FastDateFormat.getDateInstance( FastDateFormat.FULL, locale.get() ) );

    private volatile JSONObject         result;


    public EntityFeatureTransformer() {
        ConfigurationFactory.inject( this );
        fieldNameProvider.set( STANDARD );
        duplicateHandler.set( CONCAT );
        locale.set( Locale.getDefault() );
    }
    
    
    @Override
    public JSONObject apply( Entity entity ) {
        assert result == null : "Implementation is not multi-threaded currently.";
        result = new JSONObject();
        
        try {
            result.put( FulltextIndex.FIELD_ID, entity.id().toString() );
            //result.put( "_type_", entity.getClass().getName() );

            // visit all simple properties
            process( entity );

            log.debug( "   " + result.toString( 2 ) );
            return result;
        }
        finally {
            assert result != null : "Implementation is not multi-threaded currently.";
            result = null;
        }
    }

    
    @Override
    protected void visitProperty( Property prop ) {        
        PropertyInfo info = prop.info();
        if (honorQueryableAnnotation.get() && !info.isQueryable()) {
            log.debug( "   skipping non @Queryable property: " + info.getName() );
            return;
        }
        
        // the hierarchy of propeties may contain properties with same simple name
        Object value = prop.get();

        // null
        if (value == null) {
        }
        // Enum
        else if (value.getClass().isEnum()) {
            putValue( prop, value.toString() );
        }
        // Date
        else if (Date.class.isAssignableFrom( value.getClass() )) {
            putValue( prop, df.get().format( value ) );
        }
        // Number
        else if (Number.class.isAssignableFrom( value.getClass() )) {
            putValue( prop, nf.get().format( value ) );                    
        }
        // Boolean -> if true add prop name instead of 'true|false'
        else if (value.getClass().equals( Boolean.class )) {
            if (((Boolean)value).booleanValue()) {
                putValue( prop, prop.info().getName() );
            }
        }
        // String and other types
        else {
            putValue( prop, value.toString() );
        }
    }
 
    
    protected void putValue( Property prop, String value ) {
        String key = fieldNameProvider.get().apply( prop );
        putValue( key, value );
    }


    protected void putValue( String key, String value ) {
        String currentValue = result.optString( key );
        if (currentValue.length() > 0) {
            value = duplicateHandler.get().apply( new String[] {currentValue, value} );
        }
        result.put( key, value );
    }
    
}
