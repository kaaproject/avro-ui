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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class ResizePanel extends FlowPanel {
    private boolean resize = false;
    private List<PanelResizeListener> panelResizedListeners = new ArrayList<>();
    private Element resizeElement;
    
    private int minWidth = 0;
    private int minHeight = 0;

    public ResizePanel(AvroUiStyle style) {
        super();
        DOM.sinkEvents(this.getElement(), Event.ONMOUSEDOWN | Event.ONMOUSEMOVE
                | Event.ONMOUSEUP | Event.ONMOUSEOVER);
        addStyleName(style.resizePanel());
        resizeElement = DOM.createDiv();
        resizeElement.addClassName(style.resizeHandle());
        this.getElement().appendChild(resizeElement);
        
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (resize) {
                    int clientX = event.getNativeEvent().getClientX();
                    int clientY = event.getNativeEvent().getClientY();
                    int originalX = getElement().getAbsoluteLeft();
                    int originalY = getElement().getAbsoluteTop();
                    if (clientX < originalX || clientY < originalY) {
                        event.cancel();
                    }
                }
            }
            
        });
    }
    
    public void setMinSize(int width, int height) {
        if (width >= 0) {
            this.minWidth = width;
        }
        if (height >= 0) {
            this.minHeight = height;
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        final int eventType = DOM.eventGetType(event);
        if (Event.ONMOUSEOVER == eventType && isCursorResize(event)) {
            getElement().getStyle().setCursor(Cursor.DEFAULT);
        }
        if (Event.ONMOUSEDOWN == eventType) {
            if (isCursorResize(event) && !resize) {               
                resize = true;
                DOM.setCapture(this.getElement());
            }
        } else if (resize && Event.ONMOUSEMOVE == eventType) {           
            int absX = event.getClientX();
            int absY = event.getClientY();
            int originalX = getElement().getAbsoluteLeft();
            int originalY = getElement().getAbsoluteTop();
            if (absY > originalY && absX > originalX) {
                int height = absY - originalY + 2 - 
                        (getElement().getAbsoluteBottom() - resizeElement.getAbsoluteTop());
                height = Math.max(minHeight, height);
                this.setHeight(height + "px");
                int width = Math.max(minWidth, absX - originalX + 2);
                this.setWidth(width + "px");
                notifyPanelResizedListeners(width, height);
                event.preventDefault();
            }
        } else if (resize && Event.ONMOUSEUP == eventType) {        
            resize = false;
            DOM.releaseCapture(this.getElement());
        }
    }

    protected boolean isCursorResize(Event event) {
        if (resizeElement != null) {
            int cursorY = event.getClientY();
            int initialY = resizeElement.getAbsoluteTop();
            int height = resizeElement.getOffsetHeight();
            int cursorX = event.getClientX();
            int initialX = resizeElement.getAbsoluteLeft();
            int width = resizeElement.getOffsetWidth();

            if (cursorY >= initialY && cursorY <= (initialY + height) && 
                    cursorX >= initialX && cursorX <= (initialX + width)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void addPanelResizedListener(PanelResizeListener listener) {
        panelResizedListeners.add(listener);
    }

    private void notifyPanelResizedListeners(Integer width, Integer height) {
        for (PanelResizeListener listener : panelResizedListeners) {
            listener.onResized(width, height);
        }
    }
    
    public interface PanelResizeListener {
      
        public void onResized(int width, int height);
        
    }

}
