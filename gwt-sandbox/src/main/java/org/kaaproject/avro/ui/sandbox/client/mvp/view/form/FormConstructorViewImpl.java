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
import org.kaaproject.avro.ui.gwt.client.widget.FormPopup;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.FormConstructorView;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.Window;

public class FormConstructorViewImpl extends FlexTable implements FormConstructorView {

    private static final String FULL_WIDTH = "100%";
    private static final String JSON_PANEL_WIDTH = "600px";
    private static final String UPLOAD_SERVLET_PATH = "servlet/fileUploadServlet";
    private static final int MIN_PANEL_HEIGHT = 565;

    private RecordFieldWidget form;
    
    private CaptionPanel formJsonPanel;
    private SizedTextArea formJsonArea;
    
    private Button showFormJsonButton;
    private Button uploadFormFromJsonButton;
    private Button uploadButton;
    final Button uploadFileButton = new Button(Utils.constants.upload());

    private final FormPopup jsonUploadPopup = new FormPopup();
    private final FormPanel fileUploadForm = new FormPanel();
    private final FileUpload fileUpload = new FileUpload();

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
        form.getElement().getStyle().setPropertyPx("minHeight", MIN_PANEL_HEIGHT);
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

        fileUploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        fileUploadForm.setMethod(FormPanel.METHOD_POST);
        fileUploadForm.setAction(GWT.getModuleBaseURL() + UPLOAD_SERVLET_PATH);

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        fileUploadForm.setWidget(horizontalPanel);

        fileUpload.setName(Utils.constants.uploadFromFile());

        horizontalPanel.add(fileUpload);

        uploadFileButton.setEnabled(false);
        uploadFileButton.getElement().getStyle().setMarginLeft(8, Unit.PX);
        uploadFileButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (!"".equals(fileUpload.getFilename())) {
                    fileUploadForm.submit();
                }
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                if (!"".equals(fileUpload.getFilename())) uploadFileButton.setEnabled(form.getValue() != null);
                else uploadFileButton.setEnabled(false);
            }
        });

        horizontalPanel.add(uploadFileButton);

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(showFormJsonButton);
        buttonsPanel.add(uploadFormFromJsonButton);
        buttonsPanel.add(fileUploadForm);

        setWidget(row++, 0, buttonsPanel);
        
        getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.getElement().getParentElement().getStyle().setPaddingTop(0, Unit.PX);

        formJsonArea = new SizedTextArea(-1);
        formJsonArea.setWidth(FULL_WIDTH);
        formJsonArea.setWidth(JSON_PANEL_WIDTH);
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
        formJsonPanel.getElement().getStyle().setMargin(5, Unit.PX);
        formJsonPanel.add(formJsonArea);

        formJsonPanel.setVisible(false);
        
        uploadButton = new Button(Utils.constants.upload());
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);

        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                jsonUploadPopup.hide();
            }
        });
        jsonUploadPopup.addButton(uploadButton);

        jsonUploadPopup.add(formJsonPanel);

        Button close = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                jsonUploadPopup.hide();
            }
        });
        jsonUploadPopup.addButton(close);
    }

    private void fireChanged() {
        uploadFormFromJsonButton.setEnabled(form.getValue() != null);
        uploadFileButton.setEnabled(form.getValue() != null && !"".equals(fileUpload.getFilename()));
        boolean schemaFormValid = form.validate();
        showFormJsonButton.setEnabled(schemaFormValid);
    }
    
    private void showUploadJson() {
        formJsonArea.setValue("");
        formJsonPanel.setVisible(true);
        formJsonPanel.setCaptionText(Utils.constants.jsonToUpload());
        formJsonArea.getTextArea().setReadOnly(false);
        uploadButton.setVisible(true);

        jsonUploadPopup.center();
        jsonUploadPopup.show();
    }
    
    @Override
    public void reset() {
        form.setValue(null);
        formJsonArea.setValue("");
        showFormJsonButton.setEnabled(false);
        uploadFormFromJsonButton.setEnabled(false);
        uploadFileButton.setEnabled(false);
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);
        formJsonPanel.setVisible(false);
        fileUploadForm.reset();
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

        jsonUploadPopup.center();
        jsonUploadPopup.show();
    }

    @Override
    public HasClickHandlers getShowFormJsonButton() {
        return showFormJsonButton;
    }

    @Override
    public HasClickHandlers getUploadButton() {
        return uploadButton;
    }

    @Override
    public FormPanel getUploadFileForm() {
        return fileUploadForm;
    }
}
