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

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class ResizePanel extends FlowPanel {
    private boolean resize = false;
    private List<PanelResizeListener> panelResizedListeners = new ArrayList<>();
    private ImageElement resizeElement;
    
    private int minHeight;
    private int minWidth;

    public ResizePanel() {
        super();
        DOM.sinkEvents(this.getElement(), Event.ONMOUSEDOWN | Event.ONMOUSEMOVE
                | Event.ONMOUSEUP | Event.ONMOUSEOVER);
        getElement().getStyle().setPosition(Position.RELATIVE);
        resizeElement = DOM.createImg().cast();
        resizeElement.setSrc(Utils.resources.resizeHandle().getSafeUri().asString());
        resizeElement.getStyle().setPosition(Position.ABSOLUTE);
        resizeElement.getStyle().setBottom(0, Unit.PX);
        resizeElement.getStyle().setRight(0, Unit.PX);
        resizeElement.getStyle().setZIndex(10);
        this.getElement().appendChild(resizeElement);
        
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            
            @SuppressWarnings("deprecation")
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (resize) {
                    int clientX = event.getNativeEvent().getClientX();
                    int clientY = event.getNativeEvent().getClientY();
                    int originalX = DOM.getAbsoluteLeft(getElement());
                    int originalY = DOM.getAbsoluteTop(getElement());
                    if (clientX < originalX || clientY < originalY) {
                        event.cancel();
                    }
                }
            }
            
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBrowserEvent(Event event) {
        final int eventType = DOM.eventGetType(event);
        if (Event.ONMOUSEOVER == eventType && isCursorResize(event)) {
            DOM.setStyleAttribute(this.getElement(), "cursor", "default");
        }
        if (Event.ONMOUSEDOWN == eventType) {
            if (minHeight == 0 && minWidth == 0) {
                minHeight = getElement().getOffsetHeight();
                minWidth = getElement().getOffsetWidth();
            }
            if (isCursorResize(event) && !resize) {               
                resize = true;
                DOM.setCapture(this.getElement());
            }
        } else if (resize && Event.ONMOUSEMOVE == eventType) {           
            int absX = DOM.eventGetClientX(event);
            int absY = DOM.eventGetClientY(event);
            int originalX = DOM.getAbsoluteLeft(this.getElement());
            int originalY = DOM.getAbsoluteTop(this.getElement());
            if (absY > originalY && absX > originalX) {
                int height = Math.max(minHeight, absY - originalY + 2);      
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

    @SuppressWarnings("deprecation")
    protected boolean isCursorResize(Event event) {
        if (resizeElement != null) {
            int cursorY = DOM.eventGetClientY(event);
            int initialY = resizeElement.getAbsoluteTop();
            int height = resizeElement.getOffsetHeight();
            int cursorX = DOM.eventGetClientX(event);
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
