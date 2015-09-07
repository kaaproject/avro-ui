/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.avro.ui.gwt.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.BooleanField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FormField.FieldAccess;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.UnionField;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public class ArrayFieldWidget extends AbstractFieldWidget<ArrayField> {
    
    private ArrayGrid arrayGrid;
    private ScrollPanel tableScroll; 
    private FieldWidgetPanel fieldWidgetPanel;

    private static final String PX = "px";
    private static final Double WIDTH_INDEX = 0.98;

    public ArrayFieldWidget(AvroWidgetsConfig config, NavigationContainer container, boolean readOnly) {
        super(config, container, readOnly);
    }
    
    public ArrayFieldWidget(AvroWidgetsConfig config, AvroUiStyle style, NavigationContainer container, boolean readOnly) {
        super(config, style, container, readOnly);
    }
    
    @Override
    protected Widget constructForm() {
        
        fieldWidgetPanel = new FieldWidgetPanel(style, value, readOnly, true);
        if (value.isOverride()) {
            fieldWidgetPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if (!readOnly && !value.isReadOnly()) {
                        fireChanged();
                    }
                    if (event.getValue()) {
                        onShown();
                    }
                }
            });
            fieldWidgetPanel.setLegendNotes(Utils.constants.getString(value
                    .getOverrideStrategy().name().toLowerCase()
                    + "Strategy"));
        }
        fieldWidgetPanel.setWidth(config.getArrayPanelWidth());
        
        value.finalizeMetadata();
        
        if (isGridNeeded(value)) {
            fieldWidgetPanel.setContent(constructGrid());
        } else {
            fieldWidgetPanel.setContent(constructTable());
        }
        return fieldWidgetPanel;
    }
    
    @Override
    public void updateConfig(AvroWidgetsConfig config) {
        super.updateConfig(config);
        if (fieldWidgetPanel != null) {
            fieldWidgetPanel.setWidth(config.getArrayPanelWidth());
        }
        if (arrayGrid != null) {
            arrayGrid.setHeight(config.getGridHeight());
        }
        if (tableScroll != null) {
            tableScroll.setHeight(config.getTableHeight());
            String panelWidth = config.getArrayPanelWidth();
            tableScroll.setWidth("" + (Double.valueOf(panelWidth.substring(0, panelWidth.indexOf(PX)))*WIDTH_INDEX) + PX);
        }
    }

    public static boolean isGridNeeded(ArrayField field) {
        FormField metadata = field.getElementMetadata();
        if (metadata.getFieldType().isComplex()) {
            if (metadata.getFieldType() == FieldType.RECORD) {
                RecordField recordMetadata = (RecordField)metadata;
                if (!recordMetadata.getKeyIndexedFields().isEmpty()) {
                    return true;
                } else {
                    return hasComplexFields(recordMetadata.getFieldsWithAccess(FieldAccess.EDITABLE, 
                                                        FieldAccess.READ_ONLY));
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    private static boolean hasComplexFields(List<FormField> metaFields) {
        for (FormField metaField : metaFields) {
            if (metaField.getFieldType().isComplex()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onShown() {
        super.onShown();
        if (arrayGrid != null) {
            arrayGrid.reload();
        }
    }

    private Widget constructGrid() {
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth(FULL_WIDTH);

        arrayGrid = new ArrayGrid(value, !readOnly && !value.isReadOnly());
        arrayGrid.setHeight(config.getGridHeight());
        
        verticalPanel.add(arrayGrid);
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.addStyleName(style.buttonsPanel());

        if (!readOnly && !value.isReadOnly()) {
            Button addRow = new Button(Utils.constants.add());
            addRow.addStyleName(style.buttonSmall());
            addRow.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    FormField newField = value.createRow();
                    navigationContainer.addNewField(newField, new NavigationActionListener() {
                        @Override
                        public void onChanged(FormField field) {}
                        
                        @Override
                        public void onAdded(FormField field) {
                            arrayGrid.getDataProvider().addRow(field);
                        }
                    });
                }
            });
            buttonsPanel.add(addRow);
        }
        
        arrayGrid.addRowActionHandler(new RowActionEventHandler<Integer>() {
            @Override
            public void onRowAction(RowActionEvent<Integer> event) {
                final int index = event.getClickedId();
                if (event.getAction() == RowActionEvent.CLICK) {
                    FormField field = arrayGrid.getDataProvider().getData().get(index);
                    navigationContainer.showField(field, null);
                } else if (event.getAction() == RowActionEvent.DELETE) {
                    arrayGrid.getDataProvider().removeRow(index);
                    fireChanged();
                }
            }
        });
        
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel.add(buttonsPanel);
        return verticalPanel;
    }
    
    private Widget constructTable() {
        VerticalPanel verticalPanel = new VerticalPanel();
        tableScroll = new ScrollPanel();
        final FlexTable table = new FlexTable();
        table.setWidth("95%");
        table.setCellPadding(5);
        
        List<FormField> records = value.getValue();
        final int minRowCount = value.getMinRowCount();
        final FormField elementMetadata = value.getElementMetadata();
        
        final int startRow;
        
        if (elementMetadata.getFieldType() == FieldType.RECORD) {
            RecordField recordElementData = (RecordField)elementMetadata;
            float totalWeight = 0f;
            List<FormField> metaFields = recordElementData.getFieldsWithAccess(FieldAccess.EDITABLE, 
                    FieldAccess.READ_ONLY);
            for (int column=0;column<metaFields.size();column++) {
                FormField metaField = metaFields.get(column);
                totalWeight += metaField.getWeight();
            }
            
            for (int column=0;column<metaFields.size();column++) {
                FormField metaField = metaFields.get(column);
                float weight = metaField.getWeight();
                String width = String.valueOf(weight/totalWeight*100f)+"%";
                table.getColumnFormatter().setWidth(column, width);
                table.setWidget(0, column, new Label(metaField.getDisplayName()));
            }
            startRow = 1;
        } else {
            startRow = 0;
        }

        final Map<FormField, List<HandlerRegistration>> rowHandlerRegistrationMap = 
                new HashMap<>();
       
        for (int row=0;row<records.size();row++) {
            FormField record = records.get(row);
            List<HandlerRegistration> rowHandlerRegistrations = new ArrayList<>();
            setRow(table, record, row+startRow, rowHandlerRegistrations);
            registrations.addAll(rowHandlerRegistrations);
            rowHandlerRegistrationMap.put(record, rowHandlerRegistrations);
        }

        String panelWidth = config.getArrayPanelWidth();
        tableScroll.setWidth("" + (Double.valueOf(panelWidth.substring(0, panelWidth.indexOf(PX)))*WIDTH_INDEX) + PX);
        tableScroll.setHeight(config.getTableHeight());
        tableScroll.add(table);

        verticalPanel.setWidth(FULL_WIDTH);
        verticalPanel.add(tableScroll);
        
        if (!readOnly && !value.isReadOnly()) {
            Button addRow = new Button(Utils.constants.add());
            addRow.addStyleName(style.buttonSmall());
            final Button removeRow = new Button(Utils.constants.remove());
            removeRow.addStyleName(style.buttonSmall());
            removeRow.setEnabled(value.getValue().size()>minRowCount);
            
            addRow.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    FormField newField = value.createRow();
                    value.addArrayData(newField);
                    List<HandlerRegistration> rowHandlerRegistrations = new ArrayList<>();
                    setRow(table, newField, value.getValue().size() - 1 + startRow, rowHandlerRegistrations);
                    rowHandlerRegistrationMap.put(newField, rowHandlerRegistrations);
                    removeRow.setEnabled(value.getValue().size()>minRowCount);
                    fireChanged();
                }
            });
           
            removeRow.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (value.getValue().size()>0) {
                        int row = value.getValue().size()-1;
                        FormField toDelete = value.getValue().get(row);
                        List<HandlerRegistration> registrations = rowHandlerRegistrationMap.remove(toDelete);
                        if (registrations != null) {
                            for (HandlerRegistration registration : registrations) {
                                registration.removeHandler();
                            }
                            registrations.clear();
                        }
                        table.removeRow(row + startRow);
                        value.getValue().remove(row);
                        removeRow.setEnabled(value.getValue().size()>minRowCount);
                        fireChanged();
                    }
                }
            });
    
            HorizontalPanel buttonsPanel = new HorizontalPanel();
            buttonsPanel.addStyleName(style.buttonsPanel());
            buttonsPanel.add(addRow);
            buttonsPanel.add(removeRow);
            
            verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            verticalPanel.add(buttonsPanel);
        }
        return verticalPanel;
    }
    
    private void setRow(FlexTable table, FormField field, int row, List<HandlerRegistration> handlerRegistrations) {
        if (field.getFieldType() == FieldType.RECORD) {
            RecordField record = (RecordField)field;
            List<FormField> recordFields = record.getFieldsWithAccess(FieldAccess.EDITABLE,
                    FieldAccess.READ_ONLY);
            for (int column=0;column<recordFields.size();column++) {
                FormField cellField = recordFields.get(column);
                constructAndPlaceWidget(table, cellField, row, column, handlerRegistrations);
            }
        } else {
            constructAndPlaceWidget(table, field, row, 0, handlerRegistrations);
        } 
    }
    
    private static class ArrayGrid extends AbstractGrid<FormField, Integer> {
        
        private static final int MAX_CELL_STRING_LENGTH = 100;

        private List<FormField> metadata;
        private ArrayDataProvider dataProvider;
        
        public ArrayGrid(ArrayField arrayField, boolean enableActions) {
            super(Unit.PX, enableActions, true, false);
            this.dataProvider = new ArrayDataProvider(arrayField);
            FormField elementMetadata = arrayField.getElementMetadata();
            if (elementMetadata.getFieldType() == FieldType.RECORD) {
                RecordField recordElementMetadata = (RecordField)elementMetadata;
                this.metadata = recordElementMetadata.getKeyIndexedFields();
                if (this.metadata.isEmpty()) {
                    this.metadata = ((RecordField)elementMetadata).getFieldsWithAccess(FieldAccess.EDITABLE, 
                            FieldAccess.READ_ONLY);
                }
            } else if (elementMetadata.getFieldType() == FieldType.UNION && !elementMetadata.isOptional()) {
                UnionField unionElementMetadata = (UnionField)elementMetadata;
                List<FormField> acceptableValues = unionElementMetadata.getAcceptableValues();
                boolean useFirstRecord = true;
                List<FormField> keyIndexedFields = null;
                for (FormField acceptableValue : acceptableValues) {
                    if (acceptableValue != null && acceptableValue instanceof RecordField) {
                        List<FormField> recordKeyIndexedFields = ((RecordField)acceptableValue).getKeyIndexedFields();
                        if (recordKeyIndexedFields != null && !recordKeyIndexedFields.isEmpty()) {
                            if (keyIndexedFields == null) {
                                keyIndexedFields = recordKeyIndexedFields;
                            } else {
                                if (keyIndexedFields.size() != recordKeyIndexedFields.size()) {
                                    useFirstRecord = false;
                                    break;
                                } else {
                                    for (int i=0;i<recordKeyIndexedFields.size();i++) {
                                        if (!keyIndexedFields.get(i).getFieldName().equals(recordKeyIndexedFields.get(i).getFieldName())) {
                                            useFirstRecord = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            useFirstRecord = false;
                            break;
                        }
                    } else {
                        useFirstRecord = false;
                        break;
                    }
                }
                if (useFirstRecord && keyIndexedFields != null) {
                    this.metadata = keyIndexedFields;
                }
            }
            if (this.metadata == null) {
                this.metadata = new ArrayList<>();
                this.metadata.add(elementMetadata);
            }
            init();
            showShadow(false);
            this.dataProvider.addDataDisplay(getDataGrid());
        }
        
        public void reload() {
            this.dataProvider.reload(getDataGrid());
        }
        
        public ArrayDataProvider getDataProvider() {
            return this.dataProvider;
        }

        @Override
        protected float constructColumnsImpl(DataGrid<FormField> table) {
            float prefWidth = 0;
            for (final FormField metaField : metadata) {
                float width = 200 * metaField.getWeight();
                if (metaField.getFieldType() == FieldType.BOOLEAN) {
                    prefWidth += constructBooleanColumn(table, metaField.getDisplayName(),
                            new BooleanValueProvider<FormField>() {
                        @Override
                        public Boolean getValue(FormField item) {
                            FormField field = item;
                            if (field.getFieldType() == FieldType.RECORD) {
                                field = ((RecordField)item).getFieldByName(metaField.getFieldName());
                            }
                            return ((BooleanField)field).getValue();
                        }
                    }, width);
                } else if (!metaField.getFieldType().isComplex()) {
                    prefWidth += constructStringColumn(table, metaField.getDisplayName(),
                            new StringValueProvider<FormField>() {
                        @Override
                        public String getValue(FormField item) {
                            FormField field = item;
                            if (field.getFieldType() == FieldType.RECORD) {
                                field = ((RecordField)item).getFieldByName(metaField.getFieldName());
                            } else if (field.getFieldType() == FieldType.UNION) {
                                FormField unionVal = ((UnionField)field).getValue();
                                field = ((RecordField)unionVal).getFieldByName(metaField.getFieldName());
                            }
                            String value = extractStringValue(field);
                            if (value != null && value.length() > MAX_CELL_STRING_LENGTH) {
                                value = value.substring(0, MAX_CELL_STRING_LENGTH-3) + "...";
                            }
                            return value;
                        }
                    }, width);
                } else {
                    prefWidth += constructStringColumn(table, metaField.getDisplayName(),
                            new StringValueProvider<FormField>() {
                        @Override
                        public String getValue(FormField item) {
                            FormField field = item;
                            if (field.getFieldType() == FieldType.RECORD) {
                                field = ((RecordField)item).getFieldByName(metaField.getFieldName());
                            }
                            String value = "";
                            if (metadata.size()==1) {
                                int index = getObjectId(item);
                                value = "#" + index + " ";
                            }
                            if (field.getFieldType() == FieldType.UNION) {
                                FormField unionVal = ((UnionField)field).getValue();
                                if (unionVal == null) {
                                    value += "null";
                                } else {
                                    value += unionVal.getDisplayName();
                                }
                            } else {
                                value += field.getFieldType().getDisplayName();
                            }
                            if (value.length() > MAX_CELL_STRING_LENGTH) {
                                value = value.substring(0, MAX_CELL_STRING_LENGTH-3) + "...";
                            }
                            return value;
                        }
                    }, width);
                }
            }
            return prefWidth;
        }
        
        protected Integer getObjectId(FormField value) {
            return dataProvider.getData().indexOf(value);
        }
    }
    
    private static class ArrayDataProvider extends AsyncDataProvider<FormField>{

        private ArrayField arrayField;

        private boolean loaded = false;

        public ArrayDataProvider(ArrayField arrayField) {
            this.arrayField = arrayField;
        }

        public void addRow(FormField row) {
            arrayField.addArrayData(row);
            updateRowCount(arrayField.getValue().size(), true);
            updateRowData(arrayField.getValue().size()-1, 
                    arrayField.getValue().subList(arrayField.getValue().size()-1, 
                            arrayField.getValue().size()));
        }
        
        public void removeRow(int index) {
            arrayField.getValue().remove(index);
            updateRowCount(arrayField.getValue().size(), true);
            int updateIndex = index;
            updateIndex = Math.min(updateIndex, arrayField.getValue().size()-1);
            updateIndex = Math.max(0, updateIndex);
            updateRowData(updateIndex, 
                    arrayField.getValue().subList(updateIndex, 
                            arrayField.getValue().size()));
        }

        public List<FormField> getData() {
            return arrayField.getValue();
        }

        public void reload(HasData<FormField> display) {
            this.loaded = false;
            loadData(display);
        }

        @Override
        protected void onRangeChanged(final HasData<FormField> display) {
          if (!loaded) {
              loadData(display);
          }
          else {
              updateData(display);
          }
        }
        
        private void loadData(HasData<FormField> display) {
            updateRowCount(arrayField.getValue().size(), true);
            updateData(display);
            loaded = true;
        }
        
        private void updateData (HasData<FormField> display) {
            int start = display.getVisibleRange().getStart();
            int end = start + display.getVisibleRange().getLength();
            end = end >= arrayField.getValue().size() ? arrayField.getValue().size() : end;
            List<FormField> sub = arrayField.getValue().subList(start, end);
            updateRowData(start, sub);
        }
 
    }

}
