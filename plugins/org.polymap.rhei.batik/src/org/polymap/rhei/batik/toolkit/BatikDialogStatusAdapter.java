/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.toolkit;

import static org.polymap.core.ui.FormDataFactory.on;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IStatus;

import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.StatusDispatcher.Adapter;
import org.polymap.core.ui.StatusDispatcher.Style;

/**
 * Register with {@link StatusDispatcher}. Shows status in a {@link SimpleDialog}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikDialogStatusAdapter
        implements Adapter {

    private static Log log = LogFactory.getLog( BatikDialogStatusAdapter.class );

    private static final DefaultToolkit    tk = new DefaultToolkit( null, null );
    
    
    @Override
    public void handle( IStatus status, Style... styles ) {
        UIThreadExecutor.asyncFast( () -> {
            SimpleDialog dialog = new SimpleDialog();
            // title
            switch (status.getSeverity()) {
                case IStatus.WARNING : dialog.title.put( "Warning" ); break;
                case IStatus.ERROR : dialog.title.put( "Application Error" ); break;
                case IStatus.INFO : dialog.title.put( "Information" ); break;
            }
            // contents
            dialog.setContents( parent -> {
                parent.setLayout( FormLayoutFactory.defaults().spacing( 0 ).create() );
                Label msg = on( tk.createFlowText( parent, status.getMessage() ) )
                        .fill().noBottom().width( 350 ).control();

                if (status.getException() != null) {
                    Throwable exc = status.getException();
                    Throwable root = Throwables.getRootCause( exc );
                    on( tk.createFlowText( parent, "**Reason**: " + root.getMessage() ) )
                            .fill().top( msg );
                }
            });
            dialog.addOkAction( () -> dialog.close() );
            dialog.open();
        });
    }

}
