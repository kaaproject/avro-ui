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
import org.kaaproject.avro.ui.sandbox.client.mvp.view.widget.AlertPanel;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.widget.AlertPanel.Type;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
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
            }
        });
        
        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reset();
            }
        });
        
        detailsTable.setWidget(0, 0, schemaTable);
        
        schemaForm = new RecordFieldWidget();
        schemaForm.setWidth(FULL_WIDTH);
        CaptionPanel schemaFormPanel = new CaptionPanel(Utils.constants.generatedForm());
        schemaFormPanel.add(schemaForm);
        schemaFormPanel.setWidth(FULL_WIDTH);
        schemaFormPanel.getElement().getStyle().setPropertyPx("minHeight", 300);
        
        detailsTable.setWidget(0, 1, schemaFormPanel);
        detailsTable.getFlexCellFormatter().setRowSpan(0, 1, 2);

        clearMessages();
    }

    @Override
    public void reset() {
        clearMessages();
        resetImpl();
        generateFormButton.setEnabled(false);
        resetButton.setEnabled(false);
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


}
