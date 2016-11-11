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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.HorizontalFieldLayout;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLayout;
import org.polymap.rhei.field.IFormFieldListener;

/**
 * Base class of form and filter page sites.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IBasePageSite {

    /**
     * Specifies the title of this form page. The title is usually displayed
     * in the tab of this page and in the form title.
     *
     * @param title The title of this page. Must not be null.
     */
    public void setPageTitle( String title );

    /**
     * Specifies the title of the entire editor this page is part of.
     *
     * @param title The title of the editor. Must not be null.
     */
    public void setEditorTitle( String title );

    public void setActivePage( String pageId );
    
    /**
     * The parent of all controls of the page. Use this to create new controls inside
     * the page. The Composite has default {@link Layout} set as defined by the
     * particular {@link BasePageContainer container} used.
     * 
     * @return The parent Composite of the page.
     */
    public Composite getPageBody();

    public IFormToolkit getToolkit();

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
     * @param source
     * @param eventCode One of the constants in {@link IFormFieldListener}.
     * @param newFieldValue
     * @param newModelValue
     */
    public void fireEvent( Object source, String fieldName, int eventCode, Object newFieldValue, Object newModelValue );

    /**
     * Sets the model value of the given field. The model value is not necessarily
     * the value shown in the UI but the value that is send to backend on next store.
     */
    public void setFieldValue( String fieldName, Object value );
    
    /**
     * Returns the validated and transformed, current value of the given field. This
     * is not necessarily the value shown in the UI but the value that is send to
     * backend on next store.
     */
    public <T> T getFieldValue( String fieldName );

    public void setFieldEnabled( String fieldName, boolean enabled );

    /**
     * True if any field of the page is dirty and/or if {@link IFormPage2}
     * has reported that it is dirty.
     *
     * @return True if the page has unsaved changes.
     */
    public boolean isDirty();

    /**
     * True if all fields of the page have valid state. If the page is an
     * {@link IFormPage2} than the page have to have valid state too.
     * 
     * @return True if all unsaved changes of the page are valid.
     */
    public boolean isValid();
    
    /**
     * (Re)loads all fields of the editor from the backend.
     * <p/>
     * This method might long run and/or block while accessing the backend system.
     * 
     * @param monitor This method can be called from within a {@link Job}. It reports
     *        progress to this monitor. Outside a job this parameter might be
     *        <code>null</code>.
     */
    public void reload( IProgressMonitor monitor ) throws Exception;

    /**
     * Sets the default layout for all form fields. Defaults to:
     * {@link HorizontalFieldLayout} width adaptive label width depending on overall
     * display width.
     * 
     * @see HorizontalFieldLayout
     * @see VerticalFieldLayout
     */
    public void setDefaultFieldLayout( IFormFieldLayout layout );

}
