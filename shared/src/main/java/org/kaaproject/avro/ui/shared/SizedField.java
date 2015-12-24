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


public abstract class SizedField extends FormField {

    private static final long serialVersionUID = 6539576598668221454L;
    
    public static final int DEFAULT_MAX_LENGTH = -1;
    
    private Integer maxLength;
    
    public SizedField() {
        super();
    }
    
    public SizedField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
    }
    
    public int getMaxLength() {
        if (maxLength != null) {
            return maxLength.intValue();
        }
        else {
            return DEFAULT_MAX_LENGTH;
        }
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = Integer.valueOf(maxLength);
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
        SizedField clonedSizedField = (SizedField)cloned;
        clonedSizedField.maxLength = maxLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((maxLength == null) ? 0 : maxLength.hashCode());
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
        SizedField other = (SizedField) obj;
        if (maxLength == null) {
            if (other.maxLength != null)
                return false;
        } else if (!maxLength.equals(other.maxLength))
            return false;
        return true;
    }
    
    

}
