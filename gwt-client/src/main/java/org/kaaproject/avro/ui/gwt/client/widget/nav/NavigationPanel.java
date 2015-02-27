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

package org.kaaproject.avro.ui.gwt.client.widget.nav;

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class NavigationPanel extends FlowPanel {

    private Breadcrumbs breadcrumbs;
    private Element zoomElement;
    
    private ZoomListener zoomListener;
    
    public NavigationPanel() {
        super();
        setWidth("100%");
        breadcrumbs = new Breadcrumbs();
        add(breadcrumbs);
        DOM.sinkEvents(this.getElement(), Event.ONMOUSEUP);
        zoomElement = DOM.createDiv();
        zoomElement.addClassName(Utils.avroUiStyle.zoomActionPanel());
        Element zoomIcon = DOM.createDiv();
        zoomIcon.addClassName(Utils.avroUiStyle.zoomAction());
        zoomElement.appendChild(zoomIcon);
        this.getElement().appendChild(zoomElement);
    }
    
    public void enableZoom(boolean enableZoom) {
        if (enableZoom) {
            zoomElement.getStyle().clearDisplay();
        } else {
            zoomElement.getStyle().setDisplay(Display.NONE);
        }
    }
    
    public void setZoomListener(ZoomListener zoomListener) {
        this.zoomListener = zoomListener;
    }
    
    public void clearNavElements() {
        breadcrumbs.clear();
    }
    
    public void addNavElement(Widget navElement) {
        breadcrumbs.add(navElement);
    }
    
    public void removeNavElement(Widget navElement) {
        breadcrumbs.remove(navElement);
    }
 
    @Override
    public void onBrowserEvent(Event event) {
        final int eventType = DOM.eventGetType(event);
        if (Event.ONMOUSEUP == eventType && isZoomElement(event)) {        
           if (zoomListener != null) {
               zoomListener.onZoom();
           }
        } else {
            super.onBrowserEvent(event);
        }
    }
    
    protected boolean isZoomElement(Event event) {
        if (zoomElement != null) {
            int cursorY = event.getClientY();
            int initialY = zoomElement.getAbsoluteTop();
            int height = zoomElement.getOffsetHeight();
            int cursorX = event.getClientX();
            int initialX = zoomElement.getAbsoluteLeft();
            int width = zoomElement.getOffsetWidth();

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
    
    public static interface ZoomListener {
        
        void onZoom();
        
    }
}
