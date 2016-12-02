/*
 * polymap.org
 * Copyright (C) 2013-2016, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;


/**
 * Provides default implementation for property handling and panel hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultAppContext
        implements IAppContext {

    private static Log log = LogFactory.getLog( DefaultAppContext.class );

    /**
     * 
     */
    class ScopedPropertyValue {
        protected Object        value;
        protected String        scope;

        public ScopedPropertyValue( Object value, String scope ) {
            this.value = value;
            this.scope = scope;
        }
    }

    
    // instance *******************************************
    
    /** The property suppliers. */
    private List<ScopedPropertyValue>       properties = new ArrayList();
    
    protected ReadWriteLock                 propertiesLock = new ReentrantReadWriteLock();


    @Override
    public void addListener( Object handler, EventFilter... filters ) {
        EventManager.instance().subscribe( handler, filters );
    }


    @Override
    public void removeListener( Object handler ) {
        EventManager.instance().unsubscribe( handler );
    }


    public <T> T getPropertyValue( Context<T> prop ) {
        try {
            propertiesLock.readLock().lock();
            
            ScopedPropertyValue result = findPropertyValue( prop );
            return result != null ? (T)result.value : null;
        }
        finally {
            propertiesLock.readLock().unlock();
        }
    }
    
    /**
     * 
     *
     * @param prop
     * @param value The new value.
     * @return The previous value.
     */
    public <T> T setPropertyValue( Context<T> prop, T value ) {
        try {
            propertiesLock.writeLock().lock();

            ScopedPropertyValue found = findPropertyValue( prop );
            if (value == null) {
                if (found != null) {
                    properties.remove( found );
                    return (T)found.value;
                }
                return null;
            }
            else if (found != null) {
                Object result = found.value;
                found.value = value;
                return (T)result;
            }
            else {
                properties.add( new ScopedPropertyValue( value, prop.getScope() ) );
                return null;
            }
        }
        finally {
            propertiesLock.writeLock().unlock();
        }
    }

    
    public <T> boolean compareAndSetPropertyValue( Context<T> prop, T expect, T update ) {
        try {
            propertiesLock.writeLock().lock();
         
            T value = getPropertyValue( prop );
            if (Objects.equals( value, expect )) {
                setPropertyValue( prop, update );
                return true;
            }
            else {
                return false;
            }
        }
        finally {
            propertiesLock.writeLock().unlock();
        }        
    }
    
    
    protected ScopedPropertyValue findPropertyValue( Context prop ) {
        ScopedPropertyValue result = null;
        for (ScopedPropertyValue value : properties) {
            if (value.scope.equals( prop.getScope() )
                    && prop.getDeclaredType().isAssignableFrom( value.value.getClass() )) {
                if (result != null) {
                    throw new IllegalStateException( "More than one match for context property: " + prop );                    
                }
                result = value;
            }
        }
        return result;
    }


    @Override
    public <T> T propagate( T panel ) {
        assert panel != null: "Argument is null";
        new PanelContextInjector( panel, this ).run();
        return panel;
    }
    
    

//    protected int subTypeDistance( Class<?> type, Class<?> subType ) {
//        Class<?> cursor = subType;
//        for (int i=0; cursor != null; i++) {
//            if (cursor.equals( type )) {
//                return i;
//            }
//            cursor = subType.getSuperclass();
//        }
//        throw new RuntimeException( "Types are not related: " + type.getSimpleName() + " - " + subType.getSimpleName() );
//    }

}