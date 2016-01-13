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

package org.kaaproject.avro.ui.converter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
import org.kaaproject.avro.ui.shared.AlertField;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.ArrayField.OverrideStrategy;
import org.kaaproject.avro.ui.shared.Base64Utils;
import org.kaaproject.avro.ui.shared.BooleanField;
import org.kaaproject.avro.ui.shared.BytesField;
import org.kaaproject.avro.ui.shared.DependenciesField;
import org.kaaproject.avro.ui.shared.DoubleField;
import org.kaaproject.avro.ui.shared.EnumField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FixedField;
import org.kaaproject.avro.ui.shared.FloatField;
import org.kaaproject.avro.ui.shared.FormContext;
import org.kaaproject.avro.ui.shared.FormEnum;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FormField.FieldAccess;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnKey;
import org.kaaproject.avro.ui.shared.FqnReferenceField;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.avro.ui.shared.IntegerField;
import org.kaaproject.avro.ui.shared.LongField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.StringField;
import org.kaaproject.avro.ui.shared.StringField.InputType;
import org.kaaproject.avro.ui.shared.UnionField;
import org.kaaproject.avro.ui.shared.VersionField;

/**
 * The Class FormAvroConverter.
 */
public class FormAvroConverter implements ConverterConstants {
    
    /**
     * Creates the record field from schema.
     *
     * @param schema the schema
     * @return the record field
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RecordField createRecordFieldFromSchema(Schema schema) throws IOException {
        return createRecordFieldFromSchema(schema, null);
    }

    /**
     * Creates the record field from schema.
     *
     * @param schema the schema
     * @param ctlSource the ctl source
     * @return the record field
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RecordField createRecordFieldFromSchema(Schema schema, CtlSource ctlSource) throws IOException {
        FormContext context = ctlSource != null ? new FormContext(ctlSource.getCtlTypes()) : new FormContext();
        FormField formField = createFieldFromSchema(context, schema, null);
        if (formField instanceof RecordField) {
            return (RecordField)formField;
        } else {
            throw new IllegalArgumentException("Schema " + schema.getFullName() + " is not a record schema!");
        }
    }
    
    /**
     * Creates the generic record from record field.
     *
     * @param recordField the record field
     * @return the generic record
     */
    public static GenericRecord createGenericRecordFromRecordField(RecordField recordField) {
        Schema schema = new Schema.Parser().parse(recordField.getSchema());
        GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        for (FormField formField : recordField.getValue()) {
            String fieldName = formField.getFieldName();
            Field field = schema.getField(fieldName);
            Object fieldValue = convertValue(formField, field.schema());
            builder.set(field, fieldValue);
        }
        GenericRecord record = builder.build();        
        return record;
    }
    
