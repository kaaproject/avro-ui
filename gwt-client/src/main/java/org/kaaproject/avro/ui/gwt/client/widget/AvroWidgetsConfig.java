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

public class AvroWidgetsConfig {
    
    private static final String PX = "px";

    private final int recordPanelWidthPx;
    private final int fieldsColumnWidthPx;
    private final int labelsColumnWidthPx;
    
    private final int arrayPanelWidthPx;
    private final int unionPanelWidthPx;
    
    private final int gridHeightPx;
    private final int tableHeightPx;
    
    public static class Builder {
        
        private static final int DEFAULT_RECORD_PANEL_WIDTH_PX = 700;
        private static final int DEFAULT_LABELS_COLUMN_WIDTH_PX = 200;
        
        private static final int DEFAULT_GRID_HEIGHT_PX = 180;
        private static final int DEFAULT_TABLE_HEIGHT_PX = 200;
        
        private int recordPanelWidthPx = DEFAULT_RECORD_PANEL_WIDTH_PX;
        private int labelsColumnWidthPx = DEFAULT_LABELS_COLUMN_WIDTH_PX;
        
        private int gridHeightPx = DEFAULT_GRID_HEIGHT_PX;
        private int tableHeightPx = DEFAULT_TABLE_HEIGHT_PX;
        
        public Builder() {}
        
        public Builder recordPanelWidth(int widthPx) {
            this.recordPanelWidthPx = widthPx;
            return this;
        }
        
        public Builder labelsColumnWidth(int widthPx) {
            this.labelsColumnWidthPx = widthPx;
            return this;
        }

        public Builder gridHeight(int heightPx) {
            this.gridHeightPx = heightPx;
            return this;
        }
        
        public Builder tableHeight(int heightPx) {
            this.tableHeightPx = heightPx;
            return this;
        }
        
        public AvroWidgetsConfig createConfig() {
            return new AvroWidgetsConfig(recordPanelWidthPx, 
                                         labelsColumnWidthPx, 
                                         gridHeightPx, 
                                         tableHeightPx);
        }

    }
    
    public AvroWidgetsConfig(int recordPanelWidthPx, 
                              int labelsColumnWidthPx,
                              int gridHeightPx, 
                              int tableHeightPx) {
        this.recordPanelWidthPx = recordPanelWidthPx;
        this.fieldsColumnWidthPx = (int) ((float)(recordPanelWidthPx - 200) * 0.6f);
        this.labelsColumnWidthPx = labelsColumnWidthPx;
        this.arrayPanelWidthPx = recordPanelWidthPx - 100;
        this.unionPanelWidthPx = recordPanelWidthPx - 50;
        this.gridHeightPx = gridHeightPx;
        this.tableHeightPx = tableHeightPx;
    }

    public int getRecordPanelWidthPx() {
        return recordPanelWidthPx;
    }
    
    public String getRecordPanelWidth() {
        return recordPanelWidthPx + PX;
    }
    
    public int getFieldsColumnWidthPx() {
        return fieldsColumnWidthPx;
    }
    
    public String getFieldsColumnWidth() {
        return fieldsColumnWidthPx + PX;
    }

    public int getLabelsColumnWidthPx() {
        return labelsColumnWidthPx;
    }
    
    public String getLabelsColumnWidth() {
        return labelsColumnWidthPx + PX;
    }

    public int getArrayPanelWidthPx() {
        return arrayPanelWidthPx;
    }
    
    public String getArrayPanelWidth() {
        return arrayPanelWidthPx + PX;
    }
    
    public int getUnionPanelWidthPx() {
        return unionPanelWidthPx;
    }

    public String getUnionPanelWidth() {
        return unionPanelWidthPx + PX;
    }

    public int getGridHeightPx() {
        return gridHeightPx;
    }
    
    public String getGridHeight() {
        return gridHeightPx + PX;
    }

    public int getTableHeightPx() {
        return tableHeightPx;
    }
    
    public String getTableHeight() {
        return tableHeightPx + PX;
    }
    
}
