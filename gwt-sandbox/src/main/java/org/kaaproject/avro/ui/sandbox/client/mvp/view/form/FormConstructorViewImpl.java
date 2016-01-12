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

import com.google.gwt.user.client.ui.VerticalPanel;
import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
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
    
    private SizedTextArea jsonArea;
    private CaptionPanel formJsonPanel;

    private Button showJsonButton;
    private Button loadJsonButton;
    final Button uploadButton = new Button(Utils.constants.upload());
    final Button downloadButton = new Button(Utils.constants.saveFile());

    private final FormPanel uploadForm = new FormPanel();
    private final FileUpload fileUpload = new FileUpload();

    private Button generateRecordButton;

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
                fireFormChanged();
            }
        });
        
        CaptionPanel formPanel = new CaptionPanel(Utils.constants.avroUiView());
        form.getElement().getStyle().setPropertyPx("minHeight", MIN_PANEL_HEIGHT);
        formPanel.add(form);
        
        setWidget(row++, 0, formPanel);
        
        showJsonButton = new Button(Utils.constants.showJson());
        showJsonButton.setEnabled(true);

        loadJsonButton = new Button(Utils.constants.loadJson());
        loadJsonButton.setEnabled(false);

        generateRecordButton = new Button(Utils.constants.generateRecordForm());
        generateRecordButton.getElement().getStyle().setProperty("float", "right");

        generateRecordButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                generateRecordButton.setEnabled(false);
            }
        });

        FlexTable buttonTable = new FlexTable();
        buttonTable.setWidth(FULL_WIDTH);

        HorizontalPanel buttonsPanel1 = new HorizontalPanel();
        buttonsPanel1.setSpacing(15);
        buttonsPanel1.add(showJsonButton);
        buttonsPanel1.add(loadJsonButton);
        HorizontalPanel buttonsPanel2 = new HorizontalPanel();
        buttonsPanel2.setSpacing(15);
        buttonsPanel2.add(generateRecordButton);

        buttonTable.setWidget(0, 0, buttonsPanel1);
        buttonTable.setWidget(0, 1, buttonsPanel2);
        setWidget(row++, 0, buttonTable);

        buttonTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        buttonTable.getElement().getParentElement().getStyle().setPaddingTop(0, Unit.PX);

        jsonArea = new SizedTextArea(-1);
        jsonArea.getTextArea().setWidth(JSON_PANEL_WIDTH);
        jsonArea.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
        jsonArea.setVisible(false);
        jsonArea.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
                fireChanged();
            }
        });

        formJsonPanel = new CaptionPanel(Utils.constants.jsonView());
        formJsonPanel.getElement().getStyle().setMargin(5, Unit.PX);
        VerticalPanel jsonAreaPanel = new VerticalPanel();

        jsonAreaPanel.add(jsonArea);
        jsonAreaPanel.add(uploadForm);
        formJsonPanel.add(jsonAreaPanel);

        formJsonPanel.setVisible(false);

        setWidget(row, 0, formJsonPanel);

        downloadButton.setEnabled(false);

        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadForm.setMethod(FormPanel.METHOD_POST);
        uploadForm.setAction(GWT.getModuleBaseURL() + UPLOAD_SERVLET_PATH);

        FlexTable fileOpsTable = new FlexTable();
        fileOpsTable.setWidth(JSON_PANEL_WIDTH);
        fileOpsTable.setCellSpacing(8);

        int column = 0;
        uploadForm.setWidget(fileOpsTable);
        fileUpload.setName(Utils.constants.uploadFromFile());
        fileOpsTable.setWidget(0, column++, uploadButton);
        fileOpsTable.setWidget(0, column, fileUpload);
        fileOpsTable.getFlexCellFormatter().setVerticalAlignment(0, column++, HasVerticalAlignment.ALIGN_MIDDLE);

        uploadButton.setEnabled(false);
        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (!"".equals(fileUpload.getFilename())) {
                    uploadForm.submit();
                }
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                if (!"".equals(fileUpload.getFilename())) {
                    uploadButton.setEnabled(true);
                } else {
                    uploadButton.setEnabled(false);
                }
            }
        });

        fileOpsTable.setWidget(0, column, downloadButton);
        fileOpsTable.getFlexCellFormatter().setHorizontalAlignment(0, column, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    private void fireFormChanged() {
        generateRecordButton.setEnabled(form.getValue() != null
                && form.getValue().isValid());
        loadJsonButton.setEnabled(form.getValue() != null
                && jsonArea.getValue() != null && !jsonArea.getValue().isEmpty());
    }

    @Override
    public void fireChanged() {
        showJsonButton.setEnabled(true);
        boolean isNotEmpty = jsonArea.getValue() != null && !jsonArea.getValue().isEmpty();
        loadJsonButton.setEnabled(form.getValue() != null && isNotEmpty);
        downloadButton.setEnabled(isNotEmpty);
        uploadButton.setEnabled(fileUpload.getFilename() != null && !fileUpload.getFilename().isEmpty());
    }
    
    @Override
    public void reset() {
        form.setValue(null);
        jsonArea.setValue("");
        showJsonButton.setEnabled(true);
        loadJsonButton.setEnabled(false);
        uploadButton.setEnabled(false);
        downloadButton.setEnabled(false);
        formJsonPanel.setVisible(false);
        uploadForm.reset();
        generateRecordButton.setEnabled(false);
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
        return jsonArea;
    }

    @Override
    public void setFormJson(String json) {
        jsonArea.setValue(json);
        jsonArea.setVisible(true);
        jsonArea.getTextArea().setReadOnly(false);
        formJsonPanel.setVisible(true);
        boolean isNotEmpty = json != null && !json.isEmpty();
        loadJsonButton.setEnabled(form.getValue() != null && isNotEmpty);
        downloadButton.setEnabled(isNotEmpty);
    }

    @Override
    public HasClickHandlers getShowJsonButton() {
        return showJsonButton;
    }

    @Override
    public FormPanel getUploadFileForm() {
        return uploadForm;
    }

    @Override
    public Button getGenerateRecordButton() {
        return generateRecordButton;
    }

    @Override
    public HasClickHandlers getUploadJSONButton() {
        return loadJsonButton;
    }

    @Override
    public Button getDownloadJsonButton() {
        return downloadButton;
    }
}
