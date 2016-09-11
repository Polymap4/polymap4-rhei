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
package org.polymap.rhei.batik.toolkit;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A panel section is the main layout component of the Atlas UI. It consists of a
 * title and a body. The body can contain plain widgets or sub-sections. The child
 * elements are layouted in a column by default. The layout can be controlled via
 * constraints (see {@link Column#addConstraint(LayoutConstraint)}).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSection
        extends ILayoutContainer, ILayoutElement {

    /** Style constant: make an IPanelSection expandable */
    public static final int         EXPANDABLE = 1;
    
    public IPanelSection getParentPanel();
    
    /**
     * The level in the section hierarchy. Panel section created by calling
     * {@link IPanelToolkit#createPanelSection(Composite, String, int...)} return
     * <code>0</code> here.
     */
    public int getLevel();
    
    public IPanelSection setExpanded( boolean expanded );
    
    public boolean isExpanded();
    
    public String getTitle();
    
    public IPanelSection setTitle( String title );

    public Control getTitleControl();
    
}
