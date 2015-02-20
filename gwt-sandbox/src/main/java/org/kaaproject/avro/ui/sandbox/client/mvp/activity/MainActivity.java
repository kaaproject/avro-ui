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

package org.kaaproject.avro.ui.sandbox.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.sandbox.client.AvroUiSandbox;
import org.kaaproject.avro.ui.sandbox.client.mvp.ClientFactory;
import org.kaaproject.avro.ui.sandbox.client.mvp.place.MainPlace;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.FormConstructorView;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.MainView;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class MainActivity extends AbstractActivity  {

    private final ClientFactory clientFactory;
    private MainView view;
    
    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    public MainActivity(MainPlace place,
            ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getMainView();
        bind(eventBus);
        containerWidget.setWidget(view.asWidget());
    }
    
    @Override
    public void onStop() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
    }
    
    private void bind(final EventBus eventBus) {
        registrations.add(view.getGenerateFormButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doGenerateRecordForm();
            }
        }));
        FormConstructorView schemaConstructor = view.getSchemaConstructorView();
        registrations.add(schemaConstructor.getShowFormJsonButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showSchemaJson();
            }
        }));
        registrations.add(schemaConstructor.getUploadButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                uploadSchemaFromJson();
            }
        }));
        
        FormConstructorView recordConstructor = view.getRecordConstructorView();
        registrations.add(recordConstructor.getShowFormJsonButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showRecordJson();
            }
        }));
        registrations.add(recordConstructor.getUploadButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                uploadRecordFromJson();
            }
        }));        
        registrations.add(view.getResetButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                view.reset();
                loadEmptySchemaForm();
            }
        }));
       
        view.reset();
        
        loadEmptySchemaForm();
    }
  
    private void doGenerateRecordForm() {
        RecordField schemaForm = view.getSchemaConstructorView().getValue();
        AvroUiSandbox.getAvroUiSandboxService().getJsonStringFromSchemaForm(schemaForm, 
                new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        view.setErrorMessage(Utils.getErrorMessage(caught));
                    }

                    @Override
                    public void onSuccess(String result) {
                        view.clearMessages();
                        AvroUiSandbox.getAvroUiSandboxService().generateFormFromSchema(result, 
                                new BusyAsyncCallback<RecordField>() {
                                    @Override
                                    public void onSuccessImpl(RecordField result) {
                                        view.clearMessages();
                                        view.getRecordConstructorView().setValue(result, true);
                                    }
                                    
                                    @Override
                                    public void onFailureImpl(Throwable caught) {
                                        view.setErrorMessage(Utils.getErrorMessage(caught));
                                    }
                                });
                    }
        });
    }
    
    private void showRecordJson() {
        RecordField recordField = view.getRecordConstructorView().getValue();
        AvroUiSandbox.getAvroUiSandboxService().getJsonStringFromRecord(recordField, 
                new BusyAsyncCallback<String>() {
            @Override
            public void onSuccessImpl(String result) {
                view.clearMessages();
                view.getRecordConstructorView().setFormJson(result);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void uploadRecordFromJson() {
        RecordField schemaForm = view.getSchemaConstructorView().getValue();
        AvroUiSandbox.getAvroUiSandboxService().getJsonStringFromSchemaForm(schemaForm, 
                new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        view.setErrorMessage(Utils.getErrorMessage(caught));
                    }

                    @Override
                    public void onSuccess(String avroSchema) {
                        view.clearMessages();
                        String json = view.getRecordConstructorView().getFormJson().getValue();
                        
                        AvroUiSandbox.getAvroUiSandboxService().generateFormDataFromJson(avroSchema, 
                                json, new BusyAsyncCallback<RecordField>() {
                            @Override
                            public void onSuccessImpl(RecordField result) {
                                view.clearMessages();
                                view.getRecordConstructorView().setValue(result, true);
                            }
                            @Override
                            public void onFailureImpl(Throwable caught) {
                                view.setErrorMessage(Utils.getErrorMessage(caught));
                            }
                        });
                    }
        });
    }
    
    private void showSchemaJson() {
        RecordField schemaField = view.getSchemaConstructorView().getValue();
        AvroUiSandbox.getAvroUiSandboxService().getJsonStringFromSchemaForm(schemaField, 
                new BusyAsyncCallback<String>() {
            @Override
            public void onSuccessImpl(String result) {
                view.clearMessages();
                view.getSchemaConstructorView().setFormJson(result);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void uploadSchemaFromJson() {
        String avroSchema = view.getSchemaConstructorView().getFormJson().getValue();
        AvroUiSandbox.getAvroUiSandboxService().generateSchemaFormFromSchema(avroSchema,
                new BusyAsyncCallback<RecordField>() {
            @Override
            public void onSuccessImpl(RecordField result) {
                view.clearMessages();
                view.getSchemaConstructorView().setValue(result, true);
            }
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void loadEmptySchemaForm() {
        AvroUiSandbox.getAvroUiSandboxService().getEmptySchemaForm(new BusyAsyncCallback<RecordField>() {
            @Override
            public void onSuccessImpl(RecordField result) {
                view.clearMessages();
                view.getSchemaConstructorView().setValue(result, true);
            }
            
            @Override
            public void onFailureImpl(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
}
