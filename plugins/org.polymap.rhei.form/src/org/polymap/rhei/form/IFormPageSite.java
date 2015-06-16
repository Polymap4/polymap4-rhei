/*
 * polymap.org
 * Copyright (C) 2010-2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.form;

import org.opengis.feature.Property;

/**
 * Provides the interface used inside {@link IFormPage} methods to
 * interact with the framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFormPageSite
        extends IBasePageSite {

    public FieldBuilder newFormField( Property property );
    
    /**
     * Reloads all fields of the editor from the backend.
     */
    public void reloadEditor() throws Exception;

    /**
     * Submits all changed fields of the editor to the backend.
     */
    public void submitEditor() throws Exception;

}
