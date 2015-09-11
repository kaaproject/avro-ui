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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.ConfirmDialog.ConfirmListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionButtonCell.ActionValidator;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.ActionsButtonCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.LinkCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.UneditableCheckboxCell;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.HasRowActionEventHandlers;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;

public abstract class AbstractGrid<T, K> extends DockLayoutPanel implements HasRowActionEventHandlers<K> {
    
    public static final int DEFAULT_GRID_MARGIN = 10;
    
    protected static final int ACTION_COLUMN_WIDTH = 40;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static SimplePager.Resources pagerResourcesDefault = GWT.create(SimplePager.Resources.class);
    private static AvroUiPagerResourcesSmall pagerResourcesSmall = GWT.create(AvroUiPagerResourcesSmall.class);

    protected DataGrid<T> table;
    
    private int pageSize;
    private MultiSelectionModel<T> selectionModel;

    private float prefferredWidth = 0f;

    protected boolean enableActions;
    protected boolean embedded;
    protected AvroUiGridResources gridResources;
    protected SimplePager.Resources pagerResources;

    protected Column<T,T> deleteColumn;
    
    private final Map<Column<?, ?>, Comparator<T>> comparators = new HashMap<Column<?, ?>, Comparator<T>>();
    
    public AbstractGrid(Style.Unit unit) {
        this(unit, true);
    }
    
    public AbstractGrid(Style.Unit unit, int pageSize) {
        this(unit, true, pageSize);
    }

    public AbstractGrid(Style.Unit unit, boolean enableActions) {
        this(unit, enableActions, false);
    }
    
