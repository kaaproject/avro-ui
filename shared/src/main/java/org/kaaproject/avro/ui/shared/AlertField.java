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

public class AlertField extends FormField {

    private static final long serialVersionUID = -5439254303335483343L;
    private String value;
    
    public AlertField() {
        super();
    }
    
    public AlertField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if ((this.value == null && value != null) || 
            (this.value != null && !this.value.equals(value))) {
                this.value = value;
                fireValueChanged(this.value);
        }
        fireChanged();
    }

    @Override
    public String getDisplayString() {
        return super.getDisplayString() + ": \"" + valueToDisplayString(value) + "\"";
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.ALERT;
    }
    
    @Override
    public boolean isNull() {
        return value == null;
    }
    
    @Override
    protected FormField createInstance() {
        return new AlertField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
        AlertField clonedStringField = (AlertField)cloned;
        clonedStringField.value = value;
    }

    @Override
    protected boolean valid() {
        return !strIsEmpty(value);
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
        AlertField other = (AlertField) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }



}
