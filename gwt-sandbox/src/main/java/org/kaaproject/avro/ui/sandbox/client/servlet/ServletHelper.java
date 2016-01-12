/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.avro.ui.sandbox.client.servlet;

import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class ServletHelper {
    
    private static final String UPLOAD_SERVLET_PATH = "servlet/fileUploadServlet";
    
    private static final String JSON_KEY = "jsonKey";
    private static final String FILE_NAME = "fileName";
    
    private static final String JSON_SCHEMA_FILE = "schema.json";
    private static final String JSON_RECORD_FILE = "record.json";

    public static void downloadJsonSchema(String key) {
        String getUrl = composeURL(UPLOAD_SERVLET_PATH, JSON_KEY+"="+key, FILE_NAME+"="+JSON_SCHEMA_FILE);
        String url = GWT.getModuleBaseURL() + getUrl;
        Window.open( url, "_self", "enabled");
    }
    
    public static void downloadJsonRecord(String key) {
        String getUrl = composeURL(UPLOAD_SERVLET_PATH, JSON_KEY+"="+key, FILE_NAME+"="+JSON_RECORD_FILE);
        String url = GWT.getModuleBaseURL() + getUrl;
        Window.open( url, "_self", "enabled");
    }

    private static String composeURL(String servletPath, String... params) {
        String ret = servletPath;
        ret = ret.replaceAll("[\\?&]+$", "");
        String sep = ret.contains("?") ? "&" : "?";
        for (String par : params) {
          ret += sep + par;
          sep = "&";
        }
        for (Entry<String, List<String>> e : Window.Location.getParameterMap().entrySet()) {
          ret += sep + e.getKey() + "=" + e.getValue().get(0);
        }
        ret += sep + "random=" + Math.random();
        return ret;
    }

}
