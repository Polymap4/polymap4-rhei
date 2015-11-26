/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.UIUtils.setVariant;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolbar2
        implements MdItemContainer {

    private static Log log = LogFactory.getLog( MdToolbar2.class );
    
    /** Pseudo class for toolbar Composite. */
    private static final String     CSS_TOOLBAR = "toolbar2";
    /** Pseudo class for toolbar item (Button). */
    private static final String     CSS_TOOLBAR_ITEM = "toolbar2-item";
    
    /**
     * 
     */
    public enum Alignment {
        Left, Right;        
    }
    
    
    // instance *******************************************
    
    private Composite           bar;

    private MdToolkit           tk;
    
    private MdGroupItem         rootGroup = new MdGroupItem( null, "root" );            
    

    MdToolbar2( Composite parent, MdToolkit tk, int style ) {
        this.tk = tk;
        
        bar = setVariant( tk.createComposite( parent, style ), CSS_TOOLBAR );
        bar.setLayout( new FillLayout() );  //RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
        
        EventManager.instance().subscribe( this, ifType( ToolItemEvent.class, 
                ev2 -> ev2.getSource().container() == MdToolbar2.this ) );
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void onItemChange( List<ToolItemEvent> evs ) {
        renderGroup( bar, rootGroup );
    }
    

    @Override
    public void addItem( MdItem item ) {
        rootGroup.addItem( item );
    }


    @Override
    public List<MdItem> items() {
        return rootGroup.items();
    }


    protected void renderGroup( Composite parent, MdGroupItem group ) {
        // find Control of the group
        Composite control = findControl( parent, group );
        
        // create if not yet present
        if (control == null) {
            control = tk.createComposite( parent );
            control.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
            control.setData( "_item_", group );
        }
        
        //
        for (MdItem item : group.items()) {
            renderItem( control, item );    
        }
    }
    
    
    protected void renderItem( Composite parent, MdItem item ) {
        // find Control of the group
        Button btn = findControl( parent, item );
        
        // action item
        if (item instanceof MdActionItem) {
            if (btn == null) {
                btn = setVariant( tk.createButton( parent, null, SWT.PUSH ), CSS_TOOLBAR_ITEM );
                btn.setData( "_item_", item );
                btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 30 ).create() );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        ((MdActionItem)item).action.get().accept( ev );
                    }
                });
            }
            
            //o = item.text.get();
            
            btn.setText( item.text.get() );
            btn.setToolTipText( ((MdActionItem)item).tooltip.get() );
            btn.setImage( ((MdActionItem)item).icon.get() );
        }
        // unknown
        else {
            throw new RuntimeException( "Unhandled ToolItem type: " + item );
        }
    }
    
    
    protected <C extends Control> C findControl( Composite parent, MdItem item ) {
        return (C)Arrays.stream( parent.getChildren() )
                .filter( c -> c.getData( "_item_" ) == item )
                .findAny().orElse( null );
    }


    public Control getControl() {
        return bar;
    }
    
    
    // ToolItemEvent **************************************
    
    /**
     * Thrown when a {@link Config} property of an item is changed. 
     */
    static class ToolItemEvent
            extends EventObject {

        public ToolItemEvent( MdItem source ) {
            super( source );
        }

        public <T extends MdItem> T item() {
            return (T)super.getSource();
        }
        
        @Override
        public MdItem getSource() {
            return (MdItem)super.getSource();
        }
        
        /**
         * 
         */
        public static class Fire
                extends DefaultPropertyConcern {

            /**
             * This is called *before* the {@link Config2} property is set. However, there is no
             * race condition between event handler thread, that might access property value, and
             * the current thread, that sets the property value, because most {@link EventHandler}s
             * are done in display thread.
             */
            @Override
            public Object doSet( Object obj, Config prop, Object newValue ) {
                MdItem item = prop.info().getHostObject();
                EventManager.instance().syncPublish( new ToolItemEvent( item ) );
                return newValue;
            }
        }
    }
    
}
