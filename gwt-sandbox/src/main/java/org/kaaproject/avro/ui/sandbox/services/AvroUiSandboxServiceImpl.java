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
package org.kaaproject.avro.ui.sandbox.services;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.sandbox.services.cache.JsonCacheService;
import org.kaaproject.avro.ui.sandbox.services.util.Utils;
import org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService;
import org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxServiceException;
import org.kaaproject.avro.ui.shared.RecordField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("avroUiSandboxService")
public class AvroUiSandboxServiceImpl implements AvroUiSandboxService, InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AvroUiSandboxServiceImpl.class);
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private SchemaFormAvroConverter schemaFormConverter;
    
    @Autowired
    private JsonCacheService jsonCacheService;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        schemaFormConverter = new SchemaFormAvroConverter();
    }
    
    @Override
    public RecordField generateFormFromSchema(String avroSchema)
            throws AvroUiSandboxServiceException {
        try {
            Schema schema = new Schema.Parser().parse(avroSchema);
            RecordField generatedForm = FormAvroConverter.createRecordFieldFromSchema(schema);
            return generatedForm;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getJsonStringFromRecord(RecordField field)
            throws AvroUiSandboxServiceException {
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(field);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonGenerator jsonGenerator
                = new JsonFactory().createJsonGenerator(baos, JsonEncoding.UTF8);
            jsonGenerator.useDefaultPrettyPrinter();
            JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), jsonGenerator);
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(record.getSchema());
            datumWriter.write(record, jsonEncoder);
            jsonEncoder.flush();
            baos.flush();            
            return new String(baos.toByteArray(), UTF8);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateFormDataFromJson(String avroSchema, String json)
            throws AvroUiSandboxServiceException {
        try {
            Schema schema = new Schema.Parser().parse(avroSchema);
            JsonDecoder jsonDecoder = DecoderFactory.get().jsonDecoder(schema, json);
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
            GenericRecord genericRecord = datumReader.read(null, jsonDecoder);
            return FormAvroConverter.createRecordFieldFromGenericRecord(genericRecord);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public RecordField getEmptySchemaForm()
            throws AvroUiSandboxServiceException {
        try {
            return schemaFormConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateSchemaFormFromSchema(String avroSchema)
            throws AvroUiSandboxServiceException {
        try {
            RecordField generatedForm = schemaFormConverter.createSchemaFormFromSchema(avroSchema);
            return generatedForm;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getJsonStringFromSchemaForm(RecordField field)
            throws AvroUiSandboxServiceException {
        try {
            Schema schema = schemaFormConverter.createSchemaFromSchemaForm(field);
            return SchemaFormAvroConverter.createSchemaString(schema, true);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String uploadJsonToFile(String json)
            throws AvroUiSandboxServiceException {
        return jsonCacheService.putJson(json);
    }

}
