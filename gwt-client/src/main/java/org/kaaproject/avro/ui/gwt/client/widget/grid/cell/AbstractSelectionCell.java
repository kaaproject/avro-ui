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

package org.kaaproject.avro.ui.gwt.client.widget.grid.cell;

import java.util.List;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;

public abstract class AbstractSelectionCell<T, K> extends
        AbstractInputCell<T, T> {

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{0}</option>")
        SafeHtml deselected(String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
        SafeHtml selected(String option);
    }

    private static Template template;

    private final Renderer<T> renderer;

    public AbstractSelectionCell(Renderer<T> renderer) {
        super(BrowserEvents.CHANGE);
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.renderer = renderer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBrowserEvent(Context context, Element parent, T value,
            NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if (BrowserEvents.CHANGE.equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            T newValue = getValueAtIndex((K) key, select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    private T getValueAtIndex(K key, int index) {
        List<T> values = getValuesForKey(key);
        if (values != null) {
            if (index >= 0 && index < values.size()) {
                return values.get(index);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        K key = (K) context.getKey();
        int selectedIndex = getSelectedIndex(key, value);
        sb.appendHtmlConstant("<select tabindex=\"-1\">");
        int index = 0;
        for (T option : getValuesForKey(key)) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(renderer.render(option)));
            } else {
                sb.append(template.deselected(renderer.render(option)));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getSelectedIndex(K key, T value) {
        List<T> values = getValuesForKey(key);
        if (values != null) {
            return values.indexOf(value);
        }
        return -1;
    }

    protected abstract List<T> getValuesForKey(K key);

}
