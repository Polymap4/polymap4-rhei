/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.toolkit.md;

import static org.polymap.rhei.batik.toolkit.md.MdAppDesign.dp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.engine.DefaultAppDesign;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class MdActionbar
            extends Composite {

    private static final Log log = LogFactory.getLog( MdActionbar.class );

    private final MdToolkit     tk;

    private List<IAction>       actions = new ArrayList();

    //private Label               msg;
    
    private boolean             wasEverVisible = false;

 
    MdActionbar( MdToolkit tk, Composite parent ) {
        super( parent, SWT.NONE );
        this.tk = tk;
        tk.adapt( this );
        this.moveAbove( null );
        UIUtils.setVariant( this, MdToolkit.CSS_ACTIONBAR );
        FormDataFactory.on( this ).fill().top( 0, dp( DefaultAppDesign.HEADER_HEIGHT ) )
                .noBottom().height( dp( DefaultAppDesign.HEADER_HEIGHT ) );

        setLayout( FormLayoutFactory.defaults().margins( 3 ).spacing( 8 ).create() );
//        setLayout( RowLayoutFactory.fillDefaults()
//                .fill( false ).margins( 3, 3 ).spacing( 8 ).pack( true ).create() );
        
//        msg = tk.createFlowText( this, "" );
//        FormDataFactory.on( msg ).fill().noRight();
    }

    
    public MdActionbar setMessage( String msg ) {
        throw new UnsupportedOperationException( "setMessage() is not implemented yet." );
        //return this;
    }

    
    /**
     * Creates a default submit/apply action with default icon and text adds it to
     * the receiver.
     * <p/>
     * Caller should invoke {@link IAction#setEnabled(boolean)} on the result action.
     * <p/>
     * If an action is added <b>after</b> the actionbar was initially created and
     * made visible then {@link #layout()} should be called afterwards.
     */
    public Action addSubmit( Consumer<Action> task ) {
        return addSubmit( MdToolkit.i18n.get( "submit" ), task );
    }

    
    /**
     * Creates a default submit/apply action with default icon and adds it to the
     * receiver.
     * <p/>
     * Caller should invoke {@link IAction#setEnabled(boolean)} on the result action.
     * <p/>
     * If an action is added <b>after</b> the actionbar was initially created and
     * made visible then {@link #layout()} should be called afterwards.
     */
    public Action addSubmit( String text, Consumer<Action> task ) {
        return addAction( text, BatikPlugin.images().svgImageDescriptor( "check2.svg", SvgImageRegistryHelper.WHITE24 ), task );
    }

    
    /**
     * Creates a default action with the given text and icon and adds it to the
     * receiver.
     * <p/>
     * If an action is added <b>after</b> the actionbar was initially created and
     * made visible then {@link #layout()} should be called afterwards.
     * 
     * @param text The text of the action, or null.
     * @param icon The icon of the action, or null.
     * @param task The task to be performed.
     */
    public Action addAction( String text, ImageDescriptor icon, Consumer<Action> task ) {
        Action action = new Action() {
            public void run() {
                try {
                    task.accept( this );
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "Unable to perform task.", e );
                }
            }
        };
        addAction( action );

        if (text != null) {
            action.setText( text );
        }
        if (icon != null) {
            action.setImageDescriptor( icon );
        }
        return action;
    }

    
    /**
     * Adds the given action and creates a {@link Control} for it. Consider a more
     * convenient method like {@link #addSubmit(String, Consumer)}.
     * <p/>
     * If an action is added <b>after</b> the actionbar was initially created and
     * made visible then {@link #layout()} should be called afterwards.
     *
     * @param action
     * @return this
     */
    public MdActionbar addAction( IAction action ) {
        assert actions.isEmpty() : "Layout of multiple actions is not supported yet.";
        
        // create Button
        Button btn = tk.createButton( this, action.getText(), SWT.PUSH, SWT.FLAT );
        UIUtils.setVariant( btn, MdToolkit.CSS_ACTIONBAR );
        FormDataFactory.on( btn ).fill().noLeft();
        btn.addSelectionListener( UIUtils.selectionListener( ev -> action.run() ) );
        
        action.addPropertyChangeListener( ev -> {
            if (btn.isDisposed()) {
                log.warn( "Button of this Action is disposed already." );
            }
            else if (ev.getProperty().equals( "text" )) {
                btn.setText( action.getText() );
            }
            else if (ev.getProperty().equalsIgnoreCase( "tooltiptext" )) {
                btn.setToolTipText( action.getToolTipText() );
            }
            else if (ev.getProperty().equals( "image" )) {
                btn.setImage( action.getImageDescriptor() != null
                        ? action.getImageDescriptor().createImage() : null );
            }
            else if (ev.getProperty().equals( "enabled" )) {
                btn.setEnabled( action.isEnabled() );
                updateVisibility();
            }
            else {
                throw new UnsupportedOperationException( "Property is not supported: " + ev.getProperty() );
            }
        });
        
        // add
        actions.add( action );
        return this;
    }


    protected void updateVisibility() {
        setVisible( !actions.isEmpty() && actions.stream().allMatch( a -> a.isEnabled() ) );
        if (isVisible()) {
            if (!wasEverVisible) {
                log.info( "LAYOUT " );
                // layout buttons with text/icons set
                MdActionbar.this.layout( true, true );
                // help parent to make me visible
                getParent().layout( new Control[] {this} );
            }
            wasEverVisible = true;
        }
    }
    
}