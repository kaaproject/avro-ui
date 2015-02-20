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

package org.kaaproject.avro.ui.sandbox.client.util;

import org.kaaproject.avro.ui.sandbox.client.AvroUiSandboxConstants;
import org.kaaproject.avro.ui.sandbox.client.AvroUiSandboxResources;
import org.kaaproject.avro.ui.sandbox.client.i18n.AvroUiSandboxMessages;
import org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxServiceException;

import com.google.gwt.core.client.GWT;

public class Utils {

    public static final AvroUiSandboxResources resources = GWT.create(
            AvroUiSandboxResources.class);

    public static final AvroUiSandboxConstants constants = GWT.create(
            AvroUiSandboxConstants.class);

    public static final AvroUiSandboxMessages messages = GWT.create(
            AvroUiSandboxMessages.class);

    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof AvroUiSandboxServiceException) {
            AvroUiSandboxServiceException sandboxException = (AvroUiSandboxServiceException)throwable;
            String message = constants.general_error();
            message += sandboxException.getMessage();
            return message;
        } else {
            return throwable.getMessage();
        }
    }

    public static boolean isNotBlank(String string) {
        return string != null && string.length() > 0;
    }

    public static boolean isBlank(String string) {
        return string == null || string.length() == 0;
    }


}
