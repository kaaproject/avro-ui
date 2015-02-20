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
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.EnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.BooleanField;
import org.kaaproject.avro.ui.shared.EnumField;
import org.kaaproject.avro.ui.shared.FormEnum;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.IntegerField;
import org.kaaproject.avro.ui.shared.LongField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.StringField;
import org.kaaproject.avro.ui.shared.StringField.InputType;
import org.kaaproject.avro.ui.shared.UnionField;

public class FormAvroConverterTest {
    
    @Test
    public void testSingleFieldsRecord() throws IOException {
        int fieldNum = 0;
        Schema schema = TestAvroSchemas.getSingleFieldsSchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(6, field.getValue().size());
        FormField formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof StringField);
        Assert.assertEquals(1000, ((StringField)formField).getMaxLength());
        Assert.assertEquals("testString", formField.getFieldName());
        Assert.assertEquals("Test string field", formField.getDisplayName());
        Assert.assertEquals("default string", ((StringField)formField).getDefaultValue());
        Assert.assertEquals(InputType.PLAIN, ((StringField)formField).getInputType());
        
        ((StringField)formField).setValue("new string");
        
        formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof StringField);
        Assert.assertEquals(1000, ((StringField)formField).getMaxLength());
        Assert.assertEquals("testPasswordString", formField.getFieldName());
        Assert.assertEquals("Test password string field", formField.getDisplayName());
        Assert.assertEquals("default password", ((StringField)formField).getDefaultValue());
        Assert.assertEquals(InputType.PASSWORD, ((StringField)formField).getInputType());
        
        formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof IntegerField);
        Assert.assertEquals(1000, ((IntegerField)formField).getMaxLength());
        Assert.assertEquals("testInteger", formField.getFieldName());
        Assert.assertEquals("Test integer field", formField.getDisplayName());
        Assert.assertEquals(20, ((IntegerField)formField).getDefaultValue().intValue());
        
        ((IntegerField)formField).setValue(25);
        
        formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof LongField);
        Assert.assertEquals(1000, ((LongField)formField).getMaxLength());
        Assert.assertEquals("testLong", formField.getFieldName());
        Assert.assertEquals("Test long field", formField.getDisplayName());
        Assert.assertEquals(30, ((LongField)formField).getDefaultValue().longValue());
        
        ((LongField)formField).setValue(35l);
        
        formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof BooleanField);
        Assert.assertEquals("testBoolean", formField.getFieldName());
        Assert.assertEquals("Test boolean field", formField.getDisplayName());
        Assert.assertEquals(true, ((BooleanField)formField).getDefaultValue().booleanValue());
        
        ((BooleanField)formField).setValue(false);
        
        formField = field.getValue().get(fieldNum++);
        Assert.assertTrue(formField instanceof EnumField);
        Assert.assertEquals("testEnum", formField.getFieldName());
        Assert.assertEquals("Test enum field", formField.getDisplayName());
        Assert.assertEquals(Arrays.asList(new FormEnum("VALUE_ONE","Value One"), 
                                          new FormEnum("VALUE_TWO","Value Two"),
                                          new FormEnum("VALUE_THREE","Value Three")),                 
                ((EnumField)formField).getEnumValues());
        Assert.assertEquals("VALUE_TWO", ((EnumField)formField).getDefaultValue().getEnumSymbol());
        
        ((EnumField)formField).setValue(new FormEnum("VALUE_THREE","Value Three"));
        
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(field);
        
        Assert.assertNotNull(record);
        
        Object val = record.get("testString");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof String);
        Assert.assertEquals("new string", (String)val);
        
        val = record.get("testInteger");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof Integer);
        Assert.assertEquals(25, ((Integer)val).intValue());

        val = record.get("testLong");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof Long);
        Assert.assertEquals(35l, ((Long)val).longValue());
        
        val = record.get("testBoolean");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof Boolean);
        Assert.assertEquals(false, ((Boolean)val).booleanValue());
        
        val = record.get("testEnum");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof EnumSymbol);
        Assert.assertEquals("VALUE_THREE",  ((EnumSymbol)val).toString());
        
        RecordField convertedField = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        
        Assert.assertNotNull(convertedField);
        Assert.assertEquals(field, convertedField);
        
    }

    @Test
    public void testArrayRecord() throws IOException {
        Schema schema = TestAvroSchemas.getArraySchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(1, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof ArrayField);
        ArrayField arrayField = (ArrayField)formField;
        arrayField.finalizeMetadata();
        Assert.assertNotNull(arrayField.getValue());
        Assert.assertEquals(1, arrayField.getMinRowCount());
        Assert.assertEquals(1, arrayField.getValue().size());
        checkSingleFieldRecord(arrayField.getValue().get(0));
        Assert.assertNotNull(arrayField.getElementMetadata());
        checkSingleFieldRecord(arrayField.getElementMetadata());
        
        RecordField newRow = (RecordField) arrayField.createRow();
        StringField stringField = (StringField) newRow.getValue().get(0);
        stringField.setValue("cell value");
        arrayField.addArrayData(newRow);
        
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(field);
        
        Assert.assertNotNull(record);
        Object val = record.get("testArrayElements");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof GenericData.Array);
        @SuppressWarnings("unchecked")
        GenericData.Array<GenericRecord> genericArrayData = (GenericData.Array<GenericRecord>)val;
        Assert.assertEquals(2, genericArrayData.size());
        GenericRecord row = genericArrayData.get(0);
        Assert.assertNotNull(row);
        val = row.get("test");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof String);
        Assert.assertEquals("", (String)val);
        row = genericArrayData.get(1);
        Assert.assertNotNull(row);
        val = row.get("test");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof String);
        Assert.assertEquals("cell value", (String)val);
        
        RecordField convertedField = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        
        Assert.assertNotNull(convertedField);
        Assert.assertEquals(field, convertedField);

    }

    @Test
    public void testUnionRecord() throws IOException {
        Schema schema = TestAvroSchemas.getUnionSchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(1, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof UnionField);
        UnionField unionField = (UnionField)formField;
        unionField.finalizeMetadata();
        Assert.assertNull(unionField.getValue());
        Assert.assertNotNull(unionField.getAcceptableValues());
        Assert.assertEquals(2, unionField.getAcceptableValues().size());
        
        Assert.assertTrue(unionField.getAcceptableValues().get(0) instanceof RecordField);
        checkSingleFieldRecord((RecordField)unionField.getAcceptableValues().get(0));
        
        Assert.assertTrue(unionField.getAcceptableValues().get(1) instanceof RecordField);
        checkSingleFieldRecord((RecordField)unionField.getAcceptableValues().get(1));
        
        RecordField unionValue = (RecordField) unionField.getAcceptableValues().get(1).clone();
        unionValue.finalizeMetadata();
        StringField stringField = (StringField) unionValue.getValue().get(0);
        stringField.setValue("field value of union record");
        unionField.setValue(unionValue);
        
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(field);
        Assert.assertNotNull(record);
        Object val = record.get("unionField");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof GenericRecord);
        GenericRecord unionValueRecord = (GenericRecord)val;
        val = unionValueRecord.get("test");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof String);
        Assert.assertEquals("field value of union record", (String)val);
        
        RecordField convertedField = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        
        Assert.assertNotNull(convertedField);
        
        Assert.assertNotNull(convertedField.getValue());
        Assert.assertEquals(1, convertedField.getValue().size());
        formField = convertedField.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof UnionField);
        unionField = (UnionField)formField;
        unionField.finalizeMetadata();
        
        Assert.assertEquals(field, convertedField);
    }
    
    @Test
    public void testOverrideSchema() throws IOException {
        Schema schema = TestAvroSchemas.getOverrideSchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(2, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof ArrayField);
        ArrayField arrayField = (ArrayField)formField;
        Assert.assertTrue(arrayField.isOverride());
        Assert.assertFalse(arrayField.isChanged());
    }
    
    private void checkSingleFieldRecord(FormField field) {
        Assert.assertNotNull(field);
        Assert.assertTrue(field instanceof RecordField);
        RecordField recordField = (RecordField)field;
        Assert.assertNotNull(recordField.getValue());
        Assert.assertEquals(1, recordField.getValue().size());
        FormField formField = recordField.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof StringField);
        Assert.assertEquals(1000, ((StringField)formField).getMaxLength());
    }
}
