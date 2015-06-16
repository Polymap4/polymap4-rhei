/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.rhei.form.workbench.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geotools.feature.FeatureTypes;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.action.Action;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.workbench.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class StandardPageProvider
        implements IFormPageProvider {

    private static Log log = LogFactory.getLog( StandardPageProvider.class );

    
    public List<IFormPage> addPages( final FormEditor formEditor, final Feature feature ) {
        log.debug( "feature= " + feature );
  
        List<IFormPage> result = new ArrayList();
        
        result.add( new IFormPage() {

            public void dispose() {
            }
            
            public String getId() {
                return  "_standard_";
            }

            public String getTitle() {
                return  "Standard";
            }

            public byte getPriority() {
                return -1;
            }

            public Action[] getEditorActions() {
                return null;
            }

            public void createFormContents( IFormPageSite site ) {
                site.setFormTitle( feature.getIdentifier().getID() );
                site.setEditorTitle( StringUtils.abbreviate( feature.getIdentifier().getID(), 30 ) );
                site.getPageBody().setLayout( new FormLayout() );
  
//                site.getToolkit().createLabel( site.getPageBody(), "Test Label" );
                
                // properties
                Composite last = null;
                FeatureType schema = feature.getType();
                for (PropertyDescriptor prop : schema.getDescriptors()) {
                    
                    Class binding = prop.getType().getBinding();

                    for (Property value : feature.getProperties( prop.getName() )) {
                        IFormField formField = null;
                        IFormFieldValidator validator = null;
                        
                        FormData layoutData = new FormData();
                        layoutData.left = new FormAttachment( 20, 0 );
                        layoutData.right = new FormAttachment( 80, 0 );
                        layoutData.top = last != null
                                ? new FormAttachment( last, 2 )
                                : new FormAttachment( 0 );

                        // Geometry
                        if (Geometry.class.isAssignableFrom( binding )) {
                            // skip
                        }
                        // String
                        else if (String.class.isAssignableFrom( binding )) {
                            if (FeatureTypes.getFieldLength( prop ) > 255
                                    || (value.getValue() instanceof String
                                    && ((String)value.getValue()).length() > 100)) {
                                formField = new TextFormField();
                                layoutData.height = 100;
                            }
                            else {
                                formField = new StringFormField();
                            }
                        }
                        // Number
                        else if (Number.class.isAssignableFrom( binding )) {
                            formField = new StringFormField();
                            validator = new NumberValidator( binding, Polymap.getSessionLocale() );
                        }
                        // Boolean
                        else if (Boolean.class.isAssignableFrom( binding )) {
                            formField = new CheckboxFormField();
                        }
                        // Date
                        else if (Date.class.isAssignableFrom( binding )) {
                            formField = new DateTimeFormField();
                        }
                        else {
                            log.warn( "Unknown property type: " + binding );
                        }

                        if (formField != null) {
                            Composite field = site.newFormField( null, value, formField, validator );
                            field.setLayoutData( layoutData );
                            last = field;
                        }
                    }
                }
                if (last != null) {
                    new Label( site.getPageBody(), SWT.SEPARATOR ).setLayoutData(
                            FormDataFactory.filled().top( last, 2 ).bottom( 100 ).create() );
                }
            }
        });
        return result;
    }

}
