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
package org.polymap.rhei.batik.dashboard;

import java.util.List;

import org.eclipse.swt.widgets.Control;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Defaults;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.PropertyChangeSupport;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DashletSite
        extends Configurable {

    /**
     * The title of the dashlet.
     * <p/>
     * Allows null to allow no header at all.
     */
    @Concern( PropertyChangeSupport.class )
    public Config2<DashletSite,String>      title;

    /** Uninitialized specifies that {@link Dashboard#defaultBorder} is used. */
    public Config2<DashletSite,Boolean>     border;
    
    /**
     * List of layout constraints. Modify only in {@link IDashlet#init(DashletSite)}
     * <b>before</b> {@link IDashlet#createContents(org.eclipse.swt.widgets.Composite)} is called.
     */
    @Mandatory
    @Defaults
    public Config2<DashletSite,List<LayoutConstraint>> constraints;

    /** Uninitialized specifies that {@link Dashboard#defaultExpandable} is used. */
    public Config2<DashletSite,Boolean>     expandable;

    /**
     * Shortcut to {@link #constraints}.
     */
    public abstract DashletSite addConstraint( LayoutConstraint constraint );
    
    public abstract IPanelSite panelSite();
    
    public abstract IPanelToolkit toolkit();
    
    /**
     * Signals the panel that this {@link ISubmitableDashlet} can/not submit.
     * 
     * @param dirty Specifies that there is submitable content.
     * @param valid Specifies that the submitable content is valid and ready to submit.
     */
    public abstract void enableSubmit( boolean dirty, boolean valid );

    /**
     * The submit state controlled by the dashlet via {@link #enableSubmit(boolean, boolean)}.
     */
    public abstract boolean isDirty();

    /**
     * The submit state controlled by the dashlet via {@link #enableSubmit(boolean, boolean)}.
     */
    public abstract boolean isValid();

    public abstract IPanelSection getPanelSection();

    public abstract Control getTitleControl();

    public abstract boolean isExpanded();

    public abstract DashletSite setExpanded( boolean expanded );
    
}
