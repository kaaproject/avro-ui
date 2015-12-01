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

import org.kaaproject.avro.ui.gwt.client.widget.grid.ColumnFilterEvent.HasColumnFilterEventHandlers;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.FilterTextInputCell;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Header;

public class StringFilterHeader extends Header<String> implements ValueUpdater<String> {

    private FiltrableStringColumn<?> column;
    private HasColumnFilterEventHandlers source;
    private Integer key;
    
    public StringFilterHeader(HasColumnFilterEventHandlers source, FiltrableStringColumn<?> column, Integer key) {
        super(new FilterTextInputCell(100, Unit.PCT));
        setUpdater(this);
        this.source = source;
        this.column = column;
        this.key = key;
    }

    public void update(String value) {
        String prev = getValue();
        if (prev == null && value != null ||
            prev != null && value == null ||
            prev != null && value != null && !prev.equals(value)) { 
            column.setFilterString(value);
            ColumnFilterEvent.fire(source);
        }
    }

    @Override
    public String getValue() {
        return column.getFilterString();
    }
    
    @Override
    public Object getKey() {
        return key;
    }

    public static boolean isFilterFocusEvent(String eventType) {
        return BrowserEvents.CHANGE.equals(eventType) || 
                BrowserEvents.KEYUP.equals(eventType) ||
                BrowserEvents.KEYDOWN.equals(eventType) ||
                FilterTextInputCell.PASTE.equals(eventType);
    }

}
