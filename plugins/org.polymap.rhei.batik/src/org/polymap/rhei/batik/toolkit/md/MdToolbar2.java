/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.GroupItem;
import org.polymap.rhei.batik.toolkit.Item;
import org.polymap.rhei.batik.toolkit.ItemEvent;
import org.polymap.rhei.batik.toolkit.RadioItem;

/**
 * The next generation toolbar :) 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolbar2
        extends GroupItem {

    private static Log log = LogFactory.getLog( MdToolbar2.class );
    
    /** Pseudo class of the toolbar Composite: basic, no shadow */
    private static final String     CSS_TOOLBAR = "toolbar2";
    
    /** Pseudo class of the toolbar Composite: bottom shadow */
    private static final String     CSS_TOOLBAR_TOP = "toolbar2-top";
    
    /** Pseudo class of the toolbar Composite: top shadow */
    private static final String     CSS_TOOLBAR_BOTTOM = "toolbar2-bottom";

    private static final String     CSS_TOOLBAR_GROUP = "toolbar2-group";
    
    /** Pseudo class for toolbar item (Button). */
    private static final String     CSS_TOOLBAR_ITEM = "toolbar2-item";
    
    /**
     * 
     */
    public enum Alignment {
        Left, Right;        
    }
    
    
    // instance *******************************************
    
    private Composite               bar;

    private MdToolkit               tk;
    
    private Map<Item,ItemHandler>   handlers = new HashMap();


    MdToolbar2( Composite parent, MdToolkit tk, int style ) {
        super( null, "root" );
        this.tk = tk;
        
        String css = CSS_TOOLBAR;
        if ((style & SWT.TOP) > 0) {
            css = CSS_TOOLBAR_TOP;
        }
        else if ((style & SWT.BOTTOM) > 0) {
            css = CSS_TOOLBAR_BOTTOM;
        }
        else if ((style & SWT.FLAT) > 0) {
            css = CSS_TOOLBAR;
        }

        if ((style & SWT.RIGHT) > 0) {
            log.warn( "Aligment is not supported yet." );
            align.set( Alignment.Right );
        }

        bar = setVariant( tk.createComposite( parent, style ), css );

        EventManager.instance().subscribe( this, ifType( ItemEvent.class, ev -> 
                // FIXME does not work for hierarchy of GroupItem
                ev.getSource().container() == MdToolbar2.this || ev.getSource() == MdToolbar2.this) );
       
        ToolbarHandler toolbarHandler = new ToolbarHandler( this );
        toolbarHandler.onItemChange( null );
        handlers.put( this, toolbarHandler );        
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }
    
    
    @EventHandler( display=true, delay=30 )
    protected void onItemChange( List<ItemEvent> evs ) {
        if (!bar.isDisposed()) {
            //log.info( "onItemChange(): events: " + evs.size() );
            for (ItemEvent ev : evs) {
                onItemChange( ev );
            }
            bar.layout( true );
        }
        else {
            EventManager.instance().unsubscribe( MdToolbar2.this );
        }
    }


    protected void onItemChange( ItemEvent ev ) {
        ItemHandler handler = handlers.computeIfAbsent( ev.item(), item -> {
            if (item instanceof MdToolbar2) {
                return new ToolbarHandler( (MdToolbar2)item );
            }
            else if (item instanceof GroupItem) {
                return new GroupItemHandler( (GroupItem)item );
            }
            else if (item instanceof ActionItem) {
                return new ActionItemHandler( (ActionItem)item );
            }
            else if (item instanceof RadioItem) {
                return new RadioItemHandler( (RadioItem)item );
            }
            else {
                throw new RuntimeException( "Unhandled item type: " + item );
            }
        });
        handler.onItemChange( ev );
    }


    /**
     * 
     */
    protected abstract class ItemHandler<I extends Item, C extends Control> {
        
        protected I                 item;
        
        protected C                 control;
        
        public ItemHandler( I item ) {
            this.item = item;
        }

        public GroupItemHandler parent() {
            assert item.container() != null : "This is the root container.";
            return (GroupItemHandler)handlers.get( item.container() );
        }
        
        protected abstract void onItemChange( ItemEvent ev );

    }
    
    /**
     * Handles {@link MdToolbar2}.
     */
    protected class ToolbarHandler
            extends GroupItemHandler {
        
        public ToolbarHandler( GroupItem item ) {
            super( item );
        }
    
        protected void onItemChange( ItemEvent ev ) {
            if (control == null) {
                control = bar;  //setVariant( tk.createComposite( bar ), CSS_TOOLBAR_GROUP );
                control.setLayout( RowLayoutFactory.fillDefaults().spacing( 0 ).create() );
            }
        }
    }

    /**
     * Handles {@link GroupItem}.
     */
    protected class GroupItemHandler
            extends ItemHandler<GroupItem,Composite> {
        
        /** Single {@link RadioItemHandler}, or multiple Toggle. */
        protected Set<ButtonItemHandler>    selected = new HashSet();
        
        public GroupItemHandler( GroupItem item ) {
            super( item );
        }

        protected void onItemChange( ItemEvent ev ) {
            if (control == null) {
                control = setVariant( tk.createComposite( parent().control ), CSS_TOOLBAR_GROUP );
                control.setLayout( RowLayoutFactory.fillDefaults().spacing( 0 ).create() );
            }
        }
    }
    
    /**
     * 
     */
    protected abstract class ButtonItemHandler<I extends Item>
            extends ItemHandler<I,Button> {
        
        protected int               btnType = SWT.NONE;

        public ButtonItemHandler( I item ) {
            super( item );
        }

        protected void onItemChange( ItemEvent ev ) {
            if (control == null) {
                control = setVariant( tk.createButton( parent().control, null, btnType ), CSS_TOOLBAR_ITEM );
                control.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 30 ).create() );
            }
            item.text.ifPresent( v -> control.setText( v ) );
            item.tooltip.ifPresent( v -> control.setToolTipText( v ) );
            item.icon.ifPresent( v -> control.setImage( v ) );
        }
    }
    
    /**
     * 
     */
    protected class ActionItemHandler
            extends ButtonItemHandler<ActionItem> {

        public ActionItemHandler( ActionItem item ) {
            super( item );
            btnType = SWT.PUSH;
        }

        @Override
        protected void onItemChange( ItemEvent ev ) {
            // update UI
            boolean initialized = control != null;
            super.onItemChange( ev );
            
            if (!initialized) {
                control.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent sev ) {
                        item.action.ifPresent( callback -> callback.accept( null ) );
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    protected class RadioItemHandler
            extends ButtonItemHandler<RadioItem> {

        private AtomicBoolean         skipNextEvent = new AtomicBoolean();
        
        public RadioItemHandler( RadioItem item ) {
            super( item );
            btnType = SWT.TOGGLE;
        }

        @Override
        protected void onItemChange( ItemEvent ev ) {
            // update UI
            boolean initialized = control != null;
            super.onItemChange( ev );
            control.setSelection( item.selected.get() );

            if (!initialized) {
                control.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent sev ) {
                        item.selected.set( ((Button)sev.widget).getSelection() );                        
                    }
                });
            }
            
            // selection changed
            if (ev.prop() == item.selected && !skipNextEvent.getAndSet( false )) {
                
                // selected
                if (item.selected.get()) { //((Boolean)ev.newValue()) == true) {
                    // deselect previous
                    assert parent().selected.size() <= 1;
                    parent().selected.stream().findAny().ifPresent( previous -> {
                        ((RadioItemHandler)handlers.get( previous.item )).skipNextEvent.set( true );
                        ((RadioItem)previous.item).selected.set( false );
                        // we cannot wait for the event to do this
                        ((RadioItem)previous.item).onUnselected.ifPresent( callback -> callback.accept( null ) );
                        parent().selected.remove( previous );
                    });
                    
                    //
                    assert parent().selected.isEmpty();
                    parent().selected.add( RadioItemHandler.this );
                    //
                    item.onSelected.ifPresent( callback -> callback.accept( null ) );
                }
                
                // deselected
                else {
                    item.onUnselected.ifPresent( callback -> callback.accept( null ) );
                    if (!parent().selected.remove( RadioItemHandler.this )) {
                        throw new IllegalStateException( "..." );
                    }
                }
            }
        }
    }
    
    
    public Control getControl() {
        return bar;
    }
    
}
