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

import java.util.function.Function;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * An {@link ActionItem} that is placed on an {@link ActionText}. 
 *
 * @author Falko Bräutigam
 */
public class TextActionItem
        extends ActionItem {

    /**
     * The type of a {@link TextActionItem}. 
     */
    public enum Type {
        /**
         * The default action of an ActionText is performed when ENTER is pressed or,
         * depending on {@link ActionText#performOnEnter}, automatically after some
         * delay.
         * <p/>
         * If specified the {@link Item#text} is displayed as grayed hint on the text field if no
         * input has been given yet. If specified {@link Item#tooltip} is used as tooltip of the
         * entire {@link ActionText}.
         */
        DEFAULT,
        /** @deprecated Not yet implemented. */
        REFRESH, 
        NORMAL;
    }
    
    /**
     * 
     */
    @FunctionalInterface
    interface Activation 
            extends Function<ActionText,Boolean> {
    }


    // instance *******************************************

    /**
     * The type of this action. Defaults to {@link Type#NORMAL}.
     */
    @Mandatory
    @Immutable
    @Concern( ItemEvent.Fire.class )
    public Config2<TextActionItem,Type>     type;

    /**
     * Items are added right to left. First created item is placed right most on the
     * text widget.
     */
    public TextActionItem( ActionText atext, Type type ) {
        super( atext );
        this.type.set( type );
    }

}