    /**
     * Creates the record field from generic record.
     *
     * @param record the record
     * @return the record field
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RecordField createRecordFieldFromGenericRecord(GenericRecord record) throws IOException {
        return createRecordFieldFromGenericRecord(record, null);
    }
    
    /**
     * Creates the record field from generic record.
     *
     * @param record the record
     * @param ctlSource the ctl source
     * @return the record field
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RecordField createRecordFieldFromGenericRecord(GenericRecord record, CtlSource ctlSource) throws IOException {
        Schema schema = record.getSchema();
        RecordField formData = createRecordFieldFromSchema(schema, ctlSource);
        fillRecordFieldFromGenericRecord(formData.getContext(), formData, record);
        return formData;
    }
    
    /**
     * Creates the field from schema.
     *
     * @param context the context
     * @param schema the schema
     * @param fieldType the field type
     * @return the form field
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static FormField createFieldFromSchema(FormContext context, Schema schema, FieldType fieldType) throws IOException {
        FormField formField = null;
        String fieldName = schema.getName();
        String displayName = fieldName;
        JsonNode displayNameVal = schema.getJsonProp(DISPLAY_NAME);
        if (displayNameVal != null && displayNameVal.isTextual()) {
            String displayNameString = displayNameVal.asText().trim();
            if (displayNameString.length() > 0) {
                displayName = displayNameString;
            }
        }
        boolean optional = isNullTypeSchema(schema);
        boolean isOverride = isOverrideTypeSchema(schema);
        
        fieldType = fieldType == null ? toFieldType(schema) : fieldType;
        Schema fieldTypeSchema = getFieldTypeSchema(schema);
        String fieldTypeSchemaString = SchemaFormAvroConverter.createSchemaString(fieldTypeSchema, false);
        if (fieldType == FieldType.UNION) {
            UnionField unionField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            List<FormField> acceptableValues = new ArrayList<>();
            List<Schema> acceptableTypes = fieldTypeSchema.getTypes();
            for (int i=0;i<acceptableTypes.size();i++) {
                if (!isOverrideType(acceptableTypes.get(i)) && acceptableTypes.get(i).getType() != Schema.Type.NULL) {
                    FormField acceptableValue = createFieldFromSchema(context, acceptableTypes.get(i), null);
                    acceptableValues.add(acceptableValue);
                }
            }
            unionField.setAcceptableValues(acceptableValues);
            formField = unionField;
        } else if (fieldType == FieldType.RECORD) {          
            boolean isRootRecord = false;
            if (!context.containsRecordMetadata(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName())) {
                boolean isTypeHolder = false;
                boolean isTypeConsumer = false;
                JsonNode isTypeHolderVal = fieldTypeSchema.getJsonProp(IS_TYPE_HOLDER);
                if (isTypeHolderVal != null && isTypeHolderVal.isBoolean()) {
                    isTypeHolder = isTypeHolderVal.asBoolean();
                }
                JsonNode isTypeConsumerVal = fieldTypeSchema.getJsonProp(IS_TYPE_CONSUMER);
                if (isTypeConsumerVal != null && isTypeConsumerVal.isBoolean()) {
                    isTypeConsumer = isTypeConsumerVal.asBoolean();
                }
                RecordField newRecordField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
                newRecordField.setFqn(new Fqn(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName()));
                newRecordField.setIsTypeHolder(isTypeHolder);
                newRecordField.setIsTypeConsumer(isTypeConsumer);
                context.putRecordMetadata(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName(), newRecordField);
                if (context.getRootRecord() == null) {
                    context.setRootRecord(newRecordField);
                    isRootRecord = true;
                }
                parseFields(context, newRecordField, fieldTypeSchema);
            }
            RecordField metadata = context.getRecordMetadata(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName());
            RecordField recordField = (RecordField)metadata.clone();
            recordField.setFieldName(fieldName);
            recordField.setDisplayName(displayName);
            recordField.setOptional(optional);
            if (isRootRecord) {
                context.setRootRecord(recordField);
                recordField.finalizeMetadata();
            }
            formField = recordField;
        } else if (fieldType == FieldType.ARRAY) {
            ArrayField arrayField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            FormField elementMetadata = createFieldFromSchema(context, fieldTypeSchema.getElementType(), null);
            arrayField.setElementMetadata(elementMetadata);
            formField = arrayField;
        } else if (fieldType == FieldType.ENUM) {
            EnumField enumField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            enumField.setFqn(new Fqn(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName()));
            List<String> enumSymbols = fieldTypeSchema.getEnumSymbols();
            List<FormEnum> enumValues = new ArrayList<>(enumSymbols.size());
            for (int i=0;i<enumSymbols.size();i++) {
                String enumSymbol = enumSymbols.get(i);
                String displayValue = enumSymbol;
                FormEnum formEnum = new FormEnum(enumSymbol, displayValue);
                enumValues.add(formEnum);
            }
            enumField.setEnumValues(enumValues);
            formField = enumField;
        } else if (fieldType == FieldType.BYTES) {
            BytesField bytesField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = bytesField;
        } else if (fieldType == FieldType.FIXED) {
            FixedField fixedField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            fixedField.setFqn(new Fqn(fieldTypeSchema.getNamespace(), fieldTypeSchema.getName()));
            fixedField.setFixedSize(fieldTypeSchema.getFixedSize());
            formField = fixedField;
        } else if (fieldType == FieldType.BOOLEAN) {
            BooleanField booleanField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = booleanField;
        } else if (fieldType == FieldType.STRING){
            StringField stringField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = stringField;
        } else if (fieldType == FieldType.INT) {
            IntegerField integerField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = integerField;
        } else if (fieldType == FieldType.LONG) {
            LongField longField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = longField;
        } else if (fieldType == FieldType.FLOAT) {
            FloatField floatField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = floatField;
        } else if (fieldType == FieldType.DOUBLE) {
            DoubleField doubleField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = doubleField;
        } else if (fieldType == FieldType.TYPE_REFERENCE) { 
            FqnReferenceField fqnReferenceField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = fqnReferenceField;
        } else if (fieldType == FieldType.ALERT) { 
            AlertField alertField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = alertField;
        } else if (fieldType == FieldType.VERSION) { 
            VersionField versionField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = versionField;
        } else if (fieldType == FieldType.DEPENDENCIES) { 
            DependenciesField dependenciesField = createField(context, fieldType, fieldName, displayName, fieldTypeSchemaString, optional, isOverride);
            formField = dependenciesField;
        } 
        return formField;
    }
    
    /**
     * Parses the fields.
     *
     * @param context the context
     * @param formData the form data
     * @param schema the schema
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void parseFields(FormContext context, RecordField formData, Schema schema) throws IOException {
        List<Field> schemaFields = schema.getFields();
        java.util.Set<String> schemaFieldNames = new java.util.HashSet<>();

        for (Field field : schemaFields) {
            String schemaFieldName = field.name().toLowerCase();
            if (!schemaFieldNames.contains(schemaFieldName)) {
                schemaFieldNames.add(schemaFieldName);
            } else {
                throw new IllegalArgumentException("Duplicate field name: " + schemaFieldName);
            }
            FieldType fieldType;
            if (isTypeReferenceType(field)) {
                fieldType = FieldType.TYPE_REFERENCE;
            } else if (isAlertType(field)) {
                fieldType = FieldType.ALERT;
            } else if (isVersionType(field)) {
                fieldType = FieldType.VERSION;
            } else if (isDependenciesType(field)) {
                fieldType = FieldType.DEPENDENCIES;
            } else {
                fieldType = toFieldType(field.schema());
            }
            FormField formField = createFieldFromSchema(context, field.schema(), fieldType);
            
            String fieldName = field.name();
            String displayName = fieldName;
            
            JsonNode displayNameVal = field.getJsonProp(DISPLAY_NAME);
            if (displayNameVal != null && displayNameVal.isTextual()) {
                String displayNameString = displayNameVal.asText().trim();
                if (displayNameString.length() > 0) {
                    displayName = displayNameString;
                }
            }
            
            formField.setFieldName(fieldName);
            formField.setDisplayName(displayName);

            JsonNode displayPromptVal = field.getJsonProp(DISPLAY_PROMPT);
            if (displayPromptVal != null && displayPromptVal.isTextual()) {
                formField.setDisplayPrompt(displayPromptVal.asText());
            }
            
            JsonNode weightVal = field.getJsonProp(WEIGHT);
            if (weightVal != null && weightVal.isNumber()) {
                Number weight = weightVal.getNumberValue();
                formField.setWeight(weight.floatValue());
            }
            JsonNode keyIndexVal = field.getJsonProp(KEY_INDEX);
            if (keyIndexVal != null && keyIndexVal.isNumber()) {
                Number keyIndex = keyIndexVal.getNumberValue();
                formField.setKeyIndex(keyIndex.intValue());
            }
            JsonNode fieldAccessVal = field.getJsonProp(FIELD_ACCESS);
            if (fieldAccessVal != null && fieldAccessVal.isTextual()) {
                String fieldAccess = fieldAccessVal.asText();
                formField.setFieldAccess(FieldAccess.valueOf(fieldAccess.toUpperCase()));
            }
            
            JsonNode defaultValueVal = field.getJsonProp(BY_DEFAULT);
            
            if (fieldType == FieldType.UNION) {
                UnionField unionField = (UnionField)formField;
                FormField defaultValue = convertUnionDefaultValue(unionField.getAcceptableValues(), defaultValueVal);
                unionField.setDefaultValue(defaultValue);
                if (!formField.isOverride()) {
                    unionField.setValue(defaultValue);
                }
            } else if (fieldType == FieldType.ARRAY) {
                ArrayField arrayField = (ArrayField)formField;
                JsonNode minRowCountVal = field.getJsonProp(MIN_ROW_COUNT);
                if (minRowCountVal != null && minRowCountVal.isInt()) {
                    arrayField.setMinRowCount(minRowCountVal.asInt());
                } else {
                    arrayField.setMinRowCount(0);
                }
                FormField metadata = arrayField.getElementMetadata();
                if (!metadata.getFieldType().isComplex()) {
                    metadata.setDisplayPrompt(arrayField.getDisplayPrompt());
                }                
                for (int i=0; i<arrayField.getMinRowCount();i++) {
                    arrayField.addArrayData(arrayField.getElementMetadata().clone());
                }
                if (arrayField.isOverride()) {
                    JsonNode overrideStrategyVal = field.getJsonProp(OVERRIDE_STRATEGY);
                    if (overrideStrategyVal != null && overrideStrategyVal.isTextual()) {
                        OverrideStrategy overrideStrategy = 
                                OverrideStrategy.valueOf(overrideStrategyVal.asText().toUpperCase());
                        arrayField.setOverrideStrategy(overrideStrategy);
                    } else {
                        arrayField.setOverrideStrategy(OverrideStrategy.REPLACE);
                    }
                }
            } else {
                
                if (fieldType == FieldType.ENUM) {
                    EnumField enumField = (EnumField)formField;
                    List<FormEnum> enumValues = enumField.getEnumValues();
                    JsonNode displayNamesNode = field.getJsonProp(DISPLAY_NAMES);
                    if (displayNamesNode != null && displayNamesNode.isArray()) {
                        for (int i=0;i<enumValues.size();i++) {
                            String displayValue = displayNamesNode.get(i).getTextValue();
                            enumValues.get(i).setDisplayValue(displayValue);
                        }
                    }
                    String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    enumField.setDefaultValueFromSymbol(defaultValue);
                    if (!formField.isOverride()) {
                        enumField.setValueFromSymbol(defaultValue);
                    }
                } else if (fieldType == FieldType.BYTES) {
                    BytesField bytesField = (BytesField)formField;
                    String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    bytesField.setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        bytesField.setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.FIXED) {
                    FixedField fixedField = (FixedField)formField;
                    String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    fixedField.setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        fixedField.setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.BOOLEAN) {
                    BooleanField booleanField = (BooleanField)formField;
                    Boolean defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    booleanField.setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        booleanField.setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.STRING) {
                    StringField stringField = (StringField) formField;
                    JsonNode maxLengthVal = field.getJsonProp(MAX_LENGTH);
                    if (maxLengthVal != null && maxLengthVal.isInt()) {
                        stringField.setMaxLength(maxLengthVal.asInt());
                    }
                    String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    stringField.setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        stringField.setValue(defaultValue);
                    }
                    JsonNode inputTypeNode = field.getJsonProp(INPUT_TYPE);
                    if (inputTypeNode != null && inputTypeNode.isTextual()) {
                        InputType inputType = InputType.valueOf(inputTypeNode.asText().toUpperCase());
                        stringField.setInputType(inputType);
                    }
                } else if (fieldType == FieldType.INT) {
                    Integer defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    ((IntegerField) formField).setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        ((IntegerField) formField).setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.LONG) {
                    Long defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    ((LongField) formField).setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        ((LongField) formField).setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.FLOAT) {
                    Float defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    ((FloatField) formField).setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        ((FloatField) formField).setValue(defaultValue);
                    }
                } else if (fieldType == FieldType.DOUBLE) {
                    Double defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    ((DoubleField) formField).setDefaultValue(defaultValue);
                    if (!formField.isOverride()) {
                        ((DoubleField) formField).setValue(defaultValue);
                    }
                }
            }
            formData.addField(formField);
        }
    }
    
    /**
     * Creates the field.
     *
     * @param <T> the generic type
     * @param context the context
     * @param type the type
     * @param fieldName the field name
     * @param displayName the display name
     * @param schema the schema
     * @param optional the optional
     * @param isOverride the is override
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private static <T extends FormField> T createField(FormContext context, FieldType type, 
            String fieldName, 
            String displayName,
            String schema,
            boolean optional,
            boolean isOverride) {
        T field = null;
        switch (type) {
        case STRING:
            field = (T) new StringField(context, fieldName, displayName, schema, optional);
            break;
        case ARRAY:
            field = (T) new ArrayField(context, fieldName, displayName, schema, optional);
            break;
        case BOOLEAN:
            field = (T) new BooleanField(context, fieldName, displayName, schema, optional);
            break;
        case ENUM:
            field = (T) new EnumField(context, fieldName, displayName, schema, optional);
            break;
        case INT:
            field = (T) new IntegerField(context, fieldName, displayName, schema, optional);
            break;
        case LONG:
            field = (T) new LongField(context, fieldName, displayName, schema, optional);
            break;
        case FLOAT:
            field = (T) new FloatField(context, fieldName, displayName, schema, optional);
            break;            
        case DOUBLE:
            field = (T) new DoubleField(context, fieldName, displayName, schema, optional);
            break;            
        case BYTES:
            field = (T) new BytesField(context, fieldName, displayName, schema, optional);
            break;            
        case RECORD:
            field = (T) new RecordField(context, fieldName, displayName, schema, optional);
            break;
        case FIXED:
            field = (T) new FixedField(context, fieldName, displayName, schema, optional);
            break;
        case UNION:
            field = (T) new UnionField(context, fieldName, displayName, schema, optional);
            break;
        case TYPE_REFERENCE:
            field = (T) new FqnReferenceField(context, fieldName, displayName, schema, optional);
            break;
        case ALERT:
            field = (T) new AlertField(context, fieldName, displayName, schema, optional);
            break;
        case VERSION:
            field = (T) new VersionField(context, fieldName, displayName, schema, optional);
            break;        
        case DEPENDENCIES:
            field = (T) new DependenciesField(context, fieldName, displayName, schema, optional);
            break;               
        default:
            break;
        }
        field.setOverride(isOverride);
        return field;
    }
    
    /**
     * Convert json value.
     *
     * @param <T> the generic type
     * @param type the type
     * @param jsonValue the json value
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertJsonValue(FieldType type, JsonNode jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        T value = null;
        switch (type) {
        case BOOLEAN:
            value = (T) new Boolean(jsonValue.asBoolean());
            break;
        case INT:
            value = (T) new Integer(jsonValue.asInt());
            break;
        case LONG:
            value = (T) new Long(jsonValue.asLong());
            break;
        case FLOAT:
            value = (T) new Float(jsonValue.asDouble());
            break;            
        case DOUBLE:
            value = (T) new Double(jsonValue.asDouble());
            break;            
        case STRING:
        case ENUM:
            value = (T) jsonValue.asText();
            break;
        case FIXED:
        case BYTES:
            if (jsonValue.isTextual() || jsonValue.isBinary()) {
                return (T) jsonValue.asText();
            } else if (jsonValue.isArray()) {
                byte[] data = new byte[jsonValue.size()];
                for (int i=0;i<jsonValue.size();i++) {
                    int val = convertJsonValue(FieldType.INT, jsonValue.get(i));
                    data[i] = (byte) val;
                }
                return (T) Base64Utils.toBase64(data);
            }
            break;
        default:
            break;
        }
        return value;
    }
    
    /**
     * Sets the json value.
     *
     * @param field the field
     * @param jsonValue the json value
     */
    private static void setJsonValue(FormField field, JsonNode jsonValue) {
        if (jsonValue == null) {
            return;
        }
        FieldType fieldType = field.getFieldType();
        switch (field.getFieldType()) {
        case BOOLEAN:
            {
                Boolean value = convertJsonValue(fieldType, jsonValue);
                ((BooleanField)field).setValue(value);
            }
            break;
        case INT:
            {
                Integer value = convertJsonValue(fieldType, jsonValue);
                ((IntegerField)field).setValue(value);
            }
            break;
        case LONG:
            {
                Long value = convertJsonValue(fieldType, jsonValue);
                ((LongField)field).setValue(value);
            }
            break;
        case FLOAT:
            {
                Float value = convertJsonValue(fieldType, jsonValue);
                ((FloatField)field).setValue(value);
            }
            break;
        case DOUBLE:
            {
                Double value = convertJsonValue(fieldType, jsonValue);
                ((DoubleField)field).setValue(value);
            }
            break;            
        case STRING:
            {
                String value = convertJsonValue(fieldType, jsonValue);
                ((StringField)field).setValue(value);
            }
            break;            
        case ENUM:
            {
                String value = convertJsonValue(fieldType, jsonValue);
                ((EnumField)field).setValueFromSymbol(value);
            }
            break;            
        case FIXED:
            {
                String value = convertJsonValue(fieldType, jsonValue);
                ((FixedField)field).setValue(value);
            }
            break;
        case BYTES:
            {
                String value = convertJsonValue(fieldType, jsonValue);
                ((BytesField)field).setValue(value);
            }
            break;
        default:
            break;
        }
    }
    
