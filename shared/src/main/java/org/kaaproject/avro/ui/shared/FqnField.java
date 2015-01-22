package org.kaaproject.avro.ui.shared;

import java.util.Map;


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
    public boolean isSameType(FormField otherRecord) {
        if (super.isSameType(otherRecord)) {
            FqnField otherFqnField = (FqnField)otherRecord;
            return typeNamespace.equals(otherFqnField.getTypeNamespace()) &&
                    typeName.equals(otherFqnField.getTypeName());
        } else {
            return false;
        }
    }
    
    public String getTypeFullname() {
        return typeNamespace + "." + typeName;
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean child) {
        super.copyFields(cloned, child);
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
