/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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

import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.CellEditor;

/**
 * Performs an action registered via
 * {@link #addListener(org.eclipse.jface.viewers.ICellEditorListener)}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ActionCellEditor
        extends CellEditor {

    private static Log log = LogFactory.getLog( ActionCellEditor.class );
    
    private Consumer<IFeatureTableElement>      action;
    
    private IFeatureTableElement                selected;
    
    
    public ActionCellEditor( Consumer<IFeatureTableElement> action ) {
        this.action = action;
    }

    
    void setSelected( IFeatureTableElement selected ) {
        this.selected = selected;
    }


    /**
     * Triggers {@link #fireApplyEditorValue()} so that any registered listen
     * immediatelly performs its action.
     */
    @Override
    public void activate() {
        action.accept( selected );
        //fireApplyEditorValue();
    }


    /**
     * The <code>CheckboxCellEditor</code> implementation of this
     * <code>CellEditor</code> framework method does nothing and returns
     * <code>null</code>.
     */
    protected Control createControl( Composite parent ) {
        return null;
    }


    /**
     * @return null
     */
    protected Object doGetValue() {
        return null;
    }


    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    protected void doSetFocus() {
        // Ignore
    }


    /**
     * The <code>CheckboxCellEditor</code> implementation of this
     * <code>CellEditor</code> framework method accepts a value wrapped as a
     * <code>Boolean</code>.
     *
     * @param value a Boolean value
     */
    protected void doSetValue( Object value ) {
    }

}
