/* 
 * polymap.org
 * Copyright (C) 2014-2018, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.widgets.Widget;

/**
 * 
 * Renders simple external http:// or https:// style links.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ExternalLinkRenderer
        implements IMarkdownRenderer {

    @Override
    public boolean render( DefaultToolkit tk, IMarkdownNode node, MarkdownRenderOutput out, Widget widget ) {
        if (node.type() == IMarkdownNode.Type.ExpLink /**&& node.url().startsWith( "@" )*/) {
            out.url.set( node.url() );
            out.text.set( node.text() );
            out.title.set( node.title() );
            out.target.set( "_blank" );
            return true;
        }
        return false;
    }

}
