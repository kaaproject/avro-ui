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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.kaaproject.avro.ui.sandbox.services.cache.JsonCacheService;
import org.springframework.stereotype.Service;

@Service("jsonCacheService")
public class JsonCacheServiceImpl implements JsonCacheService {

    private Map<String, String> uploadedJsonMap = new HashMap<>();  
    
    @Override
    public String putJson(String json) {
        String jsonKey = RandomStringUtils.randomAlphanumeric(10);
        uploadedJsonMap.put(jsonKey, json);
        return jsonKey;
    }

    @Override
    public String getJson(String jsonKey) {
        return uploadedJsonMap.remove(jsonKey);
    }

}
