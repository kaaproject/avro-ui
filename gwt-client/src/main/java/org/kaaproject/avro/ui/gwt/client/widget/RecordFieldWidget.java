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
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.gwt.client.widget.ResizePanel.PanelResizeListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationAction;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationActionListener;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationContainer;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationElement;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationPanel;
import org.kaaproject.avro.ui.gwt.client.widget.nav.NavigationPanel.ZoomListener;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.UnionField;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class RecordFieldWidget extends AbstractFieldWidget<RecordField> implements NavigationContainer {

    private static final int NAVIGATION_HEADER_HEIGHT = 55; 
    private static final int FRAGMENT_SWITCH_ANIMATION_DURATION = 500;
    private static final int MAX_HEIGHT = 550;

    private ResizePanel resizePanel;
    private LayoutPanel rootPanel;
    private NavigationPanel navPanel;
    private FragmentLayoutPanel fragmentPanel;
    private FlexTable table = new FlexTable();
    private FieldWidgetPanel fieldWidgetPanel;
    
    private List<NavigationElement> navElements;
    
    private boolean isRoot;
    private boolean isAnimating = false;
    private boolean forceNavigation = false;
    private boolean navigationDisabled = false;
    private boolean isLayoutComplete = false;
    private int preferredWidthPx = -1;
    private int preferredHeightPx = -1;
    private String lastWidth = null;
    private String lastHeight = null;
    
    public RecordFieldWidget(AvroWidgetsConfig config, AvroUiStyle style, boolean readOnly) {
        this(config, style, null, readOnly);
    }
    
    public RecordFieldWidget(AvroWidgetsConfig config, AvroUiStyle style, NavigationContainer container, boolean readOnly) {
        super(config, style, container, readOnly);
        this.isRoot = container == null;
        init();
    }
    
    public RecordFieldWidget(AvroWidgetsConfig config) {
        this(config, false);
    }

    public RecordFieldWidget(AvroWidgetsConfig config, boolean readOnly) {
        this(config, (NavigationContainer)null, readOnly);
    }

    public RecordFieldWidget(AvroWidgetsConfig config, NavigationContainer container, boolean readOnly) {
        super(config, container, readOnly);
        this.isRoot = container == null;
        init();
    }
    
    public void setForceNavigation(boolean force) {
        this.forceNavigation = force;
    }
    
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            RecordField currentValue = value;
            setValue(null);
            setValue(currentValue);
        }
    }
    
    public void setPreferredHeightPx(int height) {
        preferredHeightPx = height;
        doLayout();
    }
    
    public void setPreferredWidthPx(int width) {
        preferredWidthPx = width;
        doLayout();
    }
    
    public static boolean isNavigationNeeded(RecordField recordField) {
        if (recordField != null && recordField.getValue() != null) {
            for (FormField field : recordField.getValue()) {
                if (isNavigationNeeded(field)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isNavigationNeeded(FormField field) {
        FieldType type = field.getFieldType();
        if (type.isComplex()) {
            if (type == FieldType.RECORD) {
                return true;
            } else if (type == FieldType.ARRAY) {
            	field.finalizeMetadata();
                if (ArrayFieldWidget.isGridNeeded((ArrayField)field)) {
                    return true;
                }
            } else if (type == FieldType.UNION) {
            	field.finalizeMetadata();
                for (FormField unionValue : ((UnionField)field).getAcceptableValues()) {
                    if (unionValue.getFieldType() == FieldType.UNION) {
                        return true;
                    } else if (unionValue.getFieldType() == FieldType.RECORD) {
                        if (isNavigationNeeded((RecordField)unionValue)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void init() {
        if (isRoot) {
            setNavigationContainer(this);
        }
    }
    
    @Override
    public void setHeight(String height) {
        if (resizePanel != null && rootPanel != null) {
            resizePanel.setHeight(height);
            rootPanel.setHeight(height);
            super.setHeight("100%");
            lastHeight = height;
        } else {
            super.setHeight(height);
        }
    }
    
    @Override
    public void setWidth(String width) {
        if (resizePanel != null && rootPanel != null) {
            resizePanel.setWidth(width);
            rootPanel.setWidth(width);
            super.setWidth("100%");
            lastWidth = width;
        } else {
            super.setWidth(width);
        }
    }
    
    public void enableZoom(boolean enable) {
        if (navPanel != null) {
            navPanel.enableZoom(enable);
        }
    }
    
    public Widget getAnchorWidget() {
        return resizePanel;
    }
    
    private void initNavigation() {
        if (resizePanel == null) {
            resizePanel = new ResizePanel(style);
            rootPanel = new LayoutPanel();
            navPanel = new NavigationPanel();
            
            navPanel.setZoomListener(new ZoomListener() {
                @Override
                public void onZoom() {
                    
                    final int prevPreferredWidthPx = preferredWidthPx;
                    final int prevPreferredHeightPx = preferredHeightPx;
                    final String prevWidth;
                    if (lastWidth != null) {
                        prevWidth = lastWidth;
                    } else {
                        prevWidth = resizePanel.getElement().getClientWidth() + "px";
                    }
                    final String prevHeight;
                    if (lastHeight != null) {
                        prevHeight = lastHeight;
                    } else {
                        prevHeight = resizePanel.getElement().getClientHeight() + "px";
                    }
                    final AvroWidgetsConfig prevConfig = config;
                    
                    final FormPopup popup = new FormPopup();
                    
                    popup.setTitle(value.getDisplayName());
                    
                    int dWidth = Window.getClientWidth() - 150;
                    int dHeight = Window.getClientHeight() - 200;
                    
                    AvroWidgetsConfig config = new AvroWidgetsConfig.Builder().recordPanelWidth(dWidth-100).
                            gridHeight(dHeight-350).tableHeight(dHeight-370).createConfig();
           
                    enableZoom(false);
                    setPreferredWidthPx(dWidth);
                    setPreferredHeightPx(dHeight);
                    updateConfig(config);
                    
                    popup.add(getAnchorWidget());

                    Button close = new Button(Utils.constants.close(), new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            popup.hide();
                        }
                    });
                    popup.addButton(close);
                    
                    popup.addCloseHandler(new CloseHandler<PopupPanel>() {
                        @Override
                        public void onClose(CloseEvent<PopupPanel> event) {
                            enableZoom(true);
                            setWidget(getAnchorWidget());
                            updateConfig(prevConfig);
                            setPreferredWidthPx(prevPreferredWidthPx);
                            setPreferredHeightPx(prevPreferredHeightPx);
                            if (prevWidth != null) {
                                setWidth(prevWidth);
                            }
                            if (prevHeight != null) {
                                setHeight(prevHeight);
                            }
                            resizePanel.getElement().getStyle().setPropertyPx("maxHeight", MAX_HEIGHT);
                            rootPanel.getElement().getStyle().setPropertyPx("maxHeight", MAX_HEIGHT);
                        }
                    });

                    resizePanel.getElement().getStyle().setPropertyPx("maxHeight", 1000);
                    rootPanel.getElement().getStyle().setPropertyPx("maxHeight", 1000);
                    popup.center();
                    popup.show();
                }
            });
            
            fragmentPanel = new FragmentLayoutPanel();
            fragmentPanel.setAnimationDuration(FRAGMENT_SWITCH_ANIMATION_DURATION);
            rootPanel.add(navPanel);
            rootPanel.setWidgetLeftRight(navPanel, 0, Unit.PX, 0, Unit.PX);
            rootPanel.setWidgetTopHeight(navPanel, 0, Unit.PX, NAVIGATION_HEADER_HEIGHT, Unit.PX);
            rootPanel.setWidgetVerticalPosition(navPanel, Alignment.END);
            rootPanel.add(fragmentPanel);
            rootPanel.setWidgetLeftRight(fragmentPanel, 0, Unit.PX, 0, Unit.PX);
            rootPanel.setWidgetTopBottom(fragmentPanel, NAVIGATION_HEADER_HEIGHT, Unit.PX, 0, Unit.PX);    
            rootPanel.setWidgetVerticalPosition(fragmentPanel, Alignment.STRETCH);
            resizePanel.add(rootPanel);
            resizePanel.setWidth(FULL_WIDTH);
            resizePanel.addPanelResizedListener(new PanelResizeListener() {
                @Override
                public void onResized(int width, int height) {
                    setWidth(width + "px");
                    setHeight(height + "px");
                }
            });
            navElements = new ArrayList<>();

            resizePanel.getElement().getStyle().setPropertyPx("maxHeight", MAX_HEIGHT);
            rootPanel.getElement().getStyle().setPropertyPx("maxHeight", MAX_HEIGHT);
            resizePanel.getElement().getStyle().setPropertyPx("minHeight", MAX_HEIGHT);
            rootPanel.getElement().getStyle().setPropertyPx("minHeight", MAX_HEIGHT);
        }
    }
    
    private void clearNavigation() {
        if (resizePanel != null) {
            resizePanel = null;
            rootPanel = null;
            navPanel.clear();
            navPanel = null;
            fragmentPanel = null;
            navElements.clear();
            navElements = null;
        }
    }
    
    private void doLayout () {
        if (isRoot) {
            Element element = getElement();
            element.getStyle().clearOverflow();
            element.getStyle().clearHeight();
            if (element.getParentElement() != null) {
                element.getParentElement().getStyle().clearProperty("minHeight");
            }
            if (preferredWidthPx > 0 || preferredHeightPx > 0) {
                if (preferredWidthPx > 0) {
                    setWidth(preferredWidthPx + "px");
                }
                if (preferredHeightPx > 0) {
                    setHeight(preferredHeightPx + "px");
                    if (element.getParentElement() != null) {
                        Element parentElement = element.getParentElement();
                        parentElement.getStyle().setPropertyPx("minHeight", childsOffsetHeight(parentElement));
                    }
                }
                if (navigationDisabled) {
                    element.getStyle().setOverflow(Overflow.AUTO);
                }
            } else if (!navigationDisabled) {
                int height = 0;
                setHeight("0px");
                if (element.getParentElement() != null) {
                    Element parentElement = element.getParentElement();
                    height = maxHeight(parentElement);
                }
                if (height <= 0) {
                    height = maxHeight(element);
                } 
                height += NAVIGATION_HEADER_HEIGHT;
                setHeight(height+"px");
            } 
        }
    }
    
    private static int childsOffsetHeight(Element element) {
        int height = 0;
        for (int i=0; i<element.getChildNodes().getLength();i++) {
            Node node = element.getChildNodes().getItem(i);
            if (Element.is(node)) {
                Element childElement = Element.as(node);
                height += childElement.getOffsetHeight();
            }
        }
        return height;
    }
    
    private static int maxHeight(Element element) {
        int height = getInnerHeight(element);
        for (int i=0; i<element.getChildNodes().getLength();i++) {
            Node node = element.getChildNodes().getItem(i);
            if (Element.is(node)) {
                Element childElement = Element.as(node);
                if (isElementVisible(element)) {
                    height = Math.max(height, maxHeight(childElement));
                }
            }
        }
        return height;
    }
    
    private static boolean isElementVisible(Element element) {
        if (UIObject.isVisible(element)) {
            String visibility = element.getStyle().getVisibility();
            if (visibility == null || !visibility.equals(Visibility.HIDDEN.getCssName())) {
                return true;
            }
        }
        return false;
    }
    
    private static int getInnerHeight(Element element) {
        int height = element.getClientHeight();
        if (height == 0) {
            height = getComputedStylePropertyPixels(element, "height");
        }
        if (height == 0) {
            height = getComputedStylePropertyPixels(element, "min-height");
        }
        height -= getComputedStylePropertyPixels(element, "padding-top");
        height -= getComputedStylePropertyPixels(element, "padding-bottom");
        height -= getComputedStylePropertyPixels(element, "border-top-width");
        height -= getComputedStylePropertyPixels(element, "border-bottom-width");
        return height;
    }
     
    private static int getComputedStylePropertyPixels(Element element, String prop) {
        int propInt = 0;
        String propString = getComputedStyleProperty(element, prop);
        if (!Utils.isBlank(propString) && propString.endsWith("px")) {
            try {
                propInt = (int)Double.parseDouble(propString.substring(0, propString.length()-2));
            } catch (NumberFormatException e) {}
        }
        return propInt;
    }

    private static native String getComputedStyleProperty(Element el, String prop)  /*-{
        var computedStyle;
        if (document.defaultView && document.defaultView.getComputedStyle) { // standard (includes ie9)
          computedStyle = document.defaultView.getComputedStyle(el, null)[prop];
       
        } else if (el.currentStyle) { // IE older
          computedStyle = el.currentStyle[prop];
       
        } else { // inline style
          computedStyle = el.style[prop];
        }
        return computedStyle;
      }-*/;

    @Override
    protected Widget constructForm() {
        Widget form;
        if (isRoot) {
            isLayoutComplete = false;
        }
        navigationDisabled = isRoot && !(forceNavigation || isNavigationNeeded(value));
        table.getColumnFormatter().setWidth(0, config.getLabelsColumnWidth());
        table.getColumnFormatter().setWidth(1, config.getFieldsColumnWidth());
        constructFormData(table, value, registrations);
        if (isRoot && !navigationDisabled) {     
            isAnimating = false;
            initNavigation();
            navPanel.clearNavElements();
            navElements.clear();
            fragmentPanel.clear();
            if (value != null) {
                navPanel.setVisible(true);
                showField(value, null);
            } else {
                navPanel.setVisible(false);
            }
            form = resizePanel;
        } else {
            clearNavigation();
            if (value != null && value.isOverride()) {
                fieldWidgetPanel = new FieldWidgetPanel(style, value, readOnly, true);
                fieldWidgetPanel.setWidth(config.getRecordPanelWidth());
                if (value.isOverride() && !readOnly && !value.isReadOnly()) {
                    fieldWidgetPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Boolean> event) {
                            fireChanged();
                        }
                    });
                }
                fieldWidgetPanel.setContent(table);
                form = fieldWidgetPanel;
            } else {
                form = table;
            }
        }
        return form;
    }
    
    
    @Override
    public void updateConfig(AvroWidgetsConfig config) {
        super.updateConfig(config);
        if (table != null) {
            table.getColumnFormatter().setWidth(0, config.getLabelsColumnWidth());
            table.getColumnFormatter().setWidth(1, config.getFieldsColumnWidth());
        }
        if (fieldWidgetPanel != null) {
            fieldWidgetPanel.setWidth(config.getRecordPanelWidth());
        }
    }

    @Override
    protected Widget constructLabel(FlexTable table, FormField field, int row,
            int column) {
        Widget label = super.constructLabel(table, field, row, column);
        if (!navigationDisabled) {
            label.addStyleName(style.padded());
        }
        return label;
    }

    @Override
    protected int placeWidget(FlexTable table, FieldType type, Widget widget,
            int row, int column, List<HandlerRegistration> handlerRegistrations) {
        if (!navigationDisabled && (!type.isComplex() || shouldPlaceNestedWidgetButton(type))) {
            widget.addStyleName(style.padded());
        }
        return super.placeWidget(table, type, widget, row, column, handlerRegistrations);
    }

    @Override
    public void goBack() {
        gotoIndex(navElements.size()-2);
    }

    @Override
    public void gotoIndex(final int gotoIndex) {
        final int index = confirmIndex(gotoIndex);
        if (!isAnimating && index < navElements.size()-1) {
            final NavigationElement navElement = navElements.get(index);
            isAnimating = true;
            fragmentPanel.setAnimationCallback(new AnimationCallback() {
                
                @Override
                public void onLayout(Layer layer, double progress) {}
                
                @Override
                public void onAnimationComplete() {
                    for (NavigationElement oldNavElement : navElements.subList(index+1, navElements.size())) {
                        navPanel.removeNavElement(oldNavElement.getLink());
                        fragmentPanel.remove(oldNavElement.getWidget());
                    }
                    navElements = navElements.subList(0, index+1);
                    navElement.onShown();
                    fragmentPanel.setAnimationCallback(null);
                    if (!readOnly) {
                        fireChanged();
                    }
                    isAnimating = false;
                }
            });
            fragmentPanel.showWidget(navElement.getIndex());
        }
    }
    
    private int confirmIndex(int index) {
        int confirmedIndex = index;
        for (int i=navElements.size()-1;i>index;i--) {
            NavigationElement navElement = navElements.get(i);
            String mayClose = navElement.mayClose();
            if (mayClose != null && !Window.confirm(mayClose)) {
                confirmedIndex = i;
                break;
            } 
        }
        return confirmedIndex;
    }

    @Override
    public void showField(FormField field, NavigationActionListener listener) {
        if (!isAnimating) {
            NavigationAction action = (readOnly || field.isReadOnly()) ? NavigationAction.VIEW : NavigationAction.EDIT;
            constructNavigationElement(field, action, listener);
        }
    }

    @Override
    public void addNewField(FormField field, NavigationActionListener listener) {
        if (!isAnimating) {
            constructNavigationElement(field, NavigationAction.ADD, listener);
            fireChanged();
        }
    }
    
    @Override
    public boolean validate() {
        boolean valid = true;
        if (navElements != null) {
            for (NavigationElement navElement : navElements) {
                valid &= navElement.isAdded();
            }
        }
        if (valid) {
            valid &= super.validate();
        }
        return valid;
    }
    
    private void constructNavigationElement(FormField field, NavigationAction action, final NavigationActionListener listener) {
        final NavigationElement navElement = new NavigationElement(config, style, this, navElements.size(), 
                field, action, 
                        new NavigationActionListener() {
                    @Override
                    public void onAdded(FormField field) {
                        if (listener != null) {
                            listener.onAdded(field);
                        }
                        fireChanged();
                    }

                    @Override
                    public void onChanged(FormField field) {
                        if (listener != null) {
                            listener.onChanged(field);
                        }
                        fireChanged();
                    }
        });
        navElements.add(navElement);
        navPanel.addNavElement(navElement.getLink());
        fragmentPanel.add(navElement.getWidget());
        final boolean doLayout = navElements.size()==1 && !isLayoutComplete;
        isAnimating = true;
        fragmentPanel.setAnimationCallback(new AnimationCallback() {
            @Override
            public void onLayout(Layer layer, double progress) {}
            
            @Override
            public void onAnimationComplete() {
                if (doLayout && !isLayoutComplete && isAnimating) {
                    doLayout();
                    isLayoutComplete = true;
                    fragmentPanel.remove(navElement.getIndex());
                    fragmentPanel.setAnimationDuration(FRAGMENT_SWITCH_ANIMATION_DURATION);
                    fragmentPanel.add(navElement.getWidget());
                    fragmentPanel.showWidget(navElement.getIndex());
                } else {
                    navElement.onShown();
                    fragmentPanel.setAnimationCallback(null);
                    isAnimating = false;
                }
            }
        });
        
        if (doLayout) {
            fragmentPanel.setAnimationDuration(0);
            fragmentPanel.showWidget(navElement.getIndex());
        } else {
            fragmentPanel.showWidget(navElement.getIndex());
        }
    }
 
    private class FragmentLayoutPanel extends DeckLayoutPanel {
        
        private AnimationCallback animationCallback;
        
        private FragmentLayoutPanel() {
            super();
        }
        
        private void setAnimationCallback(AnimationCallback animationCallback) {
            this.animationCallback = animationCallback;
        }
        
        @Override
        public void animate(int duration) {
            super.animate(duration, animationCallback);
        }
        
    }


}
