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

package org.kaaproject.avro.ui.gwt.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class UnorderedList extends ComplexPanel implements HasWidgets {
    
    @SuppressWarnings("deprecation")
    public UnorderedList() {
        setElement(DOM.createElement("ul"));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void add(Widget w) {
        add(w, getElement());
    }
    
    public UnorderedList(ListItem... widgets) {
        this();
        for (ListItem li : widgets) {
            add(li);
        }
    }

}
