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

package org.kaaproject.avro.ui.gwt.client.widget.nav;

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class NavWidget extends ListItem implements HasClickHandlers {
    
    public static final String EMPTY_HREF = "javascript:;";
    
    private final Anchor anchor = new Anchor();

    public NavWidget() {
        super.add(anchor);
        setEmptyHref();
    }

    public NavWidget(Widget w) {
        this();
        add(w);
    }
    
    public void setEmptyHref() {
        setHref(EMPTY_HREF);
    }

    public void setHref(String href) {
        anchor.setHref(href);
    }

    public void setText(String text) {
        anchor.setText(text);
    }

    public String getText() {
        return anchor.getText();
    }

    public void setActive(boolean active) {
        if (active)
            addStyleName(Utils.avroUiStyle.active());
        else
            removeStyleName(Utils.avroUiStyle.active());
    }

    public boolean isActive() {
        return this.getStyleName().contains(Utils.avroUiStyle.active());
    }

    public void setDisabled(boolean disabled) {
        anchor.setEnabled(!disabled);
    }

    public boolean isDisabled() {
        return !anchor.isEnabled();
    }

    public Anchor getAnchor() {
        return anchor;
    }


    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return anchor.addDomHandler(handler, ClickEvent.getType());
    }

    @UiChild(tagname = "widget")
    public void addWidget(Widget w) {
        super.add(w);
    }

    public void setTarget(String target) {
        anchor.setTarget(target);
    }

    public String getTarget() {
        return anchor.getTarget();
    }

    public void setName(String name) {
        anchor.setName(name);
    }

    public String getName() {
        return anchor.getName();
    }

}