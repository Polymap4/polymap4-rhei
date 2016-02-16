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
package org.polymap.rhei.table;

import java.util.Optional;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.Validators;

/**
 * Like {@link IFormFieldValidator} but used for {@link FormFeatureTableColumn}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ITableFieldValidator<F,M> {

    /**
     * 
     */
    public static <FF,MM> ITableFieldValidator<FF,MM> of( IFormFieldValidator<FF,MM> delegate ) {
        return new DelegatingValidator( delegate );
    }

    
    /**
     * 
     */
    interface ValidatorSite {

        public boolean isEditing();
        
        public IFeatureTableElement element();
        
        public FormFeatureTableColumn column();
        
        /**
         * Gets the (maybe modified) value of the given column of the same row. See
         * {@link FormFeatureTableColumn#updateFieldValue(IFeatureTableElement, Object)}.
         * 
         * @return {@link Optional#empty()} specifies that the value was modified but is not valid.
         * @throws IllegalArgumentException If no column exists for the given name. 
         * @throws RuntimeException If Exception while transforming modified field value back to model value. 
         */
        public default <T> Optional<T> columnValue( String columnName ) {
            try {
                FormFeatureTableColumn column = column().getViewer().getColumn( columnName );
                if (column == null) {
                    throw new IllegalArgumentException( "No such column: " + columnName );
                }
                return column.modifiedModelValue( element() );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        
        /**
         * 
         * @throws IllegalArgumentException If no column exists for the given name.
         * @throws RuntimeException If Exception while transforming the given model
         *         value into a field value.
         */
        public default void setColumnValue( String columnName, Object modelValue ) {
            FormFeatureTableColumn column = column().getViewer().getColumn( columnName );
            if (column == null) {
                throw new IllegalArgumentException( "No such column: " + columnName );
            }
            try {
                column.updateModelValue( element(), modelValue );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }            
        }
    }
    

    /**
     * Check the given user provided value for validity.
     * 
     * @param value
     * @return Null if the value is valid, or the error message if the value is
     *         invalid.
     */
    public String validate( F fieldValue, ValidatorSite site );
    
    /**
     * Transforms the given user input value to model value.
     * 
     * @param fieldValue The user input value to transform, might be null.
     * @return Transformed value, or null if fieldValue is null.
     * @throws Exception
     */
    public M transform2Model( F fieldValue, ValidatorSite site ) throws Exception;
    
    
    /**
     * Transforms the given value from the {@link IFeatureTableElement model} into a
     * value presented in the UI. {@link ValidatorSite#isEditing()} true specifies
     * that the returned value is a String which is the label of the cell. Otherwise
     * the returned value is used as input for the {@link IFormField} that is used
     * for {@link FormFeatureTableColumn#setEditing(IFormField) editing} the column.
     *
     * @param modelValue
     * @param site
     * @return Transformed value.
     * @throws Exception
     */
    public F transform2Field( M modelValue, ValidatorSite site ) throws Exception;
    
    /**
     * Concatenates the given validator. The delegates are called in the given order.
     *
     * @see Validators#AND(IFormFieldValidator...)
     * @param other The validator the concat.
     * @return A newly created validator that calls the delegates.
     */
    public default ITableFieldValidator and( ITableFieldValidator other ) {
        return Validators.AND( this, other );
    }    

}
