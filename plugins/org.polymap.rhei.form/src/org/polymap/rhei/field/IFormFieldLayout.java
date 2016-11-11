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
package org.polymap.rhei.field;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The layout of a form field, including label, field and decorator.
 *
 * @author Falko Br�utigam
 */
public interface IFormFieldLayout {

    public static enum Part {
        Label, Field, Decorator
    }
    
    /**
     * 
     *
     * @param parent
     * @param parts
     */
    public void createLayout( Composite parent, Map<Part,Control> parts );
    
}
