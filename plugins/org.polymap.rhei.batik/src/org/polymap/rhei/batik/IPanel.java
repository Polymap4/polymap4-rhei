/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.toolkit.ConstraintLayout;

/**
 * The panel is the main visual component of the Atlas UI. It typically hosts a
 * map view, an editor, wizard or a dashboard.
 * <p/>
 * A panel is identified by its path and name. The path defines the place in the
 * hierarchy of panel.
 * <p/>
 * {@link DefaultPanel} provides default implementations of basic methods.
 *
 * @see Scope
 * @see DefaultPanel
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanel {

    /**
     * Called by the engine right after constructing this panel and before
     * any other call. Must not be called by client code.
     *
     * @param site
     * @param context
     */
    public void setSite( PanelSite site, IAppContext context );
    
    /**
     * The {@link PanelSite} interface of this panel.
     *
     * @return The instance previously given to {@link #setSite(PanelSite, IAppContext)}.
     */
    public PanelSite site();

    /**
     * This method is called before {@link #init(IPanelSite, IAppContext)} in order
     * to check if the panel wants to be displayed on top of the current panel in the
     * given context. All {@link Context} properties are initialized when the method
     * is called.
     * <p/>
     * This method should be lightweight. It <b>must not initialize</b> anything,
     * except for the title and icon which is shown in the navigation bar.
     * <p/>
     * <h2>Examples:</h2> Check if the panel is right above the start panel:
     * 
     * <pre>
     * site.getPath().size() == 1;
     * </pre>
     * 
     * Check if parent is instance of particular type:
     * 
     * <pre>
     * IPanel parent = parentPanel(); //getContext().getPanel( getSite().getPath().removeLast( 1 ) );
     * if (parent instanceof ProjectPanel) {
     *     site().title.set( ... );
     *     return true;
     * }
     * return false;
     * 
     * </pre>
     *
     * @param site
     * @param context
     * @return True if the panels wants to be shown.
     */
    public boolean beforeInit();

    /**
     * Initializes the panel. This method is called right before the panel is
     * activated for the first time.
     * <p/>
     * This method is called just once, register for {@link PanelChangeEvent}s to get
     * notified everytime the panel is activated.
     * 
     * @param site
     * @param context
     */
    public void init();
    
    /**
     * This method is called before {@link #dispose()} in order to check if the panel
     * can be closed. This method should be lightweight and should not dispose
     * anything.
     *
     * @return False specifies that this panel does not want to be closed.
     */
    public boolean beforeDispose();
    
    public void dispose();
    
    public boolean isDisposed();

    public PanelIdentifier id();
    
    /**
     * Creates the UI elements of this panel.
     * <p/>
     * {@link ConstraintLayout} is set for the <code>panelBody</code> by default.
     * Margin width/height are set according the space available in the panel. This
     * can be changed as needed.
     * 
     * @param panelBody The parent of the UI elements to create.
     */
    public void createContents( Composite panelBody );
}
