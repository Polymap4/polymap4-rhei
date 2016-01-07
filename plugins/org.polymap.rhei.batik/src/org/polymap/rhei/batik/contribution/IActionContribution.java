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
package org.polymap.rhei.batik.contribution;

import java.util.EventObject;
import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IActionContribution
        extends IContributionProvider {
    
    public void fillAction( IContributionSite site, Consumer<Action> target );
    
    
    /**
     * 
     */
    public static class Action {

        @Concern( ActionChangeEvent.Fire.class )
        public Config2<Action,String>        text;
        
        @Concern( ActionChangeEvent.Fire.class )
        public Config2<Action,String>        tooltip;
        
        @Concern( ActionChangeEvent.Fire.class )
        public Config2<Action,Image>         icon;

        /** The action to be performed when the item is pressed. */
        @Mandatory
        @Concern( ActionChangeEvent.Fire.class )
        public Config2<Action,Consumer<SelectionEvent>> action;
    }

    
    /**
     * Thrown when a {@link Config} property of an item is changed. 
     */
    static class ActionChangeEvent
            extends EventObject {

        public ActionChangeEvent( Action source ) {
            super( source );
        }

        public <T extends Action> T action() {
            return (T)super.getSource();
        }
        
        @Override
        public Action getSource() {
            return (Action)super.getSource();
        }
        
        /**
         * 
         */
        public static class Fire
                extends DefaultPropertyConcern {

            /**
             * This is called *before* the {@link Config2} property is set. However, there is no
             * race condition between event handler thread, that might access property value, and
             * the current thread, that sets the property value, because most {@link EventHandler}s
             * are done in display thread.
             */
            @Override
            public Object doSet( Object obj, Config prop, Object newValue ) {
                Action action = prop.info().getHostObject();
                EventManager.instance().syncPublish( new ActionChangeEvent( action ) );
                return newValue;
            }
        }
    }

}
