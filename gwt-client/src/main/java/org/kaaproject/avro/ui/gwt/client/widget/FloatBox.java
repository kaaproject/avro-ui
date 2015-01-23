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

package org.kaaproject.avro.ui.gwt.client.widget;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;

import com.google.gwt.dom.client.Document;

public class FloatBox extends NumberBox<Float> {
    public FloatBox(AvroUiStyle style, String prompt, String numberFormatPattern) {
        super(style, Document.get().createTextInputElement(), prompt, new NumberRenderer<Float>(
                numberFormatPattern, true), new FloatParser(numberFormatPattern));
        setDecimal(true);
    }
}
