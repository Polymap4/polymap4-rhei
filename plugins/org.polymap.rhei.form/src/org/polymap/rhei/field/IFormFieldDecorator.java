/* 
 * polymap.org
 * Copyright (C) 2010-2017, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormToolkit;

/**
 * Creates a decoration next to the field indicating the valid and dirty state of the
 * field.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFormFieldDecorator {

    public void init( IFormFieldSite site );

    public Control createControl( Composite parent, IFormToolkit toolkit );

    public void dispose();

}
