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

import java.util.Collection;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SimpleKeyProvider;

public class ExtendedValueListBox<T> extends ValueListBox<T> {

    private AvroUiStyle style;
    private String promptText;
    
    public ExtendedValueListBox(Renderer<T> renderer, AvroUiStyle style, String promptText) {
        this(renderer, new SimpleKeyProvider<T>(), style, promptText);
    }
    
    public ExtendedValueListBox(Renderer<T> renderer,
            ProvidesKey<T> keyProvider, AvroUiStyle style, String promptText) {
        super(renderer, keyProvider);
        
        this.style = style;
        this.promptText = promptText;
    }
    
    @Override
    public void setValue(T value, boolean fireEvents) { 
        super.setValue(value, fireEvents);
        updateOptionsStyle();
    }
    
    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        super.setAcceptableValues(newValues);
        updateOptionsStyle();
    }
    
    private void updateOptionsStyle() {
        if (Utils.isNotBlank(promptText)) {
            SelectElement select = getSelectElement();
            int index = select.getSelectedIndex();
            if (index > -1) {
                OptionElement selectedOption = getOptionElement(index);
                if ("Null".equals(selectedOption.getValue())) {
                    selectedOption.setClassName(style.prompt());
                    selectedOption.setText(promptText);
                    addStyleName(style.prompt());
                    NodeList<OptionElement> options = getSelectElement().getOptions();
                    for (int i=0;i<options.getLength();i++) {
                        if (index != i) {
                            OptionElement option = options.getItem(i);
                            option.setClassName(style.noPrompt());
                        }
                    }
                } else {
                    NodeList<OptionElement> options = getSelectElement().getOptions();
                    for (int i=0;i<options.getLength();i++) {
                        OptionElement option = options.getItem(i);
                        if (Utils.isBlank(option.getValue())) {
                            option.setClassName("");
                            option.setText("");
                        }
                    }
                    removeStyleName(style.prompt());
                }
            }
        }
    }
    
    private OptionElement getOptionElement(int index) {
        return getSelectElement().getOptions().getItem(index);
    }
    
    private SelectElement getSelectElement() {
        return getElement().cast();
    }

}
