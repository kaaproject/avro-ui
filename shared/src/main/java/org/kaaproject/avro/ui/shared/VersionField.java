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

public class VersionField extends FormField {

    private static final long serialVersionUID = 863727275754175198L;

    private Integer value;
    
    public VersionField() {
        super();
    }

    public VersionField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        if ((this.value == null && value != null) || 
                (this.value != null && !this.value.equals(value))) {
                    this.value = value;
                    fireValueChanged(this.value);
        }
        fireChanged();
    }
    
    @Override
    public String getDisplayString() {
        return super.getDisplayString() + ": " + valueToDisplayString(value);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.VERSION;
    }
    
    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected FormField createInstance() {
        return new VersionField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
        VersionField clonedIntegerField = (VersionField)cloned;
        clonedIntegerField.value = value;
    }

    @Override
    protected boolean valid() {
        return value != null;
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
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VersionField other = (VersionField) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
