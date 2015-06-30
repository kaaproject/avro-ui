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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.ProvidesKey;

public class AvroUiDataGrid<T> extends DataGrid<T>{

    public static AvroUiGridResources gridResources = GWT.create(AvroUiGridResources.class);
    public static AvroUiGridResourcesSmall gridResourcesSmall = GWT.create(AvroUiGridResourcesSmall.class);

    private final AvroUiGridResources.AvroUiGridStyle style;
    
    public AvroUiDataGrid(int pageSize, ProvidesKey<T> keyProvider, AvroUiGridResources resources) {
        super(pageSize, resources, keyProvider);
        style = resources.dataGridStyle();
    }
    
    public AvroUiDataGrid(int pageSize, ProvidesKey<T> keyProvider,  boolean embedded) {
        super(pageSize, embedded ? gridResourcesSmall : gridResources, keyProvider);
        style = embedded ? gridResourcesSmall.dataGridStyle() : gridResources.dataGridStyle();
    }
    
    @Override
    public void removeColumn(int index) {
        super.removeColumn(index);
        if (isChrome()) {
            addColumnStyleName(index, style.dataGridColumnInvisible());
        }
    }
    
    @Override
    public void insertColumn(int beforeIndex, Column<T, ?> col, Header<?> header, Header<?> footer) {
        super.insertColumn(beforeIndex, col, header, footer);
        if (isChrome()) {
            removeColumnStyleName(beforeIndex, style.dataGridColumnInvisible());
        }
    }
    
    private static native boolean isChrome() /*-{
        return navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
    }-*/;
    
}
