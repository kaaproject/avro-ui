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

import java.util.Iterator;

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("deprecation")
public class FormPopup extends PopupPanel implements MouseListener {

    public static class CloseWidget extends InlineLabel {

        public CloseWidget() {
            super();
            setStyleName(Utils.avroUiStyle.closeAction());
        }

        public void doAttach() {
            super.onAttach();
        }

        public void doDetach() {
            super.onDetach();
        }
    }

    private static class BottomPanel extends HorizontalPanel {

        public BottomPanel() {
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            setWidth("100%");
        }

        public void doAttach() {
            super.onAttach();
        }

        public void doDetach() {
            super.onDetach();
        }
    }

    public static class DecoratorPanelImpl extends DecoratorPanel {

        public DecoratorPanelImpl() {
            super();
        }

        public void doAttach() {
            super.onAttach();
        }

        public void doDetach() {
            super.onDetach();
        }

        protected Element getCellElement(int row, int cell) {
            return super.getCellElement(row, cell);
        }
    }

    private class MouseHandler implements MouseDownHandler, MouseUpHandler,
            MouseOutHandler, MouseOverHandler, MouseMoveHandler {

        public void onMouseDown(MouseDownEvent event) {
            beginDragging(event);
        }

        public void onMouseMove(MouseMoveEvent event) {
            continueDragging(event);
        }

        public void onMouseOut(MouseOutEvent event) {
            FormPopup.this.onMouseLeave(decPanel.asWidget());
        }

        public void onMouseOver(MouseOverEvent event) {
            FormPopup.this.onMouseEnter(decPanel.asWidget());
        }

        public void onMouseUp(MouseUpEvent event) {
            endDragging(event);
        }
    }

    private DecoratorPanelImpl decPanel;
    private CloseWidget closeWidget;
    private boolean dragging;
    private int dragStartX, dragStartY;
    private int windowWidth;
    private int clientLeft;
    private int clientTop;

    private String desiredHeight;

    private String desiredWidth;

    private HandlerRegistration resizeHandlerRegistration;

    private BottomPanel bottomPanel;
    private HorizontalPanel buttonsPanel;

    public FormPopup() {
        this(false);
    }

    public FormPopup(boolean autoHide) {
        this(autoHide, true, true, false);
    }

