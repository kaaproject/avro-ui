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
import java.text.ParseException;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.FormContext;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.StringField;
import org.kaaproject.avro.ui.shared.UnionField;
import org.kaaproject.avro.ui.shared.VersionField;

public class SchemaFormAvroConverterTest {
    
    @Test
    public void testSchemaFormsConversion() throws IOException, ParseException {
        testSchemaFormConversion(TestAvroSchemas.getSchema(TestAvroSchemas.SINGLE_FIELDS));
        testSchemaFormConversion(TestAvroSchemas.getSchema(TestAvroSchemas.ARRAY));
        testSchemaFormConversion(TestAvroSchemas.getSchema(TestAvroSchemas.UNION));
        testSchemaFormConversion(TestAvroSchemas.getSchema(TestAvroSchemas.TYPE_REFERENCES_SCHEMA));
    }
    
    private void testSchemaFormConversion(Schema schema) throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter();
        RecordField schemaForm = converter.createSchemaFormFromSchema(schema);
        Assert.assertNotNull(schemaForm);
        Schema convertedSchema = converter.createSchemaFromSchemaForm(schemaForm);
        Assert.assertEquals(schema, convertedSchema);
    }
    
    @Test
    public void testSwitchReferenceToRecord() throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter();
        Schema typeReferencesSchema = TestAvroSchemas.getSchema(TestAvroSchemas.TYPE_REFERENCES_SCHEMA);
        RecordField schemaForm = converter.createSchemaFormFromSchema(typeReferencesSchema);
        Assert.assertNotNull(schemaForm);
        ArrayField fieldsArray = getFieldsArray(schemaForm);
        List<FormField> rows = ((ArrayField)fieldsArray).getValue();
        Assert.assertNotNull(rows);        
        Assert.assertEquals(3, rows.size());
        
        checkTypeHolder(rows, 0, "testRecord1", "org.kaaproject.avro.ui.test.TypeB");
        checkTypeConsumer(rows, 1, "testReference1", "org.kaaproject.avro.ui.test.TypeB");
        
        fieldsArray.removeRow(0);
        checkTypeHolder(rows, 0, "testReference1", "org.kaaproject.avro.ui.test.TypeB");
        
        fieldsArray.removeRow(0);
        checkTypeHolder(rows, 0, "testReference3", "org.kaaproject.avro.ui.test.TypeC");
        
        Schema convertedSchema = converter.createSchemaFromSchemaForm(schemaForm);
        
        Schema typeReferences2Schema = TestAvroSchemas.getSchema(TestAvroSchemas.TYPE_REFERENCES2_SCHEMA);
        Assert.assertEquals(typeReferences2Schema, convertedSchema);
        
    }
    
    @Test
    public void testCtlSchemaFormConversion() throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter(new TestCtlSource());
        String typeCtlReferencesSchema = TestAvroSchemas.getSchemaJson(TestAvroSchemas.TYPE_CTL_REFERENCES);
        RecordField schemaForm = converter.createSchemaFormFromSchema(typeCtlReferencesSchema);
        Assert.assertNotNull(schemaForm);
        Schema convertedSchema = converter.createSchemaFromSchemaForm(schemaForm);
        String convertedSchemaString = SchemaFormAvroConverter.createSchemaString(convertedSchema, true);
        Assert.assertEquals(typeCtlReferencesSchema.replaceAll("\r\n", "\n"), convertedSchemaString.replaceAll("\r\n", "\n"));
    }
    
    @Test
    public void testCtlDependencies() throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter(new TestCtlSource());
        String typeCtlReferencesSchema = TestAvroSchemas.getSchemaJson(TestAvroSchemas.TYPE_CTL_REFERENCES);
        RecordField schemaForm = converter.createSchemaFormFromSchema(typeCtlReferencesSchema);
        FormContext context = schemaForm.getContext();
        Assert.assertNotNull(context);
        List<FqnVersion> ctlDependencies = context.getCtlDependenciesList();
        Assert.assertNotNull(ctlDependencies);
        Assert.assertEquals(2, ctlDependencies.size());
        FqnVersion fqnVersion = ctlDependencies.get(0);
        Assert.assertNotNull(fqnVersion);
        Assert.assertEquals("org.kaaproject.ctl.TypeA", fqnVersion.getFqnString());
        Assert.assertEquals(5, fqnVersion.getVersion());
        fqnVersion = ctlDependencies.get(1);
        Assert.assertNotNull(fqnVersion);
        Assert.assertEquals("org.kaaproject.ctl.TypeB", fqnVersion.getFqnString());
        Assert.assertEquals(5, fqnVersion.getVersion());
        
        ArrayField fieldsArray = getFieldsArray(schemaForm);
        List<FormField> rows = ((ArrayField)fieldsArray).getValue();
        Assert.assertNotNull(rows);        
        Assert.assertEquals(2, rows.size());
        
        checkTypeConsumer(rows, 0, "f1", "org.kaaproject.ctl.TypeA");
        fieldsArray.removeRow(0);
        checkTypeConsumer(rows, 0, "f2", "org.kaaproject.ctl.TypeB");
        
        Assert.assertEquals(1, ctlDependencies.size());
        fqnVersion = ctlDependencies.get(0);
        Assert.assertNotNull(fqnVersion);
        Assert.assertEquals("org.kaaproject.ctl.TypeB", fqnVersion.getFqnString());
        Assert.assertEquals(5, fqnVersion.getVersion());
    }
    
    @Test
    public void testOverrideCtlDependency() throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter(new TestCtlSource());
        String typeCtlReferencesSchema = TestAvroSchemas.getSchemaJson(TestAvroSchemas.TYPE_CTL_REFERENCES);
        RecordField schemaForm = converter.createSchemaFormFromSchema(typeCtlReferencesSchema);
        StringField nameField = getStringField(schemaForm, "recordName");
        StringField namespaceField = getStringField(schemaForm, "recordNamespace");
        FormField field = schemaForm.getFieldByName("version");
        Assert.assertNotNull(field);
        Assert.assertTrue(field instanceof VersionField);
        VersionField versionField = (VersionField)field;
        versionField.setValue(null);
        nameField.setValue("TypeB");
        namespaceField.setValue("org.kaaproject.ctl");
        Assert.assertNotNull(versionField.getValue());
        Assert.assertEquals(6, versionField.getValue().intValue());
        
        FormContext context = schemaForm.getContext();
        Assert.assertNotNull(context);
        List<FqnVersion> ctlDependencies = context.getCtlDependenciesList();
        Assert.assertNotNull(ctlDependencies);
        Assert.assertEquals(1, ctlDependencies.size());
        FqnVersion fqnVersion = ctlDependencies.get(0);
        Assert.assertNotNull(fqnVersion);
        Assert.assertEquals("org.kaaproject.ctl.TypeA", fqnVersion.getFqnString());
        Assert.assertEquals(5, fqnVersion.getVersion());
    }
    
    private StringField getStringField(RecordField schemaForm, String fieldName) {
        FormField field = schemaForm.getFieldByName(fieldName);
        Assert.assertNotNull(field);
        Assert.assertTrue(field instanceof StringField);
        return (StringField)field;
    }
    
    private ArrayField getFieldsArray(RecordField schemaForm) {
        FormField fields = schemaForm.getFieldByName(SchemaFormAvroConverter.FIELDS);
        Assert.assertNotNull(fields);
        Assert.assertTrue(fields instanceof ArrayField);
        return (ArrayField)fields;
    }
    
    private RecordField checkTypeHolder(List<FormField> rows, int index, String fieldName, String fqnString) {
        FormField field = rows.get(index);
        Assert.assertNotNull(field);
        Assert.assertTrue(field instanceof RecordField);
        FormField fieldNameField = ((RecordField)field).getFieldByName(SchemaFormAvroConverter.FIELD_NAME);
        Assert.assertNotNull(fieldNameField);
        Assert.assertTrue(fieldNameField instanceof StringField);
        String fieldNameValue = ((StringField)fieldNameField).getValue();
        Assert.assertNotNull(fieldNameValue);
        Assert.assertEquals(fieldName, fieldNameValue);
        
        FormField fieldType = ((RecordField)field).getFieldByName(SchemaFormAvroConverter.FIELD_TYPE);
        Assert.assertNotNull(fieldType);
        Assert.assertTrue(fieldType instanceof UnionField);
        FormField fieldTypeValue = ((UnionField)fieldType).getValue();
        Assert.assertNotNull(fieldTypeValue);
        Assert.assertTrue(fieldTypeValue instanceof RecordField);
        RecordField typeHolder = (RecordField)fieldTypeValue;
        Assert.assertTrue(typeHolder.isTypeHolder());
        Assert.assertEquals(typeHolder.getDeclaredFqn().getFqnString(), fqnString);
        
        return typeHolder;
    }
    
    private RecordField checkTypeConsumer(List<FormField> rows, int index, String fieldName, String fqnString) {
        FormField field = rows.get(index);
        Assert.assertNotNull(field);
        Assert.assertTrue(field instanceof RecordField);
        FormField fieldNameField = ((RecordField)field).getFieldByName(SchemaFormAvroConverter.FIELD_NAME);
        Assert.assertNotNull(fieldNameField);
        Assert.assertTrue(fieldNameField instanceof StringField);
        String fieldNameValue = ((StringField)fieldNameField).getValue();
        Assert.assertNotNull(fieldNameValue);
        Assert.assertEquals(fieldName, fieldNameValue);
        
        FormField fieldType = ((RecordField)field).getFieldByName(SchemaFormAvroConverter.FIELD_TYPE);
        Assert.assertNotNull(fieldType);
        Assert.assertTrue(fieldType instanceof UnionField);
        FormField fieldTypeValue = ((UnionField)fieldType).getValue();
        Assert.assertNotNull(fieldTypeValue);
        Assert.assertTrue(fieldTypeValue instanceof RecordField);
        RecordField typeConsumer = (RecordField)fieldTypeValue;
        Assert.assertTrue(typeConsumer.isTypeConsumer());
        FormContext context = typeConsumer.getContext();
        Assert.assertNotNull(context);
        
        Assert.assertEquals(context.getDeclaredTypes().get(typeConsumer.getConsumedFqnKey()).getFqnString(), fqnString);
        
        return typeConsumer;
    }

} 
