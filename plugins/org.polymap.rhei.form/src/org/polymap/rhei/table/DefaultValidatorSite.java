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
package org.polymap.rhei.table;

/**
 *
 * 
 * @author Falko Br�utigam
 */
class DefaultValidatorSite
        implements ITableFieldValidator.ValidatorSite {

    private IFeatureTableElement    element;

    private FormFeatureTableColumn  column;
    
    private boolean                 editing;

    public DefaultValidatorSite( IFeatureTableElement element, FormFeatureTableColumn column, boolean editing ) {
        assert element != null : "element is null.";
        assert column != null : "column is null.";
        this.element = element;
        this.column = column;
        this.editing = editing;
    }

    @Override
    public IFeatureTableElement element() {
        return element;
    }

    @Override
    public FormFeatureTableColumn column() {
        return column;
    }
    
    @Override
    public boolean isEditing() {
        return editing;
    }
    
}