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

package org.kaaproject.avro.ui.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FormField implements Serializable, Cloneable, Iterable<FormField> {

    private static final long serialVersionUID = 6978997793895098628L;
    
    public static enum FieldAccess {
        EDITABLE,
        READ_ONLY,
        HIDDEN
    }
    
    protected int id;
    private String fieldName;
    private String displayName;
    private String displayPrompt;
    private String schema;
    private boolean optional;
    private FieldAccess fieldAccess = FieldAccess.EDITABLE;
    private float weight = 1f;
    private int keyIndex = -1;
    private int rowIndex = -1;
    
    private boolean isOverride = false;
    private boolean isOverrideDisabled = false;
    
    private boolean changed = false;
    
    protected FormContext context;
    
    private transient List<ChangeListener> changeListeners = new ArrayList<>();
    
    private transient List<ValueChangeListener> transientValueChangeListeners = new ArrayList<>();
    
    private List<ValueChangeListener> valueChangeListeners = new ArrayList<>();
    
    private FormField parentField;
    
    public FormField() {
    }
    
    public FormField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        this.id = context.nextFieldId();
        this.context = context;
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.schema = schema;
        this.optional = optional;
    }
    
    public FormContext getContext() {
        return context;
    }
    
    public int getId() {
        return id;
    }
    
    public FormField getParentField() {
        return parentField;
    }
    
    public void setParentField(FormField parentField) {
        this.parentField = parentField;
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
    
    public String getDisplayPrompt() {
        return displayPrompt;
    }

    public void setDisplayPrompt(String displayPrompt) {
        this.displayPrompt = displayPrompt;
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
    
    public boolean isRootChild() {
        if (parentField != null && context != null 
                && context.getRootRecord() != null) {
            return context.getRootRecord().getId() == parentField.getId();
        } else {
            return false;
        }
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
    
    protected void fireValueChanged(Object value) {
        for (ValueChangeListener listener : transientValueChangeListeners) {
            listener.onValueChanged(value);
        }
        for (ValueChangeListener listener : valueChangeListeners) {
            listener.onValueChanged(value);
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void addTransientValueChangeListener(ValueChangeListener listener) {
        transientValueChangeListeners.add(listener);
    }

    public void removeTransientValueChangeListener(ValueChangeListener listener) {
        transientValueChangeListeners.remove(listener);
    }

    public void addValueChangeListener(ValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    public void removeValueChangeListener(ValueChangeListener listener) {
        valueChangeListeners.remove(listener);
    }

    public String getTypeFullname() {
    	return getFieldType().getName();
    }
    
    public String getDisplayString() {
        String str = "\"" + getDisplayName();
        if (isOverride) {
            str += " (" + (changed ? "changed" : "unchanged") + ")";
        } 
        str += "\"";
        return str;
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
        return clone(false);
    }
    
    public FormField clone(boolean deepCopy) {
        FormField cloned = createInstance();
        cloned.id = context.nextFieldId();
        copyFields(cloned, deepCopy);
        return cloned;
    }
    
    protected abstract FormField createInstance();

    protected void copyFields (FormField cloned, boolean deepCopy) {
        cloned.context = context;
        cloned.fieldName = fieldName;
        cloned.displayName = displayName;
        cloned.displayPrompt = displayPrompt;
        cloned.schema = schema;
        cloned.optional = optional;
        cloned.fieldAccess = fieldAccess;
        cloned.weight = weight;
        cloned.keyIndex = keyIndex;
        cloned.rowIndex = rowIndex;
        cloned.isOverride = isOverride;
        cloned.isOverrideDisabled = isOverrideDisabled;
        cloned.changed = changed;
    }
    
    protected void dispose() {
        context = null;
        parentField = null;
        changeListeners.clear();
        transientValueChangeListeners.clear();
        valueChangeListeners.clear();
    }
    
    @Override
    public Iterator<FormField> iterator() {
        return FormFieldIterator.singletonIterator(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (changed ? 1231 : 1237);
        result = prime * result
                + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result
                + ((displayPrompt == null) ? 0 : displayPrompt.hashCode());
        result = prime * result
                + ((fieldAccess == null) ? 0 : fieldAccess.hashCode());
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + keyIndex;
        result = prime * result + (optional ? 1231 : 1237);
        result = prime * result + rowIndex;
        result = prime * result + Float.floatToIntBits(weight);
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
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
            return false;
        if (displayPrompt == null) {
            if (other.displayPrompt != null)
                return false;
        } else if (!displayPrompt.equals(other.displayPrompt))
            return false;
        if (fieldAccess != other.fieldAccess)
            return false;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (keyIndex != other.keyIndex)
            return false;
        if (optional != other.optional)
            return false;
        if (rowIndex != other.rowIndex)
            return false;
        if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
            return false;
        return true;
    }
    
    protected static boolean strIsEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    protected static String valueToDisplayString(Object value) {
        String str = "";
        if (value == null) {
            str = "null";
        } else {
            if (value instanceof FormField) {
                str = ((FormField)value).getDisplayString();
            } else {
                str = value.toString();
            }
            if (value instanceof CharSequence || value instanceof FormEnum) {
                str = "\"" + str + "\"";
            }
        }
        return str;
    }

    public static interface ChangeListener {
        
        public void onChanged (boolean changed); 
        
    }
    
    public static abstract class ValueChangeListener implements Serializable {

        private static final long serialVersionUID = -3772014837874641680L;
        
        public ValueChangeListener() {}
        
        public abstract void onValueChanged (Object value); 
        
    }

}
