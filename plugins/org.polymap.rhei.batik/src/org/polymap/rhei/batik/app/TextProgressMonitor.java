/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.rhei.batik.app;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * Progress monitor that shows task names and progress as simple label.
 *
 * @author Falko Bräutigam
 */
public class TextProgressMonitor
        extends NullProgressMonitor {

    private static final Log log = LogFactory.getLog( TextProgressMonitor.class );

    @Mandatory
    @DefaultBoolean( true )
    public Config<Boolean>  showWheel;
    
    private Label           msg;

    private String          taskName = "";

    private String          subTaskName = "";

    private int             total = UNKNOWN;

    private int             worked;
    
    private Timer           updated = new Timer();
    
    private boolean         canceled;
    
    private Display         display;
    
    
    public TextProgressMonitor() {
        ConfigurationFactory.inject( this );
        display = UIUtils.sessionDisplay();
        assert display != null : "Must be called from UI thread.";
    }

    public Composite createContents( Composite parent ) {
        assert msg == null : "createContent() already has been called.";
        
        Composite contents = new Composite( parent, SWT.NONE );
        contents.setLayout( FormLayoutFactory.defaults().margins( 0, 10 ).create() );
        
        Label wheel = new Label( contents, SWT.CENTER );
        wheel.setLayoutData( FormDataFactory.filled().noBottom().create() );
        wheel.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );

        msg = new Label( contents, SWT.CENTER );
        msg.setLayoutData( FormDataFactory.filled().top( wheel, 10 ).width( 250 ).create() );
        update( false );
        
        return contents;
    }

    protected void update( boolean throttle ) {
        if (throttle && updated.elapsedTime() < 1000) {
            return;
        }
        updated.start();
        UIThreadExecutor.async( () -> {
            if (msg != null && !msg.isDisposed()) {
                StringBuilder s = new StringBuilder( 128 );
                s.append( taskName );
                s.append( !isBlank( subTaskName ) ? (": "+subTaskName) : "" );
                s.append( " ..." );
                if (total != UNKNOWN) {
                    double percent = 100d / total * worked;
                    s.append( " (" ).append( (int)percent ).append( "%)" );
                }
                msg.setText( s.toString() );
            }
        });
    }
    
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void beginTask( String name, int totalWork ) {
        this.taskName = name;
        this.total = totalWork;
        update( false );
    }

    @Override
    public void setTaskName( String name ) {
        this.taskName = name;
        update( false );
    }

    @Override
    public void subTask( String name ) {
        this.subTaskName = name;
        update( true );
    }

    @Override
    public void worked( int work ) {
        worked += work;
        update( true );
    }
    
}