    public FormPopup(boolean autoHide, boolean modal, boolean showCloseButton,
            boolean autoHideOnHistoryEvents) {
        super(autoHide, modal);
        this.setAutoHideOnHistoryEventsEnabled(autoHideOnHistoryEvents);

        setGlassEnabled(true);
        setAnimationEnabled(true);

        if (showCloseButton) {
            closeWidget = new CloseWidget();
        }

        decPanel = new DecoratorPanelImpl();
        decPanel.setStyleName("");
        decPanel.setSize("100%", "100%");

        setStylePrimaryName(Utils.avroUiStyle.formPopup());
        super.setWidget(decPanel);
        setStyleName(getContainerElement(), "popupContent", false);

        if (showCloseButton) {
            Element td = getCellElement(1, 2);
            DOM.appendChild(td, closeWidget.asWidget().getElement());
            adopt(closeWidget.asWidget());
        }

        windowWidth = Window.getClientWidth();
        clientLeft = Document.get().getBodyOffsetLeft();
        clientTop = Document.get().getBodyOffsetTop();

        MouseHandler mouseHandler = new MouseHandler();
        addDomHandler(mouseHandler, MouseDownEvent.getType());
        addDomHandler(mouseHandler, MouseUpEvent.getType());
        addDomHandler(mouseHandler, MouseMoveEvent.getType());
        addDomHandler(mouseHandler, MouseOverEvent.getType());
        addDomHandler(mouseHandler, MouseOutEvent.getType());

        if (showCloseButton) {
            closeWidget.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
        }

        addAttachHandler(new AttachEvent.Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    int popupWidth = getOffsetWidth();
                    int popupHeight = getOffsetHeight();

                    int deltaWidth = (Window.getClientWidth() - popupWidth);
                    int deltaHeight = (Window.getClientHeight() - popupHeight);
                    if (deltaWidth < 0 || deltaHeight < 0) {
                        if (deltaWidth < 0) {
                            popupWidth += deltaWidth;
                        } else {
                            deltaWidth = 0;
                        }
                        if (deltaHeight < 0) {
                            popupHeight += deltaHeight;
                        } else {
                            deltaHeight = 0;
                        }
                        getElement().getStyle().setWidth(popupWidth, Unit.PX);
                        getElement().getStyle().setHeight(popupHeight, Unit.PX);
                        onSizeOverflow(deltaWidth, deltaHeight);
                    }
                }

            }
        });

    }

    public void onSizeOverflow(int deltaWidth, int deltaHeight) {
    }

    private void initButtonsPanel() {
        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);

        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        bottomPanel = new BottomPanel();
        bottomPanel.add(buttonsPanel);

        Element td = getCellElement(2, 1);
        DOM.insertChild(td, bottomPanel.getElement(), 0);
        adopt(bottomPanel);
    }

    public void addButton(com.google.gwt.user.client.ui.Button button) {
        if (buttonsPanel == null)
            initButtonsPanel();
        button.getElement().getStyle().setMarginLeft(20, Unit.PX);
        buttonsPanel.add(button);
    }

    @Override
    public void hide() {
        if (resizeHandlerRegistration != null) {
            resizeHandlerRegistration.removeHandler();
            resizeHandlerRegistration = null;
        }
        super.hide();
    }

    @Override
    public void show() {
        if (resizeHandlerRegistration == null) {
            resizeHandlerRegistration = Window
                    .addResizeHandler(new ResizeHandler() {
                        public void onResize(ResizeEvent event) {
                            windowWidth = event.getWidth();
                        }
                    });
        }
        Timer timer = new Timer() {
            public void run() {
                getElement().getStyle().setProperty("clip", "auto");
            }
        };
        timer.schedule(300);

        super.show();
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (event.getTypeInt()) {
        case Event.ONMOUSEDOWN:
        case Event.ONMOUSEUP:
        case Event.ONMOUSEMOVE:
        case Event.ONMOUSEOVER:
        case Event.ONMOUSEOUT:
            if (!dragging && !isCaptionEvent(event)) {
                return;
            }
        }

        super.onBrowserEvent(event);
    }

    @Override
    public void clear() {
        decPanel.clear();
    }

    @Override
    public Widget getWidget() {
        return decPanel.getWidget();
    }

    @Override
    public Iterator<Widget> iterator() {
        return decPanel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return decPanel.remove(w);
    }

    @Override
    public void setWidget(Widget w) {
        decPanel.setWidget(w);
        maybeUpdateSizeInternal();
    }

    void maybeUpdateSizeInternal() {
        Widget w = super.getWidget();
        if (w != null) {
            if (desiredHeight != null) {
                w.setHeight(desiredHeight);
            }
            if (desiredWidth != null) {
                w.setWidth(desiredWidth);
            }
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        desiredHeight = height;
        // If the user cleared the size, revert to not trying to control
        // children.
        if (height.length() == 0) {
            desiredHeight = null;
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        desiredWidth = width;
        // If the user cleared the size, revert to not trying to control
        // children.
        if (width.length() == 0) {
            desiredWidth = null;
        }
    }

    @Override
    protected void doAttachChildren() {
        try {
            decPanel.doAttach();
        } finally {
            if (closeWidget != null)
                closeWidget.doAttach();
            if (bottomPanel != null)
                bottomPanel.doAttach();
        }
    }

    @Override
    protected void doDetachChildren() {
        try {
            decPanel.doDetach();
        } finally {
            if (closeWidget != null)
                closeWidget.doDetach();
            if (bottomPanel != null)
                bottomPanel.doDetach();
        }
    }

    @Override
    public void onMouseDown(Widget sender, int x, int y) {
        if (DOM.getCaptureElement() == null) {
            /*
             * Need to check to make sure that we aren't already capturing an
             * element otherwise events will not fire as expected. If this check
             * isn't here, any class which extends custom button will not fire
             * its click event for example.
             */
            dragging = true;
            DOM.setCapture(getElement());
            dragStartX = x;
            dragStartY = y;
        }
    }

    @Override
    public void onMouseEnter(Widget sender) {
    }

    @Override
    public void onMouseLeave(Widget sender) {
    }

    @Override
    public void onMouseMove(Widget sender, int x, int y) {
        if (dragging) {
            int absX = x + getAbsoluteLeft();
            int absY = y + getAbsoluteTop();

            if (absX < clientLeft || absX >= windowWidth || absY < clientTop) {
                return;
            }

            setPopupPosition(absX - dragStartX, absY - dragStartY);
        }
    }

    @Override
    public void onMouseUp(Widget sender, int x, int y) {
        dragging = false;
        DOM.releaseCapture(getElement());
    }

    protected void beginDragging(MouseDownEvent event) {
        onMouseDown(decPanel.asWidget(), event.getX(), event.getY());
    }

    protected void continueDragging(MouseMoveEvent event) {
        onMouseMove(decPanel.asWidget(), event.getX(), event.getY());
    }

    protected void endDragging(MouseUpEvent event) {
        onMouseUp(decPanel.asWidget(), event.getX(), event.getY());
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();

        if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN)
                && isCaptionEvent(nativeEvent)) {
            nativeEvent.preventDefault();
        }

        super.onPreviewNativeEvent(event);
    }

    private boolean isCaptionEvent(NativeEvent event) {
        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            com.google.gwt.dom.client.Element element = Element.as(target);
            if (decPanel.getElement().isOrHasChild(element)) {
                String tag = element.getTagName();
                String className = element.getClassName();
                if (tag.equalsIgnoreCase("tr")) {
                    if ("top".equalsIgnoreCase(className)) {
                        return true;
                    }
                } else if (tag.equalsIgnoreCase("td")) {
                    if ("topLeft".equalsIgnoreCase(className)
                            || "topCenter".equalsIgnoreCase(className)
                            || "topRight".equalsIgnoreCase(className)) {
                        return true;
                    }
                } else if (tag.equalsIgnoreCase("div")) {
                    if ("topLeftInner".equalsIgnoreCase(className)
                            || "topCenterInner".equalsIgnoreCase(className)
                            || "topRightInner".equalsIgnoreCase(className)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setTitle(String title) {
        getCellElement(0, 1).setInnerText(title);
    }

    protected Element getCellElement(int row, int cell) {
        return decPanel.getCellElement(row, cell);
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        ensureDebugId(getCellElement(1, 1), baseID, "content");
    }

}
