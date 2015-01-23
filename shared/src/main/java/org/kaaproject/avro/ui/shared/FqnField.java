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

    private String typeName;
    
    private String typeNamespace;
    
    public FqnField() {
        super();
    }
    
    public FqnField(String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
    }
    
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(String typeNamespace) {
        this.typeNamespace = typeNamespace;
    }

    @Override
    public String getTypeFullname() {
        return typeNamespace + "." + typeName;
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        FqnField clonedFqnField = (FqnField)cloned;
        clonedFqnField.typeName = typeName;
        clonedFqnField.typeNamespace = typeNamespace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((typeName == null) ? 0 : typeName.hashCode());
        result = prime * result
                + ((typeNamespace == null) ? 0 : typeNamespace.hashCode());
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
        if (typeName == null) {
            if (other.typeName != null)
                return false;
        } else if (!typeName.equals(other.typeName))
            return false;
        if (typeNamespace == null) {
            if (other.typeNamespace != null)
                return false;
        } else if (!typeNamespace.equals(other.typeNamespace))
            return false;
        return true;
    }
    
}
