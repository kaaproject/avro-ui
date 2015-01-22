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

package org.kaaproject.avro.ui.sandbox.client.mvp.activity;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.sandbox.client.AvroUiSandbox;
import org.kaaproject.avro.ui.sandbox.client.mvp.ClientFactory;
import org.kaaproject.avro.ui.sandbox.client.mvp.place.MainPlace;
import org.kaaproject.avro.ui.sandbox.client.mvp.view.MainView;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class MainActivity extends AbstractActivity implements MainView.Presenter {

    private final ClientFactory clientFactory;
    private MainPlace place;
    private MainView view;
    
    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    
    public MainActivity(MainPlace place,
            ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view = clientFactory.getMainView();
        view.setPresenter(this);
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
    
    @Override
    public void goTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    private void bind(final EventBus eventBus) {
        registrations.add(view.getGenerateFormButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doGenerateForm();
            }
        }));
        registrations.add(view.getShowRecordJsonButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showRecordJson();
            }
        }));
        registrations.add(view.getUploadRecordFromJsonButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                view.showUploadJson();
            }
        }));
        registrations.add(view.getUploadButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                uploadRecordFromJson();
            }
        }));
        view.reset();
    }
  
    private void doGenerateForm() {
        String avroSchema = view.getSchema().getValue();
        AvroUiSandbox.getAvroUiSandboxService().generateFormFromSchema(avroSchema, new AsyncCallback<RecordField>() {
            @Override
            public void onSuccess(RecordField result) {
                view.clearMessages();
                view.getSchemaForm().setValue(result, true);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void showRecordJson() {
        RecordField recordField = view.getSchemaForm().getValue();
        AvroUiSandbox.getAvroUiSandboxService().getJsonStringFromRecord(recordField, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                view.clearMessages();
                view.setRecordJson(result);
            }
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
    
    private void uploadRecordFromJson() {
        String avroSchema = view.getSchema().getValue();
        String json = view.getRecordJson().getValue();
        
        AvroUiSandbox.getAvroUiSandboxService().generateFormDataFromJson(avroSchema, json, new AsyncCallback<RecordField>() {
            @Override
            public void onSuccess(RecordField result) {
                view.clearMessages();
                view.getSchemaForm().setValue(result, true);
            }
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(Utils.getErrorMessage(caught));
            }
        });
    }
   
    
}
