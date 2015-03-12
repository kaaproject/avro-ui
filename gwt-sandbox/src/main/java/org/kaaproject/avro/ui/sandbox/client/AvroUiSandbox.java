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

package org.kaaproject.avro.ui.sandbox.client;

import org.kaaproject.avro.ui.sandbox.client.layout.AppLayout;
import org.kaaproject.avro.ui.sandbox.client.mvp.ClientFactory;
import org.kaaproject.avro.ui.sandbox.client.mvp.activity.AvroUiSandboxActivityMapper;
import org.kaaproject.avro.ui.sandbox.client.mvp.activity.HeaderActivityMapper;
import org.kaaproject.avro.ui.sandbox.client.mvp.place.AvroUiSandboxPlaceHistoryMapper;
import org.kaaproject.avro.ui.sandbox.client.mvp.place.MainPlace;
import org.kaaproject.avro.ui.sandbox.client.util.Utils;
import org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxServiceAsync;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;

public class AvroUiSandbox implements EntryPoint {

    private static AvroUiSandboxServiceAsync avroUiSandboxService = AvroUiSandboxServiceAsync.Util.getInstance();

    private AppLayout appWidget = new AppLayout();

    @Override
    public void onModuleLoad() {
        init();
    }
    
    public static AvroUiSandboxServiceAsync getAvroUiSandboxService() {
        return avroUiSandboxService;
    }

    private void init() {
        Utils.injectSandboxStyles();

        ClientFactory clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();

        PlaceController placeController = clientFactory.getPlaceController();

        ActivityMapper headerActivityMapper = new HeaderActivityMapper(clientFactory);
        ActivityManager headerActivityManager = new ActivityManager(headerActivityMapper, eventBus);
        headerActivityManager.setDisplay(appWidget.getAppHeaderHolder());

        ActivityMapper appActivityMapper = new AvroUiSandboxActivityMapper(clientFactory);
        ActivityManager appActivityManager = new ActivityManager(appActivityMapper, eventBus);
        appActivityManager.setDisplay(appWidget.getAppContentHolder());

        PlaceHistoryMapper historyMapper = GWT.create(AvroUiSandboxPlaceHistoryMapper.class);

        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        Place place = new MainPlace();

        historyHandler.register(placeController, eventBus, place);

        RootLayoutPanel.get().add(appWidget);

        // Goes to the place represented on URL else default place
        historyHandler.handleCurrentHistory();
    }

}
