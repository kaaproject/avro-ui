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

package org.kaaproject.avro.ui.gwt.client.widget.choosen;

import static com.google.gwt.query.client.GQuery.$;
import static com.watopi.chosen.client.Chosen.CHOSEN_DATA_KEY;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.query.client.GQuery;
import com.watopi.chosen.client.ChosenImpl;
import com.watopi.chosen.client.ChosenOptions;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class AvroChoosenListBox {

    private static AvroChoosenResources avroChoosenResources = GWT.create(AvroChoosenResources.class);
    
    public static ChosenListBox createChoosenListBox(boolean isMultipleSelect) {
        ChosenOptions options = new ChosenOptions();
        options.setResources(avroChoosenResources);
        ChosenListBox choosenListBox = new ChosenListBox(isMultipleSelect, options);
        return choosenListBox;
    }
    
    public static void setChoosenListBoxWidth(ChosenListBox box, String width) {
        box.setWidth(width);
        box.getElement().getStyle().setMarginRight(-15, Unit.PX);
        ChosenImpl impl = $(box.getElement()).data(CHOSEN_DATA_KEY,
                ChosenImpl.class);
        if (impl != null) {
            impl.getContainer().width(width);
            impl.getContainer().css("margin-right", "-15px");
            impl.getContainer().find("div." + avroChoosenResources.css().chznDrop()).first().width(width);
            impl.getContainer().find("div." + avroChoosenResources.css().chznDrop()).first().css("margin-right", "-15px");
            setChoosenSearchFieldWidth(box, width);
        }
    }
    
    public static void setChoosenSearchFieldVisible(ChosenListBox box, boolean visible) {
        ChosenImpl impl = $(box.getElement()).data(CHOSEN_DATA_KEY,
             ChosenImpl.class);
        if (impl != null) {
            GQuery searchfield = impl.getContainer().find("li." + avroChoosenResources.css().searchField()).first();
            if (visible) {
                searchfield.show();
            } else {
                searchfield.hide();
            }
        }
    }
    
    public static void setChoosenSearchFieldWidth(ChosenListBox box, String width) {
        ChosenImpl impl = $(box.getElement()).data(CHOSEN_DATA_KEY,
             ChosenImpl.class);
        if (impl != null) {
            GQuery searchfield = impl.getContainer().find("li." + avroChoosenResources.css().searchField()).first();
            searchfield.width(width);
            GQuery searchFieldInput = searchfield.find("input." + avroChoosenResources.css().defaultClass()).first();
            if (searchFieldInput != null) {
                searchFieldInput.width(width);
            }
        }
    }
     
}
