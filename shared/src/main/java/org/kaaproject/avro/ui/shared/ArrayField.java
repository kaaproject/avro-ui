/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.avro.ui.shared;

import java.util.ArrayList;
import java.util.List;

public class ArrayField extends FormField {

    private static final long serialVersionUID = -1859402253654290694L;
    
    public static enum OverrideStrategy {
        APPEND,
        REPLACE
    }
    
    private FormField elementMetadata;
    
    private List<FormField> value;
    
    private int minRowCount = 0;
    
    private OverrideStrategy overrideStrategy;

    public ArrayField() {
        super();
        value = new ArrayList<>();
    }
    
    public ArrayField(String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
        value = new ArrayList<>();
    }
    
    public FormField getElementMetadata() {
        return elementMetadata;
    }

    public void setElementMetadata(FormField elementMetadata) {
        this.elementMetadata = elementMetadata;
    }

    public List<FormField> getValue() {
        return value;
    }
    
    public int getMinRowCount() {
        return minRowCount;
    }
    
    public void setMinRowCount(int minRowCount) {
        this.minRowCount = minRowCount;
        if (minRowCount == 0) {
        	setOptional(true);
        }
    }
    
    public FormField createRow() {
    	FormField row = elementMetadata.clone();
    	row.finalizeMetadata();
    	return row;
    }
    
    public OverrideStrategy getOverrideStrategy() {
        return overrideStrategy;
    }

    public void setOverrideStrategy(OverrideStrategy overrideStrategy) {
        this.overrideStrategy = overrideStrategy;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.ARRAY;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    public void addArrayData(FormField data) {
        data.setRowIndex(value.size());
        this.value.add(data);
    }
    
    @Override
    public void finalizeMetadata() {
    	if (elementMetadata != null) {
    		elementMetadata.finalizeMetadata();
    		elementMetadata.disableOverride();
    	}
    	for (FormField field : value) {
    		field.finalizeMetadata();
    		field.disableOverride();
    	}
    }
    
    @Override
    protected FormField createInstance() {
        return new ArrayField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        ArrayField clonedArrayField = (ArrayField)cloned;
        clonedArrayField.minRowCount = minRowCount;
        clonedArrayField.elementMetadata = elementMetadata.clone();
        for (FormField field : value) {
            clonedArrayField.value.add(field.clone());
        }
        clonedArrayField.overrideStrategy = overrideStrategy;
    }
    
    @Override
    public boolean isValid() {
        if (isOverride() && !isChanged()) {
            return true;
        } else if (isOptional()) {
            if (value.size() > 0) {
                return valid();
            } else {
                return true;
            }
        } else {
            return valid();
        }
    }

    @Override
    protected boolean valid() {
        if (value.size() > 0 && value.size() >= minRowCount) {
            boolean valid = true;
            for (FormField field : value) {
                valid &= field.isValid();
            }
            return valid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((elementMetadata == null) ? 0 : elementMetadata.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayField other = (ArrayField) obj;
        if (elementMetadata == null) {
            if (other.elementMetadata != null) {
                return false;
            }
        } else if (!elementMetadata.equals(other.elementMetadata)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
