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

package org.kaaproject.avro.ui.gwt.client.widget.grid;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class ColumnFilterEvent extends GwtEvent<ColumnFilterEvent.Handler> {

    public static interface Handler extends EventHandler {

        void onColumnFilter(ColumnFilterEvent event);

    }
    
    public static interface HasColumnFilterEventHandlers extends HasHandlers {

        HandlerRegistration addColumnFilterEventHandler(ColumnFilterEvent.Handler handler);

    }

    private static Type<Handler> TYPE;

    public static ColumnFilterEvent fire(HasColumnFilterEventHandlers source) {
        ColumnFilterEvent event = new ColumnFilterEvent();
        if (TYPE != null) {
            source.fireEvent(event);
        }
        return event;
    }

    public static Type<Handler> getType() {
        if (TYPE == null) {
            TYPE = new Type<Handler>();
        }
        return TYPE;
    }

    protected ColumnFilterEvent() {
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onColumnFilter(this);
    }
}