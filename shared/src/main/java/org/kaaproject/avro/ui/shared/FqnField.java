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

public abstract class FqnField extends FormField {

    private static final long serialVersionUID = 7473310438849667183L;

    protected Fqn fqn;
    
    public FqnField() {
        super();
    }
    
    public FqnField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
    }
    
    public Fqn getFqn() {
        return fqn;
    }

    public void setFqn(Fqn fqn) {
        this.fqn = fqn;
    }

    @Override
    public String getTypeFullname() {
        return fqn.getFqnString();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
        FqnField clonedFqnField = (FqnField)cloned;
        clonedFqnField.fqn = fqn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((fqn == null) ? 0 : fqn.hashCode());
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
        FqnField other = (FqnField) obj;
        if (fqn == null) {
            if (other.fqn != null)
                return false;
        } else if (!fqn.equals(other.fqn))
            return false;
        return true;
    }
    
}
