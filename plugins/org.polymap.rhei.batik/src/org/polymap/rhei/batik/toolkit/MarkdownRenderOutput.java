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
package org.polymap.rhei.batik.toolkit;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Describes a link to be rendered.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class MarkdownRenderOutput
        extends Configurable {

    @Mandatory
    public Config2<MarkdownRenderOutput,String>     url;
    
    public Config2<MarkdownRenderOutput,String>     title;
    
    public Config2<MarkdownRenderOutput,String>     text;
    
    public Config2<MarkdownRenderOutput,String>     id;
    
    public Config2<MarkdownRenderOutput,String>     clazz;
    
    public Config2<MarkdownRenderOutput,String>     target;
    
    /** 
     * @deprecated Use {@link #url} instead. 
     */
    public void setUrl( String linkUrl ) {
        this.url.set( linkUrl );
    }

    /** 
     * @deprecated Use {@link #title} instead. 
     */
    public void setTitle( String title ) {
        this.title.set( title );
    }

    /** 
     * @deprecated Use {@link #text} instead. 
     */
    public void setText( String text ) {
        this.text.set( text );
    }

    /** 
     * @deprecated Use {@link #id} instead. 
     */
    public void setId( String id ) {
        this.id.set( id );
    }

    /** 
     * @deprecated Use {@link #clazz} instead. 
     */
    public void setClass( String clazz ) {
        this.clazz.set( clazz );
    }
    
}