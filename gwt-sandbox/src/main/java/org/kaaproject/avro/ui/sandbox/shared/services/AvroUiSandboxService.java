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

package org.kaaproject.avro.ui.sandbox.shared.services;

import org.kaaproject.avro.ui.shared.RecordField;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/avroUiSandboxService")
public interface AvroUiSandboxService extends RemoteService {
    
    public RecordField generateFormFromSchema(String avroSchema) throws AvroUiSandboxServiceException;
    
    public String getJsonStringFromRecord(RecordField field) throws AvroUiSandboxServiceException;
    
    public RecordField generateFormDataFromJson(String avroSchema, String json) throws AvroUiSandboxServiceException;
    
    public RecordField getEmptySchemaForm() throws AvroUiSandboxServiceException;
    
    public RecordField generateSchemaFormFromSchema(String avroSchema) throws AvroUiSandboxServiceException;

    public String getJsonStringFromSchemaForm(RecordField field) throws AvroUiSandboxServiceException;
    
    public String uploadJsonToFile(String json) throws AvroUiSandboxServiceException;

}
