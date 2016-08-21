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
package org.polymap.rhei.batik;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.PropertyInfo;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.engine.PageStackLayout;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Provides the interface between {@link IPanel} client code and the Batik
 * framework. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PanelSite
        extends Configurable {

    private static Log log = LogFactory.getLog( PanelSite.class );
    
    @Immutable
    public Config2<PanelSite,Integer>       stackPriority;
    
    /**
     * The title of the page. Null specifies that the panel does not show up in
     * the panel navigator bar.
     */
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,String>        title;

    /**
     * The title and tooltip of the page.
     */
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,String>        tooltip;
    
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,Image>         icon;

    /**
     * Hints the application layouter about the preferred size of this panel. The
     * layouter tries the give focused panels at least this width. A value of
     * {@link Integer#MAX_VALUE} specifies that this panel will use all extra space
     * that might be available.
     * <p/>
     * Set this in {@link IPanel#init()} or {@link IPanel#createContents(Composite)}.
     * Setting or modifying later effects layout on next panel open/close.
     */
    @Mandatory
    @DefaultInt( Integer.MAX_VALUE )
    @Concern( NotNegative.class )
    @Concern( PreferredWidthConcern.class )
    public Config2<PanelSite,Integer>       preferredWidth;

    /**
     * Hints the application layouter to make this panel not wider than the given
     * number of pixels. The layouter uses this for pages that are ...
     * <p/>
     * Set this in {@link IPanel#init()} or {@link IPanel#createContents(Composite)}.
     * Setting or modifying later effects layout on next panel open/close.
     */
    @Mandatory
    @DefaultInt( Integer.MAX_VALUE )
    @Concern( NotNegative.class )
    @Concern( MaxWidthConcern.class )
    public Config2<PanelSite,Integer>       maxWidth;

    /**
     * Hints the application layouter to make this panel not smaller than the given
     * number of pixels. The layouter uses this for pages that are not focused.
     * <p/>
     * Set this in {@link IPanel#init()} or {@link IPanel#createContents(Composite)}.
     * Setting or modifying later effects layout on next panel open/close.
     */
    @Mandatory
    @DefaultInt( PageStackLayout.DEFAULT_PAGE_MIN_WIDTH )
    @Concern( NotNegative.class )
    @Concern( MinWidthConcern.class )
    public Config2<PanelSite,Integer>       minWidth;


    /**
     * Sets the size constraints of the panel.
     * <p/>
     * Call this from {@link IPanel#init()} or
     * {@link IPanel#createContents(Composite)}. Setting or modifying later has no
     * effect.
     *
     * @param min The {@link #minWidth} of the panel.
     * @param preferred The {@link #preferredWidth} of the panel.
     * @param max The {@link #maxWidth} of the panel.
     */
    public PanelSite setSize( int min, int preferred, int max ) {
        minWidth.set( min );
        preferredWidth.set( preferred );
        maxWidth.set( max );
        return this;    
    }
    
    /**
     * The entiry path of the panel including the name of the panel as last segment.
     */
    public abstract PanelPath path();
    
    public abstract PanelStatus panelStatus();
    
    public abstract Memento memento();
        
    public abstract IPanelToolkit toolkit();

    public abstract void layout( boolean changed );

    /**
     * Layout preferences should be used by client code in order to fit panel layout
     * into the layout of the application.
     */
    public abstract LayoutSupplier layoutPreferences();
    

    /**
     * 
     */
    public static class MinWidthConcern
            extends DefaultPropertyConcern<Integer> {

        @Override
        public Integer doSet( Object obj, Config<Integer> prop, Integer value ) {
            PanelSite site = (PanelSite)obj;
            check( value, site.preferredWidth.get(), 1, "minWidth", "preferredWidth" );                
            check( value, site.maxWidth.get(), 1, "minWidth", "maxWidth" );                
            return value;
        }
    }

    
    /**
     * 
     */
    public static class MaxWidthConcern
            extends DefaultPropertyConcern<Integer> {

        @Override
        public Integer doSet( Object obj, Config<Integer> prop, Integer value ) {
            PanelSite site = (PanelSite)obj;
            check( value, site.preferredWidth.get(), -1, "maxWidth", "preferredWidth" );                
            check( value, site.minWidth.get(), -1, "maxWidth", "minWidth" );                
            return value;
        }
    }

    
    /**
     * 
     */
    public static class PreferredWidthConcern
            extends DefaultPropertyConcern<Integer> {

        @Override
        public Integer doSet( Object obj, Config<Integer> prop, Integer value ) {
            PanelSite site = (PanelSite)obj;
            check( value, site.minWidth.get(), -1, "preferredWidth", "minWidth" );                
            check( value, site.maxWidth.get(), 1, "preferredWidth", "maxWidth" );                
            return value;
        }
    }

    
    /**
     * 
     */
    public static class NotNegative
            extends DefaultPropertyConcern<Integer> {

        @Override
        public Integer doSet( Object obj, Config<Integer> prop, Integer value ) {
            return check( value, 0, -1, "max/min/preferredWidth", "0" );
        }
    }

    
    protected static Integer check( Integer v1, Integer v2, int falseExpected, String b1, String b2 ) {
        if (v1.compareTo( v2 ) == falseExpected) {
            String op = falseExpected < 0 ? "<" : ">";
            throw new IllegalArgumentException( Joiner.on( " " ).join( "Illegal value:", b1, op, b2, "(", v1, op, v2, ")" ) );                
        }
        return v1;
    }
    
    /**
     * 
     */
    public static class FireEvent
            extends DefaultPropertyConcern {

        /**
         * This is called *before* the {@link Config2} property is set. However, there is no
         * race condition between event handler thread, that might access property value, and
         * the current thread, that sets the property value, because most {@link EventHandler}s
         * are done in display thread.
         */
        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            PropertyInfo info = prop.info();
            PanelSite site = info.getHostObject();
            
            FireEventType a = info.getAnnotation( FireEventType.class );
            assert a != null : "Missing @FireEventType annotation!";
            
            Object oldValue = info.getRawValue();
            if (!Objects.equals( newValue, oldValue )) {
                EventManager.instance().publish( new PanelChangeEvent( site, a.value(), newValue, oldValue ) );
            }
            return newValue;
        }
    }

}
