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
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.ProvidesKey;

public class AvroUiDataGrid<T> extends DataGrid<T>{

    public static AvroUiGridResources gridResources = GWT.create(AvroUiGridResources.class);
    public static AvroUiGridResourcesSmall gridResourcesSmall = GWT.create(AvroUiGridResourcesSmall.class);

    private final AvroUiGridResources.AvroUiGridStyle style;
    
    private int filterFocusedCellColumn = -1;
    private int filterFocusedCellRow = -1;
    
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
    
    @Override
    protected void onBrowserEvent2(Event event) {
        if (hasFilterHeaders()) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
              return;
            }
            final Element target = event.getEventTarget().cast();
            TableSectionElement thead = getTableHeadElement();
            TableSectionElement targetTableSection = null;
            TableCellElement targetTableCell = null;
            Element headerParent = null;
            
            Element maybeTableCell = null;
            Element cur = target;
            
            while (cur != null && targetTableSection == null) {
                if (cur == thead) {
                    targetTableSection = cur.cast();
                    if (maybeTableCell != null) {
                        targetTableCell = maybeTableCell.cast();
                        break;
                    }
                }
                
                String tagName = cur.getTagName();
                if (TableCellElement.TAG_TD.equalsIgnoreCase(tagName)
                    || TableCellElement.TAG_TH.equalsIgnoreCase(tagName)) {
                  maybeTableCell = cur;
                }
                
                if (headerParent == null && getHeaderBuilder().isHeader(cur)) {
                    headerParent = cur;
                }
                
                cur = cur.getParentElement();
            }
            if (targetTableCell != null) {
                String eventType = event.getType();
                TableRowElement targetTableRow = targetTableCell.getParentElement().cast();
                int col = targetTableCell.getCellIndex();
                if (targetTableSection == thead) {
                    if (headerParent != null) {
                        Header<?> header =
                                getHeaderBuilder().getHeader(headerParent);
                        if (header != null) {
                          int headerIndex = getHeaderBuilder().getRowIndex(targetTableRow);
                          if (header instanceof StringFilterHeader) {
                              if (StringFilterHeader.isFilterFocusEvent(eventType)) {          
                                  filterFocusedCellColumn = col;
                                  filterFocusedCellRow = headerIndex;
                              } else {
                                  filterFocusedCellColumn = -1;
                                  filterFocusedCellRow = -1;
                              }
                          }
                         }
                      }
                }
            }
        }
        super.onBrowserEvent2(event);
    }
    
    private boolean hasFilterHeaders() {
        return getHeaderBuilder() instanceof StringFilterHeaderBuilder;
    }
    
    @Override
    protected boolean resetFocusOnCell() {
        boolean focused = false;
        if (hasFilterHeaders() && filterFocusedCellColumn > -1 && filterFocusedCellRow > -1) {
            TableSectionElement thead = getTableHeadElement();
            NodeList<TableRowElement> rows = thead.getRows();
            if (filterFocusedCellRow < rows.getLength()) {
                TableRowElement row = rows.getItem(filterFocusedCellRow);
                NodeList<TableCellElement> cells = row.getCells();
                if (filterFocusedCellColumn < cells.getLength()) {
                    TableCellElement cell = cells.getItem(filterFocusedCellColumn);
                    if (getHeaderBuilder().isHeader(cell)) {
                        Header<?> header = getHeaderBuilder().getHeader(cell);
                        Context context = new Context(0, 0, header.getKey());
                        focused = resetFocusOnFilterCellImpl(context, header, cell);
                    }
                }
            }
        }
        if (!focused) {
            focused = super.resetFocusOnCell();
        }
        return focused;
    }
    
    private <C> boolean resetFocusOnFilterCellImpl(Context context, Header<C> header,
            Element cellParent) {
          C headerValue = header.getValue();
          Cell<C> cell = header.getCell();
          return cell.resetFocus(context, cellParent, headerValue);
    }
    
    private static native boolean isChrome() /*-{
        return navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
    }-*/;
    
}
