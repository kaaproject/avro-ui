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

package org.kaaproject.avro.ui.sandbox.client.mvp.view.form;

import static org.kaaproject.avro.ui.sandbox.client.util.Utils.isNotBlank;

import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.FormConstructorView;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class FormConstructorViewImpl extends FlexTable implements FormConstructorView {

    private static final String FULL_WIDTH = "100%";
    
    private RecordFieldWidget form;
    
    private CaptionPanel formJsonPanel;
    private SizedTextArea formJsonArea;
    
    private Button showFormJsonButton;
    private Button uploadFormFromJsonButton;
    private Button uploadButton;
    
    public FormConstructorViewImpl() {
        setWidth(FULL_WIDTH);
        
        int row = 0;
        
        HorizontalPanel toolbarPanel = new HorizontalPanel();        
        CheckBox readOnlyCheckBox = new CheckBox(Utils.constants.read_only());
        readOnlyCheckBox.setWidth(FULL_WIDTH);
        readOnlyCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                form.setReadOnly(event.getValue());
            }
        });
        toolbarPanel.add(readOnlyCheckBox);
        Button showDisplayStringButton = new Button(Utils.constants.view_display_string());
        showDisplayStringButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        showDisplayStringButton.addStyleName(Utils.avroUiStyle.buttonSmall());
        showDisplayStringButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                RecordField field = form.getValue();
                String displayString = field != null ? field.getDisplayString() : "null";
                Window.alert(Utils.constants.display_string() + ":\n" + displayString);
            }
        });
        toolbarPanel.add(showDisplayStringButton);
        setWidget(row++, 0, toolbarPanel);
        
        form = new RecordFieldWidget(new AvroWidgetsConfig.Builder().createConfig());
        
        form.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                fireChanged();
            }
        });
        
        CaptionPanel formPanel = new CaptionPanel(Utils.constants.form());
        formPanel.add(form);
        
        setWidget(row++, 0, formPanel);
        
        showFormJsonButton = new Button(Utils.constants.showJson());
        showFormJsonButton.setEnabled(false);
        
        uploadFormFromJsonButton = new Button(Utils.constants.uploadFromJson());
        uploadFormFromJsonButton.setEnabled(false);
        
        uploadFormFromJsonButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showUploadJson();
            }
        });
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(showFormJsonButton);
        buttonsPanel.add(uploadFormFromJsonButton);
        
        setWidget(row++, 0, buttonsPanel);
        
        getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.getElement().getParentElement().getStyle().setPaddingTop(0, Unit.PX);

        formJsonArea = new SizedTextArea(-1);
        formJsonArea.setWidth(FULL_WIDTH);
        formJsonArea.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
        formJsonArea.getTextArea().setReadOnly(true);
        formJsonArea.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
               boolean enableUpload = isNotBlank(formJsonArea.getValue());
               uploadButton.setEnabled(enableUpload);
            }
        });
        
        formJsonPanel = new CaptionPanel(Utils.constants.generatedJson());
        formJsonPanel.add(formJsonArea);
        
        setWidget(row++, 0, formJsonPanel);
        formJsonPanel.setVisible(false);
        
        uploadButton = new Button(Utils.constants.upload());
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);

        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(uploadButton);

        setWidget(row++, 0, buttonsPanel);
        getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
    }
    
    private void fireChanged() {
        uploadFormFromJsonButton.setEnabled(form.getValue() != null);
        boolean schemaFormValid = form.validate();
        showFormJsonButton.setEnabled(schemaFormValid);
    }
    
    private void showUploadJson() {
        formJsonArea.setValue("");
        formJsonPanel.setVisible(true);
        formJsonPanel.setCaptionText(Utils.constants.jsonToUpload());
        formJsonArea.getTextArea().setReadOnly(false);
        uploadButton.setVisible(true);
    }
    
    @Override
    public void reset() {
        form.setValue(null);
        formJsonArea.setValue("");
        showFormJsonButton.setEnabled(false);
        uploadFormFromJsonButton.setEnabled(false);
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);
        formJsonPanel.setVisible(false);
    }
    
    @Override
    public RecordField getValue() {
        return form.getValue();
    }

    @Override
    public void setValue(RecordField value) {
        form.setValue(value);
    }

    @Override
    public void setValue(RecordField value, boolean fireEvents) {
        form.setValue(value, fireEvents);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<RecordField> handler) {
        return form.addValueChangeHandler(handler);
    }
    
    @Override
    public HasValue<String> getFormJson() {
        return formJsonArea;
    }

    @Override
    public void setFormJson(String json) {
        formJsonArea.setValue(json);
        formJsonPanel.setVisible(true);
        formJsonPanel.setCaptionText(Utils.constants.generatedJson());
        formJsonArea.getTextArea().setReadOnly(true);
        uploadButton.setVisible(false);
    }

    @Override
    public HasClickHandlers getShowFormJsonButton() {
        return showFormJsonButton;
    }

    @Override
    public HasClickHandlers getUploadButton() {
        return uploadButton;
    }

}
