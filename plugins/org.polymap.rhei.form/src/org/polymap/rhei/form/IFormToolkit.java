/*
 * polymap.org 
 * Copyright (C) 2010-2015, Falko Br�utigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.rhei.form;

import java.util.Date;
import java.util.Set;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.filter.FilterEditor;

import org.polymap.rap.updownload.upload.Upload;

/**
 * Provides a general factory facade for the creation of UI element. Concrete
 * implementations are provided for {@link FormEditor}, {@link FilterEditor}
 * and maybe other usecases.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFormToolkit {

    public abstract Button createButton( Composite parent, String text, int... styles );

    public abstract Composite createComposite( Composite parent, int... styles );

    public abstract Composite createCompositeSeparator( Composite parent );

    public abstract ExpandableComposite createExpandableComposite( Composite parent,
            int expansionStyle );

    public abstract Form createForm( Composite parent );

    public abstract FormText createFormText( Composite parent, boolean trackFocus );

    public abstract Hyperlink createHyperlink( Composite parent, String text, int... styles );

    public abstract ImageHyperlink createImageHyperlink( Composite parent, int... styles );

    public abstract Label createLabel( Composite parent, String text, int... styles );

    public abstract ScrolledPageBook createPageBook( Composite parent, int... styles );

    public abstract ScrolledForm createScrolledForm( Composite parent );

    public abstract Section createSection( Composite parent, int sectionStyle );

    public abstract Label createSeparator( Composite parent, int... styles );

    public abstract Table createTable( Composite parent, int... styles );

    public abstract Text createText( Composite parent, String value, int... styles );

    public abstract Tree createTree( Composite parent, int... styles );

    public abstract Combo createCombo( Composite parent, Set<String> values, int... styles );
    
    public abstract DateTime createDateTime( Composite parent, Date value );
    
    public abstract DateTime createDateTime( Composite parent, Date value, int style );

    public abstract Upload createUpload( Composite parent, int style, int flags );

    public abstract List createList( Composite parent, int... styles );
    
}