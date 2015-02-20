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

package org.kaaproject.avro.ui.gwt.client.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface AvroUiConstants extends ConstantsWithLookup {

    @DefaultStringValue("add items")
    String appendStrategy();

    @DefaultStringValue("replace items")
    String replaceStrategy();
    
    @DefaultStringValue("There is no data to display")
    String dataGridEmpty();

    @DefaultStringValue("Delete")
    String delete();

    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();
    
    @DefaultStringValue("Add")
    String add();
 
    @DefaultStringValue("Remove")
    String remove();

    @DefaultStringValue("Back")
    String back();
    
    @DefaultStringValue("Create")
    String create();
    
    @DefaultStringValue("Open")
    String open();
    
    @DefaultStringValue("Empty")
    String empty();
    
    @DefaultStringValue("Processing request...")
    String busyPopupText();
    
}
