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

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.avro.ui.shared.RecordField;

public class SchemaFormAvroConverterTest {
    
    @Test
    public void testSchemaFormsConversion() throws IOException, ParseException {
        testSchemaFormConversion(TestAvroSchemas.getSingleFieldsSchema());
        testSchemaFormConversion(TestAvroSchemas.getArraySchema());
        testSchemaFormConversion(TestAvroSchemas.getUnionSchema());
    }
    
    private void testSchemaFormConversion(Schema schema) throws IOException, ParseException {
        SchemaFormAvroConverter converter = 
                new SchemaFormAvroConverter();
        RecordField schemaForm = converter.createSchemaFormFromSchema(schema);
        Assert.assertNotNull(schemaForm);
        Schema convertedSchema = converter.createSchemaFromSchemaForm(schemaForm);
        Assert.assertEquals(schema, convertedSchema);
    }

} 
