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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.choosen.AvroChoosenListBox;
import org.kaaproject.avro.ui.shared.FqnKey;
import org.kaaproject.avro.ui.shared.Fqn;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class FqnReferenceBox extends SimplePanel implements HasValue<FqnKey>, ChosenChangeHandler {

    private ChosenListBox fqnListBox;
    
    private FqnKey value;
    
    private Map<String, FqnKey> hashToKeyMap = new LinkedHashMap<>();
    
    private TreeMap<FqnKey, Fqn> fqnsMap = new TreeMap<>();
    
    public FqnReferenceBox(String displayPrompt) {
        fqnListBox = AvroChoosenListBox.createChoosenListBox(true);
        fqnListBox.setMaxSelectedOptions(1);
        fqnListBox.setPlaceholderText(displayPrompt);
        
        fqnListBox.setAllowSingleDeselect(true);
        fqnListBox.addChosenChangeHandler(this);
        
        fqnListBox.setWidth("100%");
        AvroChoosenListBox.setChoosenListBoxWidth(fqnListBox, "100%");
        add(fqnListBox);
    }

    @Override
    public void onChange(ChosenChangeEvent event) {
        FqnKey newValue = null;
        if (fqnListBox.getValues().length == 1) {
            String hashValue = fqnListBox.getValues()[0];
            if (hashValue != null) {
                newValue = hashToKeyMap.get(hashValue);
            }
        }
        setValue(newValue, true);
    }
   
    @Override
    public FqnKey getValue() {
        return value;
    }

    @Override
    public void setValue(FqnKey value) {
        setValue(value, false);
    }

    @Override
    public void setValue(FqnKey value, boolean fireEvents) {
        if (value != null && !fqnsMap.containsKey(value)) {
            value = null;
        }
        
        if (value == this.value
                || (this.value != null && this.value.equals(value))) {
            return;
        }

        FqnKey before = this.value;
        this.value = value;
        
        updateListBox();

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<FqnKey> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
    public void updateDeclaredFqns(Map<FqnKey, Fqn> declaredFqns) {
        FqnComparator fc = new FqnComparator(declaredFqns);
        fqnsMap = new TreeMap<>(fc);
        fqnsMap.putAll(declaredFqns);
        hashToKeyMap.clear();
        for (FqnKey key : this.fqnsMap.keySet()) {
            hashToKeyMap.put(key.hashCode() + "", key);
        }
        clearListBox();
        setValue(this.value, true);
    }
    
    private void updateListBox() {
        if (value != null) {
            fqnListBox.setSelectedValue(value.hashCode() + "");
        } else {
           clearListBox();
        }
        if (fqnListBox.getValues().length == 0) {
            AvroChoosenListBox.setChoosenSearchFieldVisible(fqnListBox, true);
            fqnListBox.forceRedraw();
        } else {
            AvroChoosenListBox.setChoosenSearchFieldVisible(fqnListBox, false);
        }
    }
    
    private void clearListBox() {
        fqnListBox.clear();
        for (String hash : hashToKeyMap.keySet()) {
            FqnKey key = hashToKeyMap.get(hash);
            fqnListBox.addItem(fqnValueText(fqnsMap.get(key)), hash);
        }
        fqnListBox.update();
    }
    
    private static final int MAX_FQN_TEXT_ROW_LENGTH = 45;
    
    private String fqnValueText(Fqn fqn) {
        if (fqn != null) {
            String stringFqn = fqn.getFqnString();
            if (stringFqn.length() > MAX_FQN_TEXT_ROW_LENGTH) {
                String parts[] = stringFqn.split("\\.");
                stringFqn = "";
                int rowLength = 0;
                for (int i=0;i<parts.length;i++) {
                    if (i>0) {
                        stringFqn += ".";
                        rowLength++;
                    }
                    if (rowLength + parts[i].length() > MAX_FQN_TEXT_ROW_LENGTH) {
                        stringFqn += "\n";
                        rowLength = 0;
                    }
                    stringFqn += parts[i];
                    rowLength += parts[i].length();
                }
            }
            return stringFqn;
        } else {
            return Utils.constants.invalidFqn();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        fqnListBox.update();
        fqnListBox.setWidth("100%");
        AvroChoosenListBox.setChoosenListBoxWidth(fqnListBox, "100%");
        AvroChoosenListBox.setChoosenSearchFieldVisible(fqnListBox, fqnListBox.getValues().length == 0);
    }
    
    class FqnComparator implements Comparator<FqnKey> {
        
        Map<FqnKey, Fqn> map;
     
        public FqnComparator(Map<FqnKey, Fqn> base) {
            this.map = base;
        }
     
        public int compare(FqnKey a, FqnKey b) {
            Fqn af = map.get(a);
            Fqn bf = map.get(b);
            if (af == null) {
                if (a.equals(b)) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return af.compareTo(bf);
            }
        }
    }

}