    /**
     * Convert union default value.
     *
     * @param acceptableValues the acceptable values
     * @param jsonValue the json value
     * @return the form field
     */
    private static FormField convertUnionDefaultValue(List<FormField> acceptableValues, JsonNode jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        FormField value = null;
        for (FormField field : acceptableValues) {
            if (!field.getFieldType().isComplex() && matchesType(field.getFieldType(), jsonValue)) {
                if (field.getFieldType() == FieldType.ENUM) {
                    String val = convertJsonValue(FieldType.ENUM, jsonValue);
                    List<FormEnum> enumValues = ((EnumField)field).getEnumValues();
                    boolean found = false;
                    for (FormEnum enumVal : enumValues) {
                        if (enumVal.getEnumSymbol().equals(val)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }
                } 
                value = field.clone();
                setJsonValue(value, jsonValue);
                return value;
            }
        }
        return value;
    }
    
    /**
     * Matches type.
     *
     * @param type the type
     * @param jsonValue the json value
     * @return true, if successful
     */
    private static boolean matchesType(FieldType type, JsonNode jsonValue) {
        switch (type) {
        case BOOLEAN:
            return jsonValue.isBoolean();
        case INT:
            return jsonValue.isInt();
        case LONG:
            return jsonValue.isIntegralNumber();
        case FLOAT:
            return jsonValue.isDouble();
        case DOUBLE:
            return jsonValue.isFloatingPointNumber();
        case STRING:
        case ENUM:
            return jsonValue.isTextual();
        case FIXED:
        case BYTES:
            return jsonValue.isBinary() || jsonValue.isArray();
        default:
            return false;
        }
    }
    
    /**
     * To field type.
     *
     * @param schema the schema
     * @return the field type
     */
    private static FieldType toFieldType(Schema schema) {
        switch(schema.getType()) {
            case RECORD:
                return FieldType.RECORD;
            case STRING:
                return FieldType.STRING;
            case INT:
                return FieldType.INT;
            case LONG:
                return FieldType.LONG;
            case FLOAT:
                return FieldType.FLOAT;
            case DOUBLE:
                return FieldType.DOUBLE;
            case BOOLEAN:
                return FieldType.BOOLEAN;
            case BYTES:
                return FieldType.BYTES;                
            case ENUM:
                return FieldType.ENUM;
            case ARRAY:
                return FieldType.ARRAY;
            case FIXED:
                return FieldType.FIXED;
            case UNION:
                if (isOverrideTypeSchema(schema)) {
                    boolean isNullType = isNullTypeSchema(schema);
                    if (isNullType && schema.getTypes().size() > 3 || 
                            !isNullType && schema.getTypes().size() > 2) {
                        return FieldType.UNION;
                    }
                    for (Schema typeSchema : schema.getTypes()) {
                        if (!isOverrideType(typeSchema)) {
                            FieldType type = toFieldType(typeSchema);
                            if (type != null) {
                                return type;
                            }
                        }
                    }
                    throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
                } else if (isNullTypeSchema(schema)) {
                    if (schema.getTypes().size() > 2 || 
                            (schema.getTypes().size() == 1 && schema.getTypes().get(0).getType() == Schema.Type.NULL)) {
                        return FieldType.UNION;
                    } else {
                        for (Schema typeSchema : schema.getTypes()) {
                            FieldType type = toFieldType(typeSchema);
                            if (type != null) {
                                return type;
                            }
                        }
                        throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
                    }
                } else {
                    return FieldType.UNION;
                }
            case NULL:
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
        }
    }
    
    /**
     * Checks if is type reference type.
     *
     * @param field the field
     * @return true, if is type reference type
     */
    private static boolean isTypeReferenceType(Field field) {
        JsonNode fqnReferenceVal = field.getJsonProp(TYPE_REFERENCE);
        if (fqnReferenceVal != null && fqnReferenceVal.isBoolean()) {
            return fqnReferenceVal.asBoolean();
        }
        return false;
    }
    
    /**
     * Checks if is alert type.
     *
     * @param field the field
     * @return true, if is alert type
     */
    private static boolean isAlertType(Field field) {
        JsonNode alertVal = field.getJsonProp(ALERT);
        if (alertVal != null && alertVal.isBoolean()) {
            return alertVal.asBoolean();
        }
        return false;
    }
    
    /**
     * Checks if is version type.
     *
     * @param field the field
     * @return true, if is version type
     */
    private static boolean isVersionType(Field field) {
        JsonNode typeVersionVal = field.getJsonProp(TYPE_VERSION);
        if (typeVersionVal != null && typeVersionVal.isBoolean()) {
            return typeVersionVal.asBoolean();
        }
        return false;
    }
    
    /**
     * Checks if is dependencies type.
     *
     * @param field the field
     * @return true, if is dependencies type
     */
    private static boolean isDependenciesType(Field field) {
        JsonNode typeDependenciesVal = field.getJsonProp(TYPE_DEPENDENCIES);
        if (typeDependenciesVal != null && typeDependenciesVal.isBoolean()) {
            return typeDependenciesVal.asBoolean();
        }
        return false;
    }
    
    /**
     * Gets the field type schema.
     *
     * @param schema the schema
     * @return the field type schema
     */
    protected static Schema getFieldTypeSchema(Schema schema) {
        if (isOverrideTypeSchema(schema)) {
            boolean isNullType = isNullTypeSchema(schema);
            if (isNullType && schema.getTypes().size() > 3 || 
                    !isNullType && schema.getTypes().size() > 2) {
                return schema;
            }
            for (Schema typeSchema : schema.getTypes()) {
                if (!isOverrideType(typeSchema)) {
                    FieldType type = toFieldType(typeSchema);
                    if (type != null) {
                        return typeSchema;
                    }
                }
            }
            throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
        } else if (isNullTypeSchema(schema)) {
            if (schema.getTypes().size() > 2 || 
                    (schema.getTypes().size() == 1 && schema.getTypes().get(0).getType() == Schema.Type.NULL)) {
                return schema;
            } else {
                for (Schema typeSchema : schema.getTypes()) {
                    FieldType type = toFieldType(typeSchema);
                    if (type != null) {
                        return typeSchema;
                    }
                }
                throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
            }
        } else {
            return schema;
        }
    }
    
    /**
     * Fill record field from generic record.
     *
     * @param context the context
     * @param recordField the record field
     * @param record the record
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void fillRecordFieldFromGenericRecord(FormContext context, RecordField recordField, GenericRecord record) throws IOException {
    	if (recordField.isNull()) {
    		recordField.finalizeMetadata();
    	}

        for (FormField field : recordField.getValue()) {
            Object value = record.get(field.getFieldName());
            setFormFieldValue(context, field, value);
        }
        
        if (context.isCtlSchema() && recordField.isRoot()) {
            Object value = record.get(SchemaFormAvroConverter.DEPENDENCIES);
            if (value != null) {
                setDependenciesValue(context, value);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void setDependenciesValue(FormContext context, Object value) throws IOException {
        Iterable<Object> dependenciesData = (Iterable<Object>)value;
        if (dependenciesData != null) {
            List<FqnVersion> fqnVersions = new ArrayList<>();
            for (Object arrayValue : dependenciesData) {
                GenericRecord record = (GenericRecord)arrayValue;
                String fqn = record.get(SchemaFormConstants.FQN).toString();
                Integer version = (Integer)record.get(SchemaFormConstants.VERSION);
                fqnVersions.add(new FqnVersion(fqn, version));
            }
            context.setCtlDependenciesList(fqnVersions);
            context.updateCtlDependencies();
        }
    }
    /**
     * Sets the form field value.
     *
     * @param context the context
     * @param field the field
     * @param value the value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    private static void setFormFieldValue(FormContext context, FormField field, Object value) throws IOException {
        if (field.isOverride() && isOverrideUnchangedDatum(value)) {
            field.setChanged(false);
        } else {
            if (field.isOverride()) {
                field.setChanged(true);
            }
            switch(field.getFieldType()) {
            case RECORD:
            	RecordField recordField = (RecordField)field;
            	if (value != null) {
            		GenericRecord record = (GenericRecord)value;
            		fillRecordFieldFromGenericRecord(context, recordField, record);
            	} else {
            		recordField.setNull();
            	}
                break;
            case TYPE_REFERENCE:
                FqnReferenceField fqnReferenceField = (FqnReferenceField)field;
                if (value != null) {
                    String fqnString = value.toString();
                    Fqn fqn = new Fqn(fqnString);
                    FqnKey fqnKey = context.fqnToFqnKey(fqn);
                    if (fqnKey == null) {
                        throw new IllegalArgumentException("Type with FQN '" + fqnString + "' is not defined.");
                    }                    
                    fqnReferenceField.setValue(fqnKey);
                } else {
                    fqnReferenceField.setValue(null);
                }
                break;
            case ALERT:
                AlertField alertField = (AlertField)field;
                if (value != null) {
                    alertField.setValue(value.toString());
                } else {
                    alertField.setValue(null);
                }
                break;
            case VERSION:
                VersionField versionField = (VersionField)field;
                if (value != null) {
                    versionField.setValue((Integer)value);
                } else {
                    versionField.setValue(null);
                }
                break;        
            case UNION:
                UnionField unionField = (UnionField)field;
                if (value != null) {
                    Schema unionSchema = new Schema.Parser().parse(unionField.getSchema());
                    Schema valueSchema = unionSchema.getTypes().get(GenericData.get().resolveUnion(unionSchema, value));
                    FormField unionValue = createFieldFromSchema(context, valueSchema, null);
                    setFormFieldValue(context, unionValue, value);
                    unionField.setValue(unionValue);
                } else {
                    unionField.setValue(null);
                }
                break;
            case STRING:
                StringField stringField = (StringField)field;
                if (value != null) {
                    stringField.setValue(value.toString());
                } else {
                    stringField.setValue(null);
                }
                break;
            case BYTES:
                BytesField bytesField = (BytesField)field;
                if (value != null) {
                    bytesField.setBytes(((ByteBuffer)value).array());
                } else {
                    bytesField.setValue(null);
                }
                break;            
            case INT:
                ((IntegerField)field).setValue((Integer)value);
                break;
            case LONG:
                ((LongField)field).setValue((Long)value);
                break;
            case FLOAT:
                ((FloatField)field).setValue((Float)value);
                break;
            case DOUBLE:
                ((DoubleField)field).setValue((Double)value);
                break;
            case BOOLEAN:
                ((BooleanField)field).setValue((Boolean)value);
                break;
            case FIXED:
                FixedField fixedField = (FixedField)field;
                if (value != null) {
                    byte[] bytesData = ((GenericData.Fixed)value).bytes();
                    fixedField.setBytes(bytesData);
                } else {
                    fixedField.setValue(null);
                }
                break;              
            case ENUM:
                EnumField enumField = (EnumField)field;
                if (value != null) {
                    enumField.setValueFromSymbol(value.toString());
                }
                else {
                    enumField.setValue(null);
                }
                break;
            case ARRAY:
                ArrayField arrayField = (ArrayField)field;
                arrayField.getValue().clear();
                arrayField.finalizeMetadata();
                Iterable<Object> arrayData = (Iterable<Object>)value;
                if (arrayData != null) {
                    for (Object arrayValue : arrayData) {
                        FormField fieldValue = arrayField.createRow();
                        setFormFieldValue(context, fieldValue, arrayValue);
                        ((ArrayField)field).addArrayData(fieldValue);
                    }
                }
                break;
            default:
                break;
            }
        }
    }
    
    /**
     * Convert value.
     *
     * @param formField the form field
     * @param fieldSchema the field schema
     * @return the object
     */
    private static Object convertValue(FormField formField, Schema fieldSchema) {
        if (formField.isOverride() && !formField.isChanged()) {
            return unchangedSymbol;
        }
        //Schema fieldSchema = new Schema.Parser().parse(formField.getSchema());
        if (formField.isNull() && !(hasType(fieldSchema, Schema.Type.NULL))) {
            throw new UnsupportedOperationException("Avro field doesn't support null values!");
        }
        switch(fieldSchema.getType()) {
        case RECORD:
            return createGenericRecordFromRecordField((RecordField)formField);
        case STRING:
            if (formField instanceof FqnReferenceField) {
                Fqn fqn = ((FqnReferenceField)formField).getFqnValue();
                return fqn != null ? fqn.getFqnString() : null;
            } else if (formField instanceof AlertField) {
                return ((AlertField)formField).getValue();
            }
            return ((StringField)formField).getValue();
        case INT:
            return ((IntegerField)formField).getValue();
        case LONG:
            return ((LongField)formField).getValue();
        case FLOAT:
            return ((FloatField)formField).getValue();
        case DOUBLE:
            return ((DoubleField)formField).getValue();
        case BOOLEAN:
            // Explicitly set if the field is left untouched by the user
            if (((BooleanField) formField).getValue() == null && !formField.isOptional()) {
                ((BooleanField) formField).setValue(Boolean.FALSE);
            }
            return ((BooleanField)formField).getValue();
        case BYTES:
            BytesField bytesField = (BytesField)formField;
            byte[] bytesData = null;
            try {
                bytesData = bytesField.getBytes();
            } catch (ParseException e) {}
            if (bytesData != null) {
                return ByteBuffer.wrap(bytesData);
            } else {
                return null;
            }
        case ENUM:
            String enumSymbol = ((EnumField)formField).getValue().getEnumSymbol();
            return new GenericData.EnumSymbol(fieldSchema, enumSymbol);
        case FIXED:
            FixedField fixedField = (FixedField)formField;
            byte[] fixedData = null;
            try {
                fixedData = fixedField.getBytes();
            } catch (ParseException e) {}
            if (fixedData != null) {
                GenericData.Fixed genericFixed = new GenericData.Fixed(fieldSchema, fixedData);
                return genericFixed;    
            } else {
                return null;
            }
        case ARRAY:
            List<FormField> arrayData = ((ArrayField)formField).getValue();
            GenericData.Array<Object> genericArrayData = new GenericData.Array<>(arrayData.size(), fieldSchema);
            for (FormField arrayField : arrayData) {
                Object data =  convertValue(arrayField, fieldSchema.getElementType());
                genericArrayData.add(data);
            }
            return genericArrayData;
        case UNION:
            if (formField.isNull()) {
                if (hasType(fieldSchema, Schema.Type.NULL)) {
                    return null;
                } else {
                    throw new UnsupportedOperationException("Avro field doesn't support null values!");
                }
            } else {
                FormField value;
                if (formField.getFieldType() == FieldType.UNION) {
                    UnionField unionField = (UnionField)formField;
                    value = unionField.getValue();
                } else if (formField.getFieldType() == FieldType.VERSION) {
                    return ((VersionField)formField).getValue();
                } else if (formField.getFieldType() == FieldType.DEPENDENCIES) {
                    List<FqnVersion> fqnVersions = ((DependenciesField)formField).getValue();
                    int index = fieldSchema.getIndexNamed(FieldType.ARRAY.getName());
                    Schema dependenciesSchema = fieldSchema.getTypes().get(index);
                    genericArrayData = new GenericData.Array<>(fqnVersions.size(), dependenciesSchema);
                    Schema dependencySchema = dependenciesSchema.getElementType();
                    for (FqnVersion fqnVersion : fqnVersions) {
                        GenericRecordBuilder builder = new GenericRecordBuilder(dependencySchema);
                        Field field = dependencySchema.getField(SchemaFormAvroConverter.FQN);
                        builder.set(field, fqnVersion.getFqnString());
                        field = dependencySchema.getField(SchemaFormAvroConverter.VERSION);
                        builder.set(field, fqnVersion.getVersion());
                        GenericRecord record = builder.build();
                        genericArrayData.add(record);
                    }
                    return genericArrayData;
                } else {
                    value = formField;
                }
                int index = fieldSchema.getIndexNamed(value.getTypeFullname());
                Schema schema = fieldSchema.getTypes().get(index);
                return convertValue(value, schema);
            }
        default:
            throw new UnsupportedOperationException("Unsupported avro field type: " + fieldSchema.getType());
        }
    }
    
    /**
     * Checks for type.
     *
     * @param unionSchema the union schema
     * @param type the type
     * @return true, if successful
     */
    private static boolean hasType(Schema unionSchema, Schema.Type type) {
        if (unionSchema.getType()==Type.UNION) {
            for (Schema typeSchema : unionSchema.getTypes()) {
                if (typeSchema.getType()==type) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if is null type schema.
     *
     * @param schema the schema
     * @return true, if is null type schema
     */
    protected static boolean isNullTypeSchema(Schema schema) {
        if (schema.getType() == Type.UNION) {
                for (Schema typeSchema : schema.getTypes()) {
                    if (typeSchema.getType() == Schema.Type.NULL) {
                        return true;
                    }
                }
        }
        return false;
    }
    
    /**
     * Checks if is override type schema.
     *
     * @param schema the schema
     * @return true, if is override type schema
     */
    private static boolean isOverrideTypeSchema(Schema schema) {
        if (schema.getType() == Type.UNION && schema.getTypes().size() >= 2) {
            for (Schema typeSchema : schema.getTypes()) {
                if (isOverrideType(typeSchema)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if is override type.
     *
     * @param typeSchema the type schema
     * @return true, if is override type
     */
    private static boolean isOverrideType(Schema typeSchema) {
        return typeSchema.getType() == Schema.Type.ENUM && 
                typeSchema.getNamespace().equals(DEFAULT_CONFIG_NAMESPACE) &&
                typeSchema.getName().equals(UNCHANGED_NAME);
    }
    
    /**
     * Checks if is override unchanged datum.
     *
     * @param datum the datum
     * @return true, if is override unchanged datum
     */
    private static boolean isOverrideUnchangedDatum(Object datum) {
        if (datum != null && datum instanceof GenericEnumSymbol) {
            Schema schema = ((GenericContainer)datum).getSchema();
            return isOverrideType(schema);
        } else {
            return false;
        }
    }
    
    
}
