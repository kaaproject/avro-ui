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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class RecordField extends FqnField {

    private static final long serialVersionUID = -2006331166074707248L;

    private static final String FQN_FIELD = "fqn";
    private static final String RECORD_NAMESPACE_FIELD = "recordNamespace";
    private static final String RECORD_NAME_FIELD = "recordName";
    private static final String DISPLAY_NAME_FIELD = "displayName";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String VERSION_FIELD = "version";
    
    private List<FormField> value;
    private boolean isNull = true;
    private boolean isTypeHolder = false;
    private boolean isTypeConsumer = false;
    
    private FqnValueChangeListener fqnValueChangeListener;
    private VersionValueChangeListener versionValueChangeListener;
    private ConsumedFqnValueChangeListener consumedFqnValueChangeListener;
    
    public RecordField() {
        super();
        this.value = new ArrayList<>();
    }
    
    public RecordField(FormContext context,
            String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(context, fieldName, displayName, schema, optional);
        this.value = new ArrayList<>();
    }
    
    public boolean isRoot() {
        if (context != null && context.getRootRecord() != null) {
            return this.id == context.getRootRecord().getId();
        }
        return false;
    }
    
    public void orderSchemaTypes() {
        if (context != null) {
            context.orderSchemaTypes();
        }
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
    
    public void setIsTypeHolder(boolean isTypeHolder) {
        this.isTypeHolder = isTypeHolder;
    }
    
    public boolean isTypeHolder() {
        return isTypeHolder;
    }
    
    public void setIsTypeConsumer(boolean isTypeConsumer) {
        this.isTypeConsumer = isTypeConsumer;
    }
    
    public boolean isTypeConsumer() {
        return isTypeConsumer;
    }
    
    public Fqn getDeclaredFqn() {
        FormField nameField = getFieldByName(RECORD_NAME_FIELD);
        FormField namespaceField = getFieldByName(RECORD_NAMESPACE_FIELD);
        if (nameField != null && namespaceField != null) {
            String name = ((StringField)nameField).getValue();
            String namespace = ((StringField)namespaceField).getValue();
            if (strIsEmpty(namespace) && !isRoot() 
                    && context != null && context.getRootRecord() != null
                    && context.getRootRecord().getDeclaredFqn() != null) {
                namespace = context.getRootRecord().getDeclaredFqn().getNamespace();
            }
            if (!strIsEmpty(name) && !strIsEmpty(namespace)) {
                return new Fqn(namespace, name);
            }
        }
        return null;
    }
    
    public FqnKey getConsumedFqnKey() {
        FormField fqnField = getFieldByName(FQN_FIELD);
        if (fqnField != null) {
            return ((FqnReferenceField)fqnField).getValue();
        }
        return null;
    }
    
    public void updateConsumedFqnKey(FqnKey key) {
        FormField fqnField = getFieldByName(FQN_FIELD);
        if (fqnField != null) {
            ((FqnReferenceField)fqnField).setValue(key);
        }
    }
    
    public String getDisplayNameFieldValue() {
    	FormField displayNameField = getFieldByName(DISPLAY_NAME_FIELD);
    	if (displayNameField != null) {
    		return ((StringField)displayNameField).getValue();
    	}
    	return null;
    }
    
    public void setDisplayNameFieldOptional(boolean optional) {
    	FormField displayNameField = getFieldByName(DISPLAY_NAME_FIELD);
    	if (displayNameField != null) {
    		displayNameField.setOptional(optional);
    	}
    }

    public String getDescriptionFieldValue() {
    	FormField descriptionField = getFieldByName(DESCRIPTION_FIELD);
    	if (descriptionField != null) {
    		return ((StringField)descriptionField).getValue();
    	}
    	return null;
    }

    public Integer getVersion() {
        if (isRoot() && context.isCtlSchema()) {
            FormField versionField = getFieldByName(VERSION_FIELD);
            if (versionField != null) {
                return ((VersionField)versionField).getValue();
            }
        }
        return null;
    }
    
    public void updateVersion(Integer version) {
        if (isRoot() && context.isCtlSchema()) {
            FormField versionField = getFieldByName(VERSION_FIELD);
            if (versionField != null) {
                ((VersionField)versionField).setValue(version);
            }
        }
    }
    
    public FormField getFieldByName(String name) {
        if (!isNull) {
            for (int i=0;i<value.size();i++) {
                if (value.get(i).getFieldName().equals(name)) {
                    return value.get(i);
                }
            }
        }
        return null;
    }
    
    public FormField getFieldByType(FieldType type) {
        if (!isNull) {
            for (int i=0;i<value.size();i++) {
                if (value.get(i).getFieldType() == type) {
                    return value.get(i);
                }
            }
        }
        return null;
    }
    
    public int getFieldIndex(String name) {
        if (!isNull) {
            for (int i=0;i<value.size();i++) {
                if (value.get(i).getFieldName().equals(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void addField(FormField field) {
        value.add(field);
        field.setParentField(this);
        isNull = false;
    }
    
    public void insertFieldAtIndex(FormField field, int index) {
        if (!isNull && index > -1 && index <= value.size()) {
            value.add(index, field);
            field.setParentField(this);
        }
    }
    
    public boolean removeFieldByName(String name) {
        if (!isNull) {
            int index = getFieldIndex(name);
            if (index > -1) {
                FormField field = value.remove(index);
                if (field != null) {
                    field.dispose();
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean hideFieldByName(String name) {
        if (!isNull) {
            FormField field = getFieldByName(name);
            if (field != null) {
                field.setFieldAccess(FieldAccess.HIDDEN);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getDisplayString() {
        String str = super.getDisplayString();
        if (isNull) {
            str += ": null";
        } else {
            List<FormField> fields = getKeyIndexedFields();
            if (fields.isEmpty()) {
                fields = getFieldsWithAccess(FieldAccess.EDITABLE, FieldAccess.READ_ONLY);
            }
            if (!fields.isEmpty()) {
                str += ": { ";
                for (int i=0;i<fields.size();i++) {
                    FormField field = fields.get(i);
                    if (i>0) {
                        str += ", ";
                    }
                    str += field.getDisplayString();
                }
                str += " }";
            }
        }
        return str;
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
    public void finalizeMetadata() {
    	create();
    }
   
    @Override
	public void disableOverride() {
		super.disableOverride();
		for (FormField field : value) {
			field.disableOverride();
		}
	}

	@Override
    protected FormField createInstance() {
        return new RecordField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean deepCopy) {
        super.copyFields(cloned, deepCopy);
        RecordField clonedRecordField = (RecordField)cloned;
        clonedRecordField.isTypeHolder = isTypeHolder;
        clonedRecordField.isTypeConsumer = isTypeConsumer;
        if (deepCopy) {
            for (FormField field : value) {
                FormField clonedField = field.clone();
                clonedField.setParentField(clonedRecordField);
                clonedRecordField.value.add(clonedField);
            }
            clonedRecordField.isNull = false;
            clonedRecordField.registerListeners();
        }
    }
    
    public void create() {
        if (isNull) {
            RecordField recordField = context.getRecordMetadata(getFqn());
            for (FormField field : recordField.getValue()) {
                FormField newField = field.clone();
                newField.setParentField(this);
                value.add(newField);
            }
            if (isOverrideDisabled()) {
            	disableOverride();
            }
            isNull = false;
            fireChanged();
            registerListeners();
        }
    }
    
    protected void registerListeners() {
        if (isTypeHolder) {
            fqnValueChangeListener = new FqnValueChangeListener(this);
            FormField nameField = getFieldByName(RECORD_NAME_FIELD);
            FormField namespaceField = getFieldByName(RECORD_NAMESPACE_FIELD);
            if (nameField != null && namespaceField != null) {
                nameField.addValueChangeListener(fqnValueChangeListener);
                namespaceField.addValueChangeListener(fqnValueChangeListener);
                if (isRoot()) {
                    namespaceField.setOptional(false);
                }
            }
            if (isRoot() && context.isCtlSchema()) {
                versionValueChangeListener = new VersionValueChangeListener(this);
                FormField versionField = getFieldByName(VERSION_FIELD);
                if (versionField != null) {
                    versionField.addValueChangeListener(versionValueChangeListener);
                }
            }
        } else if (isTypeConsumer) {
            context.registerTypeConsumer(this);
            if (context.isCtlSchema()) {
                consumedFqnValueChangeListener = new ConsumedFqnValueChangeListener(this);
                FormField fqnField = getFieldByName(FQN_FIELD);
                if (fqnField != null) {
                    fqnField.addValueChangeListener(consumedFqnValueChangeListener);
                }
            }
        }
    }
    
    private void showAlert(String alert) {
        FormField alertField = getFieldByType(FieldType.ALERT);
        if (alertField != null) {
            ((AlertField)alertField).setValue(alert);
        }
    }
    
    private void clearAlert() {
        showAlert(null);
    }
    
    public static class FqnValueChangeListener extends ValueChangeListener {
        
        private static final long serialVersionUID = -7766362467689033532L;
        
        private RecordField recordField;
        
        public FqnValueChangeListener() {
        }
        
        public FqnValueChangeListener(RecordField recordField) {
            this.recordField = recordField;
        }

        @Override
        public void onValueChanged(Object value) {
            if (validateFqnAndVersion(recordField, true)) {
                recordField.clearAlert();
                recordField.context.updateTypeHolder(recordField);
            }
        }
    }
    
    public static class VersionValueChangeListener extends ValueChangeListener {
        
        private static final long serialVersionUID = -7766362467689033532L;
        
        private RecordField recordField;
        
        public VersionValueChangeListener() {
        }
        
        public VersionValueChangeListener(RecordField recordField) {
            this.recordField = recordField;
        }

        @Override
        public void onValueChanged(Object value) {
            if (validateFqnAndVersion(recordField, false)) {
                recordField.clearAlert();
                recordField.context.updateTypeHolder(recordField);
            }
        }
    }
    
    public static class ConsumedFqnValueChangeListener extends ValueChangeListener {

        private static final long serialVersionUID = -3906282204505179394L;
        
        private RecordField recordField;
        
        public ConsumedFqnValueChangeListener() {
        }
        
        public ConsumedFqnValueChangeListener(RecordField recordField) {
            this.recordField = recordField;
        }

        @Override
        public void onValueChanged(Object value) {
            recordField.context.updateCtlDependencies();
        }
    }
    
    private static boolean validateFqnAndVersion(RecordField recordField, boolean checkVersion) {
        Fqn fqn = recordField.getDeclaredFqn();
        if (recordField.isRoot() && recordField.context.isCtlSchema()) {
            Integer version = recordField.getVersion();
            if (recordField.context.isFqnAlreadyDeclared(recordField.id, fqn, true)) {
                recordField.showAlert("FQN '" + fqn.getFqnString() + "' is already declared!");
                return false;
            } else if (!recordField.context.checkIsVersionAvailable(fqn, version)) {
                recordField.showAlert("FQN '" + fqn.getFqnString() + "' with version " + version.intValue() + " is already declared!");
                return false;
            }
            if (checkVersion && version == null && fqn != null) {
                version = recordField.context.getMaxVersion(fqn);
                if (version != null) {
                    recordField.updateVersion(version.intValue()+1);
                }
            }
        } else if (recordField.context.isFqnAlreadyDeclared(recordField.id, fqn, true)) {
            recordField.showAlert("FQN '" + fqn.getFqnString() + "' is already declared!");
            return false;
        }
        return true;
    }
    
    public boolean setNull() {
        if (!isNull) {
            if (context != null) {
                if (isTypeHolder) {
                    if (!context.removeTypeHolder(this)) {
                        return false;
                    }
                    fqnValueChangeListener = null;
                    versionValueChangeListener = null;
                } else if (isTypeConsumer) {
                    context.unregisterTypeConsumer(id);
                    consumedFqnValueChangeListener = null;
                }
            }
            for (FormField field : value) {
                field.dispose();
            }
        	value.clear();
        	isNull = true;
        	fireChanged();
        }
    	return true;
    }
    
    @Override
    public void dispose() {
        if (setNull()) {
            super.dispose();
        }
    }
    
    @Override
    public boolean isValid() {
        if (isOverride() && !isChanged()) {
            return true;
        } else if (isOptional()) {
            if (!isNull) {
                return valid();
            } else {
                return true;
            }
        } else {
            return !isNull && valid();
        }
    }

    @Override
    protected boolean valid() {
        boolean valid = true;
        if (isTypeHolder) {
            if (!validateFqnAndVersion(this, false)) {
                valid = false;
            } else {
                clearAlert();
            }
        }
        for (FormField field : value) {
            valid &= field.isValid();
        }
        return valid;
    }
    
    @Override
    public Iterator<FormField> iterator() {
        return FormFieldIterator.concatItemWithCollection(this, value).iterator();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isNull ? 1231 : 1237);
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
        RecordField other = (RecordField) obj;
        if (isNull != other.isNull)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
