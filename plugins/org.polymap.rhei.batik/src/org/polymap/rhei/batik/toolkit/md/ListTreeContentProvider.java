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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.runtime.StreamIterable;

/**
 * A content provider for use with {@link MdListViewer} that provides no hierarchy,
 * just a single list of elements. Supports {@link Collection}, {@link Iterable} or
 * array of elements as input.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ListTreeContentProvider
        implements ITreeContentProvider {

    private static final Log log = LogFactory.getLog( ListTreeContentProvider.class );
    
    private Object[]            input;
    
    @Override
    public Object[] getChildren( Object parent ) {
        return getElements( parent );
    }

    @Override
    public Object[] getElements( Object inputElement ) {
        return input;
    }

    @Override
    public Object getParent( Object element ) {
        return null;
    }

    @Override
    public boolean hasChildren( Object element ) {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if (newInput == null) {
            input = null;
        }
        else if (newInput.getClass().isArray()) {
            this.input = (Object[])newInput;
        }
        else if (newInput instanceof Collection) {
            this.input = ((Collection)newInput).toArray();
        }
        else if (newInput instanceof Iterable) {
            if (newInput instanceof StreamIterable) {
                log.warn( "!!!Check deprecated StreamIterable!!!" );
            }
            input = FluentIterable.from( (Iterable)newInput ).toArray( Object.class );
        }
        else {
            throw new RuntimeException( "Unsupported input: " + input );
        }
    }
    
}
