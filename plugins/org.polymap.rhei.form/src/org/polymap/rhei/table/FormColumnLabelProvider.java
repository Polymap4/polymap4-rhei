/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FormColumnLabelProvider
        extends ColumnLabelProvider {
        
    private FormFeatureTableColumn      tableColumn;

    
    public FormColumnLabelProvider( FormFeatureTableColumn tableColumn ) {
        this.tableColumn = tableColumn;
    }

    
    @Override
    public String getText( Object elm ) {
        try {
            IFeatureTableElement felm = (IFeatureTableElement)elm;
            //log.info( "getText(): fid=" + featureElm.fid() + ", prop=" + prop.getName().getLocalPart() );

            Object result = tableColumn.modifiedFieldValue( felm, false );
            if (result == null) {
                return "";
            }
            // XXX the modified field value is the value that the IFormField can work with;
            // this can be any type (for example when using PicklistFormField) but we need a
            // String representation here; validator.transform2Field() expects the value from
            // the model, so it is a wild guess to use it for the field value
            else if (result instanceof String) {
                return (String)result;
            }
            else {
                try {
                    DefaultValidatorSite site = new DefaultValidatorSite( felm, tableColumn, false );
                    return (String)tableColumn.validator.transform2Field( result, site );
                }
                catch (Exception e) {
                    return result.toString();
                }
            }
        }
        catch (Exception e) {
            FormFeatureTableColumn.log.warn( "", e );
            return "Fehler: " + e.getLocalizedMessage();
        }
    }


    @Override
    public String getToolTipText( Object elm ) {
        return elm != null ? getText( elm ) : null;
    }
    
}