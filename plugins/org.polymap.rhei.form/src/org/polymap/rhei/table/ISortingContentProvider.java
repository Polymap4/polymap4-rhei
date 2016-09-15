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

/**
 * Signals the {@link FeatureTableViewer} that this content provider provides its own
 * content sorting algorithm.
 * <p/>
 * This by-passes the default sorting which loads all elements in memory and does
 * sorting via {@link IFeatureTableColumn#newComparator(int)}.
 *
 * @author Falko Bräutigam
 */
public interface ISortingContentProvider {

   /**
    * Do sorting and refresh the viewer.
    * 
    * @param dir {@link SWT#UP}, {@link SWT#DOWN} or {@link SWT#NONE}
    * @param column
    */
   public void sortContent( IFeatureTableColumn column, int dir );

}
