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

package org.kaaproject.avro.ui.sandbox.client.mvp.view.main;


import static org.kaaproject.avro.ui.sandbox.client.util.Utils.isNotBlank;

import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.MainView;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MainViewImpl extends Composite implements MainView, InputEventHandler {

    interface MainViewImplUiBinder extends UiBinder<Widget, MainViewImpl> { }
    private static MainViewImplUiBinder uiBinder = GWT.create(MainViewImplUiBinder.class);

    private static final String REQUIRED = "required";

    private static final int SCHEMA_BOX_SIZE = 65536;
    
    private static final String FULL_WIDTH = "100%";
    private static final String HALF_WIDTH = "50%";

    @UiField public Label titleLabel;
    @UiField public FlexTable detailsTable;
    @UiField (provided=true) public AlertPanel errorPanel;
    @UiField (provided=true) public AlertPanel infoPanel;
    @UiField public FlowPanel footer;

    private Presenter presenter;
    private boolean hasChanged = false;
    
    private SizedTextArea schema;
    private Button generateFormButton;
    private Button resetButton;
    private RecordFieldWidget schemaForm;
    private Button showRecordJsonButton;
    private SizedTextArea recordJsonArea;
    private CaptionPanel jsonRecordPanel;
    
    private Button uploadRecordFromJsonButton;
    private Button uploadButton;

    public MainViewImpl() {
        errorPanel = new AlertPanel(Type.ERROR);
        infoPanel =  new AlertPanel(Type.INFO);
        initWidget(uiBinder.createAndBindUi(this));

        titleLabel.setText("Main console");
        
        detailsTable.setWidth("95%");
        detailsTable.setCellPadding(6);

        detailsTable.getColumnFormatter().setWidth(0, HALF_WIDTH);
        detailsTable.getColumnFormatter().setWidth(1, HALF_WIDTH);
        
        FlexTable schemaTable = new FlexTable();
        
        schemaTable.getColumnFormatter().setWidth(0, "20%");
        schemaTable.getColumnFormatter().setWidth(1, "80%");

        schema = new SizedTextArea(SCHEMA_BOX_SIZE);
        schema.setWidth(FULL_WIDTH);
        schema.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
        schema.addInputHandler(this);
        
        Label schemaLabel = new Label(Utils.constants.avroJsonSchema());
        schemaLabel.addStyleName(REQUIRED);
        schemaTable.setWidget(0, 0, schemaLabel);
        schemaTable.setWidget(0, 1, schema);
        
        generateFormButton = new Button(Utils.constants.generateForm());
        resetButton = new Button(Utils.constants.reset());
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(generateFormButton);
        
        schemaTable.setWidget(1, 1, buttonsPanel);
        
        schemaTable.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsPanel.getElement().getParentElement().getStyle().setPaddingTop(15, Unit.PX);
        
        generateFormButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                generateFormButton.setEnabled(false);
                hasChanged = false;
                schemaForm.setValue(null);
                showRecordJsonButton.setEnabled(false);
                jsonRecordPanel.setVisible(false);
                uploadButton.setEnabled(false);
                uploadButton.setVisible(false);
            }
        });
        
        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reset();
            }
        });
        
        detailsTable.setWidget(0, 0, schemaTable);
        
        FlexTable schemaFormTable = new FlexTable();
        schemaFormTable.setWidth(FULL_WIDTH);
        
        int row = 0;
        
        HorizontalPanel toolbarPanel = new HorizontalPanel();
        
        CheckBox readOnlyCheckBox = new CheckBox("Read only");
        readOnlyCheckBox.setWidth(FULL_WIDTH);
        readOnlyCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                schemaForm.setReadOnly(event.getValue());
            }
        });
        
        toolbarPanel.add(readOnlyCheckBox);
        Button showDisplayStringButton = new Button("View display string");
        showDisplayStringButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        showDisplayStringButton.addStyleName("b-app-button-small");
        showDisplayStringButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                RecordField field = schemaForm.getValue();
                String displayString = field != null ? field.getDisplayString() : "null";
                Window.alert("Display string:\n" + displayString);
            }
        });
        toolbarPanel.add(showDisplayStringButton);
        
        schemaFormTable.setWidget(row++, 0, toolbarPanel);
        
        schemaForm = new RecordFieldWidget();
        schemaForm.setWidth(FULL_WIDTH);
        
        schemaForm.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                 fireChanged();
            }
        });
        
        CaptionPanel schemaFormPanel = new CaptionPanel(Utils.constants.generatedForm());
        schemaFormPanel.add(schemaForm);
        schemaFormPanel.setWidth(FULL_WIDTH);
        schemaFormPanel.getElement().getStyle().setPropertyPx("minHeight", 300);
        
        schemaFormTable.setWidget(row++, 0, schemaFormPanel);
        
        showRecordJsonButton = new Button(Utils.constants.showRecordJson());
        showRecordJsonButton.setEnabled(false);
        
        uploadRecordFromJsonButton = new Button(Utils.constants.uploadRecordFromJson());
        uploadRecordFromJsonButton.setEnabled(false);

        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(showRecordJsonButton);
        buttonsPanel.add(uploadRecordFromJsonButton);
        
        schemaFormTable.setWidget(row++, 0, buttonsPanel);
        
        schemaFormTable.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.getElement().getParentElement().getStyle().setPaddingTop(15, Unit.PX);

        recordJsonArea = new SizedTextArea(-1);
        recordJsonArea.setWidth(FULL_WIDTH);
        recordJsonArea.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 300);
        recordJsonArea.getTextArea().setReadOnly(true);
        recordJsonArea.addInputHandler(new InputEventHandler() {
            @Override
            public void onInputChanged(InputEvent event) {
               boolean enableUpload = isNotBlank(recordJsonArea.getValue());
               uploadButton.setEnabled(enableUpload);
            }
        });
        jsonRecordPanel = new CaptionPanel(Utils.constants.generatedRecordJson());
        jsonRecordPanel.add(recordJsonArea);
        
        schemaFormTable.setWidget(row++, 0, jsonRecordPanel);
        jsonRecordPanel.setVisible(false);
        
        uploadButton = new Button(Utils.constants.upload());
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);
        
        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.add(uploadButton);
        
        schemaFormTable.setWidget(row++, 0, buttonsPanel);
        schemaFormTable.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
        
        detailsTable.setWidget(0, 1, schemaFormTable);
        detailsTable.getFlexCellFormatter().setRowSpan(0, 1, 2);
        
        clearMessages();
    }

    @Override
    public void reset() {
        clearMessages();
        resetImpl();
        generateFormButton.setEnabled(false);
        resetButton.setEnabled(false);
        showRecordJsonButton.setEnabled(false);
        uploadRecordFromJsonButton.setEnabled(false);
        uploadButton.setEnabled(false);
        uploadButton.setVisible(false);
        jsonRecordPanel.setVisible(false);
        hasChanged = false;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    @Override
    public void clearMessages() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
        infoPanel.setMessage("");
        infoPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }
    
    @Override
    public void setInfoMessage(String message) {
        infoPanel.setMessage(message);
        infoPanel.setVisible(true);
    }

    private void resetImpl() {
        schema.setValue("");
        schemaForm.setValue(null);
        recordJsonArea.setValue("");
    }

    @Override
    public void onInputChanged(InputEvent event) {
        fireChanged();
    }
    
    private void fireChanged() {
        boolean valid = validate();
        generateFormButton.setEnabled(valid);
        hasChanged = true;
        resetButton.setEnabled(valid || schemaForm.getValue() != null);
        uploadRecordFromJsonButton.setEnabled(schemaForm.getValue() != null);
        boolean schemaFormValid = schemaForm.validate();
        showRecordJsonButton.setEnabled(schemaFormValid);
    }

    private boolean validate() {
        return isNotBlank(schema.getValue());
    }

    @Override
    public HasClickHandlers getGenerateFormButton() {
        return generateFormButton;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public HasValue<String> getSchema() {
        return schema;
    }

    @Override
    public HasValue<RecordField> getSchemaForm() {
        return schemaForm;
    }

    @Override
    public HasClickHandlers getShowRecordJsonButton() {
        return showRecordJsonButton;
    }
    
    @Override
    public HasClickHandlers getUploadRecordFromJsonButton() {
        return uploadRecordFromJsonButton;
    }

    @Override
    public HasClickHandlers getUploadButton() {
        return uploadButton;
    }

    @Override
    public void setRecordJson(String json) {
        recordJsonArea.setValue(json);
        jsonRecordPanel.setVisible(true);
        jsonRecordPanel.setCaptionText(Utils.constants.generatedRecordJson());
        recordJsonArea.getTextArea().setReadOnly(true);
        uploadButton.setVisible(false);
    }

    @Override
    public void showUploadJson() {
        recordJsonArea.setValue("");
        jsonRecordPanel.setVisible(true);
        jsonRecordPanel.setCaptionText(Utils.constants.recordJsonToUpload());
        recordJsonArea.getTextArea().setReadOnly(false);
        uploadButton.setVisible(true);
    }

    @Override
    public HasValue<String> getRecordJson() {
        return recordJsonArea;
    }

}
