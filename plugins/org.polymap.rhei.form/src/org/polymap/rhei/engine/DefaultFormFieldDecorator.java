/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;

import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.engine.form.FormEditorToolkit;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFormFieldDecorator
        implements IFormFieldDecorator, IFormFieldListener {

    private static Log log = LogFactory.getLog( DefaultFormFieldDecorator.class );

    public static final Image   dirtyImage, invalidImage;
    
    private IFormFieldSite      site;
    
    private Label               label;
    
    private boolean             dirty, focus, invalid;
    
    private Color               origForeground;

    private String              origTooltip;


    static {
        dirtyImage = ImageDescriptor.createFromURL( 
                RheiFormPlugin.getDefault().getBundle().getResource( "icons/field_dirty.gif" ) ).createImage();
//        focusImage = ImageDescriptor.createFromURL( 
//                RheiFormPlugin.getDefault().getBundle().getResource( "icons/elcl16/field_dirty2.gif" ) ).createImage();
        invalidImage = ImageDescriptor.createFromURL( 
                RheiFormPlugin.getDefault().getBundle().getResource( "icons/field_invalid.gif" ) ).createImage();
    }
    
    @Override
    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    
    @Override
    public void dispose() {
        if (site != null) {
            site.removeChangeListener( this );
        }
    }

    
    @Override
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        label = toolkit.createLabel( parent, null, SWT.NO_FOCUS );
//        label = new CLabel( parent, SWT.NONE );
//        label.setMargins( 1, 3, 0, 0 );
        label.setAlignment( SWT.LEFT );
        label.setBackground( parent.getBackground() );
        label.pack();
        
//        label.setCursor( new Cursor( label.getDisplay(), SWT.CURSOR_HAND ) );
        label.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseUp( MouseEvent ev ) {
                if (invalid) {
                    ToolTip tooltip = new ToolTip( UIUtils.shellToParentOn(), SWT.BALLOON );
                    tooltip.setText( site.getErrorMessage() );
                    tooltip.setVisible( true );
                }
            }            
        });
        
        this.invalid = site.getErrorMessage() != null;
        updateUI();
        
        site.addChangeListener( this );
        return label;
    }

    
    protected void updateUI() {
        if (!label.isDisposed()) {
            origForeground = origForeground == null ? site.getFieldControl().getForeground() : origForeground;
            origTooltip = origTooltip == null ? site.getFieldControl().getToolTipText() : origTooltip;
            
            // invalid
            if (invalid) {
                label.setImage( invalidImage );
                label.setToolTipText( site.getErrorMessage() );
                // don't display red cursor without text
                if (site.getFieldControl() instanceof Text && ((Text)site.getFieldControl()).getText().length()>0) {
                    site.getFieldControl().setForeground( FormEditorToolkit.invalid );
                }
            }
            // dirty
            else if (dirty) {
                label.setImage( dirtyImage );
                site.getFieldControl().setForeground( origForeground );
                site.getFieldControl().setToolTipText( origTooltip );
                try {
                    String tooltip = "Eingabe ist korrekt.";
                    Object origValue = site.getFieldValue();
                    if (origValue != null && origValue.toString().length() > 0) {
                        tooltip += " Originalwert: '" + origValue + "'";
                    }
                    label.setToolTipText( tooltip );
                }
                catch (Exception e) {
                    log.warn( e );
                    label.setToolTipText( dirty ? "Dieser Wert wurde geändert. Originalwert kann nicht ermittelt werden." : "" );
                }
            }
            else {
                label.setImage( null );
                label.setToolTipText( "" );
                site.getFieldControl().setForeground( origForeground );
                site.getFieldControl().setToolTipText( origTooltip );
            }
        }
    }
    
    
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == FOCUS_GAINED) {
            focus = true;
        }
        else if (ev.getEventCode() == FOCUS_LOST) {
            focus = false;
        }
        else if (ev.getEventCode() == VALUE_CHANGE) {
            dirty = site.isDirty();
            invalid = !site.isValid();

            log.debug( "fieldChange(): dirty= " + dirty + ", focus= " + focus + ", invalid=" + invalid );
            updateUI();
        }
    }

}
