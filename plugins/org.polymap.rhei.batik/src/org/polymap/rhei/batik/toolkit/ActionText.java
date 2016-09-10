/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.toolkit;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.NumberRangeValidator;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.TextActionItem.Type;

/**
 * Like {@link Text} but allows to add {@link TextActionItem}s to perform action,
 * search or clear. One item is the default item which is performed automatically or
 * when ENTER is pressed, depending on {@link #performOnEnter}.
 * <p/>
 * Items are added right to left. First created item is placed right most on the text
 * widget.
 *
 * @author Falko Bräutigam
 */
public class ActionText
    extends Configurable
    implements FocusListener, ItemContainer<TextActionItem> {

    private static final Log log = LogFactory.getLog( ActionText.class );

    /**
     * True specifies that the action is performed when keyboard Enter is pressed.
     * Otherwise the action is performed automatically (after
     * {@link #performDelayMillis}) when input has changed.
     */
    @DefaultBoolean( true )
    public Config2<ActionText,Boolean>      performOnEnter;

    /**
     * The delay before auto search starts. Only used if {@link #performOnEnter} is
     * set to false.
     */
    @DefaultInt( 750 )
    @Check( value=NumberRangeValidator.class, args={"0","10000"} )
    public Config2<ActionText,Integer>      performDelayMillis;

    protected Composite                     container;
    
    protected Text                          text;
    
    private List<TextActionItem>            actions = new ArrayList();

    private List<Button>                    actionBtns = new ArrayList();
    
    private boolean                         modified;


    ActionText( Composite parent, String defaultTxt, int... styles ) {
        container = new Composite( parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
 
        text = new Text( container, SWT.SEARCH | SWT.CANCEL );
        text.setLayoutData( FormDataFactory.filled().create() );
        text.moveBelow( null );

        text.addFocusListener( this );

        text.addKeyListener( new KeyAdapter() {
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.Selection && performOnEnter.get()) {
                    defaultAction().ifPresent( item -> item.action.get().accept( null ) );
                }
            }
        });
//        text.addModifyListener( new ModifyListener() {
//            @Override
//            public void modifyText( ModifyEvent ev ) {
//            }
//        });

        EventManager.instance().subscribe( this, ifType( ItemEvent.class, ev -> 
                // FIXME does not work for hierarchy of GroupItem
                ev.getSource().container() == ActionText.this ) );
    }

    
    @EventHandler( display=true, delay=30 )
    protected void onItemChange( List<ItemEvent> evs ) {
        if (!container.isDisposed()) {
            updateActions();
            container.layout();
            container.getParent().layout( true );
        }
        else {
            EventManager.instance().unsubscribe( ActionText.this );
        }
    }

    
    @Override
    public void focusLost( FocusEvent ev ) {
//        if (text.getText().length() == 0) {
//            searchTxt.setText( "Suchen..." );
//            searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
//            clearBtn.setVisible( false );
//        }
    }
    
    @Override
    public void focusGained( FocusEvent ev ) {
        if (!modified) {
            modified = true;
            text.setText( "" );
            text.setForeground( text.getParent().getForeground() );
        }
    }
    
    
    @Override
    public boolean addItem( TextActionItem action ) {
        return actions.add( action );
    }

    
    @Override
    public boolean removeItem( TextActionItem action ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    protected Optional<TextActionItem> defaultAction() {
        assert actions.stream().filter( a -> a.type.get().equals( Type.DEFAULT ) ).count() <= 1;
        return actions.stream().filter( a -> a.type.get().equals( Type.DEFAULT ) ).findAny();
    }

    
    protected void updateActions() {
        // defaultAction
        defaultAction().ifPresent( action -> {
            text.setForeground( UIUtils.getColor( 0xa0, 0xa0, 0xa0 ) );
            action.text.ifPresent( v -> text.setText( v ) );
            action.tooltip.ifPresent( v -> text.setToolTipText( v ) );
        });
        
        // actionBtns
        actionBtns.forEach( btn -> btn.dispose() );
        actionBtns.clear();
        Label lastBtn = null;
        for (TextActionItem action : actions) {
            Label btn = createActionBtn( action );
            FormDataFactory layout = FormDataFactory.on( btn ).top( 0, 6 ).right( 100, -8 ).noLeft();
            if (lastBtn != null) {
                layout.right( lastBtn, -2 );
            }
            lastBtn = btn;
        }
        
        text.moveBelow( null );
    }
    
    
    protected Label createActionBtn( ActionItem item ) {
        Label btn = new Label( container, SWT.PUSH | SWT.SEARCH );
        item.tooltip.ifPresent( v -> btn.setToolTipText( v ) );
        item.icon.ifPresent( v -> btn.setImage( v ) );
        
        btn.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent ev ) {
                item.action.get().accept( null );
            }
        });
        btn.setVisible( true );
        return btn;
    }

    
    @Override
    public List<TextActionItem> items() {
        return Collections.unmodifiableList( actions );
    }


//    /**
//     * The action is performed inside a {@link UIJob}. Refreshing the UI
//     * is done by {@link #doRefresh()} inside the display thread.
//     */
//    public ActionText addItem( ActionItem action, Activation activation ) {
//        actions.put( action, activation );
//        return this;
//    }
//    
//
//    /**
//     * Like {@link #addAction(ActionItem)} but makes this action the action to be
//     * performed on Enter.
//     * 
//     * @throws AssertionError If default action is already set.
//     */
//    public ActionText setDefaultAction( ActionItem action ) {
//        assert this.defaultAction == null;
//        this.defaultAction = action;
//        return this;
//    }
//    
//
//    /**
//     * Updates the UI after another action has been performed.
//     */
//    public ActionText addRefreshAction( ActionItem action ) {
//        refreshActions.add( action );
//        return this;
//    }
    

    public Composite getControl() {
        return container;
    }


    public Text getText() {
        return text;
    }


}
