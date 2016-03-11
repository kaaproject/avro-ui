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

import static org.kaaproject.avro.ui.gwt.client.util.Utils.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog.ConfirmListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.shared.AlertField;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.BooleanField;
import org.kaaproject.avro.ui.shared.BytesField;
import org.kaaproject.avro.ui.shared.DependenciesField;
import org.kaaproject.avro.ui.shared.DoubleField;
import org.kaaproject.avro.ui.shared.EnumField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FixedField;
import org.kaaproject.avro.ui.shared.FloatField;
import org.kaaproject.avro.ui.shared.FormContext;
import org.kaaproject.avro.ui.shared.FormContext.CtlDependenciesListener;
import org.kaaproject.avro.ui.shared.FormContext.DeclaredTypesListener;
import org.kaaproject.avro.ui.shared.FormEnum;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FormField.ChangeListener;
import org.kaaproject.avro.ui.shared.FormField.FieldAccess;
import org.kaaproject.avro.ui.shared.FormField.ValueChangeListener;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnKey;
import org.kaaproject.avro.ui.shared.FqnReferenceField;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.avro.ui.shared.IntegerField;
import org.kaaproject.avro.ui.shared.LongField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.SizedField;
import org.kaaproject.avro.ui.shared.StringField;
import org.kaaproject.avro.ui.shared.StringField.InputType;
import org.kaaproject.avro.ui.shared.UnionField;
import org.kaaproject.avro.ui.shared.VersionField;

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

public abstract class AbstractFieldWidget<T extends FormField> extends SimplePanel implements HasValue<T>, ShowableWidget {
    
    private static final String DEFAULT_INTEGER_FORMAT = "#";
    private static final String DEFAULT_DECIMAL_FORMAT = "#.#############################";
    protected static final String FULL_WIDTH = "100%";
    
    protected AvroWidgetsConfig config;
    
    protected List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    protected T value;
    
    protected NavigationContainer navigationContainer;
    
    protected final AvroUiStyle style;
    
    protected boolean readOnly = false;
    
    private static AvroUiStyle getDefaultStyle() {
        return Utils.avroUiStyle;
    }
    
    public AbstractFieldWidget(AvroWidgetsConfig config, NavigationContainer navigationContainer, boolean readOnly) {
        this(config, getDefaultStyle(), navigationContainer, readOnly);
    }
    
