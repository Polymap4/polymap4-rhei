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
package org.polymap.rhei.batik.toolkit.md;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.ImageCell;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.md.MdAppDesign.FontStyle;

/**
 * Expandable list. Use the {@link Config} properties to configure the viewer.
 * 
 * @see <a href="http://www.google.com/design/spec/components/lists.html">Material Design</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class MdListViewer
        extends TreeViewer {

    private static final Log log = LogFactory.getLog( MdListViewer.class );
    
    public static final String      CELL_ICON = "icon";
    public static final String      CELL_FIRSTLINE = "firstLine";
    public static final String      CELL_SECONDLINE = "secondLine";
    public static final String      CELL_THIRDLINE = "thirdLine";
    public static final String      CELL_EXPAND = "expand";
    public static final String      CELL_FIRSTACTION = "firstAction";
    public static final String      CELL_SECONDACTION = "secondAction";
    public static final String      CELL_THIRDACTION = "thirdAction";
    
    
    /**
     * Provides the primary icon if any.
     */
    public Config<CellLabelProvider>   iconProvider;

    /**
     * The label provider of the first line.
     */
    public Config<CellLabelProvider>   firstLineLabelProvider;
    
    public Config<CellLabelProvider>   secondLineLabelProvider;
    
    public Config<CellLabelProvider>   thirdLineLabelProvider;
    
    /** ActionProvider */
    public Config<CellLabelProvider>   firstSecondaryActionProvider;

    public Config<ActionProvider>      secondSecondaryActionProvider;

    public Config<ActionProvider>      thirdSecondaryActionProvider;

    /** Make grid lines visible. See {@link Tree#setLinesVisible(boolean)}. */
    @Mandatory
    @DefaultBoolean( true )
    public Config<Boolean>             linesVisible;

    private boolean                    customized = false;

    private boolean                    openListenerPresent;
    
    /**
     * 
     * <p/>
     * Possible style: {@link SWT#VIRTUAL}, {@link SWT#FULL_SELECTION}
     * 
     * @param parent
     * @param style
     */
    public MdListViewer( Composite parent, int style ) {
        super( parent, style );
        ConfigurationFactory.inject( this );
    }

    
    /**
     * Method is called after all label providers and configurations are done by
     * client code. It allows to customize the tree with templates, label providers
     * and stuff.
     * <p/>
     * Client code <b>must not</b> call this before {@link #setInput(Object)}.
     */
    @Override
    public Control getControl() {
        customizeTree();
        return super.getControl();
    }
    
    
    /**
     * @see ListTreeContentProvider
     */
    @Override
    public void setContentProvider( IContentProvider provider ) {
        super.setContentProvider( provider );
    }


    /**
     * 
     */
    protected void customizeTree() {
        if (!customized) {
            customized = true;
            
            getTree().setLinesVisible( linesVisible.get() );
            
            Template template = new Template();

            // 16dp used for the tree node handle
            dp left = iconProvider.isPresent() ? dp( 58 ) : dp( 0 );
            dp tileHeight = dp( 0 );
            int colCount = 0;
            
            // first line
            if (firstLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( firstLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_FIRSTLINE );
                cell.setLeft( left.pix() ).setRight( 50 )
                        .setTop( dp( 10 ).pix() ).setHeight( 18 )
                        .setHorizontalAlignment( SWT.LEFT );
                cell.setBindingIndex( colCount++ );
                // this makes text a link, which flickers and does not look very smooth
                //cell.setSelectable( openListenerPresent );
                cell.setFont( MdAppDesign.font( FontStyle.Subhead ) );
                
                tileHeight = dp( 50 );
            }
            // second line
            if (secondLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( secondLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_SECONDLINE );
                cell.setLeft( left.pix() ).setRight( 50 ).setTop( dp( 37 ).pix() ).setHeight( 16 );
                cell.setBindingIndex( colCount++ );
                cell.setForeground( UIUtils.getColor( 150, 150, 150 ) );

                tileHeight = dp( 72 );
            }
            // third line
            if (thirdLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( thirdLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_THIRDLINE );
                cell.setLeft( 30 ).setRight( 30 ).setTop( 30 ).setHeight( 15 );
                cell.setBindingIndex( colCount++ );
            }
            // primary icon
            if (iconProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( iconProvider.get() );

                ImageCell cell = new ImageCell( template );
                cell.setName( CELL_ICON );
                cell.setLeft( 1 ).setWidth( dp( 56 ).pix() )
                        .setTop( 0 ).setHeight( tileHeight.pix() )
                        .setVerticalAlignment( SWT.CENTER ).setHorizontalAlignment( SWT.CENTER );
                cell.setBindingIndex( colCount++ );
                cell.setSelectable( true );
            }
            // actions
            int width = 46;
            int actionCount = getActionCount()-1;  // -1 because no expand icon
            if (thirdSecondaryActionProvider.isPresent()) {
                createActionCell( template, thirdSecondaryActionProvider.get(), CELL_THIRDACTION, 
                        dp( actionCount--*width ), tileHeight ).setBindingIndex( colCount++ );
            }
            if (secondSecondaryActionProvider.isPresent()) {
                createActionCell( template, secondSecondaryActionProvider.get(), CELL_SECONDACTION, dp( actionCount--*width ), tileHeight )
                        .setBindingIndex( colCount++ );
            }
            if (firstSecondaryActionProvider.isPresent()) {
                createActionCell( template, firstSecondaryActionProvider.get(), CELL_FIRSTACTION, dp( actionCount--*width ), tileHeight )
                        .setBindingIndex( colCount++ );
            }

//            // expand
//            TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
//            col.setLabelProvider( new CellLabelProvider() {
//                @Override
//                public void update( ViewerCell cell ) {
//                    if (cell.getElement() != null) {
//                        IContentProvider contentProvider = MdListViewer.this.getContentProvider();
//                        boolean expandable = true;
//                        if (contentProvider instanceof ITreeContentProvider) {
//                            expandable = ((ITreeContentProvider)contentProvider).hasChildren( cell.getElement() );
//                        }
//                        if (expandable) {
//                            cell.setImage( getExpandedState( cell.getElement() )
//                                    ? BatikPlugin.images().svgImage( "close.svg", SvgImageRegistryHelper.DISABLED12 )
//                                    : BatikPlugin.images().svgImage( "chevron-down.svg", SvgImageRegistryHelper.DISABLED24 ) );
//                        }
//                        else {
//                            cell.setImage( null );
//                        }
//                    }
//                }
//            });
//            ImageCell cell = new ImageCell( template );
//            cell.setName( CELL_EXPAND );
//            cell.setRight( 1 ).setWidth( dp( 56 ).pix() ).setTop( 0 ).setHeight( tileHeight.pix() )
//                    .setVerticalAlignment( SWT.CENTER ).setHorizontalAlignment( SWT.CENTER );
//            cell.setBindingIndex( colCount++ );
//            cell.setSelectable( true );
    
            //
            getTree().addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    Object elm = ev.item.getData();
                    // expand
                    if (CELL_EXPAND.equals( ev.text )) {
                        if (!getExpanded( (Item)ev.item )) {
                            expandToLevel( elm, 1 );
                        }
                        else {
                            collapseToLevel( elm, 1 );
                        }
                    }
                    //
                    else if (CELL_FIRSTACTION.equals( ev.text ) && firstSecondaryActionProvider.get() instanceof ActionProvider) {
                        ((ActionProvider)firstSecondaryActionProvider.get()).perform( MdListViewer.this, elm );
                    }
                    else if (CELL_SECONDACTION.equals( ev.text )) {
                        secondSecondaryActionProvider.get().perform( MdListViewer.this, elm );
                    }
                    else if (CELL_THIRDACTION.equals( ev.text )) {
                        thirdSecondaryActionProvider.get().perform( MdListViewer.this, elm );
                    }
                    // open
                    else {
                        fireOpen( new OpenEvent( MdListViewer.this, new StructuredSelection( elm ) ) );
                    }
                }
            });
            super.addSelectionChangedListener( new ISelectionChangedListener() {
                @Override
                public void selectionChanged( SelectionChangedEvent ev ) {
                    log.debug( "selection: " + ev );
                }
            });

            getTree().setData( RWT.ROW_TEMPLATE, template );        
            getTree().setData( RWT.CUSTOM_ITEM_HEIGHT, tileHeight.pix() );
        }
    }
    
    
    private ImageCell createActionCell( Template template, CellLabelProvider actionProvider, String cellName, dp right, dp tileHeight ) {
        TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
        col.setLabelProvider( actionProvider );

        ImageCell cell = new ImageCell( template );
        cell.setName( cellName );
        cell.setRight( right.pix()+1 ).setWidth( dp( 56 ).pix() )
            .setTop( 0 ).setHeight( tileHeight.pix() )
            .setVerticalAlignment( SWT.CENTER ).setHorizontalAlignment( SWT.CENTER );
        cell.setSelectable( true );
        return cell;
    }


    private int getActionCount() {
        int actionCount = 0;
        if(firstSecondaryActionProvider.isPresent()) {
            actionCount++;
            if(secondSecondaryActionProvider.isPresent()) {
                actionCount++;
                if(thirdSecondaryActionProvider.isPresent()) {
                    actionCount++;
                }
            } else if(thirdSecondaryActionProvider.isPresent()) {
                actionCount++;
            }
        }
        return actionCount;
    }


    /**
     * 
     *
     * @param elementOtTreePath
     * @return this
     * @see #collapseAllNotInPathOf(Object)
     */
    public MdListViewer toggleItemExpand( Object elementOtTreePath ) {
        if (!getExpandedState( elementOtTreePath )) {
            expandToLevel( elementOtTreePath, 1 );
        } else {
            collapseToLevel( elementOtTreePath, 1 );
        }
        return this;
    }

   
    /**
     * Collapses all currently expanded ancestors that are outside the path of the
     * given element. This allows to mutual exclusively expand nodes.
     * <pre>
     *     viewer.collapseAllNotInPathOf( elm );
     *     viewer.toggleItemExpand( elm );
     * </pre>
     * 
     * @param elementOrTreePath
     * @return this
     */
    public MdListViewer collapseAllNotInPathOf( Object elementOrTreePath ) {
        TreePath path = elementOrTreePath instanceof TreePath
                ? (TreePath)elementOrTreePath 
                : getTreePathFromItem( (Item)findItem( elementOrTreePath ) );
        
        List<TreePath> expandedPaths = Lists.newArrayList( getExpandedTreePaths() );
        for (int i=0; i < path.getSegmentCount(); i++) {
            for (TreePath expanded : new ArrayList<TreePath>( expandedPaths )) {
                if (expanded.getSegmentCount()-1 < i) {
                    expandedPaths.remove( expanded );
                }
                else {
                    Object s1 = expanded.getSegment( i );
                    Object s2 = path.getSegment( i );
                    if (!s1.equals( s2 )) {
                        collapseToLevel( s1, 1 );
                        expandedPaths.remove( expanded );
                    }
                }
            }

        }
        return this;
    }

    
    @Override
    public void expandToLevel( Object elementOrTreePath, int level ) {
        log.debug( "EXPAND: " + elementOrTreePath );
        super.expandToLevel( elementOrTreePath, level );
        update( elementOrTreePath, null );  // update chevron icon state
    }
    
    @Override
    public void collapseToLevel( Object elementOrTreePath, int level ) {
        log.debug( "COLLAPSE: " + elementOrTreePath );
        super.collapseToLevel( elementOrTreePath, level );
        update( elementOrTreePath, null );  // update chevron icon state
    }

    
    /**
     * Adds a listener for selection-open in this viewer. Has no effect if an
     * identical listener is already registered. On touch devices this is a single
     * tap. On desktop the first line is displayed as a link. Single click on this
     * link fires an event. Clicking else where in the entry also fires an event.
     */
    @Override
    public void addOpenListener( IOpenListener listener ) {
        this.openListenerPresent = true;
        super.addOpenListener( listener );        
    }

    
//    /**
//     * 
//     */
//    @Override
//    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
//        this.selectionListenerPresent = true;
//        super.addSelectionChangedListener( listener );
//    }

    
    @Override
    public void setLabelProvider( IBaseLabelProvider labelProvider ) {
        throw new UnsupportedOperationException( "The Material Design list supports multiple lines of text, for example call #setFirstLineLabelProvider()." );
    }


    @Override
    protected void fireOpen( OpenEvent ev ) {
        // allow package classes to access
        super.fireOpen( ev );
    }

}
