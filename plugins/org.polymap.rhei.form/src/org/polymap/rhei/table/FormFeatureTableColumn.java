/*
 * polymap.org
 * Copyright (C) 2011-2015, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.table;

import static org.polymap.core.runtime.event.SourceEventFilter.Identical;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.beans.PropertyChangeEvent;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.event.SourceEventFilter;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.table.ITableFieldValidator.ValidatorSite;

/**
 * An {@link IFeatureTableColumn} that employes {@link IFormField} and
 * {@link IFormFieldValidator} to display/validate/transform and edit values of a
 * {@link FeatureTableViewer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFeatureTableColumn
        implements IFeatureTableColumn {

    static Log log = LogFactory.getLog( FormFeatureTableColumn.class );

    public static final Color       INVALID_BACKGROUND = UIUtils.getColor( 0xff, 0xd0, 0xe0 );
    public static final Color       DIRTY_BACKGROUND = UIUtils.getColor( 0xd0, 0xf0, 0xc0 );

    private FeatureTableViewer      viewer;

    private PropertyDescriptor      prop;
    
    private IFormField              editingFormField;
    
    protected ITableFieldValidator  validator;
    
    private ColumnLabelProvider     labelProvider;
    
    private FormEditingSupport      editingSupport;
    
    private CellEditor              cellEditor;

    private Comparator<IFeatureTableElement> sorter;

    private String                  header;

    private int                     weight = -1;

    private int                     minimumWidth = -1;
    
    private int                     align = -1;
    
    private boolean                 sortable = true;
    
    private Map<IFeatureTableElement,Object>    modifiedFieldValues = new HashMap();

    private TableViewerColumn       viewerColumn;


    public FormFeatureTableColumn( PropertyDescriptor prop ) {
        super();
        assert prop != null : "Argument is null.";
        this.prop = prop;
    }

    public FormFeatureTableColumn addFieldChangeListener( Object annotated ) {
        EventManager.instance().subscribe( annotated, new SourceEventFilter( this, Identical ) );
        return this;
    }
    
    public FeatureTableViewer getViewer() {
        return viewer;
    }
    
    public PropertyDescriptor getProperty() {
        return prop;
    }

    @Override
    public String getName() {
        return prop.getName().getLocalPart();
    }

    /**
     * Sets the 'raw' label provider of this column. A column with such a label provider
     * set can not participate in field modification infrastructure. The label provider
     * is just able to provide the value from the underlaying {@link IFeatureTableElement}
     * without any modifications from editing.
     * <p>
     * Consider using {@link #setLabelProvider(IFormFieldValidator)}.
     */
    @Override
    public FormFeatureTableColumn setLabelProvider( ColumnLabelProvider labelProvider ) {
        assert validator == null : "setLabelsProvider() was called already.";
        this.labelProvider = labelProvider;
        return this;
    }

    @Override
    public ColumnLabelProvider getLabelProvider() {
        return labelProvider;
    }


    /**
     * Sets the validator that provides labels, transformation and validation:
     * <ul>
     * <li>Label of each cell: {@link ITableFieldValidator#transform2Field(Object,ValidatorSite)} </li>
     * <li>Editing...</li>
     * </ul>
     */
    public FormFeatureTableColumn setLabelsAndValidation( ITableFieldValidator validator ) {
        assert labelProvider == null : "setLabelProvider() was called already.";
        this.validator = validator;
        return this;
    }


    public FormFeatureTableColumn setHeader( String header ) {
        this.header = header;
        return this;
    }

    public FormFeatureTableColumn setWeight( int weight, int minimumWidth ) {
        this.weight = weight;
        this.minimumWidth = minimumWidth;
        return this;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }

    public FormFeatureTableColumn setAlign( int align ) {
        this.align = align;
        return this;
    }

    public boolean isSortable() {
        return sortable;
    }
    
    public FormFeatureTableColumn setSortable( boolean sortable ) {
        this.sortable = sortable;
        return this;
    }

    public FormFeatureTableColumn setSortable( Comparator<IFeatureTableElement> sorter ) {
        this.sorter = sorter;
        this.sortable = true;
        return this;
    }
    

    /**
     * The form field used to edit values of this column. Must be compatible with
     * value types delivered by {@link #setLabelsAndValidation(ITableFieldValidator)}.
     */
    public FormFeatureTableColumn setEditing( IFormField formField ) {
        assert viewer == null : "Call before table is created.";
        this.editingFormField = formField;
        return this;
    }

    
    public FormFeatureTableColumn setEditing( CellEditor cellEditor ) {
        assert viewer == null : "Call before table is created.";
        this.cellEditor = cellEditor;
        return this;
    }

    
    @Override
    public void setViewer( FeatureTableViewer viewer ) {
        this.viewer = viewer;
    }
    
    
    @Override
    public TableViewerColumn getViewerColumn() {
        return viewerColumn;
    }

    
    @Override
    public TableViewerColumn newViewerColumn() {
        assert viewerColumn == null;
        
        if (align == -1) {
            align = Number.class.isAssignableFrom( prop.getType().getBinding() )
                    || Date.class.isAssignableFrom( prop.getType().getBinding() )
                    ? SWT.RIGHT : SWT.LEFT;
        }

        viewerColumn = new TableViewerColumn( viewer, align );
        viewerColumn.getColumn().setMoveable( true );
        viewerColumn.getColumn().setResizable( true );
        viewerColumn.getColumn().setText( header != null ? header : StringUtils.capitalize( getName() ) );
        
        boolean formEditing = editingFormField != null;
        
        // defaults for basic types
        Class binding = prop.getType().getBinding();
        Locale locale = Optional.ofNullable( Polymap.getSessionLocale() ).orElse( Locale.getDefault() );
        // Number
        if (Number.class.isAssignableFrom( binding )) {
            validator = validator != null ? validator : new NumberValidator( binding, locale ).forTable();
            editingFormField = editingFormField != null ? editingFormField : new StringFormField();
        }
        // Boolean
        else if (Boolean.class.isAssignableFrom( binding )) {
            throw new RuntimeException( "Not yet supported: Boolean" );
        }
        // default: String
        else {
            validator = validator != null ? validator : new NullValidator().forTable();
            editingFormField = editingFormField != null ? editingFormField : new StringFormField();
        }
        
        // labelProvider
        labelProvider = labelProvider != null ? labelProvider : new FormColumnLabelProvider( this );
        viewerColumn.setLabelProvider( new LoadingCheckLabelProvider( labelProvider ) );
        
        // editingSupport
        if (formEditing) {
            editingSupport = new FormEditingSupport( viewer, this, editingFormField, validator );
            viewerColumn.setEditingSupport( editingSupport );
        }

        if (cellEditor != null) {
            editingSupport = new FormEditingSupport( viewer, this, cellEditor );
            viewerColumn.setEditingSupport( editingSupport );
        }
        
        // sort listener for supported prop bindings
        Class propBinding = prop.getType().getBinding();
        if (sortable /*&&
                (String.class.isAssignableFrom( propBinding )
                || Number.class.isAssignableFrom( propBinding )
                || Date.class.isAssignableFrom( propBinding ))*/) {

            viewerColumn.getColumn().addListener( SWT.Selection, new Listener() {
                public void handleEvent( Event ev ) {
                    TableColumn sortColumn = viewer.getTable().getSortColumn();
                    final TableColumn selectedColumn = (TableColumn)ev.widget;
                    int dir = viewer.getTable().getSortDirection();
                    //log.info( "Sort: sortColumn=" + sortColumn.getText() + ", selectedColumn=" + selectedColumn.getText() + ", dir=" + dir );

                    if (sortColumn == selectedColumn) {
                        dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                    } 
                    else {
                        dir = SWT.DOWN;
                    }
                    viewer.sortContent( FormFeatureTableColumn.this, dir );
                }
            });
        }
        
        TableLayout tableLayout = (TableLayout)viewer.getTable().getLayout();

        if (weight > -1) {
            tableLayout.addColumnData( new ColumnWeightData( weight, minimumWidth, true ) );            
        }
        else if (String.class.isAssignableFrom( propBinding )) {
            tableLayout.addColumnData( new ColumnWeightData( 20, 120, true ) );
        }
        else {
            tableLayout.addColumnData( new ColumnWeightData( 10, 80, true ) );            
        }
        return viewerColumn;
    }

    
    public Comparator<IFeatureTableElement> newComparator( int sortDir ) {
        Comparator<IFeatureTableElement> result = null;
        if (sorter != null) {
            result = sorter;
        }
        else {
            result = new Comparator<IFeatureTableElement>() {

                private String                  sortPropName = getName();
                private ColumnLabelProvider     lp = getLabelProvider();

                @Override
                public int compare( IFeatureTableElement elm1, IFeatureTableElement elm2 ) {
                    // the value from the elm or String from LabelProvider as fallback
                    Object value1 = elm1.getValue( sortPropName );
                    Object value2 = elm2.getValue( sortPropName );
                    
                    if (value1 == null && value2 == null) {
                        return 0;
                    }
                    else if (value1 == null) {
                        return -1;
                    }
                    else if (value2 == null) {
                        return 1;
                    }
                    else if (!value1.getClass().equals( value2.getClass() )) {
                        throw new RuntimeException( "Column types do not match: " + value1.getClass().getSimpleName() + " - " + value2.getClass().getSimpleName() );
                    }
                    else if (value1 instanceof String) {
                        return ((String)value1).compareToIgnoreCase( (String)value2 );
                    }
                    else if (value1 instanceof Number) {
                        return (int)(((Number)value1).doubleValue() - ((Number)value2).doubleValue());
                    }
                    else if (value1 instanceof Date) {
                        return ((Date)value1).compareTo( (Date)value2 );
                    }
                    else {
                        throw new RuntimeException( "Unable to compare value: " + value1 );
                        //return value1.toString().compareTo( value2.toString() );
                    }
                }
            };
        }
        return sortDir == SWT.UP ? Collections.reverseOrder( result ) : result;
    }

    
    @Override
    public FormFeatureTableColumn sort( int dir ) {
        assert viewerColumn != null : "Add this column to the viewer before calling sort()!";
        viewer.sortContent( FormFeatureTableColumn.this, dir );
        return this;
    }

    
    void updateFieldValue( IFeatureTableElement elm, Object newFieldValue ) {
        modifiedFieldValues.put( elm, newFieldValue );
        getViewer().update( elm, null );
        EventManager.instance().publish( new PropertyChangeEvent( this, getName(), null, null ) );
    }
    

    /**
     * 
     *
     * @param elm
     * @throws Exception The Exception thrown by the validator. 
     */
    Object modifiedFieldValue( IFeatureTableElement elm, boolean editing ) throws Exception {
        return modifiedFieldValues.getOrDefault( elm, 
                validator.transform2Field( elm.getValue( getName() ), new DefaultValidatorSite( elm, this, editing ) ) );
    }
    

    /**
     * Used by {@link ValidatorSite#setColumnValue(String, Date)}.
     * @throws Exception The Exception thrown by the validator. 
     */
    void updateModelValue( IFeatureTableElement elm, Object newModelValue ) throws Exception {
        DefaultValidatorSite site = new DefaultValidatorSite( elm, this, true );
        Object newFieldValue = validator.transform2Field( newModelValue, site );
        modifiedFieldValues.put( elm, newFieldValue );
        getViewer().update( elm, null );
//        EventManager.instance().publish( new PropertyChangeEvent( this, getName(), null, null ) );
    }
    

    /**
     * Used by {@link ValidatorSite#columnValue(String)}.
     * @throws Exception The Exception thrown by the validator. 
     */
    <T> Optional<T> modifiedModelValue( IFeatureTableElement elm ) throws Exception {
        assert elm != null : "elm is null.";
        // check contains as null is allowed in map (?)
        if (modifiedFieldValues.containsKey( elm )) {
            Object fieldValue = modifiedFieldValues.get( elm );
            DefaultValidatorSite site = new DefaultValidatorSite( elm, this, false );
            return validator.validate( fieldValue, site ) == null
                    ? Optional.ofNullable( (T)validator.transform2Model( fieldValue, site ) )
                    : Optional.empty();
        }
        else {
            return Optional.ofNullable( (T)elm.getValue( getName() ) );
        }
    }
    

    public boolean isDirty() {
        return modifiedFieldValues.isEmpty();
    }
    
    
    public boolean isValid() {
        for (IFeatureTableElement elm : modifiedFieldValues.keySet()) {
            DefaultValidatorSite site = new DefaultValidatorSite( elm, this, false );
            Object value = modifiedFieldValues.get( elm );
            if (validator.validate( value, site ) != null) {
                return false;
            }
        }
        return true;
    }

    
    public void submit( IProgressMonitor monitor ) throws Exception {
        for (IFeatureTableElement elm : modifiedFieldValues.keySet()) {
            Object newFieldValue = modifiedFieldValues.get( elm );
            DefaultValidatorSite site = new DefaultValidatorSite( elm, this, false );
            Object newModelValue = validator.transform2Model( newFieldValue, site );
            elm.setValue( getName(), newModelValue );
        }
        modifiedFieldValues.clear();
    }
    
    
    /**
     * The currently modified elements of this column mapped to the new values. The values are
     * field values provided by the user input. The model value/type might be different.
     */
    public Map<IFeatureTableElement,Object> modified() {
        return modifiedFieldValues;
    }
    
    
    /**
     * Loading and dirty/valid decoration. 
     */
    class LoadingCheckLabelProvider
            extends ColumnLabelProvider {
    
        private ColumnLabelProvider     delegate;

        public LoadingCheckLabelProvider( ColumnLabelProvider delegate ) {
            assert delegate != null;
            this.delegate = delegate;
        }

        public String getText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? "Loading..."
                    : delegate.getText( element );
        }

        public String getToolTipText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? null : delegate.getToolTipText( element );
        }

        @Override
        public Image getImage( Object elm ) {
            if (elm == FeatureTableViewer.LOADING_ELEMENT) {
                return null;
            }
            else {
                return delegate.getImage( elm );
            }
        }

        @Override
        public Color getForeground( Object elm ) {
            return elm == FeatureTableViewer.LOADING_ELEMENT
                    ? FeatureTableViewer.LOADING_FOREGROUND
                    : delegate.getForeground( elm );
        }

        @Override
        public Color getBackground( Object elm ) {
            if (elm == FeatureTableViewer.LOADING_ELEMENT) {
                return FeatureTableViewer.LOADING_BACKGROUND;
            }
            
            IFeatureTableElement felm = (IFeatureTableElement)elm;
            Object modifiedValue = modifiedFieldValues.get( felm );
            DefaultValidatorSite validatorSite = new DefaultValidatorSite( felm, FormFeatureTableColumn.this, true );
            if (modifiedValue != null && validator.validate( modifiedValue, validatorSite ) != null ) {
                return INVALID_BACKGROUND;
            }
            else if (modifiedValue != null ) {
                return DIRTY_BACKGROUND;
            }
            else {
                return delegate.getBackground( elm );
            }
        }

        @Override
        public void addListener( ILabelProviderListener listener ) {
            delegate.addListener( listener );
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }

        @Override
        public boolean isLabelProperty( Object element, String property ) {
            return delegate.isLabelProperty( element, property );
        }

        @Override
        public Font getFont( Object element ) {
            return delegate.getFont( element );
        }

        @Override
        public void removeListener( ILabelProviderListener listener ) {
            delegate.removeListener( listener );
        }

        @Override
        public Image getToolTipImage( Object object ) {
            return delegate.getToolTipImage( object );
        }

        @Override
        public Color getToolTipBackgroundColor( Object object ) {
            return delegate.getToolTipBackgroundColor( object );
        }

        @Override
        public Color getToolTipForegroundColor( Object object ) {
            return delegate.getToolTipForegroundColor( object );
        }

        @Override
        public Font getToolTipFont( Object object ) {
            return delegate.getToolTipFont( object );
        }

        @Override
        public Point getToolTipShift( Object object ) {
            return delegate.getToolTipShift( object );
        }

        @Override
        public boolean useNativeToolTip( Object object ) {
            return delegate.useNativeToolTip( object );
        }

        @Override
        public int getToolTipTimeDisplayed( Object object ) {
            return delegate.getToolTipTimeDisplayed( object );
        }

        @Override
        public int getToolTipDisplayDelayTime( Object object ) {
            return delegate.getToolTipDisplayDelayTime( object );
        }

        @Override
        public int getToolTipStyle( Object object ) {
            return delegate.getToolTipStyle( object );
        }

        @Override
        public void dispose( @SuppressWarnings("hiding") ColumnViewer viewer, ViewerColumn column ) {
            delegate.dispose( viewer, column );
        }
        
    }

}
