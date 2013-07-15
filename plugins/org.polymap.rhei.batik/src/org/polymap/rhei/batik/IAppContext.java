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

import com.google.common.base.Predicate;

import org.polymap.core.runtime.event.EventFilter;

/**
 * An app context is shared by all {@link IPanel} instances in the same panel
 * hierachy.
 * <p/>
 * Properties of the context can be injected into an {@link IPanel} by declaring a
 * {@link ContextProperty} with appropriate type and scope.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAppContext {

    /**
     *
     *
     * @param parent The path of the panel to open.
     * @param name The name of the panel to open
     * @return Null if the given panels was not found.
     */
    public IPanel openPanel( PanelIdentifier panelId );

    public void closePanel();

    /**
     * All direct children of the given path.
     *
     * @see PanelFilters
     * @param path
     */
    public Iterable<IPanel> findPanels( Predicate<IPanel> filter );

    /**
     * Registers the given {@link EventHandler event handler} for event types:
     * <ul>
     * <li>{@link PanelChangeEvent}</li>
     * </ul>
     *
     * @see EventHandler
     * @see EventManager
     * @param handler
     */
    public void addEventHandler( Object handler, EventFilter<PanelChangeEvent>... filters );

    public void removeEventHandler( Object handler );

}
