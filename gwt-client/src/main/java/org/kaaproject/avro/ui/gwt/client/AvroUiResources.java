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
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface AvroUiResources extends ClientBundle {
 
    public interface AvroUiStyle extends AvroUiTheme {
        
        String DEFAULT_CSS = "AvroUiTheme.css";
        
    }
    
    @NotStrict
    @Source(AvroUiStyle.DEFAULT_CSS)
    AvroUiStyle avroUiStyle();
    
    @ImageOptions(width = 0, height = 0)
    @Source("images/circles.png")
    ImageResource circles();

    @ImageOptions(width = 0, height = 0)
    @Source("images/circles_ie6.png")
    ImageResource circles_ie6();
    
    @ImageOptions(width = 0, height = 0)
    @Source("images/vborder.png")
    ImageResource vborder();

    @ImageOptions(width = 0, height = 0)
    @Source("images/vborder_ie6.png")
    ImageResource vborder_ie6();
    
    @Source("images/arrowBottomImage.png")
    ImageResource arrowBottomImage();
    
    @Source("images/arrowRightImage.png")
    ImageResource arrowRightImage();
    
    @ImageOptions(width = 14, height = 14)
    @Source("images/remove.png")
    ImageResource remove();
    
    @ImageOptions(width = 14, height = 14)
    @Source("images/close.png")
    ImageResource close();
    
    @ImageOptions(width = 16, height = 16)
    @Source("images/zoom_out.png")
    ImageResource zoomOut();
    
    @ImageOptions(width = 14, height = 14)
    @Source("images/resizeHandle.png")
    ImageResource resizeHandle();
    
    @ImageOptions(width = 48, height = 48)
    @Source("images/busyIndicator.gif")
    ImageResource busyIndicator();

}

