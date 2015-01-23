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

package org.kaaproject.avro.ui.gwt.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface AvroUiResources extends ClientBundle {
 
    public interface AvroUiStyle extends CssResource {
        
        String DEFAULT_CSS = "AvroUi.css";

        String fieldWidget();

        String padded();
        
        String requiredField();
        
        String secondaryLabel();
        
        String fieldNotes();
        
        String prompt();
        
        String noPrompt();
        
        String invalidField();        
        
        String buttonsPanel();
        
        String fieldSet();
        
        String legend();
        
        String legendImage();
        
        String legendCheckBox();
        
        String legendLabel();
        
        String legendNotes();
        
        String legendPanel();
        
        String content();
        
        String fieldsetVisible();
        
        String fieldsetInvisible();
        
        String breadcrumb();
        
        String divider();
        
        String active();
        
        String buttonSmall();
        
        String cellButton();
        
        String cellButtonSmall();
        
        String actionPopup();
        
        String linkCell();
        
        String error();

        String hint();
        
        String info();
        
        String success();
        
        String warning();        

        @ClassName("icon-error")
        String iconError();
        
        @ClassName("icon-hint")
        String iconHint();
        
        @ClassName("icon-info")
        String iconInfo();
        
        @ClassName("icon-success")
        String iconSuccess();
        
        @ClassName("icon-warning")
        String iconWarning();
        
        @ClassName("ui-icon")
        String uiIcon();
        
        @ClassName("ui-icon-error")
        String uiIconError();
        
        @ClassName("ui-icon-hint")
        String uiIconHint();
        
        @ClassName("ui-icon-info")
        String uiIconInfo();
        
        @ClassName("ui-icon-success")
        String uiIconSuccess();
        
        @ClassName("ui-icon-warning")
        String uiIconWarning();
        
        @ClassName("ui-message")
        String uiMessage();
        
    }
    
    @NotStrict
    @Source(AvroUiStyle.DEFAULT_CSS)
    AvroUiStyle avroUiStyle();
    
    @Source("images/arrowBottomImage.png")
    ImageResource arrowBottomImage();
    
    @Source("images/arrowRightImage.png")
    ImageResource arrowRightImage();
    
    @ImageOptions(width = 14, height = 14)
    @Source("images/remove.png")
    ImageResource remove();
    
    @ImageOptions(width = 14, height = 14)
    @Source("images/resizeHandle.png")
    ImageResource resizeHandle();

}

