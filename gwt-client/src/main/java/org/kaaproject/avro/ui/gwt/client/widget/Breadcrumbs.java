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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

public class Breadcrumbs extends UnorderedList {
    
    private WidgetCollection children = new WidgetCollection(this);

    private List<Divider> dividerList = new ArrayList<Divider>();
    
    private String divider = "/";
    
    private static class Divider extends ComplexPanel implements HasWidgets {
        
        @SuppressWarnings("deprecation")
        public Divider(String divider) {
            setElement(DOM.createElement("span"));
            setStyleName(Utils.avroUiStyle.divider());
            setDivider(divider);
        }

        public void setDivider(String divider) {
            getElement().setInnerText(divider);
        }
    }
    
    public Breadcrumbs() {
        setStyleName(Utils.avroUiStyle.breadcrumb());
    }
    
    public Breadcrumbs(Widget... widgets) {
        this();
        for (Widget widget : widgets) {
            this.add(widget);
        }
    }
    
    public Breadcrumbs(String divider) {
        this();
        this.setDivider(divider);
    }
    
    public void setDivider(String divider) {
        if (divider == null || divider.isEmpty()) {
            this.divider = "/";
        } else {
            this.divider = divider;
        }
        for (Divider dividerWidget : dividerList) {
            dividerWidget.setDivider(this.divider);
        }
    }
    
    @Override
    protected void onAttach() {
        if (!isOrWasAttached() && children.size() > 0) {
            Widget lastWidget = children.get(children.size() - 1);
            for (Widget w : children) {
                ListItem item = lastWidget.equals(w) ? change2TextListItem(w)
                        : getOrCreateListItem(w);
                super.add(item);
            }
        }
        super.onAttach();
    }
    
    @Override
    public void add(Widget w) {
        w.removeStyleName(Utils.avroUiStyle.active());
        if (!isOrWasAttached()) {
            children.add(w);
            return;
        }
        if (children.size() > 0) {
            // Change last widget 2 Link
            // pygical remove
            super.remove(getWidget(getWidgetCount() - 1));
            ListItem item = getOrCreateListItem(children
                    .get(children.size() - 1));
            super.add(item);
        }
        ListItem newest = change2TextListItem(w);
        super.add(newest);
        children.add(w);
    }

    private ListItem getOrCreateListItem(Widget lastWidget) {
        ListItem item = null;
        Divider dividerWidget = new Divider(divider);
        if (lastWidget instanceof NavWidget) {
            NavWidget w = (NavWidget) lastWidget;
            if (hasDivier(w)) {
                return w;
            } else {
                dividerList.add(dividerWidget);
                w.addWidget(dividerWidget);
                return w;
            }
        } else if (lastWidget instanceof ListItem) {
            item = (ListItem) lastWidget;
        } else {
            item = new ListItem(lastWidget);
        }
        if (hasDivier(item)) {
            return item;
        }
        item.add(dividerWidget);
        dividerList.add(dividerWidget);
        return item;
    }

    private boolean hasDivier(ListItem item) {
        for (Widget w : item) {
            if (w instanceof Divider) {
                return true;
            }
        }
        return false;
    }

    private ListItem change2TextListItem(Widget w) {
        String text = null;
        if (w instanceof HasText) {
            text = ((HasText) w).getText();
        } else {
            text = w.getElement().getInnerText();
        }
        ListItem newest = new ListItem();
        newest.setStyleName(Utils.avroUiStyle.active());
        newest.getElement().appendChild(Document.get().createTextNode(text));
        return newest;
    }

    @Override
    public boolean remove(Widget w) {
        if (!isOrWasAttached() && children.contains(w)) {
            children.remove(w);
            return true;
        }
        if (getWidgetIndex(w) < 0 && !children.contains(w)) {
            return false;
        }
        boolean isLastWidget = (children.indexOf(w) == children.size() - 1)
                || (getWidgetIndex(w) == getWidgetCount() - 1);
        if (getWidgetIndex(w) >= 0 && children.contains(w)) {
            children.remove(w);
            super.remove(w);
        } else if (getWidgetIndex(w) >= 0 && !children.contains(w)) {
            children.remove(getWidgetIndex(w));
            super.remove(w);
        } else if (getWidgetIndex(w) < 0 && children.contains(w)) {
            return remove(getWidget(children.indexOf(w)));
        } else {
            return false;
        }
        if (isLastWidget && getWidgetCount() > 0) {
            Widget l = getWidget(getWidgetCount() - 1);
            super.remove(l);
            super.add(change2TextListItem(l));
        }
        return true;
    }

    @Override
    public void clear() {
        super.clear();
        children = new WidgetCollection(this);
        dividerList.clear();
    }
}
