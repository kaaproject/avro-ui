/*
 * Copyright 2014 CyberVision, Inc.
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

import static org.kaaproject.avro.ui.gwt.client.util.Utils.isBlank;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog.ConfirmListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.BooleanField;
import org.kaaproject.avro.ui.shared.BytesField;
import org.kaaproject.avro.ui.shared.DoubleField;
import org.kaaproject.avro.ui.shared.EnumField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FixedField;
import org.kaaproject.avro.ui.shared.FloatField;
import org.kaaproject.avro.ui.shared.FormEnum;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FormField.ChangeListener;
import org.kaaproject.avro.ui.shared.FormField.FieldAccess;
import org.kaaproject.avro.ui.shared.IntegerField;
import org.kaaproject.avro.ui.shared.LongField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.SizedField;
import org.kaaproject.avro.ui.shared.StringField;
import org.kaaproject.avro.ui.shared.StringField.InputType;
import org.kaaproject.avro.ui.shared.UnionField;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractFieldWidget<T extends FormField> extends SimplePanel implements HasValue<T> {
    
    private static final String DEFAULT_INTEGER_FORMAT = "#";
    private static final String DEFAULT_DECIMAL_FORMAT = "#.#############################";
    protected static final String FULL_WIDTH = "100%";
    
    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    protected T value;
    
    protected NavigationContainer navigationContainer;
    
    protected final AvroUiStyle style;
    
    protected boolean readOnly = false;

    private static AvroUiStyle getDefaultStyle() {
        return Utils.avroUiStyle;
    }
    
    public AbstractFieldWidget(NavigationContainer navigationContainer, boolean readOnly) {
        this(getDefaultStyle(), navigationContainer, readOnly);
    }
    
    public AbstractFieldWidget(AvroUiStyle style, NavigationContainer navigationContainer, boolean readOnly) {
        
        this.navigationContainer = navigationContainer;
        this.readOnly = readOnly;
        
        // Inject the stylesheet.
        this.style = style;
        this.style.ensureInjected();
        
        this.addStyleName(style.fieldWidget());
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }
        T before = this.value;
        this.value = value;
        updateFields();
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }
    
    public boolean validate() {
        return value != null && value.isValid();
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public void onShown() {
        traverseShown(this);
    }
    
    private void traverseShown(HasWidgets hasWidgets) {
        for (Widget childWidget : hasWidgets) {
            if (childWidget instanceof AbstractFieldWidget) {
                ((AbstractFieldWidget<?>)childWidget).onShown();
            } else if (childWidget instanceof HasWidgets) {
                traverseShown((HasWidgets)childWidget);
            } else if (childWidget instanceof FieldWidgetPanel) {
                Widget w = ((FieldWidgetPanel)childWidget).getContent();
                if (w instanceof AbstractFieldWidget) {
                    ((AbstractFieldWidget<?>)w).onShown();
                } else if (w instanceof HasWidgets) {
                    traverseShown((HasWidgets)w);
                }
            }
        }
    }
    
    protected void setNavigationContainer(NavigationContainer container) {
        this.navigationContainer = container;
    }
    
    protected void fireChanged() {
        ValueChangeEvent.fire(this, value);
    }

    private void updateFields() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
        setWidget(constructForm());
    }
    
    protected abstract Widget constructForm();

    protected void constructFormData(FlexTable table, FormField field, List<HandlerRegistration> handlerRegistrations) {
        table.removeAllRows();
        table.getColumnFormatter().setWidth(0, "200px");
        table.getColumnFormatter().setWidth(1, "300px");
        if (field != null && field.getFieldAccess() != FieldAccess.HIDDEN) {
            int row = 0;
            if (field.getFieldType()==FieldType.RECORD) {
                for (FormField formField : ((RecordField)field).getValue()) {
                    row = constructField(table, row, formField, handlerRegistrations);
                    row++;
                }
            } else {
                constructField(table, row, field, handlerRegistrations);
            }
        }
    }
    
    private int constructField(FlexTable table, int row, FormField field, List<HandlerRegistration> handlerRegistrations) {
        int column = 0;
        Widget widget;
        if (shouldPlaceNestedWidgetButton(field.getFieldType())) {
            widget = constructNestedWidgetButton(field, handlerRegistrations);       
        } else {
            widget = constructWidget(field, handlerRegistrations);
        }
        if (shouldPlaceLabel(field.getFieldType())) {
            constructLabel(table, field, row, column);
            column++;
        }
        row = placeWidget(table, field.getFieldType(), widget, row, column, handlerRegistrations);
        return row;
    }
    
    private boolean shouldPlaceLabel(FieldType type) {
        if (type.isComplex()) {
            if (type == FieldType.UNION) {
                return value.getFieldType() == FieldType.UNION;
            } else {
                return type != FieldType.ARRAY;
            }
        } else {
            return true;
        }
    }
    
    private boolean shouldPlaceNestedWidgetButton(FieldType type) {
        if (type.isComplex()) {
            if (type == FieldType.UNION) {
                return value.getFieldType() == FieldType.UNION;
            } else {
                return type != FieldType.ARRAY;
            }
        } else {
            return false;
        }
    }
    
    protected int constructAndPlaceWidget(FlexTable table, FormField field, int row, int column, List<HandlerRegistration> handlerRegistrations) {
        Widget widget = constructWidget(field, handlerRegistrations);     
        row = placeWidget(table, field.getFieldType(), widget, row, column, handlerRegistrations);
        return row;
    }
    
    protected int placeWidget(FlexTable table, FieldType type, Widget widget, int row, int column, List<HandlerRegistration> handlerRegistrations) {
        if (type.isComplex() && !shouldPlaceNestedWidgetButton(type)) {
            table.setText(row, 0, "");
            table.setText(row, 1, "");
            row++;
            table.setWidget(row, column, widget);
            table.getFlexCellFormatter().setColSpan(row, column, 3);
        } else {
            if (value.getFieldType() == FieldType.RECORD) {
                widget.addStyleName(style.padded());
            }
            table.setWidget(row, column, widget);
        }
        return row;
    }
    
    protected void constructLabel(FlexTable table, FormField field, int row, int column) {
        FieldWidgetLabel label = new FieldWidgetLabel(field);
        if (value.getFieldType() == FieldType.RECORD) {
            label.addStyleName(style.padded());
        }
        table.setWidget(row, column, label);
    }
    
    protected Widget constructWidget(FormField field, List<HandlerRegistration> handlerRegistrations) {
        Widget widget = null;
        
        if ((readOnly || field.isReadOnly()) && !field.getFieldType().isComplex()) {
            if (field.getFieldType() == FieldType.BOOLEAN) {
                widget = constructBooleanWidget((BooleanField)field, handlerRegistrations);
            } else {
                String text = extractStringValue(field);
                HTML textLabel = new HTML("&nbsp;");
                textLabel.setHeight(FULL_WIDTH);
                textLabel.setStyleName(style.secondaryLabel());
                if (!isBlank(text)) {
                    textLabel.setText(text);
                }
                widget = textLabel;
            }
        } else {
            switch (field.getFieldType()) {
                case STRING:
                    widget = constructStringWidget((StringField)field, handlerRegistrations);
                    break;
                case BYTES:
                    widget = constructBytesWidget((BytesField)field, handlerRegistrations);
                    break;            
                case FIXED:
                    widget = constructFixedWidget((FixedField)field, handlerRegistrations);
                    break;            
                case INT:
                    widget = constructIntegerWidget((IntegerField)field, handlerRegistrations);
                    break;
                case FLOAT:
                    widget = constructFloatWidget((FloatField)field, handlerRegistrations);
                    break;
                case DOUBLE:
                    widget = constructDoubleWidget((DoubleField)field, handlerRegistrations);
                    break;
                case LONG:
                    widget = constructLongWidget((LongField)field, handlerRegistrations);
                    break;
                case ENUM:
                    widget = constructEnumWidget((EnumField)field, handlerRegistrations);
                    break;
                case BOOLEAN:
                    widget = constructBooleanWidget((BooleanField)field, handlerRegistrations);
                    break;
                case ARRAY:
                    widget = constructArrayWidget((ArrayField)field, handlerRegistrations);
                    break;
                case RECORD:
                	throw new RuntimeException("Can't create record widget inside table.");
                case UNION:
                    widget = constructUnionWidget((UnionField)field, handlerRegistrations);
                    break;
            }
        }
        widget.setWidth(FULL_WIDTH);
        return widget;
    }
    
    protected static String extractStringValue(FormField field) {
        switch (field.getFieldType()) {
        case STRING:
            return ((StringField)field).getValue();
        case BYTES:
            return ((BytesField)field).getValue();
        case FIXED:
            return ((FixedField)field).getValue();
        case INT:
            Integer intVal = ((IntegerField)field).getValue();
            return intVal != null ? String.valueOf(intVal) : null;
        case FLOAT:
            Float floatVal = ((FloatField)field).getValue();
            return floatVal != null ? String.valueOf(floatVal) : null;
        case DOUBLE:
            Double doubleVal = ((DoubleField)field).getValue();
            return doubleVal != null ? String.valueOf(doubleVal) : null;
        case LONG:
            Long longVal = ((LongField)field).getValue();
            return longVal != null ? String.valueOf(longVal) : null;
        case ENUM:
            FormEnum enumVal = ((EnumField)field).getValue();
            return enumVal != null ? enumVal.getDisplayValue() : null;
        default:
            return "";
        }
    }
    
    private Widget constructStringWidget(final StringField field,
            List<HandlerRegistration> handlerRegistrations) {
        final SizedTextBox textBox = new SizedTextBox(style,
                field.getInputType(), field.getDisplayHint(), field.getMaxLength(), true,
                field.getMaxLength() != SizedField.DEFAULT_MAX_LENGTH);
        textBox.setValue(field.getValue());
        handlerRegistrations.add(textBox.addInputHandler(new InputEventHandler() {
                    @Override
                    public void onInputChanged(InputEvent event) {
                        field.setValue(textBox.getValue());
                        fireChanged();
                    }
                }));
        return textBox;
    }
    
    private Widget constructBytesWidget(final BytesField field,
            List<HandlerRegistration> handlerRegistrations) {
        final SizedTextBox textBox = new SizedTextBox(style,
                InputType.PLAIN, field.getDisplayHint(), SizedField.DEFAULT_MAX_LENGTH, true,
                false);
        textBox.setValue(field.getValue());
        handlerRegistrations.add(textBox.addInputHandler(new InputEventHandler() {
                    @Override
                    public void onInputChanged(InputEvent event) {
                        field.setValue(textBox.getValue());
                        textBox.setInvalid(!field.isValid());
                        fireChanged();
                    }
                }));
        return textBox;
    }
    
    private Widget constructFixedWidget(final FixedField field,
            List<HandlerRegistration> handlerRegistrations) {
        final SizedTextBox textBox = new SizedTextBox(style,
                InputType.PLAIN, field.getDisplayHint(), field.getStringMaxSize(), true,
                false);       
        textBox.setValue(field.getValue());        
        handlerRegistrations.add(textBox.addInputHandler(new InputEventHandler() {
                    @Override
                    public void onInputChanged(InputEvent event) {
                        field.setValue(textBox.getValue());
                        textBox.setInvalid(!field.isValid());
                        fireChanged();
                    }
                }));
        return textBox;
    }
    
    private Widget constructIntegerWidget(final IntegerField field, List<HandlerRegistration> handlerRegistrations) {
        final IntegerBox integerBox = new IntegerBox(style, field.getDisplayHint(), DEFAULT_INTEGER_FORMAT);
        integerBox.setValue(field.getValue());
        handlerRegistrations.add(integerBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(integerBox.getValue());
                fireChanged();                
            }
        }));       
        return integerBox;
    }

    private Widget constructLongWidget(final LongField field, List<HandlerRegistration> handlerRegistrations) {
        final LongBox longBox = new LongBox(style, field.getDisplayHint(), DEFAULT_INTEGER_FORMAT);
        longBox.setValue(field.getValue());
        handlerRegistrations.add(longBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(longBox.getValue());
                fireChanged();                
            }
        }));       
        return longBox;
    }
    
    private Widget constructFloatWidget(final FloatField field, List<HandlerRegistration> handlerRegistrations) {
        final FloatBox floatBox = new FloatBox(style, field.getDisplayHint(), DEFAULT_DECIMAL_FORMAT);
        floatBox.setValue(field.getValue());
        handlerRegistrations.add(floatBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(floatBox.getValue());
                fireChanged();                
            }
        }));       
        return floatBox;
    }
    
    private Widget constructDoubleWidget(final DoubleField field, List<HandlerRegistration> handlerRegistrations) {
        final DoubleBox doubleBox = new DoubleBox(style, field.getDisplayHint(), DEFAULT_DECIMAL_FORMAT);
        doubleBox.setValue(field.getValue());
        handlerRegistrations.add(doubleBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(doubleBox.getValue());
                fireChanged();                
            }
        }));       
        return doubleBox;
    }
    
    private Widget constructEnumWidget(final EnumField field, List<HandlerRegistration> handlerRegistrations) {
        FormEnumListBox enumBox = new FormEnumListBox(style, field.getDisplayHint());
        if (!field.isOptional() && !field.isOverride()) {
            enumBox.setValue(field.getValue());
        }        
        enumBox.setAcceptableValues(field.getEnumValues());
        if (field.isOptional() || field.isOverride()) {
            enumBox.setValue(field.getValue());
        }
        handlerRegistrations.add(enumBox.addValueChangeHandler(new ValueChangeHandler<FormEnum>() {
            @Override
            public void onValueChange(ValueChangeEvent<FormEnum> event) {
                field.setValue(event.getValue());
                fireChanged();  
            }
        }));
        return enumBox;
    }
    
    private Widget constructBooleanWidget(final BooleanField field, List<HandlerRegistration> handlerRegistrations) {
        CheckBox checkBox = new CheckBox();
        checkBox.setValue(field.getValue());
        checkBox.setEnabled(!readOnly && !field.isReadOnly());
        if (!readOnly && !field.isReadOnly()) {
            handlerRegistrations.add(checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    field.setValue(event.getValue());
                    fireChanged();  
                }
            }));
        }
        return checkBox;
    }
    
    private Widget constructArrayWidget(final ArrayField field, List<HandlerRegistration> handlerRegistrations) {
        ArrayFieldWidget arrayWidget = new ArrayFieldWidget(style, navigationContainer, readOnly);
        arrayWidget.setValue(field);
        if (!readOnly && !field.isReadOnly()) {
            handlerRegistrations.add(arrayWidget.addValueChangeHandler(new ValueChangeHandler<ArrayField>() {
                @Override
                public void onValueChange(ValueChangeEvent<ArrayField> event) {
                    fireChanged();  
                }
            }));
        }
        return arrayWidget;
    }
    
    private Widget constructUnionWidget(final UnionField field, List<HandlerRegistration> handlerRegistrations) {
        UnionFieldWidget unionWidget = new UnionFieldWidget(style, navigationContainer, readOnly);
        unionWidget.setValue(field);
        if (!readOnly && !field.isReadOnly()) {
            handlerRegistrations.add(unionWidget.addValueChangeHandler(new ValueChangeHandler<UnionField>() {
                @Override
                public void onValueChange(ValueChangeEvent<UnionField> event) {
                    fireChanged();  
                }
            }));
        }
        return unionWidget;
    }
    
    private Widget constructNestedWidgetButton(final FormField field, List<HandlerRegistration> handlerRegistrations) {
        HorizontalPanel nestedWidget = new HorizontalPanel();
        nestedWidget.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        final String fieldTypeName = field.getFieldType().getName();
        Label label = new Label(Utils.messages.nestedEntry(fieldTypeName));
        label.setStyleName(style.fieldNotes());
        label.getElement().getStyle().setPaddingRight(10, Unit.PX);
        final boolean createRecord = field.getFieldType() == FieldType.RECORD && field.isNull();
        final Button openButton = new Button(createRecord ? Utils.constants.create() : Utils.constants.open());
        openButton.addStyleName(style.buttonSmall());
        final Button deleteButon = new Button(Utils.constants.delete());
        deleteButon.addStyleName(style.buttonSmall());        
        deleteButon.getElement().getStyle().setMarginLeft(10, Unit.PX);
        boolean disabled = (readOnly || field.isReadOnly()) && field.isOverride() && !field.isChanged();
        deleteButon.setVisible(field.getFieldType() == FieldType.RECORD && !field.isNull() && !disabled);
        openButton.setEnabled(!disabled);
        if (!disabled) {
            openButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                	if (createRecord) {
                		((RecordField)field).create();
                		openButton.setText(Utils.constants.open());
                		deleteButon.setVisible(true);
                		fireChanged();
                	}
                    navigationContainer.showField(field, new NavigationActionListener() {
                        @Override
                        public void onChanged(FormField field) {
                            fireChanged();
                        }
                        @Override
                        public void onAdded(FormField field) {}
                    });
                }
            });
          	deleteButon.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 			        ConfirmListener listener = new ConfirmListener() {
 			            @Override
 			            public void onNo() {
 			            }

 			            @Override
 			            public void onYes() {
 			                ((RecordField)field).setNull();
 			                openButton.setText(Utils.constants.create());
 			                deleteButon.setVisible(false);
 			                fireChanged();
 			            }
 			        };
 			        ConfirmDialog dialog = new ConfirmDialog(listener, Utils.messages.deleteNestedEntryTitle(), 
 			        		Utils.messages.deleteNestedEntryQuestion(fieldTypeName, field.getDisplayName()));
 			        dialog.center();
 			        dialog.show();
 				}
 			});
        }
        nestedWidget.add(label);
        nestedWidget.add(openButton);
        nestedWidget.add(deleteButon);
        return nestedWidget;
    }
    
    private class FieldWidgetLabel extends FlowPanel implements ChangeListener {
        
        private CheckBox overrideBox;
        
        public FieldWidgetLabel(final FormField field) {
            this.getElement().getStyle().setPosition(Position.RELATIVE);
            Label label = new Label(field.getDisplayName());
            label.getElement().getStyle().setPosition(Position.ABSOLUTE);
            if (field.isOverride()) {
                overrideBox = new CheckBox();
                overrideBox.getElement().getStyle().setPosition(Position.ABSOLUTE);
                label.getElement().getStyle().setLeft(28, Unit.PX);
                overrideBox.setValue(field.isChanged());
                overrideBox.setEnabled(!readOnly && !field.isReadOnly());
                add(overrideBox);
                if (!readOnly && !field.isReadOnly()) {
                    registrations.add(overrideBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Boolean> event) {
                            field.setChanged(event.getValue(), true);
                            fireChanged();
                        }
                    }));
                    field.addChangeListener(this);
                }
            }
            if (!field.isOptional()) {
                label.addStyleName(style.requiredField());
            }      
            add(label);
        }

        @Override
        public void onChanged(boolean changed) {
            overrideBox.setValue(changed);
        }
        
    }

}
