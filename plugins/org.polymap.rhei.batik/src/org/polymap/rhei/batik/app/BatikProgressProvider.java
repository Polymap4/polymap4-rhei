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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.engine.DefaultAppManager;
import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.Item;
import org.polymap.rhei.batik.toolkit.Snackbar;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;

/**
 * Provides {@link Snackbar} progress indicator.
 *
 * @author Falko Bräutigam
 */
public class BatikProgressProvider
        extends ProgressProvider {

    private static final Log log = LogFactory.getLog( BatikProgressProvider.class );

    private static final DefaultToolkit tk = new DefaultToolkit( null, null );

    private Snackbar            snackbar;
    

    @Override
    public IProgressMonitor createMonitor( Job job ) {
        return !job.isSystem() ? new BatikProgressMonitor( job ) : new NullProgressMonitor();
    }
    
    
    /**
     * 
     */
    public class BatikProgressMonitor
            implements IProgressMonitor {

        public static final String  LOG_PREFIX = "[PROGRESS] ";
        
        private Timer               updated = new Timer();
        
        private volatile boolean    canceled;
        
        private volatile String     taskName = "";

        private volatile String     subTaskName = "";

        private volatile int        total = UNKNOWN;

        private volatile double     worked;

        private Job                 job;
        
        /** If ever contributed to the snackbar -> show 'done' message. */
        private boolean             snackbarEverSeen;


        public BatikProgressMonitor( Job job ) {
            this.job = job;
        }

        protected void update( boolean throttle ) {
            if (!throttle || updated.elapsedTime() > 1000) {
                updated.start();

                if (!(job instanceof UIJob) || ((UIJob)job).getDisplay() == null) {
                    // FIXME
                    log.warn( "Job is no UIJob or has no display -> no progress." );
                    return;
                }
                ((UIJob)job).getDisplay().asyncExec( () -> {
                    StringBuilder msg = new StringBuilder( 128 )
                            .append( taskName )
                            .append( !StringUtils.isBlank( subTaskName ) ? (" - "+subTaskName) : "" )
                            .append( " ..." );
                    
                    if (total != UNKNOWN) {
                        double percent = 100d / total * worked;
                        msg.append( " " ).append( (int)percent ).append( "%" );
                    }
                    
                    if (snackbar == null || snackbar.isDisposed()) {
                        ActionItem stopAction = new ActionItem( null )
                                .action.put( ev -> {
                                    setCanceled( true );
                                    job.getThread().interrupt();
                                })
                                .text.put( worked != total ? "STOP" : "DONE" )
                                .tooltip.put( "Stop processing of this task" );

                        //BatikApplication.instance().getAppDesign()
                        PanelPath top = ((DefaultAppManager)BatikApplication.instance().getAppManager()).topPanel();
                        Composite topParent = BatikApplication.instance().getAppDesign().panelParent( top );
                        
                        snackbar = new Snackbar( tk, topParent /*UIUtils.shellToParentOn()*/ )
                                .hideTimeout.put( 4 )
                                .appearance.put( Appearance.FlyIn )
                                .message.put( msg.toString() )
                                .actions.put( new Item[] {stopAction} );
                    }
                    else {
                        snackbar.message.set( msg.toString() );
                    }
                    snackbarEverSeen = true;
                });
            }
        }

        @Override
        public void beginTask( String name, int totalWork ) {
            log.info( LOG_PREFIX + name + " - beginTask: " );
            this.taskName = name;
            this.total = totalWork;
            update( true );
        }

        @Override
        public void setTaskName( String name ) {
            this.taskName = name;
            update( true );
        }

        @Override
        public void subTask( String name ) {
            log.info( LOG_PREFIX + taskName + " - subtask: " + name );
            this.subTaskName = name;
            update( true );
        }

        @Override
        public void worked( int work ) {
            internalWorked( work );
        }

        @Override
        public void done() {
            log.info( LOG_PREFIX + taskName + " - done." );
            this.worked = this.total;
            this.subTaskName = "done";
            update( !snackbarEverSeen );
        }

        @Override
        public void internalWorked( double work ) {
            //log.info( "worked: " + work );
            assert work > 0;
            worked += work;
            update( true );
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled( boolean canceled ) {
            log.info( LOG_PREFIX + taskName + " - setCanceld: " + canceled );
            this.canceled = canceled;
        }

    }
    
}
