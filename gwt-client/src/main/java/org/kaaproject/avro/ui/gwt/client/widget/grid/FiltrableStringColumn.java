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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

public abstract class FiltrableStringColumn<T> extends Column<T, String> {

    private String filterString = "";
    
    public FiltrableStringColumn(Cell<String> cell) {
        super(cell);
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }
    
    public boolean isFilterEmpty() {
        return filterString == null || filterString.isEmpty();
    }
    
    public boolean matched(T object) {
        if (!isFilterEmpty()) {
            String value = getValue(object);
            return value != null ? value.toLowerCase().contains(filterString.toLowerCase()) : false;
        } else {
            return true;
        }
    }

}
