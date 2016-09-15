/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.rhei.table;

import static org.polymap.rhei.table.FeatureTableViewer.LOADING_ELEMENT;

import java.util.Optional;

import java.io.IOException;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.collect.Function;

/**
 * Uses the backend {@link FeatureSource} for sorting. Elements are loaded in a
 * background job so that accessing the backend FeatureSource does not block the
 * display thread.
 * <p/>
 * Accepts {@link FeatureSource} and {@link FeatureCollection} inputs.
 *
 * @author Falko Br�utigam
 */
public class LazyFeatureContentProvider
        implements ILazyContentProvider, ISortingContentProvider {

    private static final Log log = LogFactory.getLog( LazyFeatureContentProvider.class );
    
    /** Provides a {@link FeatureCollection} from different inputs. */
    private Function<Query,FeatureCollection,IOException> fc;
    
    private Filter                  filter = Filter.INCLUDE;
    
    private IFeatureTableColumn     sortedBy;
    
    private SortOrder               sortOrder;
    
    private FeatureTableViewer      viewer;
    
    /** Table index -> feature */
    private Cache<Integer,SimpleFeatureTableElement> cache = CacheConfig.defaults().concurrencyLevel( 4 ).createCache();
    
    
    public LazyFeatureContentProvider setFeatureFilter( Filter filter ) {
        this.filter = filter;
        cache.clear();
        viewer.refresh();
        return this;
    }
    
    
    @Override
    public void inputChanged( Viewer newViewer, Object oldInput, Object newInput ) {
        assert viewer == null || viewer == newViewer;
        this.viewer = (FeatureTableViewer)newViewer;
        if (newInput == null) {
            return;
        }
        // FeatureSource
        else if (newInput instanceof FeatureSource) {
            this.fc = query -> ((FeatureSource)newInput).getFeatures( query );
        }
        // FeatureCollection
        else if (newInput instanceof FeatureCollection) {
            this.fc = query -> {
                if (query.getFilter().equals( Filter.INCLUDE )) {
                    return (FeatureCollection)newInput;
                }
                else {
                    FeatureCollection result = ((FeatureCollection)newInput).subCollection( query.getFilter() );
                    SortBy[] sortBy = query.getSortBy();
                    if (sortBy != null && sortBy.length > 1) {
                        throw new UnsupportedOperationException( "GeoAPI FeatureSource can sort by multiple columns, however FeatrueCollection can not! :(");
                    }
                    else if (sortBy != null && sortBy.length > 0) {
                        result.sort( sortBy[0] );
                    }
                    if (query.getStartIndex().intValue() > 0 || query.getMaxFeatures() < Query.DEFAULT_MAX) {
                        throw new RuntimeException( "not yet..." );
//                        DefaultFeatureCollection limited = new DefaultFeatureCollection();
//                        try (
//                            FeatureIterator it = result.features();
//                        ){
//                            for (int i=0; i<query.getStartIndex().intValue(); i++) {
//                                throw new RuntimeException( "not yet..." );
//                            }
//                        }
                    }
                    return result;
                }
            };
        }
        else {
            throw new IllegalArgumentException( "Input is not a FeatureSource: " + newInput );
        }
        
        refreshViewer();
    }

    
    protected void refreshViewer() {
        cache.clear();
        viewer.setItemCount( 1 );
        viewer.replace( LOADING_ELEMENT, 0 );

        new UIJob( "Size" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                int size = fc.apply( new Query( null, filter ) ).size();
                UIThreadExecutor.async( () -> {
                    if (!viewer.getControl().isDisposed()) {
                        viewer.setItemCount( size );
                        if (size >= 1) {
                            updateElement( 0 );
                        }
                    }
                });
            }
        }.schedule();
    }

    
    @Override
    public void dispose() {
    }

    
    public void filter( Filter newFilter ) {
        this.filter = newFilter;

        if (viewer != null) {
            refreshViewer();
        }
    }
    
    
    @Override
    public void sortContent( IFeatureTableColumn column, int dir ) {
        this.sortedBy = column;
        if (dir == SWT.UP) {
            this.sortOrder = SortOrder.ASCENDING;
        }
        else if (dir == SWT.DOWN) {
            this.sortOrder = SortOrder.DESCENDING;            
        }
        else {
            throw new IllegalArgumentException( "Sort dir: " + dir );
        }

        cache.clear();
        if (viewer != null) {
            viewer.refresh();
        }
    }


    public int indexOfFid( String fid ) throws IOException {
        // check cache
        for (Integer index : cache.keySet()) {
            SimpleFeatureTableElement elm = cache.get( index );
            if (elm != null && elm.fid().equals( fid )) {
                return index;
            }
        }
        
        // search entire FeatureSource
        FeatureCollection features = fc.apply( query() );
        try (
            FeatureIterator it = features.features();
        ){
            for (int i=0; it.hasNext(); i++) {
                Feature feature = it.next();
                if (feature.getIdentifier().getID().equals( fid )) {
                    return i;
                }
            }
            throw new IllegalStateException( "Fid not found: " + fid );
        }
    }

    
    protected Query query() {
        Query query = new Query();
        query.setFilter( filter );
        if (sortedBy != null) {
            SortBy sort = DataPlugin.ff.sort( sortedBy.getName(), sortOrder );
            query.setSortBy( new SortBy[] {sort}  );
        }
        return query;
    }

    
    @Override
    public void updateElement( int index ) {
        SimpleFeatureTableElement elm = cache.get( index );
        if (elm != null) {
            log.info( "Index from cache: " + index );
            viewer.replace( elm, index );
        }
        else {
            log.info( "Index requested: " + index );
            viewer.replace( LOADING_ELEMENT, index );
            new ContentUpdater( index, 1 ).schedule();
        }
    }

    
    /**
     * 
     */
    protected class ContentUpdater
            extends UIJob {

        private int index;
        private int num;

        public ContentUpdater( int index, int num ) {
            super( "LazyFeatureContentProvider" );
            this.index = index;
            this.num = num;
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            Query query = query();
            query.setMaxFeatures( num );
            query.setStartIndex( index );
            FeatureCollection features = fc.apply( query );
            try (
                FeatureIterator it = features.features();
            ){
                for (int i=0; i<num && it.hasNext(); i++) {
                    SimpleFeature feature = (SimpleFeature)it.next();
                    log.info( "Feature loaded: " + feature.getID() );
                    int offset = i;
                    UIThreadExecutor.async( () -> {
                        if (!viewer.getControl().isDisposed()) {
                            SimpleFeatureTableElement elm = new SimpleFeatureTableElement( feature );
                            cache.putIfAbsent( index+offset, elm );
                            viewer.replace( elm, index+offset );
                        }
                    });
                }
            }
        }
    }

    public static class SimpleFeatureTableElement
            extends DefaultFeatureTableElement {

        private SimpleFeature       feature;

        public SimpleFeatureTableElement( SimpleFeature feature ) {
            this.feature = feature;
        }

        @Override
        public String fid() {
            return feature.getID();
        }

        @Override
        public Object getValue( String name ) {
            return feature.getAttribute( name );
        }

        @Override
        public void setValue( String name, Object value ) {
            throw new RuntimeException( "not yet implemented." );
        }
        
        @Override
        public <T> Optional<T> unwrap( Class<T> targetClass ) {
            if (targetClass.isAssignableFrom( feature.getClass() )) {
                return Optional.of( (T)feature );
            }
            return super.unwrap( targetClass );
        }

    }
    
}
