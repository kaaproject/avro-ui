package org.kaaproject.avro.ui.shared;

import java.text.ParseException;

public class FixedField extends FqnField {
    
    private static final long serialVersionUID = -8517277173705569328L;

    private String defaultValue;
    
    private String value;
    
    private int fixedSize;

    public FixedField() {
        super();
    }
    
    public FixedField(String fieldName, 
            String displayName, 
            String schema,
            boolean optional) {
        super(fieldName, displayName, schema, optional);
    }
    
    public byte[] getBytes() throws ParseException {
        return Base64Utils.fromBase64(value);
    }
    
    public void setBytes(byte[] data) {
        this.value = Base64Utils.toBase64(data);
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
     public String getValue() {
        return value;
    }
 
    public void setValue(String value) {
        this.value = value;
        fireChanged();
    }
    
    public int getFixedSize() {
        return fixedSize;
    }

    public void setFixedSize(int fixedSize) {
        this.fixedSize = fixedSize;
    }
    
    public int getStringMaxSize() {
        return 4 * ((fixedSize + 2) / 3);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.FIXED;
    }

    @Override
    public boolean isNull() {
        return strIsEmpty(value);
    }
    
    @Override
    public boolean isValid() {
        if ((isOptional() && isNull()) || 
        		((isOverride() && !isChanged()))) {
            return true;
        } else {
            return valid();
        }
    }

    @Override
    protected boolean valid() {
        try {
            byte[] data = getBytes();
            return data != null && data.length == fixedSize;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    protected FormField createInstance() {
        return new FixedField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        FixedField clonedFixedField = (FixedField)cloned;
        clonedFixedField.defaultValue = defaultValue;
        clonedFixedField.value = value;
        clonedFixedField.fixedSize = fixedSize;
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
        FixedField other = (FixedField) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
