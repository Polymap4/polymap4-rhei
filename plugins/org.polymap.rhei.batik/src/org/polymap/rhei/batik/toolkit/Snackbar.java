/*
 * polymap.org 
 * Copyright (C) 2015-2016 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.toolkit;

import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.core.ui.UIUtils.setVariant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.toolkit.md.MdAppDesign;

/**
 * Snackbars provide lightweight feedback about an operation by showing a brief
 * message at the bottom of the screen. Snackbars can contain an action.
 * 
 * @see <a href="http://www.google.com/design/spec/components/snackbars-toasts.html">
 *      Material Design</a>.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("javadoc")
public class Snackbar
        extends Configurable {

    private static Log log = LogFactory.getLog( Snackbar.class );

    public enum Appearance {
        /**
         * Makes the snackbar flying in from bottom.
         * <p/>
         * Use this to give feedback about some <b>progress</b>, such as a process
         * has been started or something has been opened.
         */
        FlyIn,
        /** Fade in at bottom.
         * <p/>
         * Use this to give feedback about a <b>single action</b>, such as user input
         * was wrong.
         */
        FadeIn    
    }

    @Mandatory
    public Config2<Snackbar,Appearance>   appearance;
    
    /** The timeout before the snackbar disappears. */
    @Mandatory
    @DefaultInt( 5 )
    public Config2<Snackbar,Integer>      hideTimeout;
    
    /** The message to display in the Snackbar. */
    @Mandatory
    public Config2<Snackbar,String>       message;
    
    @Mandatory
    public Config2<Snackbar,Item[]>       actions;

    private Composite                     control;
    
    
    public Snackbar( IPanelToolkit tk, Composite parent ) {
        UIThreadExecutor.async( () -> createContents( tk, parent ) );
    }
    
    
    protected void createContents( IPanelToolkit tk, Composite parent ) {
        if (parent.isDisposed()) {
            log.warn( "Parent is disposed." );
            return;
        }
        
        int height = MdAppDesign.dp( 80 );
        
        control = tk.createComposite( parent );
        control.moveAbove( null );
        
        // appearance
        if (appearance.get() == Appearance.FlyIn) {
            setVariant( control, "snackbar-fly" );
            on( control ).fill().top( 100, -height );
        }
        else if (appearance.get() == Appearance.FadeIn) {
            setVariant( control, "snackbar-fade" );
            int margin = height / 2;
            on( control ).left( 0, margin ).right( 100, -margin ).top( 100, -(height+margin) ).bottom( 100, -margin );
        }
        else {
            throw new RuntimeException( "Unhandled Appearance type: " + appearance.get() );
        }
        
        // layout
        control.setLayout( FormLayoutFactory.defaults().margins( height/2, (height-17)/2 ).spacing( 8 ).create() );
        
        // message
        on( setVariant( tk.createLabel( control, message.get() ), "snackbar-message" ) )
                .fill().noRight().height( height );
        
        // actions
        Control lastBtn = null;
        for (Item action : actions.get()) {
            Control btn = null;
            if (action instanceof ActionItem) {
                btn = tk.createButton( control, action.text.get(), SWT.FLAT );
                ((Button)btn).addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        ((ActionItem)action).action.ifPresent( a -> a.accept( ev ) );
                    }
                });
            }
            else {
                throw new RuntimeException();
            }
            FormDataFactory layoutData = on( btn ).fill().noLeft();
            if (lastBtn != null) {
                layoutData.right( lastBtn );
            }
            lastBtn = btn;
        }

        // force display
        parent.layout( new Control[] {control} );
        
        // auto hide
        parent.getDisplay().timerExec( hideTimeout.get()*1000, () -> {
            if (!control.isDisposed()) {
                // start hide animation
                control.setVisible( false );
                // delayed so that dispose() does not break animation
                parent.getDisplay().timerExec( 1000, () -> {
                    if (!control.isDisposed()) {
                        control.dispose();
                    }
                });
            }
        });
    }

}
