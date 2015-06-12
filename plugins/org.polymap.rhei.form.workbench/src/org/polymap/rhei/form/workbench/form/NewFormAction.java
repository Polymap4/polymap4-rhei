/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.form.workbench.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiFormPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class NewFormAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( NewFormAction.class );

    private FeatureSelectionView    view;
    
    /** The layer we are associated with. Might be null. */
    private ILayer                  layer;

    
    public void init( IViewPart _view ) {
        if (view instanceof FeatureSelectionView) {
            log.debug( "init(): found GeoSelectionView..." );
            this.view = (FeatureSelectionView)_view;
            this.layer = (view).getLayer();
            assert layer != null : "Layer must not be null.";
        }
    }


    public void run( IAction action ) {
        try {
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
            if (fs != null && fs.getSchema().getGeometryDescriptor() != null) {
                MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(),
                        Messages.get( "NewFormAction_noGeomTitle" ), Messages.get( "NewFormAction_noGeomMsg", layer.getLabel() ) );
            }
            else {
                NewFeatureOperation op = new NewFeatureOperation( layer, null );
                OperationSupport.instance().execute( op, false, false );
            }
//            FeatureId fid = op.getCreatedFid();
//
//            FeatureStore fs = view != null
//                    ? view.getFeatureStore()
//                    // FIXME do blocking operation inside a job?
//                    : PipelineFeatureSource.forLayer( layer, false );
//
//            Id fidFilter = CommonFactoryFinder.getFilterFactory( null ).id( Collections.singleton( fid ) );
//            FeatureCollection coll = fs.getFeatures( fidFilter );
//            Feature feature = (Feature)coll.toArray( new Feature[1] )[0];
//            FormEditor.open( fs, feature );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.debug( "selectionChanged(): sel= " + sel );
        
        // called when popup menu is opened
        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            
            if (elm instanceof ILayer) {
                layer = (ILayer)elm;
            }
        }
    }

}
