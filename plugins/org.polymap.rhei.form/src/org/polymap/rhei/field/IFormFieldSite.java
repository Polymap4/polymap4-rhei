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
package org.polymap.rhei.field;

import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.editor.FormEditor;

import org.polymap.rhei.form.IFormToolkit;

/**
 * The primary interface between a form field and the {@link FormEditor}.
 * <p>
 * A {@link FormEditor} exposes its implemention of the interface via this
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFormFieldSite {

    public String getFieldName();

    /**
     * Returns the current value of this field from the backend store. The value
     * is transformed via {@link IFormFieldValidator#transform2Field(Object)} in
     * order to represent the value type corresponding to the particular
     * {@link IFormField}.So this method can be used to initialize or
     * {@link IFormField#load()} the state of the UI.
     * 
     * @throws Exception When the value could not be validated/transformed with
     *         the {@link IFormFieldValidator} of this field.
     */
    public Object getFieldValue() throws Exception;

    /**
     * Changes the value of this field as the result of a submit action. The
     * value is transformed via
     * {@link IFormFieldValidator#transform2Model(Object)}. This method should
     * be called by {@link IFormField#store()}.
     * 
     * @throws Exception When the value could not be validated/transformed with
     *         the {@link IFormFieldValidator} of this field.
     */
    public void setFieldValue( Object value ) throws Exception;
    
    public boolean isValid();
    
    public boolean isDirty();
    
    public void addChangeListener( IFormFieldListener l );
    
    public void removeChangeListener( IFormFieldListener l );
    
    /**
     *
     * @param source XXX
     * @param eventCode One of the constants in {@link IFormFieldListener}.
     * @param newValue
     */
    public void fireEvent( Object source, int eventCode, Object newFieldValue ); 
    
    public IFormToolkit getToolkit();
    
    /**
     *
     * @return The current error message, or null if no error.
     */
    public String getErrorMessage();

    public void setErrorMessage( String msg );
    
    public Control getFieldControl();
}
