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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.UnionField;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class UnionFieldWidget extends AbstractFieldWidget<UnionField> implements ValueChangeHandler<FormField> {

    private List<HandlerRegistration> recordTableRegistrations = new ArrayList<HandlerRegistration>();
    private FieldWidgetPanel fieldWidgetPanel;
    private FlexTable recordTable;
    
    public UnionFieldWidget(NavigationContainer container, boolean readOnly) {
        super(container, readOnly);
    }
    
    public UnionFieldWidget(AvroUiStyle style, NavigationContainer container, boolean readOnly) {
        super(style, container, readOnly);
    }

    @Override
    protected Widget constructForm() {
        
        fieldWidgetPanel = new FieldWidgetPanel(style, value, readOnly, value.getValue() != null);
        if (value.isOverride() && !readOnly && !value.isReadOnly()) {
            fieldWidgetPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    fireChanged();
                }
            });
        }
        
        recordTable = new FlexTable();
        fieldWidgetPanel.setWidth("650px");
        fieldWidgetPanel.setContent(recordTable);

        boolean isReadOnly = readOnly || value.isReadOnly();
        
        final FormValuesListBox formValuesBox = new FormValuesListBox(style, isReadOnly ? null : value.getDisplayHint());
        formValuesBox.setEnabled(!isReadOnly);
        fieldWidgetPanel.setLegendWidget(formValuesBox);
        
        value.finalizeMetadata();
        
        if (!isReadOnly) {
            registrations.add(formValuesBox.addValueChangeHandler(this));
            if (!value.isOptional()) {
                formValuesBox.setValue(value.getValue());
            }
            formValuesBox.setAcceptableValues(value.getAcceptableValues());
            if (value.isOptional()) {
                formValuesBox.setValue(value.getValue());
            }
        } else {
            formValuesBox.setValue(value.getValue());
        }
        constructFormData(recordTable, value.getValue(), recordTableRegistrations);
        return fieldWidgetPanel;
    }
    
    public void setOpen(boolean open) {
        if (value != null && !value.isOverride()) {
            boolean isOpen = value.getValue() == null ? false : open;
            fieldWidgetPanel.setOpen(isOpen, false);
        }
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<FormField> event) {
        for (HandlerRegistration registration : recordTableRegistrations) {
            registration.removeHandler();
        }
        recordTableRegistrations.clear();
        
        FormField formField = event.getValue();
        value.setValue(formField);
        
        if (formField == null) {
            recordTable.clear();
        }
        else {
            constructFormData(recordTable, formField, recordTableRegistrations);
        }
        fieldWidgetPanel.setValue(true, false, true);
        fireChanged();
    }
    
    static class FormValuesListBox extends ExtendedValueListBox<FormField> {

        public FormValuesListBox(AvroUiStyle style, String promptText) {
            super(new FormFieldRenderer(), new FormFieldKeyProvider(), style, promptText);
        }
        
        public ListBox getListBox() {
            return (ListBox) getWidget();
        }
        
    }
    
    static class FormFieldKeyProvider implements ProvidesKey<FormField> {

        @Override
        public Object getKey(FormField item) {
            return item != null ? item.getFieldName() : null;
        }
        
    }
    
    static class FormFieldRenderer implements Renderer<FormField> {

        @Override
        public String render(FormField object) {
            return object != null ? object.getDisplayName() : "";
        }

        @Override
        public void render(FormField object, Appendable appendable)
                throws IOException {
            appendable.append(render(object));
        }
    }

}
