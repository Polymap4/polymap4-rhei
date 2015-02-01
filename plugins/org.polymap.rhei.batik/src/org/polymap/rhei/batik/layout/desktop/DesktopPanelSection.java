/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik.layout.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rap.rwt.RWT;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.ILayoutElement;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopPanelSection
        implements IPanelSection {

    private static Log log = LogFactory.getLog( DesktopPanelSection.class );
    
    private int                     level;

    private Composite               control;
    
    private Label                   title;

    private Composite               client;

    private Label                   sep;

    
    public DesktopPanelSection( DesktopToolkit tk, Composite parent, int[] styles ) {
        control = new Composite( parent, SWT.NO_FOCUS | tk.stylebits( styles ) );
        UIUtils.setVariant( control, DesktopToolkit.CSS_SECTION  );
        control.setData( "panelSection", this );
        control.setMenu( parent.getMenu() );
        control.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );

        // title
        title = new Label( control, SWT.NO_FOCUS );
        UIUtils.setVariant( title, DesktopToolkit.CSS_SECTION_TITLE  );
        title.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        FormDataFactory.filled().bottom( -1 ).height( 25 ).applyTo( title );
        title.setVisible( false );
        
        // separator
        sep = new Label( control, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.HORIZONTAL );
        UIUtils.setVariant( sep, DesktopToolkit.CSS_SECTION_SEPARATOR );
        FormDataFactory.filled().top( this.title ).bottom( -1 ).applyTo( sep );
        sep.setVisible( false );
        sep.moveBelow( title );

        // client
        client = tk.adapt( new Composite( control, SWT.NO_FOCUS ) );
        UIUtils.setVariant( client, DesktopToolkit.CSS_SECTION_CLIENT );
        FormDataFactory.filled().top( sep ).applyTo( client );

//        ColumnLayout clientLayout = ColumnLayoutFactory.defaults().columns( 1, 3 ).spacing( 10 ).margins( 10 ).create(); 
        ConstraintLayout clientLayout = new ConstraintLayout( 0, 0, 10 );
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
    public IPanelSection setTitle( String txt ) {
        if (txt != null) {
            title.setText( txt );
        }
        title.setVisible( txt != null );
        if (sep != null) {
            sep.setVisible( txt != null );
        }
        return this;
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public IPanelSection setExpanded( boolean expanded ) {
        return this;
    }
    
}
