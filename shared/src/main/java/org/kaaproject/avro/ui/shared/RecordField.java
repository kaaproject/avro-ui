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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordField extends FqnField {

    private static final long serialVersionUID = -2006331166074707248L;
    
    private List<FormField> value;
    
    private RecordField rootRecord;
    
    private Map<String, RecordField> recordsMetadata;
    
    private boolean isNull = true;
    
    public RecordField() {
        super();
    }
    
    public RecordField(RecordField rootRecord) {
        super();
        this.rootRecord = rootRecord;
        if (rootRecord == null) {
            recordsMetadata = new HashMap<>();
            this.rootRecord = this;
        } else {
            this.rootRecord = rootRecord;
        }
        value = new ArrayList<>();
    }
    
    public RecordField(RecordField rootRecord, 
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
        value = new ArrayList<>();
        if (rootRecord == null) {
            recordsMetadata = new HashMap<>();
            this.rootRecord = this;
        } else {
            this.rootRecord = rootRecord;
        }
    }
    
    public void putRecordMetadata(String fqn, RecordField field) {
        recordsMetadata.put(fqn, field);
    }
    
    public boolean containsRecordMetadata(String fqn) {
        return recordsMetadata.containsKey(fqn);
    }
    
    public RecordField getRecordMetadata(String fqn) {
        return recordsMetadata.get(fqn);
    }
    
    public List<FormField> getValue() {
        return value;
    }
    
    public List<FormField> getFieldsWithAccess(FieldAccess... accesses) {
        List<FormField> result = new ArrayList<FormField>();
        for (FormField field : value) {
            for (FieldAccess access : accesses) {
                if (field.getFieldAccess() == access) {
                    result.add(field);
                    break;
                }
            }
        }
        return result;
    }
    
    public List<FormField> getKeyIndexedFields() {
        List<FormField> result = new ArrayList<FormField>();
        List<FormField> activeFields = getFieldsWithAccess(FieldAccess.EDITABLE, 
                FieldAccess.READ_ONLY);
        for (FormField field : activeFields) {
            if (field.getKeyIndex() > -1) {
                result.add(field);
            }
        }
        Collections.sort(result, new Comparator<FormField>() {
            @Override
            public int compare(FormField o1, FormField o2) {
                return o1.getKeyIndex() - o2.getKeyIndex();
            }
            
        });
        return result;
    }

    public void addField(FormField field) {
        value.add(field);
    }
    
    @Override
    public FieldType getFieldType() {
        return FieldType.RECORD;
    }
    
    @Override
    public boolean isNull() {
        return isNull;
    }

    @Override
    protected FormField createInstance(boolean child) {
//        if (child) {
//            RecordField recordField = rootRecord.getRecordMetadata(getTypeFullname());
//            recordField.isCloned = false;
//            return recordField;
//        } else {
            return new RecordField(rootRecord);
      //  }
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean child) {
        super.copyFields(cloned, true);
        RecordField clonedRecordField = (RecordField)cloned;
        if (!child) {            
            for (FormField field : value) {
                clonedRecordField.value.add(field.clone(true));
            }
            clonedRecordField.isNull = false;
        } else {
            clonedRecordField.isNull = true;
        }
    }
    
    public void setNotNull() {
        if (isNull) {
            RecordField recordField = rootRecord.getRecordMetadata(getTypeFullname());
            for (FormField field : recordField.getValue()) {
                value.add(field.clone(true));
            }
            isNull = false;
        }
    }

    @Override
    protected boolean valid() {
        if (isNull) {
            return true;
        }
        boolean valid = true;
        for (FormField field : value) {
            valid &= field.isValid();
        }
        return valid;
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
        RecordField other = (RecordField) obj;
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
