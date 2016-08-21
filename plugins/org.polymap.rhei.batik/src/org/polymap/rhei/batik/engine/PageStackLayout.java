/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.engine;

import static java.lang.Math.min;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.ui.forms.widgets.ILayoutExtension;

import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.engine.PageStack.Page;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Layout of the {@link PageStack}.
 * <p/>
 * <b>Layout rules:</b>
 * <ul>
 * <li>children are sorted in a <b>priority stack</b></li>
 * <li>client can request a page {@link PageStack.Page#isVisible}</li>
 * <li>one visible page is marked as "focused"<li>
 * <li>the layout decides which pages are actually {@link PageStack.Page#isShown} depending on:</li> 
 *     <ul>
 *     <li>the priority of the page starting from the "focused" page
 *     <li>minimal (preferred?) width of the pages</b>
 *     <li>available space</li>
 *     </ul>
 * <li></li>
 * <li></li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PageStackLayout 
        extends Layout 
        implements ILayoutExtension {

    private static Log log = LogFactory.getLog( PageStackLayout.class );

    public static final int         DEFAULT_PAGE_MIN_WIDTH = 350;
    
    private final PageStack         pageStack;

    private LayoutSupplier          margins;
    
    private Rectangle               cachedClientArea;

    private Color                   white = UIUtils.getColor( 0xff, 0xff, 0xff );
    private Color                   grey = UIUtils.getColor( 0xfb, 0xfb, 0xfb );
    

    PageStackLayout( PageStack pageStack, LayoutSupplier margins ) {
        this.pageStack = pageStack;
        this.margins = margins;
    }

    
    @Override
    protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
        Rectangle clientArea = pageStack.getClientArea();
        
        Point result = new Point( wHint, hHint );
        result.x = wHint != SWT.DEFAULT ? wHint : clientArea.width;
        result.y = hHint != SWT.DEFAULT ? hHint : clientArea.height;
        return result;
    }
    
    
    @Override
    protected void layout( Composite composite, boolean flushCache ) {
        assert pageStack == composite;

        if (pageStack.getPages().isEmpty()) {
            return;
        }
        
        Rectangle clientArea = pageStack.getClientArea();
        log.debug( "layout(): clientArea=" + clientArea );        

        Rectangle displayArea = composite.getDisplay().getBounds();
        if (clientArea.width > displayArea.width) {
            log.debug( "Invalid client area: " + clientArea + ", display width: " + displayArea.width + ", flushCache: " + flushCache );
            return;
        }
        
        if (!flushCache && clientArea.equals( cachedClientArea )) {
            log.info( "Ignoring cachedClientArea: " + cachedClientArea + ", flushCache: " + flushCache );
            return;
        }
        cachedClientArea = clientArea;
        
        pageStack.preUpdateLayout();
        
        // available size
        int availWidth = clientArea.width - margins.getMarginLeft() - margins.getMarginRight();

        // top down sorted pages
        Collection<Page> pages = pageStack.getPages();
        List<Page> topDown = pages.stream()
                .sorted( Comparator.comparing( page -> Integer.MAX_VALUE-page.priority ) )
                .collect( Collectors.toList() );

        // 1: minimum width: max pages visible
        int filledWidth = 0;
        boolean pageVisible = true;
        for (Page page : topDown) {
            page.isShown = false;

            if (pageVisible) {
                // right most: always displayed; preferred width
                if (filledWidth == 0) {
                    int pageWidth = min( page.size.preferred(), availWidth );
                    page.bounds = new Rectangle( 0, 0, pageWidth, clientArea.height );
                    page.isShown = true;
                    filledWidth = pageWidth;
                }
                // others: min width
                else {
                    int pageWidth = min( page.size.min(), availWidth );
                    if (filledWidth + pageWidth + margins.getSpacing() <= availWidth) {
                        page.bounds = new Rectangle( 0, 0, pageWidth, clientArea.height );
                        page.isShown = true;
                        filledWidth += pageWidth + margins.getSpacing();
                    }
                    else {
                        pageVisible = false;
                    }
                }
            }
        }

        // 2: preferred width: distribute remaining (actually set bounds)
        int panelX = availWidth - margins.getMarginRight();
        int remainWidth = availWidth - filledWidth;
        for (Page page : topDown) {
            page.control.setVisible( page.isShown );
            if (page.isShown) {
                // does page want more width?
                int preferred = min( page.size.preferred(), page.bounds.width + remainWidth );
                if (preferred > page.bounds.width) {
                    remainWidth -= (preferred - page.bounds.width);
                    page.bounds.width = preferred;
                }
                page.bounds.x = panelX - page.bounds.width; 
                page.control.setBounds( page.bounds );
//                page.control.layout( true /*flushCache*/ );

                panelX -= page.bounds.width + margins.getSpacing();
            }
        }

        // 3: maximum: still space remaining
        if (remainWidth > 0) {
            panelX = availWidth - margins.getMarginRight();
            for (Page page : topDown) {
                if (page.isShown) {
                    // does page want more width?
                    int max = min( page.size.max(), page.bounds.width + remainWidth );
                    if (max > page.bounds.width) {
                        remainWidth -= (max - page.bounds.width);
                        page.bounds.width = max;
                    }
                    page.bounds.x = panelX - page.bounds.width; 
                    page.control.setBounds( page.bounds );
                    
                    panelX -= page.bounds.width + margins.getSpacing();        
                }
            }
        }
        
        // 4: over maximum: distribute remaining over all pages
        if (remainWidth > 0) {
            long delta = remainWidth / topDown.stream().filter( p -> p.isShown ).count();
            panelX = availWidth - margins.getMarginRight();
            for (Page page : topDown) {
                if (page.isShown) {
                    page.bounds.width += delta;
                    
                    page.bounds.x = panelX - page.bounds.width; 
                    page.control.setBounds( page.bounds );
                    
                    panelX -= page.bounds.width + margins.getSpacing();        
                }
            }
        }
        
        //
        log.debug( "available: " + availWidth );
        for (Page page : topDown) {
            if (page.isShown) {
                log.debug( "    panel width: " + page.bounds.width );
            }
            else {
                log.debug( "    panel: invisible" );
            }
        }
        
        pageStack.postUpdateLayout();
    }
    
    
    @Override
    public int computeMaximumWidth( Composite parent, boolean changed ) {
        return computeSize( parent, SWT.DEFAULT, SWT.DEFAULT, changed ).x;
    }
    
    
    @Override
    public int computeMinimumWidth( Composite parent, boolean changed ) {
        return computeSize( parent, 0, SWT.DEFAULT, changed ).x;
    }
    
}