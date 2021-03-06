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

import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.rhei.batik.toolkit.md.MdAppDesign.dp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Messages;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.engine.PageStack;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.SimpleDialog;
import org.polymap.rhei.batik.toolkit.md.MdAppDesign.FontStyle;

/**
 * Material design toolkit.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolkit
        extends DefaultToolkit {

    public static final IMessages   i18n = Messages.forPrefix( "MdToolkit" );
    
    public static final String      CSS_FAB = CSS_PREFIX + "-fab";
    public static final String      CSS_ACTIONBAR = CSS_PREFIX + "-actionbar";

    
    public MdToolkit( PanelPath panelPath, PageStack<PanelPath>.Page panelPage ) {
        super( panelPath, panelPage );
        assert panelPage != null;
    }


    /**
     * Creates a new {@link Label}.
     * <p/>
     * {@link Label#setEnabled(boolean) Disabling} the label gives it a slightly
     * grayed look. This can be used for longer description text.
     */
    @Override
    public Label createLabel( Composite parent, String text, int... styles ) {
        return super.createLabel( parent, text, styles );
    }


    /**
     * The following button types are allowed:
     * <ul>
     * <li>{@link SWT#PUSH}</li>
     * <li>{@link SWT#TOGGLE}</li>
     * <li>{@link SWT#CHECK}</li>
     * <li>{@link SWT#RADIO}</li>
     * </ul>
     * {@link SWT#PUSH} and {@link SWT#TOGGLE} buttons are Raised buttons be default. {@link SWT#BORDER}
     * has no effect. A Flat button appearance can be created by giving {@link SWT#FLAT} flag.
     * 
     * @see <a href="http://www.google.com/design/spec/components/buttons.html">Material Design</a>
     */
    @Override
    public Button createButton( Composite parent, String text, int... styles ) {
        return super.createButton( parent, text, styles );
    }


    /**
     * Creates a default Floating Action Button with default "check" icon and
     * position TOP|RIGHT.
     * 
     * @see #createFab(String, Image, int)
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab() {
        return createFab( SWT.TOP|SWT.RIGHT );
    }
    
    
    /**
     * Creates a default Floating Action Button with the given icon.
     * 
     * @see #createFab(String, Image, int)
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( Image icon ) {
        return createFab( null, icon, SWT.TOP|SWT.RIGHT );
    }
    
    
    /**
     * Creates a default Floating Action Button with the given text.
     * 
     * @see #createFab(String, Image, int)
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( String text ) {
        return createFab( text, null, SWT.TOP|SWT.RIGHT );
    }
    
    
    /**
     * Creates a default Floating Action Button with default "check" icon.
     * 
     * @param position A combination of {@link SWT#TOP}, {@link SWT#BOTTOM},
     *        {@link SWT#LEFT} and {@link SWT#RIGHT}.
     * @see #createFab(Image, int)
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( int position ) {
        return createFab( i18n.get( "fab" ), null /*BatikPlugin.images().svgImage( "check.svg", SvgImageRegistryHelper.WHITE24 )*/, position );
    }
    
    
    /**
     * Creates a Floating Action Button.
     * 
     * @param icon
     * @param position A combination of {@link SWT#TOP}, {@link SWT#BOTTOM},
     *        {@link SWT#LEFT} and {@link SWT#RIGHT}.
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( String text, Image icon, int position ) {
        assert (position & ~SWT.TOP & ~SWT.BOTTOM & ~SWT.LEFT & ~SWT.RIGHT) == 0 : "position param is not valid: " + position;
        
        Button result = createButton( panelPage.control, "", SWT.PUSH );
        assert text == null || icon == null : "Icon *and* text is not allowed for FAB!";
        if (icon != null) {
            result.setImage( icon );
        }
        else if (text != null) {
            result.setText( text );
            result.setFont( MdAppDesign.font( FontStyle.Body2 ) );
        }
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        
        int marginTop = dp( 78 );
        int margin = dp( 40 );
        int size = dp( 90 );

        FormDataFactory layout = on( result ).width( size ).height( size );
        
        if ((position & SWT.TOP) != 0) {
            layout.top( 0, marginTop );
        }
        else if ((position & SWT.BOTTOM) != 0) {
            layout.bottom( 100, margin );
        }
        else {
            layout.top( 50, -size/2 );            
        }
        
        if ((position & SWT.LEFT) != 0) {
            layout.left( 0, margin );
        }
        else if ((position & SWT.RIGHT) != 0) {
            layout.right( 100, -margin );
        }
        else {
            layout.left( 50, -size/2 );            
        }
        return result;
    }


//    public Composite createCard( Composite parent ) {
//        throw new RuntimeException( "not yet..." );
//    }


    /**
     *
     * @param styles
     *        <ul>
     *        <li>SWT.TOP - Toolbar on top, shadow at the bottom.</li>
     *        <li>SWT.BOTTOM - Toolbar on bottom, shadow at the top.</li>
     *        </ul>
     * @see <a href="https://material.io/design/components/app-bars-top.html#">
     *      Material Design</a>
     */
    public MdActionbar createFloatingActionbar() {
        return new MdActionbar( this, panelPage.control );
    }


    public MdListViewer createListViewer( Composite parent, int... styles ) {
        return new MdListViewer( parent, stylebits( styles ) );
    }

    
    @Override
    public SimpleDialog createSimpleDialog( String title ) {
        SimpleDialog result = super.createSimpleDialog( title );
        result.centerOn.put( panelPage.control );
        return result;
    }


    /**
     * 
     *
     * @param parent
     * @param styles
     *        <ul>
     *        <li>SWT.TOP - Toolbar on top, shadow at the bottom.</li>
     *        <li>SWT.BOTTOM - Toolbar on bottom, shadow at the top.</li>
     *        <li>SWT.FLAT|default - Flat style, no shadow</li>
     *        </ul>
     * @see <a href="http://www.google.com/design/spec/components/toolbars.html">
     *      Material Design</a>
     */
    public MdToolbar2 createToolbar( Composite parent, int... styles ) {
        return new MdToolbar2( parent, this, stylebits( styles ) );
    }

    
    @Override
    public Section createSection( Composite parent, String title, int... styles ) {
       Section result = super.createSection( parent, title, styles );
       result.setFont( MdAppDesign.font( FontStyle.Subhead ) );
       return result;
    }

}