    public AbstractFieldWidget(AvroWidgetsConfig config, AvroUiStyle style, NavigationContainer navigationContainer, boolean readOnly) {
        
        this.config = config;
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

    @Override
    protected void onUnload() {
        //clearRegistrations();
        super.onUnload();
    }

    public boolean validate() {
        return value != null && value.isValid();
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    @Override
    public void onShown() {
        traverseShown(this);
    }
    
    protected void clearRegistrations() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
        registrations.clear();
    }
    
    protected void traverseShown(HasWidgets hasWidgets) {
        for (Widget childWidget : hasWidgets) {
            if (childWidget instanceof ShowableWidget) {
                ((ShowableWidget)childWidget).onShown();
            } else if (childWidget instanceof HasWidgets) {
                traverseShown((HasWidgets)childWidget);
            } else if (childWidget instanceof FieldWidgetPanel) {
                Widget w = ((FieldWidgetPanel)childWidget).getContent();
                if (w instanceof ShowableWidget) {
                    ((ShowableWidget)w).onShown();
                } else if (w instanceof HasWidgets) {
                    traverseShown((HasWidgets)w);
                }
            }
        }
    }
    
    public void updateConfig(AvroWidgetsConfig config) {
        this.config = config;
        traverseUpdateConfig(this, this.config);
    }
    
    private void traverseUpdateConfig(HasWidgets hasWidgets, AvroWidgetsConfig config) {
        for (Widget childWidget : hasWidgets) {
            if (childWidget instanceof AbstractFieldWidget) {
                ((AbstractFieldWidget<?>)childWidget).updateConfig(config);
            } else if (childWidget instanceof HasWidgets) {
                traverseUpdateConfig((HasWidgets)childWidget, config);
            } else if (childWidget instanceof FieldWidgetPanel) {
                Widget w = ((FieldWidgetPanel)childWidget).getContent();
                if (w instanceof AbstractFieldWidget) {
                    ((AbstractFieldWidget<?>)w).updateConfig(config);
                } else if (w instanceof HasWidgets) {
                    traverseUpdateConfig((HasWidgets)w, config);
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

    protected void updateFields() {
        clearRegistrations();
        setWidget(constructForm());
    }
    
    protected abstract Widget constructForm();

    protected void constructFormData(FlexTable table, FormField field, List<HandlerRegistration> handlerRegistrations) {
        table.removeAllRows();
        if (field != null && field.getFieldAccess() != FieldAccess.HIDDEN) {
            int row = 0;
            if (field.getFieldType()==FieldType.RECORD) {
                RecordField recordField = ((RecordField)field);
                for (FormField formField : recordField.getValue()) {
                    if (formField.getFieldAccess() != FieldAccess.HIDDEN) {
                        row = constructField(table, row, formField, handlerRegistrations);
                        row++;
                    }
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
                return type != FieldType.ARRAY && type != FieldType.DEPENDENCIES;
            }
        } else {
            return type != FieldType.ALERT;
        }
    }
    
    protected boolean shouldPlaceNestedWidgetButton(FieldType type) {
        if (type.isComplex()) {
            if (type == FieldType.UNION) {
                return value.getFieldType() == FieldType.UNION;
            } else {
                return type != FieldType.ARRAY && type != FieldType.DEPENDENCIES;
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
            table.setWidget(row, column, widget);
            if (type == FieldType.ALERT) {
                table.getFlexCellFormatter().setColSpan(row, column, 2);
            }
        }
        return row;
    }
    
    protected Widget constructLabel(FlexTable table, FormField field, int row, int column) {
        FieldWidgetLabel label = new FieldWidgetLabel(field);
        table.setWidget(row, column, label);
        return label;
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
                case TYPE_REFERENCE:
                    widget = constructFqnReferenceWidget((FqnReferenceField)field, handlerRegistrations);
                    break;    
                case ALERT:
                    widget = constructAlertWidget((AlertField)field, handlerRegistrations);
                    break;
                case VERSION:
                    widget = constructVersionWidget((VersionField)field, handlerRegistrations);
                    break;          
                case DEPENDENCIES:
                    widget = constructDependenciesWidget((DependenciesField)field, handlerRegistrations);
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
        case TYPE_REFERENCE:
            Fqn fqnVal = ((FqnReferenceField)field).getFqnValue();
            return fqnVal != null ? fqnVal.getFqnString() : null;
        case VERSION:
            Integer versionVal = ((VersionField)field).getValue();
            return versionVal != null ? String.valueOf(versionVal) : null;
        default:
            return "";
        }
    }
    
    private Widget constructStringWidget(final StringField field,
            List<HandlerRegistration> handlerRegistrations) {
        final SizedTextBox textBox = new SizedTextBox(style,
                field.getInputType(), field.getDisplayPrompt(), field.getMaxLength(), true,
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
                InputType.PLAIN, field.getDisplayPrompt(), SizedField.DEFAULT_MAX_LENGTH, true,
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
                InputType.PLAIN, field.getDisplayPrompt(), field.getStringMaxSize(), true,
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
        final IntegerBox integerBox = new IntegerBox(style, field.getDisplayPrompt(), DEFAULT_INTEGER_FORMAT);
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
        final LongBox longBox = new LongBox(style, field.getDisplayPrompt(), DEFAULT_INTEGER_FORMAT);
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
        final FloatBox floatBox = new FloatBox(style, field.getDisplayPrompt(), DEFAULT_DECIMAL_FORMAT);
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
        final DoubleBox doubleBox = new DoubleBox(style, field.getDisplayPrompt(), DEFAULT_DECIMAL_FORMAT);
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
    
    private Widget constructFqnReferenceWidget(final FqnReferenceField field, List<HandlerRegistration> handlerRegistrations) {
        final FqnReferenceBox fqnBox = new FqnReferenceBox(field.getDisplayPrompt());
        fqnBox.updateDeclaredFqns(field.getContext().getDeclaredTypes());
        fqnBox.setValue(field.getValue());
        final DeclaredTypesListener listener = new DeclaredTypesListener() {
            @Override
            public void onDeclaredTypesUpdated(Map<FqnKey, Fqn> declaredFqns) {
                if (fqnBox.isAttached()) {
                    fqnBox.updateDeclaredFqns(declaredFqns);
                }
            }
        };
        field.getContext().addDeclaredTypesListener(listener);
        final FormContext context = field.getContext();
        HandlerRegistration handlerRegistration = new HandlerRegistration() {
            @Override
            public void removeHandler() {
                context.removeDeclaredTypesListener(listener);
            }
        };
        handlerRegistrations.add(handlerRegistration);
        handlerRegistrations.add(fqnBox.addValueChangeHandler(new ValueChangeHandler<FqnKey>() {
            @Override
            public void onValueChange(ValueChangeEvent<FqnKey> event) {
                field.setValue(event.getValue());
                fireChanged();
            }
        }));       
        return fqnBox;
    }
    
    private Widget constructAlertWidget(final AlertField field, List<HandlerRegistration> handlerRegistrations) {
        final AlertPanel alertPanel = new AlertPanel(Type.ERROR);
        alertPanel.getElement().getStyle().setMargin(5, Unit.PX);
        if (field.getValue() != null) {
            alertPanel.setMessage(field.getValue());
            alertPanel.setVisible(true);
        } else {
            alertPanel.setMessage("");
            alertPanel.setVisible(false);
        }
        final ValueChangeListener listener = new ValueChangeListener() {
            
            private static final long serialVersionUID = -4935107365199693997L;

            @Override
            public void onValueChanged(Object value) {
                if (value != null) {
                    alertPanel.setMessage(value.toString());
                    alertPanel.setVisible(true);
                } else {
                    alertPanel.setMessage("");
                    alertPanel.setVisible(false);
                }
            }
        };
        field.addTransientValueChangeListener(listener);
        HandlerRegistration handlerRegistration = new HandlerRegistration() {
            @Override
            public void removeHandler() {
                field.removeTransientValueChangeListener(listener);
            }
        };
        handlerRegistrations.add(handlerRegistration);
        return alertPanel;
    }
    
    private Widget constructVersionWidget(final VersionField field, List<HandlerRegistration> handlerRegistrations) {
        final IntegerBox integerBox = new IntegerBox(style, field.getDisplayPrompt(), DEFAULT_INTEGER_FORMAT);
        integerBox.setValue(field.getValue());
        handlerRegistrations.add(integerBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                field.setValue(integerBox.getValue());
                fireChanged();                
            }
        }));       
        
        final ValueChangeListener listener = new ValueChangeListener() {
            
            private static final long serialVersionUID = -6240272895089248662L;

            @Override
            public void onValueChanged(Object value) {
                integerBox.setValue(field.getValue());
            }
        };
        field.addTransientValueChangeListener(listener);
        HandlerRegistration handlerRegistration = new HandlerRegistration() {
            @Override
            public void removeHandler() {
                field.removeTransientValueChangeListener(listener);
            }
        };
        handlerRegistrations.add(handlerRegistration);
        
        return integerBox;
    }
    
    private Widget constructDependenciesWidget(final DependenciesField field, List<HandlerRegistration> handlerRegistrations) {
        final DependenciesFieldWidget widget = new DependenciesFieldWidget(config, style, navigationContainer, readOnly);
        widget.setValue(field);
        
        if (field.getValue() != null && !field.getValue().isEmpty()) {
            widget.setVisible(true);
        } else {
            widget.setVisible(false);
        }
        
        final CtlDependenciesListener listener = new CtlDependenciesListener() {
            @Override
            public void onCtlDependenciesUpdated(
                    List<FqnVersion> ctlDependenciesList) {
                if (ctlDependenciesList != null && !ctlDependenciesList.isEmpty()) {
                    widget.setVisible(true);
                    widget.reload();
                } else {
                    widget.setVisible(false);
                }
                fireChanged();
            }
        };
        
        field.getContext().addCtlDependenciesListener(listener);
        
        HandlerRegistration handlerRegistration = new HandlerRegistration() {
            @Override
            public void removeHandler() {
                field.getContext().removeCtlDependenciesListener(listener);
            }
        };
        handlerRegistrations.add(handlerRegistration);
        
        return widget;
    }
    
    private Widget constructEnumWidget(final EnumField field, List<HandlerRegistration> handlerRegistrations) {
        FormEnumListBox enumBox = new FormEnumListBox(style, field.getDisplayPrompt());
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
        checkBox.setTitle(field.getDisplayPrompt());
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
        ArrayFieldWidget arrayWidget = new ArrayFieldWidget(config, style, navigationContainer, readOnly);
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
        UnionFieldWidget unionWidget = new UnionFieldWidget(config, style, navigationContainer, readOnly);
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
        Label emptyLabel = new Label(Utils.constants.empty());
        label.setStyleName(style.fieldNotes());
        label.getElement().getStyle().setPaddingRight(10, Unit.PX);
        final boolean isEmptyRecord = field.getFieldType() == FieldType.RECORD && field.isNull();
        final Button openButton = new Button(isEmptyRecord ? Utils.constants.create() : Utils.constants.open());
        openButton.addStyleName(style.buttonSmall());
        final Button deleteButon = new Button(Utils.constants.delete());
        deleteButon.addStyleName(style.buttonSmall());        
        deleteButon.getElement().getStyle().setMarginLeft(10, Unit.PX);
        
        boolean isReadOnly = readOnly || field.isReadOnly();
        boolean showEmptyLabel = isReadOnly && field.isNull();
        
        if (showEmptyLabel) {
            openButton.setVisible(false);
            deleteButon.setVisible(false);
            emptyLabel.setVisible(true);
        } else {
            openButton.setVisible(true);
            emptyLabel.setVisible(false);
            handlerRegistrations.add(openButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (field.getFieldType() == FieldType.RECORD && field.isNull()) {
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
            }));            
            handlerRegistrations.add(deleteButon.addClickHandler(new ClickHandler() {
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
            }));            
            deleteButon.setVisible(field.getFieldType() == FieldType.RECORD &&  
                    !field.isNull() && !isReadOnly);
        }
 
        nestedWidget.add(label);
        nestedWidget.add(emptyLabel);
        nestedWidget.add(openButton);
        nestedWidget.add(deleteButon);
        return nestedWidget;
    }
    
    private class FieldWidgetLabel extends FlowPanel implements ChangeListener  {
        
        private CheckBox overrideBox;
        
        public FieldWidgetLabel(final FormField field) {
            this.getElement().getStyle().setPosition(Position.RELATIVE);
            this.getElement().getStyle().setProperty("display", "flex");
            Label label = new Label(field.getDisplayName());
            if (field.isOverride()) {
                overrideBox = new CheckBox();
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
