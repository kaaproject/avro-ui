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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.commons.compress.utils.IOUtils;

public class TestAvroSchemas {

    public static final String SINGLE_FIELDS = "single-fields.avsc";
    public static final String ARRAY = "array.avsc";
    public static final String UNION = "union.avsc";
    public static final String OVERRIDE_SCHEMA = "override-schema.avsc";
    public static final String TYPE_REFERENCES_SCHEMA = "type-references.avsc";
    public static final String TYPE_REFERENCES2_SCHEMA = "type-references2.avsc";
    public static final String TYPE_CTL_REFERENCES = "type-ctl-references.avsc";
    
    private static final Map<String, Schema> schemasMap = new HashMap<>();
    private static final Map<String, String> schemaJsonsMap = new HashMap<>();
    
    public static Schema getSchema(String resourceName) throws IOException {
        Schema schema = schemasMap.get(resourceName);
        if (schema == null) {
            schema = new Schema.Parser().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
            schemasMap.put(resourceName, schema);
        }
        return schema;
    }
    
    public static String getSchemaJson(String resourceName) throws IOException {
        String schema = schemaJsonsMap.get(resourceName);
        if (schema == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName), baos);
            schema = new String(baos.toByteArray());
            schemaJsonsMap.put(resourceName, schema);
        }
        return schema;
    }


}
