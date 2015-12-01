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

import java.util.List;

public class DependenciesField extends FormField {

    private static final long serialVersionUID = 863727275754175198L;

    public DependenciesField() {
        super();
    }

    public DependenciesField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
    }

    public List<FqnVersion> getValue() {
        if (isRootChild()) {
            return context.getCtlDependenciesList();
        } else {
            return null;
        }
    }
    
    public void setValue(List<FqnVersion> fqnVersions) {
        if (isRootChild()) {
            context.setCtlDependenciesList(fqnVersions);
        }
    }
    
    @Override
    public String getDisplayString() {
        return super.getDisplayString();
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.DEPENDENCIES;
    }
    
    @Override
    public boolean isNull() {
        return getValue() == null;
    }

    @Override
    protected FormField createInstance() {
        return new DependenciesField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
    }

    @Override
    protected boolean valid() {
        return getValue() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DependenciesField other = (DependenciesField) obj;
        if (getValue() == null) {
            if (other.getValue() != null)
                return false;
        } else if (!getValue().equals(other.getValue()))
            return false;        
        return true;
    }

}
