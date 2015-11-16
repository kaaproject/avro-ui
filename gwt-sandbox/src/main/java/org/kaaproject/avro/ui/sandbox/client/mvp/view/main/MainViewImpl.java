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

package org.kaaproject.avro.ui.sandbox.client.mvp.view.main;


import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.avro.ui.sandbox.client.AvroUiSandboxResources.AvroUiSandboxStyle;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.FormConstructorView;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.MainView;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.form.FormConstructorViewImpl;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainViewImpl extends Composite implements MainView {

    interface MainViewImplUiBinder extends UiBinder<Widget, MainViewImpl> { }
    private static MainViewImplUiBinder uiBinder = GWT.create(MainViewImplUiBinder.class);

    @UiField public FlexTable detailsTable;
    @UiField (provided=true) public final AlertPanel errorPanel;
    @UiField (provided=true) public final AlertPanel infoPanel;
    @UiField (provided=true) public final AvroUiSandboxStyle avroUiSandboxStyle;
    @UiField public FlowPanel footer;

    private FormConstructorView schemaConstructorView;
    private FormConstructorView recordConstructorView;

    public MainViewImpl() {
        errorPanel = new AlertPanel(Type.ERROR);
        infoPanel =  new AlertPanel(Type.INFO);
        avroUiSandboxStyle = Utils.avroUiSandboxStyle;
        
        initWidget(uiBinder.createAndBindUi(this));

        detailsTable.setWidth("95%");
        detailsTable.getElement().getStyle().setPaddingTop(0, Unit.PX);
        detailsTable.setCellPadding(0);

        detailsTable.getColumnFormatter().setWidth(0, "50%");
        detailsTable.getColumnFormatter().setWidth(1, "50%");
        
        detailsTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        
        schemaConstructorView = new FormConstructorViewImpl();
        CaptionPanel schemaConstructorPanel = new CaptionPanel(Utils.constants.schemaConstructor());
        schemaConstructorPanel.add(schemaConstructorView);

        detailsTable.setWidget(1, 0, schemaConstructorPanel);

        recordConstructorView = new FormConstructorViewImpl();
        CaptionPanel recordConstructorPanel = new CaptionPanel(Utils.constants.recordConstructor());
        recordConstructorPanel.add(recordConstructorView);
        
        detailsTable.setWidget(1, 1, recordConstructorPanel);
        clearMessages();
    }

    @Override
    public void reset() {
        clearMessages();
        schemaConstructorView.reset();
        recordConstructorView.reset();
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

    @Override
    public FormConstructorView getSchemaConstructorView() {
        return schemaConstructorView;
    }

    @Override
    public FormConstructorView getRecordConstructorView() {
        return recordConstructorView;
    }
}
