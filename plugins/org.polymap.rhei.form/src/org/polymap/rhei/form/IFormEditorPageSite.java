/*
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.form;

import org.opengis.feature.Property;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;

/**
 * Provides the interface used inside {@link IFormEditorPage} methods to
 * interact with the framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFormEditorPageSite {

    /**
     * Specifies the title of this form page. The title is usually displayed
     * in the tab of this page and in the form title.
     *
     * @param title The title of this page. Must not be null.
     */
    public void setFormTitle( String title );

    /**
     * Specifies the title of the entire editor this page is part of.
     *
     * @param title The title of the editor. Must not be null.
     */
    public void setEditorTitle( String title );

    public void setActivePage( String pageId );
    
    /**
     * The parent of all controls of the page. Use this to create new controls inside
     * the page. The returned Composite has no particular {@link Layout} set.
     * 
     * @return The parent Composite of the page.
     */
    public Composite getPageBody();

    public IFormEditorToolkit getToolkit();

    /**
     *
     * @param parent
     * @param prop
     * @param field
     * @param validator A validator, or null if the {@link NullValidator} should be used.
     * @param label
     */
    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator, String label );

    /**
     *
     * @param parent
     * @param prop
     * @param field
     * @param validator A validator, or null if the {@link NullValidator} should be used.
     */
    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator );

    /**
     * Registers the given listener that is notified about changes of an {@link IFormField}.
     * <p/>
     * The listener is handled by the global {@link EventManager}. The caller has to make
     * sure that there is a (strong) reference as long as the listener is active.
     *
     * @param listener
     * @throws IllegalStateException If the given listener is registered already.
     * @see EventManager#subscribe(Object, org.polymap.core.runtime.event.EventFilter...)
     */
    public void addFieldListener( IFormFieldListener listener );

    public void removeFieldListener( IFormFieldListener listener );

    /**
     *
     * @param source XXX
     * @param eventCode One of the constants in {@link IFormFieldListener}.
     * @param validatedNewValue
     */
    public void fireEvent( Object source, String fieldName, int eventCode, Object validNewValue );

    public void setFieldValue( String fieldName, Object value );

    
    /**
     * Returns the validated and transformed value of the given field. This is not
     * necessarily the value shown in the UI but the value that is send to backend on
     * next store.
     * 
     * @param fieldName
     */
    public <T> T getFieldValue( String fieldName );

    public void setFieldEnabled( String fieldName, boolean enabled );

    /**
     * Reloads all fields of the editor from the backend.
     */
    public void reloadEditor()
    throws Exception;

    /**
     * Submits all changed fields of the editor to the backend.
     */
    public void submitEditor()
    throws Exception;

    /**
     * True if any field of the page is dirty and/or if {@link IFormEditorPage2}
     * has reported that it is dirty.
     *
     * @return True if the page has unsaved changes.
     */
    public boolean isDirty();

    
    /**
     * True if all fields of the page have valid state. If the page is an
     * {@link IFormEditorPage2} than the page have to have valid state too.
     * 
     * @return True if all unsaved changes of the page are valid.
     */
    public boolean isValid();

    public void clearFields();

}
