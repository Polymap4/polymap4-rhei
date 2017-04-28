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

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

/**
 * An action to be used with {@link ActionText}. Clears the text. 
 *
 * @author Falko Bräutigam
 */
public class ClearTextAction
        extends TextActionItem {

    public ClearTextAction( ActionText atext ) {
        super( atext, Type.NORMAL );
        icon.set( BatikPlugin.images().svgImage( "broom.svg", SvgImageRegistryHelper.DISABLED12 ) );
        tooltip.set( "Clear text" );
        action.set( ev -> {
            atext.getText().setText( "" );
            atext.defaultAction().ifPresent( item -> item.action.get().accept( null ) );
        });
    }

}
