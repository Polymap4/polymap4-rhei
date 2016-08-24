/* 
 * polymap.org
 * Copyright (C) 2011-2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.form.json;

import java.util.Date;
import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.IFormToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@SuppressWarnings( "deprecation" )
public class JsonForm
        implements IFormPage {

    private static Log log = LogFactory.getLog( JsonForm.class );
    
    private JSONObject              json;

    private IFormPageSite           site;

    private IFormToolkit            tk;

    
    protected JsonForm() {
    }
    
    
    public JsonForm( JSONObject json ) {
        this.json = json;
    }
    
    
    /**
     * 
     * @param url URL to load the contents of the JSON from.
     * @throws JSONException 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public JsonForm( URL url ) 
    throws JSONException, UnsupportedEncodingException, IOException {
        Reader in = null;        
        try {
            in = new BufferedReader( new InputStreamReader( url.openStream(), "ISO-8859-1" ) );
            json = new JSONObject( new JSONTokener( in ) );
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    
    protected void setJson( JSONObject json ) {
        this.json = json;
    }

    
    @Override
    public String getId() {
        try {
            return json.getString( "id" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: id", e );
        }
    }


    @Override
    public String getTitle() {
        try {
            return json.getString( "title" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: title", e );
        }
    }

    
    @Override
    public byte getPriority() {
        return 0;
    }


    @Override
    public void createFormContents( IFormPageSite _site ) {
        log.debug( "createFormContent(): json= " + json );
        this.site = _site;
        this.tk = site.getToolkit();
        DefaultFormPageLayouter layouter = new DefaultFormPageLayouter();

        site.setPageTitle( getTitle() );
        site.getPageBody().setLayout( new FormLayout() );
        Composite client = site.getPageBody();
        client.setLayout( layouter.newLayout() );

        try {
            JSONArray fields = json.getJSONArray( "fields" );
            for (int i=0; i<fields.length(); i++) {
                JSONObject field_json = fields.getJSONObject( i );
                
                Composite field = newFormField( client, field_json );
                layouter.setFieldLayoutData( field );
            }
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: " + e, e );
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException( "Field type not valid: " + e, e );
        }
    }

    
    protected Composite newFormField( Composite parent, JSONObject field_json )
    throws JSONException, ClassNotFoundException {
        IFormField formField = null;
        IFormFieldValidator validator = null;
        
        // check type -> build default field/validator
        String valueTypeName = field_json.optString( "type" );
        
        if (valueTypeName != null) {
            Class valueType = Thread.currentThread().getContextClassLoader().loadClass( valueTypeName );
            // Date
            if (Date.class.isAssignableFrom( valueType )) {
                formField = new DateTimeFormField();
            }
            // String
            else if (String.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
            }
            // Integer
            else if (Integer.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Integer.class, Locale.getDefault() );
            }
            // Long
            else if (Long.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Long.class, Locale.getDefault() );
            }
            // Float
            else if (Float.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Integer.class, Locale.getDefault(), 10, 2 );
            }
            // Double
            else if (Double.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Double.class, Locale.getDefault(), 10, 2 );
            }
            else {
                throw new RuntimeException( "Unhandled valueType: " + valueType );
            }
        }

        // create the form field
        String label = field_json.optString( "label" );
        String name = field_json.getString( "name" );
        Object defaultValue = field_json.opt( "value" );

        return site.newFormField( findProperty( name, defaultValue ) )
                .field.put( formField )
                .validator.put( validator )
                .label.put( label )
                .create();
    }


    /**
     * Sub classes may overwrite to provide proper properties for the property
     * names found in the JSON form description.
     * 
     * @param propName Property name that was found in the JSON form
     *        description.
     * @param defaultValue
     * @return
     */
    protected Property findProperty( String propName, Object defaultValue ) {
        return new PropertyAdapter( propName, defaultValue );
    }

    @Override
    public Action[] getActions() {
        return null;
    }

}
