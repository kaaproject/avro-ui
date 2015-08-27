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


public class LongField extends FormField {

    private static final long serialVersionUID = -5046250549233854347L;

    private Long defaultValue;
    
    private Long value;
    
    public LongField() {
        super();
    }
    
    public LongField(String fieldName, 
            String displayName,
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
    }
    
    public Long getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Long defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
        fireChanged();
    }
    
    @Override
    public String getDisplayString() {
        return super.getDisplayString() + ": " + valueToDisplayString(value);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.LONG;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected FormField createInstance() {
        return new LongField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        LongField clonedLongField = (LongField)cloned;
        clonedLongField.defaultValue = defaultValue;
        clonedLongField.value = value;
    }

    @Override
    protected boolean valid() {
        return value != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LongField other = (LongField) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        } else if (!defaultValue.equals(other.defaultValue))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }



}
