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

import java.util.HashSet;
import java.util.Set;

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DefaultHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class StringFilterHeaderBuilder<T> extends DefaultHeaderOrFooterBuilder<T> {

    private AbstractGrid<T,?> grid;
    
    public StringFilterHeaderBuilder(AbstractGrid<T,?> grid) {
        super(grid.getDataGrid(), false);
        this.grid = grid;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean buildHeaderOrFooterImpl() {
        boolean result = super.buildHeaderOrFooterImpl();
        AbstractCellTable<T> table = getTable();
        Header<?> prevHeader = getHeader(0);
        Style style = table.getResources().style();
        StringBuilder classesBuilder = new StringBuilder(style.header());
        classesBuilder.append(" ").append(Utils.avroUiStyle.fieldWidget());
        
        TableRowBuilder tr = startRow();
        for (int i=0;i<table.getColumnCount();i++) {
            appendExtraStyles(prevHeader, classesBuilder);
            TableCellBuilder th = tr.startTH().className(classesBuilder.toString());
            Column<T,?> column = table.getColumn(i);
            Header<?> header = null;
            if (column instanceof FiltrableStringColumn) {
                StringFilterHeader filterHeader = new StringFilterHeader(grid, (FiltrableStringColumn<T>)column, new Integer(i));
                
                Set<String> headerEvents = filterHeader.getCell().getConsumedEvents();
                Set<String> consumedEvents = new HashSet<String>();
                if (headerEvents != null) {
                    consumedEvents.addAll(headerEvents);
                }
                sinkEvents(table, consumedEvents);
                header = filterHeader;
            } else {
                header = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(""));
            }
            Context context = new Context(0, 0, header.getKey());
            renderHeader(th, context, header);
            
            th.endTH();
        }
        tr.endTR();
        return result;
    }
    
    private <H> void appendExtraStyles(Header<H> header, StringBuilder classesBuilder) {
        if (header == null) {
          return;
        }
        String headerStyleNames = header.getHeaderStyleNames();
        if (headerStyleNames != null) {
          classesBuilder.append(" ");
          classesBuilder.append(headerStyleNames);
        }
    }
    
    public final void sinkEvents(Widget widget, Set<String> typeNames) {
        if (typeNames == null) {
          return;
        }

        int eventsToSink = 0;
        for (String typeName : typeNames) {
          int typeInt = Event.getTypeInt(typeName);
          if (typeInt < 0) {
            widget.sinkBitlessEvent(typeName);
          } else {
            typeInt = sinkEvent(widget, typeName);
            if (typeInt > 0) {
              eventsToSink |= typeInt;
            }
          }
        }
        if (eventsToSink > 0) {
          widget.sinkEvents(eventsToSink);
        }
      }

      protected int sinkEvent(Widget widget, String typeName) {
        return Event.getTypeInt(typeName);
      }

}
