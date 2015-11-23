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

import java.io.IOException;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.cell.AbstractSelectionCell;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.shared.DependenciesField;
import org.kaaproject.avro.ui.shared.FormContext;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnVersion;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class DependenciesFieldWidget extends AbstractFieldWidget<DependenciesField> {

    private FieldWidgetPanel fieldWidgetPanel;
    private DependenciesGrid dependenciesGrid;
    
    public DependenciesFieldWidget(AvroWidgetsConfig config, NavigationContainer container, boolean readOnly) {
        super(config, container, readOnly);
    }

    public DependenciesFieldWidget(AvroWidgetsConfig config, AvroUiStyle style, NavigationContainer navigationContainer, boolean readOnly) {
        super(config, style, navigationContainer, readOnly);
    }

    @Override
    protected Widget constructForm() {
        fieldWidgetPanel = new FieldWidgetPanel(style, value, readOnly, true);
        fieldWidgetPanel.setWidth(config.getArrayPanelWidth());
        
        dependenciesGrid = new DependenciesGrid(value, readOnly, config.getDependenciesPageSize());
        dependenciesGrid.setHeight(config.getDependenciesHeigh());
        
        fieldWidgetPanel.setContent(dependenciesGrid);
        fieldWidgetPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    reload();
                }
            }
        });
        
        return fieldWidgetPanel;
    }

    @Override
    public void updateConfig(AvroWidgetsConfig config) {
        super.updateConfig(config);
        if (fieldWidgetPanel != null) {
            fieldWidgetPanel.setWidth(config.getArrayPanelWidth());
        }
        if (dependenciesGrid != null) {
            dependenciesGrid.setHeight(config.getDependenciesHeigh());
            dependenciesGrid.setPageSize(config.getDependenciesPageSize());
        }

    }
    
    @Override
    public void onShown() {
        super.onShown();
        reload();
    }
    
    public void reload() {
        if (dependenciesGrid != null) {
            dependenciesGrid.reload();
        }
    }
    
    private static class DependenciesGrid extends AbstractGrid<FqnVersion, Fqn> {

        private ListDataProvider<FqnVersion> dataProvider;
        private DependenciesField dependenciesField;
        private boolean readOnly;
        
        public DependenciesGrid(DependenciesField dependenciesField, boolean readOnly, int pageSize) {
            super(Unit.PX, false, true, pageSize, false);
            this.dependenciesField = dependenciesField;
            this.readOnly = readOnly;
            init();
            showShadow(false);
            dataProvider = new ListDataProvider<>(dependenciesField.getValue(), getDataGrid().getKeyProvider());
            dataProvider.addDataDisplay(getDataGrid());
        }
        
        public void reload() {
            this.dataProvider.refresh();
            this.dataProvider.flush();
        }

        @Override
        protected float constructColumnsImpl(DataGrid<FqnVersion> table) {
            float prefWidth = 0;
            prefWidth += constructStringColumn(table, Utils.constants.fqn(),
                    new StringValueProvider<FqnVersion>() {
                @Override
                public String getValue(FqnVersion item) {
                    return item.getFqnString();
                }
            }, 200);
            
            if (readOnly) {
                prefWidth += constructStringColumn(table, Utils.constants.version(),
                        new StringValueProvider<FqnVersion>() {
                    @Override
                    public String getValue(FqnVersion item) {
                        return item.getVersion() + "";
                    }
                }, 50);
            } else {
                VersionSelectionCell versionSelectionCell = new VersionSelectionCell(dependenciesField.getContext());
                Column<FqnVersion, Integer> versionColumn = new Column<FqnVersion, Integer>(versionSelectionCell) {
                    @Override
                    public Integer getValue(FqnVersion object) {
                        return object.getVersion();
                    }
                };
                
                Header<SafeHtml> versionHeader = new SafeHtmlHeader(
                        SafeHtmlUtils.fromSafeConstant(Utils.constants.version()));
                
                table.addColumn(versionColumn, versionHeader);
                
                versionColumn.setFieldUpdater(new FieldUpdater<FqnVersion, Integer>() {
                    
                    @Override
                    public void update(int index, FqnVersion object, Integer value) {
                        object.setVersion(value);
                        dataProvider.refresh();
                    }
                });
                table.setColumnWidth(versionColumn, 50, Unit.PX);
            }
            prefWidth += 50;
            
            return prefWidth;
        }
        
        @Override
        protected Fqn getObjectId(FqnVersion value) {
            return value.getFqn();
        }
        
        private static class VersionSelectionCell extends AbstractSelectionCell<Integer, Fqn> {

            private FormContext formContext;
            
            public VersionSelectionCell(FormContext formContext) {
                super(new Renderer<Integer>() {

                    @Override
                    public String render(Integer object) {
                        if (object != null) {
                            return object.toString();
                        }
                        return "";
                    }
                    
                    @Override
                    public void render(Integer object, Appendable appendable)
                            throws IOException {
                        appendable.append(render(object));
                    }
                    
                });
                
                this.formContext = formContext;
            }

            @Override
            protected List<Integer> getValuesForKey(Fqn key) {
                return formContext.getAvailableVersions(key);
            }
        }

    }
    

    
}
