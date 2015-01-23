/*
 * Copyright 2014 CyberVision, Inc.
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

import com.google.gwt.i18n.client.Messages;

public interface AvroUiMessages extends Messages {

    @DefaultMessage("{0} of {1} characters")
    String charactersLength(int length, int maxLenght);

    @DefaultMessage("Page {0} of {1}")
    String pagerText(int current, int total);

    @DefaultMessage("Are you sure you want to delete selected entry?")
    String deleteSelectedEntryQuestion();

    @DefaultMessage("Delete entry")
    String deleteSelectedEntryTitle();
    
    @DefaultMessage("Are you sure you want to delete nested {0} which is value of field ''{1}''?")
    String deleteNestedEntryQuestion(String nestedValueType, String fieldName);
    
    @DefaultMessage("Delete nested entry")
    String deleteNestedEntryTitle();
    
    @DefaultMessage("Add new {0}")
    String addNewEntry(String entryTitle);
    
    @DefaultMessage("Nested {0}")
    String nestedEntry(String entryTitle);

}
