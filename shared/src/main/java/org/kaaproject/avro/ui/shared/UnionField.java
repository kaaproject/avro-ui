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
import java.util.Map;

public class UnionField extends FormField {

    private static final long serialVersionUID = -7020719983305986557L;

    private List<FormField> acceptableValues;
    
    private FormField defaultValue;
    
    private FormField value;
    
    public UnionField() {
        super();
        acceptableValues = new ArrayList<>();
    }
    
    public UnionField(String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
        acceptableValues = new ArrayList<>();
    }
    
    public FormField getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(FormField defaultValue) {
        this.defaultValue = defaultValue;
    }

    public FormField getValue() {
        return value;
    }

    public void setValue(FormField value) {
        if (value != null) {
            int index = -1;
            for (int i=0;i<acceptableValues.size();i++) {
                if (acceptableValues.get(i).isSameType(value)) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                this.value = value;
                this.acceptableValues.set(index, value);
                fireChanged();
            }
            else {
                throw new IllegalArgumentException("Value type not in list of union types!");
            }
        }
        else {
            this.value = null;
            fireChanged();
        }
    }
    
    public List<FormField> getAcceptableValues() {
        return acceptableValues;
    }
    
    public void setAcceptableValues(List<FormField> acceptableValues) {
        this.acceptableValues = acceptableValues;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.UNION;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected boolean valid() {
        return value != null && value.isValid();
    }

    @Override
    protected FormField createInstance(boolean child) {
        return new UnionField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean child) {
        super.copyFields(cloned, child);
        UnionField clonedUnionField = (UnionField)cloned;
        for (FormField acceptableValue : acceptableValues) {
            clonedUnionField.acceptableValues.add(acceptableValue.clone(child));
        }
        clonedUnionField.defaultValue = defaultValue;
        clonedUnionField.setValue(value != null ? value.clone(child) : null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        UnionField other = (UnionField) obj;
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
