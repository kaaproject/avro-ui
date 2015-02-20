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

import org.apache.avro.Schema;

public class TestAvroSchemas {

    private static final String SINGLE_FIELDS = "single-fields.avsc";
    private static Schema singleFieldsSchema;

    private static final String ARRAY = "array.avsc";
    private static Schema arraySchema;

    private static final String UNION = "union.avsc";
    private static Schema unionSchema;
    
    private static final String OVERRIDE_SCHEMA = "override-schema.avsc";
    private static Schema overrideSchema;
    
    public static Schema getSingleFieldsSchema() throws IOException {
        if (singleFieldsSchema == null) {
            singleFieldsSchema = new Schema.Parser().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(SINGLE_FIELDS));
        }
        return singleFieldsSchema;
    }

    public static Schema getArraySchema() throws IOException {
        if (arraySchema == null) {
            arraySchema = new Schema.Parser().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(ARRAY));
        }
        return arraySchema;
    }
    
    public static Schema getUnionSchema() throws IOException {
        if (unionSchema == null) {
            unionSchema = new Schema.Parser().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(UNION));
        }
        return unionSchema;
    }
    
    public static Schema getOverrideSchema() throws IOException {
        if (overrideSchema == null) {
            overrideSchema = new Schema.Parser().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(OVERRIDE_SCHEMA));
        }
        return overrideSchema;
    }

}
