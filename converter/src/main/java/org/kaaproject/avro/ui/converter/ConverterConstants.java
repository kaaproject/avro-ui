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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;

/**
 * The Interface ConverterConstants.
 */
public interface ConverterConstants {

    /** The Constant DISPLAY_NAME. */
    public static final String DISPLAY_NAME = "displayName";
    
    /** The Constant DESCRIPTION. */
    public static final String DESCRIPTION = "description";
    
    /** The Constant DISPLAY_PROMPT. */
    public static final String DISPLAY_PROMPT = "displayPrompt";
    
    /** The Constant IS_FQN_HOLDER. */
    public static final String IS_TYPE_HOLDER = "isTypeHolder";
    
    /** The Constant IS_FQN_CONSUMER. */
    public static final String IS_TYPE_CONSUMER = "isTypeConsumer";
    
    /** The Constant FQN_REFERENCE. */
    public static final String TYPE_REFERENCE = "typeReference";
    
    /** The Constant ALERT. */
    public static final String ALERT = "alert";
    
    /** The Constant TYPE_VERSION. */
    public static final String TYPE_VERSION = "typeVersion";
    
    /** The Constant TYPE_DEPENDENCIES. */
    public static final String TYPE_DEPENDENCIES = "typeDependencies";
    
    /** The Constant BY_DEFAULT. */
    public static final String BY_DEFAULT = "by_default";
    
    /** The Constant DISPLAY_NAMES. */
    public static final String DISPLAY_NAMES = "displayNames";
    
    /** The Constant WEIGHT. */
    public static final String WEIGHT = "weight";
    
    /** The Constant KEY_INDEX. */
    public static final String KEY_INDEX = "keyIndex";
    
    /** The Constant MIN_ROW_COUNT. */
    public static final String MIN_ROW_COUNT = "minRowCount";

    /** The Constant OVERRIDE_STRATEGY. */
    public static final String OVERRIDE_STRATEGY = "overrideStrategy";

    /** The Constant MAX_LENGTH. */
    public static final String MAX_LENGTH = "maxLength";
    
    /** The Constant FIELD_ACCESS. */
    public static final String FIELD_ACCESS = "fieldAccess";
    
    /** The Constant INPUT_TYPE. */
    public static final String INPUT_TYPE = "inputType";
    
    /** The Constant ADDRESSABLE. */
    public static final String ADDRESSABLE = "addressable";
    
    /** The Constant DEFAULT_CONFIG_NAMESPACE. */
    static final String DEFAULT_CONFIG_NAMESPACE = "org.kaaproject.configuration";
    
    /** The Constant UNCHANGED_NAME. */
    static final String UNCHANGED_NAME = "unchangedT";
    
    /** The Constant UNCHANGED_SYMBOL. */
    static final String UNCHANGED_SYMBOL = "unchanged";
    
    /** The Constant defaultUnchangedSchema. */
    static final Schema defaultUnchangedSchema = new Schema.Parser().parse(
             "{" +
                "\"type\" : \"enum\","+
                "\"name\" : \"" + UNCHANGED_NAME + "\","+
                "\"namespace\" : \"" + DEFAULT_CONFIG_NAMESPACE + "\","+
                "\"symbols\" : [ \"" + UNCHANGED_SYMBOL + "\" ]"+
             "}");
    
    /** The Constant unchangedSymbol. */
    static final GenericEnumSymbol unchangedSymbol = 
            new GenericData.EnumSymbol(defaultUnchangedSchema, UNCHANGED_SYMBOL);
    
}
