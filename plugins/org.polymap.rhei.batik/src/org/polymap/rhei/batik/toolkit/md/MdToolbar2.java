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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.GroupItem;
import org.polymap.rhei.batik.toolkit.Item;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.RadioItem;
import org.polymap.rhei.batik.toolkit.ToggleItem;
import org.polymap.rhei.batik.toolkit.ItemEvent;

/**
 * The next generation toolbar :) 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolbar2
        implements ItemContainer {

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
    
    private Composite               bar;

    private MdToolkit               tk;
    
    private GroupItem               rootGroup = new GroupItem( null, "root" );
    
    private Map<GroupItem,Button>   selectedRadios = new HashMap();
    

    MdToolbar2( Composite parent, MdToolkit tk, int style ) {
        this.tk = tk;
        
        bar = setVariant( tk.createComposite( parent, style ), CSS_TOOLBAR );
        bar.setLayout( new FillLayout() );  //RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
        
        EventManager.instance().subscribe( this, ifType( ItemEvent.class, 
                ev2 -> ev2.getSource().container() == MdToolbar2.this ) );
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void onItemChange( List<ItemEvent> evs ) {
        renderGroup( bar, rootGroup );
    }
    

    @Override
    public void addItem( Item item ) {
        rootGroup.addItem( item );
    }


    @Override
    public List<Item> items() {
        return rootGroup.items();
    }


    protected void renderGroup( Composite parent, GroupItem group ) {
        // find Control of the group
        Composite control = findControl( parent, group );
        
        // create if not yet present
        if (control == null) {
            control = tk.createComposite( parent );
            control.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
            control.setData( "_item_", group );
        }
        
        // items
        for (Item item : group.items()) {
            renderItem( control, item, group );
        }
    }
    
    
    protected void renderItem( Composite parent, Item item, GroupItem group ) {
        // find Control of the group
        Button btn = findControl( parent, item );
        
        // action
        if (item instanceof ActionItem) {
            if (btn == null) {
                btn = createBtn( item, parent, SWT.PUSH );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        ((ActionItem)item).action.get().accept( ev );
                    }
                });
            }
            updateBtn( item, btn );
        }

        // toggle
        else if (item instanceof ToggleItem) {
            if (btn == null) {
                btn = createBtn( item, parent, SWT.TOGGLE );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        (((Button)ev.widget).getSelection()
                            ? ((ToggleItem)item).onSelected.get()
                            : ((ToggleItem)item).onUnselected.get()).accept( ev );                            
                    }
                });
            }
            updateBtn( item, btn );
        }

        // radio
        else if (item instanceof RadioItem) {
            if (btn == null) {
                btn = createBtn( item, parent, SWT.TOGGLE );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        Button _btn = (Button)ev.widget;
                        Button currentSelection = selectedRadios.remove( group );
                        // selected
                        if (_btn.getSelection()) {
                            if (currentSelection != null) {
                                currentSelection.setSelection( false );
                            }
                            selectedRadios.put( group, _btn );
                        }
                        // action
                        (_btn.getSelection()
                                ? ((RadioItem)item).onSelected.get()
                                : ((RadioItem)item).onUnselected.get()).accept( ev );                            
                    }
                });
            }
            updateBtn( item, btn );
        }

        // unknown
        else {
            throw new RuntimeException( "Unhandled ToolItem type: " + item );
        }
    }


    protected Button createBtn( Item item, Composite parent, int style ) {
        Button btn = setVariant( tk.createButton( parent, null, style ), CSS_TOOLBAR_ITEM );
        btn.setData( "_item_", item );
        btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 30 ).create() );
        return btn;
    }
    
    
    protected void updateBtn( Item item, Button btn ) {
        btn.setText( item.text.get() );
        btn.setToolTipText( ((Item)item).tooltip.get() );
        btn.setImage( ((Item)item).icon.get() );
    }


    protected <C extends Control> C findControl( Composite parent, Item item ) {
        return (C)Arrays.stream( parent.getChildren() )
                .filter( c -> c.getData( "_item_" ) == item )
                .findAny().orElse( null );
    }


    public Control getControl() {
        return bar;
    }
    
}
