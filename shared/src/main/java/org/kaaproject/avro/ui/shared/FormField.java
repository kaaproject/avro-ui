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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class FormField implements Serializable, Cloneable {

    private static final long serialVersionUID = 6978997793895098628L;
    
    public static enum FieldAccess {
        EDITABLE,
        READ_ONLY,
        HIDDEN
    }
    
    private String fieldName;
    private String displayName;
    private String displayHint;
    private String schema;
    private boolean optional;
    private FieldAccess fieldAccess = FieldAccess.EDITABLE;
    private float weight = 1f;
    private int keyIndex = -1;
    private int rowIndex = -1;
    
    private boolean isOverride = false;
    private boolean isOverrideDisabled = false;
    
    private boolean changed = false;
    
    private transient List<ChangeListener> changeListeners = new ArrayList<>();
    
    public FormField() {
    }
    
    public FormField(String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.schema = schema;
        this.optional = optional;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayHint() {
        return displayHint;
    }

    public void setDisplayHint(String displayHint) {
        this.displayHint = displayHint;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public float getWeight() {
        return weight;
    }
    
    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public FieldAccess getFieldAccess() {
        return fieldAccess;
    }

    public void setFieldAccess(FieldAccess fieldAccess) {
        this.fieldAccess = fieldAccess;
    }
    
    public boolean isReadOnly() {
        return this.fieldAccess == FieldAccess.READ_ONLY;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public void setOverride(boolean isOverride) {
        this.isOverride = isOverride;
    }
    
    public boolean isOverrideDisabled() {
    	return isOverrideDisabled;
    }

    public boolean isChanged() {
        return changed;
    }
    
    protected void fireChanged() {
        setChanged(true, true);
    }
    
    public void setChanged(boolean changed) {
        setChanged(changed, false);
    }

    public void setChanged(boolean changed, boolean fireChanged) {
        if (this.changed != changed) {
            this.changed = changed;
            if (fireChanged) {
                for (ChangeListener listener : changeListeners) {
                    listener.onChanged(changed);
                }
            }
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
    
    public String getTypeFullname() {
    	return getFieldType().getName();
    }
    
    public abstract FieldType getFieldType();
    
    public abstract boolean isNull();
    
    public boolean isSameType(FormField otherRecord) {
        return getTypeFullname().equals(otherRecord.getTypeFullname());
    }
    
    public boolean isValid() {
        if (optional || (isOverride && !changed)) {
            return true;
        } else {
            return valid();
        }
    }
    
    protected abstract boolean valid();
    
    public void finalizeMetadata() {}
    
    public void disableOverride() {
    	isOverride = false;
    	isOverrideDisabled = true;
    }

    public FormField clone() {
        FormField cloned = createInstance();
        copyFields(cloned);
        return cloned;
    }

    protected abstract FormField createInstance();

    protected void copyFields (FormField cloned) {
        cloned.fieldName = fieldName;
        cloned.displayName = displayName;
        cloned.displayHint = displayHint;
        cloned.schema = schema;
        cloned.optional = optional;
        cloned.fieldAccess = fieldAccess;
        cloned.weight = weight;
        cloned.keyIndex = keyIndex;
        cloned.isOverride = isOverride;
        cloned.isOverrideDisabled = isOverrideDisabled;
        cloned.changed = changed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (changed ? 1231 : 1237);
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + rowIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormField other = (FormField) obj;
        if (changed != other.changed)
            return false;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (rowIndex != other.rowIndex)
            return false;
        return true;
    }

    protected static boolean strIsEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static interface ChangeListener {
        
        void onChanged (boolean changed); 
        
    }

}
