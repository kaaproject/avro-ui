package org.kaaproject.avro.ui.shared;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

public class BytesField extends FormField {

    private static final long serialVersionUID = -3828240264357032609L;
    
    private String defaultValue;
    
    private String value;
    
    public BytesField() {
        super();
    }
    
    public BytesField(String fieldName, 
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
    
    @Override
    public FieldType getFieldType() {
        return FieldType.BYTES;
    }

    @Override
    public boolean isNull() {
        return strIsEmpty(value);
    }

    @Override
    protected boolean valid() {
        try {
            byte[] data = getBytes();
            return data != null;
        } catch (ParseException e) {
            return false;
        }
    }
    
    @Override
    protected FormField createInstance(boolean child) {
        return new BytesField();
    }
    
    @Override
    protected void copyFields(FormField cloned, boolean child) {
        super.copyFields(cloned, child);
        BytesField clonedBytesField = (BytesField)cloned;
        clonedBytesField.defaultValue = defaultValue;
        clonedBytesField.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((defaultValue == null) ? 0 : defaultValue.hashCode());
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
        BytesField other = (BytesField) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        } else if (!defaultValue.equals(other.defaultValue))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
