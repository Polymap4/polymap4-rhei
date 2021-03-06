/* 
 * polymap.org
 * Copyright (C) 2013-2014, Polymap GmbH. All rights reserved.
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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.event.TypeEventFilter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.PropertyAccessEvent.TYPE;
import org.polymap.rhei.batik.Scope;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContextPropertyInstance<T>
        implements Context<T> {

    private DefaultAppContext   context;
    
    private Field               field;
    
    /**
     * 
     */
    private Lazy<Class<?>>      declaredType = new PlainLazyInit( () -> {
        Type ftype = field.getGenericType();
        Type result = ((ParameterizedType)ftype).getActualTypeArguments()[0];
        if (result instanceof ParameterizedType) {
            return ((ParameterizedType)result).getRawType();
        }
        else if (result instanceof Class) {
            return result;
        }
        else {
            throw new IllegalStateException( "Unhandled type: " + result );
        }
    });

    /**
     * 
     */
    private Lazy<String>        scope = new PlainLazyInit( () -> {
        String result = field.getDeclaringClass().getPackage().getName();
        Scope a = field.getAnnotation( Scope.class );
        if (a != null && a.value().length() > 0) {
            result = a.value();
        }
        return result;
    });
    
    /**
     * 
     */
    private Lazy<Boolean>       isMandatory = new PlainLazyInit( () -> field.getAnnotation( Mandatory.class ) != null );
    

    public ContextPropertyInstance( Field field, DefaultAppContext context ) {
        this.field = field;
        this.context = context;
    }

    
    @Override
    public boolean equals( Object obj ) {
        if (obj instanceof ContextPropertyInstance) {
            ContextPropertyInstance rhs = (ContextPropertyInstance)obj;
            return getScope().equals( rhs.getScope() ) 
                    && getDeclaredType().equals( rhs.getDeclaredType() );
        }
        else {
            return false;
        }
    }


    @Override
    public boolean isPresent() {
        return context.getPropertyValue( this ) != null;
    }


    @Override
    public void ifPresent( Consumer<T> consumer ) {
        T value = context.getPropertyValue( this );
        if (value != null) {
            consumer.accept( value );
        }
    }


    //    @SuppressWarnings("deprecation")
    @Override
    public T get() {
        T result = context.getPropertyValue( this );
        if (result == null && isMandatory.get()) {
            throw new IllegalStateException( "@Context field is @Mandatory: " + field );
        }
        //EventManager.instance().publish( new PropertyAccessEvent( this, TYPE.GET ) );
        return result;
    }

    
    @Override
    public T getOrWait( int timeout, TimeUnit unit ) {
        // static reference that is not changed by concurrent thread
        T result = null;
        Timer timer = new Timer();
        while ((result = get()) == null && timer.elapsedTime() < unit.toMillis( timeout )) {
            synchronized (this) {
                // XXX notify
                try { wait( 100 ); } catch (InterruptedException e) {}
            }
        }
        return result;
    }

    
    @Override
    public T set( T newValue ) {
        if (newValue == null && isMandatory.get()) {
            throw new IllegalArgumentException( "@Context field is @Mandatory: " + field );
        }
        T previous = context.setPropertyValue( this, newValue );
        EventManager.instance().publish( new PropertyAccessEvent( this, TYPE.SET, newValue, previous ) );
        return previous;
    }

    
    @Override
    public boolean compareAndSet( T expect, T update ) {
        if (update == null && isMandatory.get()) {
            throw new IllegalArgumentException( "@Context field is @Mandatory: " + field );
        }
        boolean updated = context.compareAndSetPropertyValue( this, expect, update );
        if (updated) {
            EventManager.instance().publish( new PropertyAccessEvent( this, TYPE.SET, update, expect ) );
            return true;
        }
        else {
            return false;
        }
    }


    @Override
    public Class getDeclaredType() {
        return declaredType.get();
    }

    
    @Override
    public String getScope() {
        return scope.get();
    }

    
    @Override
    public void addListener( Object annotated, final EventFilter<PropertyAccessEvent>... filters ) {
        EventManager.instance().subscribe( annotated, TypeEventFilter.isType( PropertyAccessEvent.class, ev -> {
            Context src = ev.getSource();
            return src.getDeclaredType().equals( getDeclaredType() )
                    && src.getScope().equals( getScope() )
                    && Arrays.stream( filters ).allMatch( filter -> filter.apply( ev ) );
        }));
    }

    @Override
    public boolean removeListener( Object annotated ) {
        return EventManager.instance().unsubscribe( annotated );
    }

    @Override
    public String toString() {
        return "ContextPropertyInstance[value=" + get() + ",scope=" + scope + ", type=" + declaredType + "]";
    }
    
}