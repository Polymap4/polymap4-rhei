/* 
 * polymap.org
 * Copyright (C) 2013-2016, Polymap GmbH. All rights reserved.
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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.DISABLED12;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionEvent;

import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormDataFactory.Alignment;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.app.IAppDesign;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelSection
        extends Configurable
        implements IPanelSection {

    private static final Log log = LogFactory.getLog( PanelSection.class );
    
    @Mandatory
    @DefaultBoolean( true )
    public Config2<PanelSection,Boolean> expandable;
    
    private int                 level;

    private Composite           control;
    
    private Composite           titleParent;
    
    private Label               title;

    private Composite           client;

    private Label               sep;
    
    private boolean             expanded = true;

    private Label               chevron;

    
    public PanelSection( DefaultToolkit tk, Composite parent, int[] styles ) {
        control = new Composite( parent, SWT.NO_FOCUS | tk.stylebits( styles ) );
        UIUtils.setVariant( control, DefaultToolkit.CSS_SECTION  );
        control.setData( "panelSection", this );
        control.setMenu( parent.getMenu() );
        control.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );

        // title
        title = new Label( control, SWT.NO_FOCUS );
        UIUtils.setVariant( title, DefaultToolkit.CSS_SECTION_TITLE  );
        title.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        FormDataFactory.on( title ).fill().noBottom().height( 26 );
        title.setVisible( false );
        title.addListener( SWT.MouseUp, e -> toggleExpanded() );
        
        if (ArrayUtils.contains( styles, EXPANDABLE )) {
            UIUtils.setVariant( title, DefaultToolkit.CSS_SECTION_TITLE_EXPANDABLE );
            chevron = new Label( control, SWT.NO_FOCUS );
            chevron.setImage( BatikPlugin.images().svgImage( expanded ? "chevron-down.svg" : "chevron-right.svg", DISABLED12 ) );
            FormDataFactory.on( chevron ).top( title, 2, Alignment.CENTER ).left( 0 );
            FormDataFactory.on( title ).left( chevron );
            chevron.addListener( SWT.MouseUp, e -> toggleExpanded() );
        }
        
        // separator
        sep = new Label( control, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.HORIZONTAL );
        UIUtils.setVariant( sep, DefaultToolkit.CSS_SECTION_SEPARATOR );
        FormDataFactory.filled().top( this.title ).bottom( -1 ).applyTo( sep );
        sep.setVisible( false );
        sep.moveBelow( title );

        // client; border style signals CSS that the section has a border
        client = tk.adapt( new Composite( control, SWT.NO_FOCUS | tk.styleHas( styles, SWT.BORDER ) ) );
        UIUtils.setVariant( client, DefaultToolkit.CSS_SECTION_CLIENT );
        FormDataFactory.filled().top( sep ).applyTo( client );

        IAppDesign appDesign = BatikApplication.instance().getAppDesign();
        ConstraintLayout clientLayout = new ConstraintLayout( appDesign.getPanelLayoutPreferences() );
        client.setLayout( clientLayout );
        client.moveBelow( title );
        
        level = getParentPanel() != null ? getParentPanel().getLevel()+1 : 0;
    }

    
    public void dispose() {
        control.dispose();
    }


    @Override
    public ILayoutElement addConstraint( LayoutConstraint... constraints ) {
        if (control.getLayoutData() == null) {
            control.setLayoutData( new ConstraintData() );
        }
        ((ConstraintData)control.getLayoutData()).add( constraints );
        return this;
    }


    /**
     * Do the layout of this newly added children. 
     */
    protected void controlAdded( Control c ) {
        log.info( "control added: " + c );
    }

    protected void controlRemoved( Control c ) {
        log.info( "control removed: " + c );
    }
    
    @Override
    public Composite getControl() {
        return control;
    }

    @Override
    public Composite getBody() {
        return client;
    }

    @Override
    public Control getTitleControl() {
        return title;
    }

    @Override
    public IPanelSection getParentPanel() {
        // parent -> section -> client
        for (Composite cursor=control.getParent(); cursor!=null; cursor=cursor.getParent()) {
            Object result = cursor.getData( "panelSection" );
            if (result != null) {
                return (IPanelSection)result;
            }
        }
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getTitle() {
        return title.getText();
    }

    @Override
    public IPanelSection setTitle( String s ) {
        if (s != null) {
            title.setText( s );
        }
        title.setVisible( s != null );
        if (sep != null) {
            sep.setVisible( s != null );
        }
        return this;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public IPanelSection setExpanded( boolean expanded ) {
        if (this.expanded != expanded) {
            if (expanded) {
                client.setVisible( true );
                sep.setVisible( true );
                //Point sepSize = sep.computeSize( control.getSize().x, SWT.DEFAULT );
                sep.setLayoutData( FormDataFactory.filled().top( title ).noBottom().create() );
                client.setLayoutData( FormDataFactory.filled().top( sep ).create() );
            }
            else {
                client.setVisible( false );
                sep.setVisible( false );
                sep.setLayoutData( FormDataFactory.on( sep ).top( title ).height( 0 ).create() );
                client.setLayoutData( FormDataFactory.on( client ).top( sep ).height( 0 ).create() );
            }
            chevron.setImage( BatikPlugin.images().svgImage( expanded ? "chevron-down.svg" : "chevron-right.svg", DISABLED12 ) );
            
            ExpansionEvent ev = new ExpansionEvent( PanelSection.this, expanded );
            EventManager.instance().publish( ev );
            this.expanded = expanded;
        }
        
        // layout data even if expanded state has not changed; Dashboard uses this
        // after content has been created
        control.layout();
        control.getParent().layout();
        return this;
    }
    
    public IPanelSection toggleExpanded() {
        setExpanded( !expanded );
        return this;
    }
    
}
