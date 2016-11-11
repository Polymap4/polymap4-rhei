/* 
 * polymap.org
 * Copyright (C) 2010-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.engine;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.rhei.engine.form.FormEditorToolkit;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFormFieldLabeler
        implements IFormFieldLabel, IFormFieldListener {

    public static final String  CUSTOM_VARIANT_VALUE = "formeditor-label";

    private IFormFieldSite      site;
    
    private String              labelStr;
    
    private int                 maxWidth;

    private Label               label;
    
    private Font                orig;
    
    
    public DefaultFormFieldLabeler( String label ) {
        this.labelStr = label;
    }

    
    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    
    public void dispose() {
        site.removeChangeListener( this );
    }

    
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        String text = labelStr == null ? StringUtils.capitalize( site.getFieldName() ) : labelStr;
        
        label = toolkit.createLabel( parent, text, SWT.WRAP );
        // label.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
    
        // focus listener
        site.addChangeListener( this );
        return label;
    }

    
    public void fieldChange( FormFieldEvent ev ) {
        if (label.isDisposed()) {
            return;
        }
        if (ev.getEventCode() == FOCUS_GAINED) {
            label.setForeground( FormEditorToolkit.labelForegroundFocused );
            orig = label.getFont();
            //label.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        }
        else if (ev.getEventCode() == FOCUS_LOST) {
            label.setForeground( FormEditorToolkit.labelForeground );
            label.setFont( orig );
        }
    }
    
    
//    public void setMaxWidth( int maxWidth ) {
//        this.maxWidth = maxWidth;
//    }
//
//    
//    public int getMaxWidth() {
//        return maxWidth;
//    }

}
