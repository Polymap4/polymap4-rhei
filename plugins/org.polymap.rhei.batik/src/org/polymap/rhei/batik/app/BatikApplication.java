/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.lifecycle.IEntryPoint;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IApplicationLayouter;
import org.polymap.rhei.batik.internal.BatikComponentFactory;
import org.polymap.rhei.batik.internal.Messages;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikApplication
        implements IEntryPoint {

    private static final Log log = LogFactory.getLog( BatikApplication.class );


    /**
     * The {@link Display} of the session of the current thread. Null, if the
     * current thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does need to
     * be the UI thread of the session.
     */
    public static Display sessionDisplay() {
        return Polymap.getSessionDisplay();
    }


    /**
     * Return an appropriate shell to parent dialogs on. This will be one of the
     * workbench windows (the active one) should any exist. Otherwise
     * <code>null</code> is returned.
     *
     * @return The shell to parent on or <code>null</code> if there is no
     *         appropriate shell.
     */
    public static Shell shellToParentOn() {
        return Polymap.getSessionDisplay().getActiveShell();
    }


    /**
     * @see #handleError(String, String, Throwable)
     */
    public static void handleError( final String msg, Throwable e ) {
        handleError( BatikPlugin.PLUGIN_ID, msg, e );
    }


    /**
     * Handle the given error by opening an error dialog and logging the given
     * message to the CorePlugin log.
     *
     * @param msg The error message. If null, then a standard message is used.
     * @param e The reason of the error, must not be null.
     */
    public static void handleError( String pluginId, final String msg, Throwable e ) {
        log.error( msg, e );

        final Status status = new Status( IStatus.ERROR, pluginId, e.getLocalizedMessage(), e );
        // XXX causes Exception; don't know why doing this anyway
        //CorePlugin.getDefault().getLog().log( status );

        final Display display = Polymap.getSessionDisplay();
        if (display == null) {
            log.error( "No display -> no error message." );
            return;
        }

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Shell shell = shellToParentOn();
                    ErrorDialog dialog = new ErrorDialog(
                            shell,
                            Messages.get( "PolymapWorkbench_errorDialogTitle" ),
                            msg != null ? msg : "Fehler beim Ausführen der Operation.",
                            status,
                            IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR );
//                dialog.setBlockOnOpen( true );
                    dialog.open();
                }
                catch (Throwable ie) {
                    log.warn( ie );
                }
            }
        };
        if (Display.getCurrent() == display) {
            runnable.run();
        } else {
            display.asyncExec( runnable );
        }
    }


    // instance *******************************************

    private Display                     display;

    private IApplicationLayouter        appLayouter;

    private Window                      mainWindow;


    @Override
    public int createUI() {
//        ScopedPreferenceStore prefStore = (ScopedPreferenceStore)PrefUtil.getAPIPreferenceStore();
//        String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
//        String presentationId = prefStore.getString( keyPresentationId );

        // security config / login
//        Polymap.instance().login();

        // start Atlas UI
//        try {
            display = PlatformUI.createDisplay();

            log.info( "Display DPI: " + display.getDPI().x + "x" + display.getDPI().y );

            appLayouter = BatikComponentFactory.instance().createApplicationLayouter();
            mainWindow = appLayouter.initMainWindow( display );
            mainWindow.open();

            Shell shell = mainWindow.getShell();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            log.info( "Exiting..." );
            display.dispose();
            return PlatformUI.RETURN_OK;
//        }
//        catch (Exception e) {
//            //
//            handleError( CorePlugin.PLUGIN_ID, e.getLocalizedMessage(), e );
//            return PlatformUI.RETURN_OK;
//        }
    }

}