    public AbstractGrid(Style.Unit unit, boolean enableActions, int pageSize) {
        this(unit, enableActions, false, pageSize);
    }

    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded) {
        this(unit, enableActions, embedded, true);
    }
    
    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded, int pageSize) {
        this(unit, enableActions, embedded, pageSize, true);
    }
    
    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded, boolean init) {
        this(unit, enableActions, embedded, DEFAULT_PAGE_SIZE, init);
    }
    
    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded, int pageSize, boolean init) {
        this(unit, enableActions, embedded, pageSize, init, 
                embedded ? AvroUiDataGrid.gridResourcesSmall : AvroUiDataGrid.gridResources,
                        embedded ? pagerResourcesSmall : pagerResourcesDefault);
    }

    public AbstractGrid(Style.Unit unit, boolean enableActions, boolean embedded, int pageSize, boolean init, 
            AvroUiGridResources gridResources, SimplePager.Resources pagerResources) {
        super(unit);
        this.enableActions = enableActions;
        this.embedded = embedded;
        this.pageSize = pageSize;
        this.gridResources = gridResources;
        this.pagerResources = pagerResources;
        if (init) {
            init();
        }
    }
    
    protected void init() {
        ProvidesKey<T> keyProvider = new ProvidesKey<T>() {
            @Override
            public Object getKey(T item) {
                return item != null ? getObjectId(item) : null;
            }
        };
        table = new AvroUiDataGrid<T>(this.pageSize, keyProvider, gridResources);
        table.setAutoHeaderRefreshDisabled(true);
        Label emptyTableLabel = new Label(Utils.constants.dataGridEmpty());
        if (embedded) {
            emptyTableLabel.getElement().getStyle().setFontSize(14, Unit.PX);
            emptyTableLabel.getElement().getStyle().setColor("#999999");
        }
        table.setEmptyTableWidget(emptyTableLabel);

        selectionModel = new MultiSelectionModel<T>(keyProvider);

        table.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<T> createCheckboxManager());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                table.redrawHeaders();
            }
        });

        prefferredWidth = initColumns(table);
        table.setMinimumTableWidth(prefferredWidth, Unit.PX);

        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true) {
            @Override
            protected String createText() {
                HasRows display = getDisplay();
                Range range = display.getVisibleRange();
                int currentPage = range.getStart() / (range.getLength() != 0 ? range.getLength() : 1) + 1;
                int total = ((int)Math.ceil((float)display.getRowCount()/(float)range.getLength()));
                if (total == 0) {
                    total = 1;
                }
                return Utils.messages.pagerText(String.valueOf(currentPage), String.valueOf(total));
            }
            
            @Override
            public void setPageStart(int index) {
                if (getDisplay() != null) {
                  Range range = getDisplay().getVisibleRange();
                  int pageSize = range.getLength();
                  
                  index = Math.max(0, index);
                  if (index != range.getStart()) {
                      getDisplay().setVisibleRange(index, pageSize);
                  }
                }
            }
        };
        pager.setDisplay(table);

        String pagerId = "pager_"+pager.hashCode();

        String html =     "<table " +
                        "style='width:100%'>" +
                        "  <tr>" +
                        "     <td" +
                        "       align='right'>" +
                        "        <div id='" + pagerId + "'/>" +
                        "     </td>" +
                        "  </tr>" +
                        "</table>";

        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(pager, pagerId);

        addNorth(htmlPanel, 40);
        add(table); // center

        showShadow(embedded);
    }
    
    public void showShadow(boolean show) {
        if (show) {
            getElement().getStyle().clearMargin();
            getElement().getStyle().setProperty("boxShadow", "0px 0px 8px rgba(0,0,0,0.5)");
            table.getElement().getStyle().setMargin(DEFAULT_GRID_MARGIN, Unit.PX);
        } else {
            getElement().getStyle().clearProperty("boxShadow");
            table.getElement().getStyle().clearMargin();
            getElement().getStyle().setMargin(DEFAULT_GRID_MARGIN, Unit.PX);
        }
    }

    public MultiSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public DataGrid<T> getDataGrid() {
        return table;
    }

    public void sort(List<T> data, Column<?, ?> column, boolean isSortAscending) {
        final Comparator<T> comparator = comparators.get(column);
        if (comparator == null) {
            return;
        }
        if (isSortAscending) {
            Collections.sort(data, comparator);
        } else {
            Collections.sort(data, new Comparator<T>() {
                public int compare(T o1, T o2) {
                    return -comparator.compare(o1, o2);
                }
            });
        }
    }
    
    protected void onRowClicked(K id) {
        RowActionEvent<K> rowClickEvent = new RowActionEvent<>(id, RowActionEvent.CLICK);
        fireEvent(rowClickEvent);
    }

    protected void onRowDelete(K id) {
        RowActionEvent<K> rowClickEvent = new RowActionEvent<>(id, RowActionEvent.DELETE);
        fireEvent(rowClickEvent);
    }

    @Override
    public HandlerRegistration addRowActionHandler(RowActionEventHandler<K> handler) {
        return this.addHandler(handler, RowActionEvent.getType());
    }

    private float initColumns (DataGrid<T> table) {
        float prefWidth = 0f;
        prefWidth += constructColumnsImpl(table);
        prefWidth += constructActions(table, prefWidth);
        return prefWidth;
    }
    
    protected float constructStringColumn(DataGrid<T> table, String title,
            final StringValueProvider<T> valueProvider, float prefWidth) {
        return constructStringColumn(table, title, valueProvider, null, null, prefWidth);
    }

    protected float constructStringColumn(DataGrid<T> table, String title,
            final StringValueProvider<T> valueProvider, 
            Comparator<T> comparator, Boolean isSortAscending, float prefWidth) {
        Header<SafeHtml> header = new SafeHtmlHeader(
                SafeHtmlUtils.fromSafeConstant(title));
        Column<T, String> column = new Column<T, String>(new LinkCell()) {
            @Override
            public String getValue(T item) {
                return valueProvider.getValue(item);
            }
        };
        column.setFieldUpdater(new FieldUpdater<T,String>() {
            @Override
            public void update(int index, T object, String value) {
                onRowClicked(getObjectId(object));
            }
        });
        table.addColumn(column, header);
        table.setColumnWidth(column, prefWidth, Unit.PX);
        processColumnSort(table, column, comparator, isSortAscending);
        return prefWidth;
    }

    protected float constructBooleanColumn(DataGrid<T> table, String title,
            final BooleanValueProvider<T> valueProvider, float prefWidth) {
        return constructBooleanColumn(table, title, valueProvider, null, null, prefWidth);
    }
    
    protected float constructBooleanColumn(DataGrid<T> table, String title,
            final BooleanValueProvider<T> valueProvider, 
            Comparator<T> comparator, Boolean isSortAscending, float prefWidth) {
        Header<SafeHtml> header = new SafeHtmlHeader(
                SafeHtmlUtils.fromSafeConstant(title));
        Column<T, Boolean> column = new Column<T, Boolean>(new UneditableCheckboxCell()) {
            @Override
            public Boolean getValue(T item) {
                return valueProvider.getValue(item);
            }
        };
        column.setFieldUpdater(new FieldUpdater<T,Boolean>() {
            @Override
            public void update(int index, T object, Boolean value) {
                onRowClicked(getObjectId(object));
            }
        });
        table.addColumn(column, header);
        table.setColumnWidth(column, prefWidth, Unit.PX);
        processColumnSort(table, column, comparator, isSortAscending);
        return prefWidth;
    }
    
    private void processColumnSort(DataGrid<T> table, Column<?,?> column, 
            Comparator<T> comparator, Boolean isSortAscending) {
        if (comparator != null) {
            comparators.put(column, comparator);
            column.setSortable(true);
            if (isSortAscending != null) {
                ColumnSortList.ColumnSortInfo sortInfo = new ColumnSortList.ColumnSortInfo(column, isSortAscending.booleanValue());
                ColumnSortList sortList = table.getColumnSortList();
                sortList.insert(sortList.size(), sortInfo);
            }
        }
    }

    protected float constructActions(DataGrid<T> table, float prefWidth) {
        if (enableActions) {
            if (deleteColumn == null || table.getColumnIndex(deleteColumn) == -1) {
                Header<SafeHtml> deleteHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.delete()));

                deleteColumn = constructDeleteColumn("");
                table.addColumn(deleteColumn, deleteHeader);
                table.setColumnWidth(deleteColumn, ACTION_COLUMN_WIDTH, Unit.PX);
                return ACTION_COLUMN_WIDTH;
            }
            else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    protected float removeActions(DataGrid<T> table) {
        int index = table.getColumnIndex(deleteColumn);
        if (index > -1) {
            table.removeColumn(deleteColumn);
            return ACTION_COLUMN_WIDTH;
        }
        return 0;
    }

    public void setEnableActions(boolean enableActions) {
        this.enableActions = enableActions;
        if (enableActions) {
            prefferredWidth += constructActions(table, prefferredWidth);
            table.setMinimumTableWidth(prefferredWidth, Unit.PX);
        }
        else {
            prefferredWidth -= removeActions(table);
            table.setMinimumTableWidth(prefferredWidth, Unit.PX);
        }
    }

    protected void constructAdditionalActions(ActionsButtonCell<T> cell) {
    }

    protected Column<T, T> constructDeleteColumn(String text) {
        ActionButtonCell<T> cell = new ActionButtonCell<T>(Utils.resources.remove(),
                text, embedded,
                new ActionListener<T> () {
                    @Override
                    public void onItemAction(T value) {
                        deleteItem(value);
                    }
                },
                new ActionValidator<T> () {
                    @Override
                    public boolean canPerformAction(T value) {
                        return canDelete(value);
                    }
                }
        );
        Column<T, T> column = new Column<T, T>(cell) {
            @Override
            public T getValue(T item) {
                return item;
            }
        };
        return column;
    }

    protected void deleteItem(final T value) {
        ConfirmListener listener = new ConfirmListener() {
            @Override
            public void onNo() {
            }

            @Override
            public void onYes() {
                onRowDelete(getObjectId(value));
            }
        };

        String question = deleteQuestion();
        String title = deleteTitle();

        ConfirmDialog dialog = new ConfirmDialog(listener, title, question);
        dialog.center();
        dialog.show();
    }

    protected String deleteQuestion() {
        return Utils.messages.deleteSelectedEntryQuestion();
    }

    protected String deleteTitle() {
        return Utils.messages.deleteSelectedEntryTitle();
    }

    protected boolean canDelete(T value) {
        return true;
    }

    protected K getObjectId(T value) {
        return null;
    }

    protected abstract float constructColumnsImpl(DataGrid<T> table);

    public interface StringValueProvider<T> {
        String getValue(T item);
    }

    public interface BooleanValueProvider<T> {
        Boolean getValue(T item);
    }

}
