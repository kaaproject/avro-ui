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

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class BusyPopup extends SimplePanel {
    
    private static BusyPopup instance;
    
    public static void showPopup() {
        if (instance == null) {
            instance = new BusyPopup();
        }
        instance.rollDown();
    }
    
    public static void hidePopup() {
        if (instance != null) {
            instance.hide();
        }
    }
    
    private static final int ANIMATION_DURATION = 300;
    
    private static final int GLASS_Z_INDEX = 32766;
    
    private static final int POPUP_Z_INDEX = 32767;
    
    private ResizeHandler glassResizer = new ResizeHandler() {
        public void onResize(ResizeEvent event) {
          Style style = glass.getStyle();

          int winWidth = Window.getClientWidth();
          int winHeight = Window.getClientHeight();

          style.setDisplay(Display.NONE);
          style.setWidth(0, Unit.PX);
          style.setHeight(0, Unit.PX);

          int width = Document.get().getScrollWidth();
          int height = Document.get().getScrollHeight();

          style.setWidth(Math.max(width, winWidth), Unit.PX);
          style.setHeight(Math.max(height, winHeight), Unit.PX);

          style.setDisplay(Display.BLOCK);
        }
    };
    
    private boolean showing;
    private boolean isAnimationEnabled = true;
    private Element glass;
    
    private HandlerRegistration nativePreviewHandlerRegistration;
    private int leftPosition = -1;
    private int topPosition = -1;
    private RollAnimation rollAnimation = new RollAnimation(this);

    public BusyPopup() {
        glass = Document.get().createDivElement();
        glass.setClassName(Utils.avroUiStyle.busyGlass());

        glass.getStyle().setPosition(Position.ABSOLUTE);
        glass.getStyle().setLeft(0, Unit.PX);
        glass.getStyle().setTop(0, Unit.PX);
        glass.getStyle().setZIndex(GLASS_Z_INDEX);
        
        getElement().getStyle().setZIndex(POPUP_Z_INDEX);
        
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSize("320px", "70px");
        panel.addStyleName(Utils.avroUiStyle.busyPopup());
        Image image = new Image();
        image.setResource(Utils.resources.busyIndicator());
        panel.add(image);
        panel.setCellWidth(image, "60px");
        panel.setCellHorizontalAlignment(image, HasHorizontalAlignment.ALIGN_CENTER);
        panel.setCellVerticalAlignment(image, HasVerticalAlignment.ALIGN_MIDDLE);
        Label label = new Label();
        label.setText(Utils.constants.busyPopupText());
        label.getElement().getStyle().setPaddingRight(15, Unit.PX);
        panel.add(label);
        panel.setCellHorizontalAlignment(label, HasHorizontalAlignment.ALIGN_CENTER);
        panel.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
        setWidget(panel);
        
    }
    
    public void setAnimationEnabled(boolean enable) {
        isAnimationEnabled = enable;
    }
    
    public void setPopupPosition(int left, int top) {
        leftPosition = left;
        topPosition = top;

        left -= Document.get().getBodyOffsetLeft();
        top -= Document.get().getBodyOffsetTop();

        Element elem = getElement();
        elem.getStyle().setPropertyPx("left", left);
        elem.getStyle().setPropertyPx("top", top);
    }
    
    public void rollDown() {
        boolean initiallyShowing = showing;
        boolean initiallyAnimated = isAnimationEnabled;
        
        if (!initiallyShowing) {
            setVisible(false);
            setAnimationEnabled(false);
            show();
        }
        
        Element elem = getElement();
        elem.getStyle().setPropertyPx("left", 0);
        elem.getStyle().setPropertyPx("top", 0);

        int left = (Window.getClientWidth() - getOffsetWidth()) >> 1;
        int top = -getOffsetHeight();
        
        setPopupPosition(Math.max(Window.getScrollLeft() + left, 0), Math.max(
                Window.getScrollTop() + top, -getOffsetHeight()));
        
        if (!initiallyShowing) {
            setAnimationEnabled(initiallyAnimated);
            if (initiallyAnimated) {
              setVisible(true);
              rollAnimation.run(ANIMATION_DURATION);
            } else {
              setVisible(true);
            }
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
      getElement().getStyle().setProperty("visibility", visible ? "visible" : "hidden");
      if (glass != null) {
        glass.getStyle().setProperty("visibility", visible ? "visible" : "hidden");
      }
    }
    
    public boolean isShowing() {
        return showing;
    }
    
    public void show() {
        if (showing) {
          return;
        } else if (isAttached()) {
          this.removeFromParent();
        }
        rollAnimation.setState(true, false);
    }
    
    public void hide() {
        if (!isShowing()) {
            return;
        }
        rollAnimation.setState(false, false);
    }
    
    private void previewNativeEvent(NativePreviewEvent event) {
        event.cancel();
        return;
    }
    
    private void updateHandlers() {
        if (nativePreviewHandlerRegistration != null) {
            nativePreviewHandlerRegistration.removeHandler();
            nativePreviewHandlerRegistration = null;
        }
        if (showing) {
            nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
                public void onPreviewNativeEvent(NativePreviewEvent event) {
                  previewNativeEvent(event);
                }
            });
        }
    }
    
    static class RollAnimation extends Animation {

        private BusyPopup curPanel = null;
        private boolean isUnloading;
        private boolean showing;
        private Timer showTimer;
        private boolean glassShowing;
        private HandlerRegistration resizeRegistration;
        private int offsetHeight = -1;
        
        public RollAnimation(BusyPopup panel) {
            this.curPanel = panel;
        }

        public void setState(boolean showing, boolean isUnloading) {
            this.isUnloading = isUnloading;
            cancel();
            
            if (showTimer != null) {
                showTimer.cancel();
                showTimer = null;
                onComplete();
            }
            
            curPanel.showing = showing;
            curPanel.updateHandlers();
            
            boolean animate = !isUnloading && curPanel.isAnimationEnabled;
            this.showing = showing;
            if (animate) {
                if (showing) {
                    maybeShowGlass();
                    curPanel.getElement().getStyle().setProperty("position", "absolute");
                    if (curPanel.topPosition != -1) {
                        curPanel.setPopupPosition(curPanel.leftPosition,
                            curPanel.topPosition);
                    }
                    RootPanel.get().add(curPanel);
                    showTimer = new Timer() {
                        @Override
                        public void run() {
                          showTimer = null;
                          RollAnimation.this.run(ANIMATION_DURATION);
                        }
                    };
                    showTimer.schedule(1);
                } else {
                    run(ANIMATION_DURATION);
                }
            } else {
                onInstantaneousRun();
            }
        }
        
        @Override
        protected void onComplete() {
            if (!showing) {
                maybeShowGlass();
                if (!isUnloading) {
                    RootPanel.get().remove(curPanel);
                }
            }
            curPanel.getElement().getStyle().setProperty("overflow", "visible");
        }
        
        @Override
        protected void onStart() {
            offsetHeight = curPanel.getOffsetHeight();
            super.onStart();
        }
        
        @Override
        protected void onUpdate(double progress) {
            if (!showing) {
                progress = 1.0 - progress;
            }
            
            int topPosition = (int) (progress * offsetHeight) - offsetHeight;
            
            curPanel.setPopupPosition(curPanel.leftPosition, Math.max(
                    Window.getScrollTop() + topPosition, -offsetHeight));
        }
        
        private void maybeShowGlass() {
            if (showing) {
                Document.get().getBody().appendChild(curPanel.glass);
                resizeRegistration = Window.addResizeHandler(curPanel.glassResizer);
                curPanel.glassResizer.onResize(null);
                glassShowing = true;
            } else if (glassShowing) {
                Document.get().getBody().removeChild(curPanel.glass);
                resizeRegistration.removeHandler();
                resizeRegistration = null;
                glassShowing = false;
            }
        }

        private void onInstantaneousRun() {
            maybeShowGlass();
            if (showing) {
                curPanel.getElement().getStyle().setProperty("position", "absolute");
                if (curPanel.topPosition != -1) {
                    curPanel.setPopupPosition(curPanel.leftPosition, curPanel.topPosition);
                  }
                  RootPanel.get().add(curPanel);
            } else {
                if (!isUnloading) {
                    RootPanel.get().remove(curPanel);
                }
            }
            curPanel.getElement().getStyle().setProperty("overflow", "visible");
        }

    }
}
