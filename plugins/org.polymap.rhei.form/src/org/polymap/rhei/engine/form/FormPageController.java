/* 
 * polymap.org
 * Copyright 2010-2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.engine.form;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.SubMonitor;

import org.polymap.rhei.engine.DefaultFormFieldDecorator;
import org.polymap.rhei.engine.DefaultFormFieldLabeler;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.FieldBuilder;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPage2;
import org.polymap.rhei.form.IFormPageSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class FormPageController
        extends BasePageController<FormFieldComposite> 
        implements IFormPageSite {
    
    private static Log log = LogFactory.getLog( FormPageController.class );

    private IFormPage               page;
    
    
    public FormPageController( IFormPage page ) {
        this.page = page;
    }
    
    
    @Override
    public synchronized void dispose() {
        if (page != null && page instanceof IFormPage2) {
            ((IFormPage2)page).dispose();
        }
        super.dispose();
    }

    
    @Override
    public boolean isDirty() {
        if (page instanceof IFormPage2) {
            if (((IFormPage2)page).isDirty()) {
                return true;
            }
        }
        return super.isDirty();
    }
    
    
    @Override
    public boolean isValid() {
        if (page instanceof IFormPage2) {
            if (!((IFormPage2)page).isValid()) {
                return false;
            }
        }
        return super.isValid();
    }
    
    
    public Map<Property,Object> doSubmit( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Submit", fields.size() + 3 );
        Map<Property,Object> result = new HashMap();
        
        for (FormFieldComposite field : fields.values()) {
            if (field.isDirty()) {
                Object newValue = field.store();
                Object old = result.put( field.getProperty(), newValue );
                if (old != null) {
                    throw new RuntimeException( "Submitted value already exists for property: " + field.getProperty() );
                }
            }
            monitor.worked( 1 );
        }

        // after form fields in order to allow subclassed Property instances
        // to be notified of submit
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doSubmit( SubMonitor.on( monitor, 3 ) );
        }

        monitor.done();
        return result;
    }

    
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Load", fields.size() + 3 );
        
        SubMonitor submon = SubMonitor.on( monitor, 3 );
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doLoad( submon );
        }
        submon.done();
        
        super.doLoad( monitor );
    }

    
    // IFormPageSite ****************************
    
    
    @Override
    public FieldBuilder newFormField( Property property ) {
        return new FieldBuilder() {
            @Override
            protected Class<?> propBinding() {
                return property.getType().getBinding();
            }
            @Override
            protected Composite createFormField() {
                FormFieldComposite result = new FormFieldComposite( 
                        getEditor(),
                        FormPageController.this, 
                        getToolkit(), 
                        property, 
                        field.get(),
                        new DefaultFormFieldLabeler( label.get() ), 
                        new DefaultFormFieldDecorator(), 
                        validator.orElse( new NullValidator() ),
                        layout.orElse( defaultFieldLayout ) );
                
                fields.put( result.getFieldName(), result );

                Composite fieldComposite = createFieldComposite( parent.orElse( getPageBody() ) );
                result.createComposite( fieldComposite, SWT.NONE );
                return fieldComposite;
            }
        };
    }

    
    protected abstract Composite createFieldComposite( Composite parent );
    

    @Override
    public void submit( IProgressMonitor monitor ) throws Exception {
        doSubmit( monitor != null ? monitor : new NullProgressMonitor() );            
    }

}