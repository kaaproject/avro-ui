/*
 * Copyright 2014 CyberVision, Inc.
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
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.FieldType;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.UnionField;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class RecordFieldWidget extends AbstractFieldWidget<RecordField> implements NavigationContainer {

    private ResizePanel resizePanel;
    private LayoutPanel rootPanel;
    private Breadcrumbs breadcrumbs;
    private FragmentLayoutPanel fragmentPanel;
    private FlexTable table = new FlexTable();
    
    private List<NavigationElement> navElements;
    
    private boolean isRoot;
    private boolean forceNavigation = false;
    
    public RecordFieldWidget(AvroUiStyle style, boolean readOnly) {
        this(style, null, readOnly);
    }
    
    public RecordFieldWidget(AvroUiStyle style, NavigationContainer container, boolean readOnly) {
        super(style, container, readOnly);
        this.isRoot = container == null;
        init();
    }
    
    public RecordFieldWidget() {
        this(false);
    }

    public RecordFieldWidget(boolean readOnly) {
        this((NavigationContainer)null, readOnly);
    }

    public RecordFieldWidget(NavigationContainer container, boolean readOnly) {
        super(container, readOnly);
        this.isRoot = container == null;
        init();
    }
    
    public void setForceNavigation(boolean force) {
        this.forceNavigation = force;
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
                if (ArrayFieldWidget.isGridNeeded((ArrayField)field)) {
                    return true;
                }
            } else if (type == FieldType.UNION) {
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
        super.setHeight(height);
        if (resizePanel != null ) {
            resizePanel.setHeight(height);
        }
        if (rootPanel != null) {
            rootPanel.setHeight(height);
        }     
    }
    
    private void initNavigation() {
        if (resizePanel == null) {
            resizePanel = new ResizePanel();
            rootPanel = new LayoutPanel();
            breadcrumbs = new Breadcrumbs();
            fragmentPanel = new FragmentLayoutPanel();
            fragmentPanel.setAnimationDuration(500);
            rootPanel.add(breadcrumbs);
            rootPanel.setWidgetLeftRight(breadcrumbs, 0, Unit.PX, 0, Unit.PX);
            rootPanel.setWidgetTopHeight(breadcrumbs, 0, Unit.PX, 60, Unit.PX);
            rootPanel.setWidgetVerticalPosition(breadcrumbs, Alignment.END);
            rootPanel.add(fragmentPanel);
            rootPanel.setWidgetLeftRight(fragmentPanel, 0, Unit.PX, 0, Unit.PX);
            rootPanel.setWidgetTopBottom(fragmentPanel, 60, Unit.PX, 0, Unit.PX);    
            rootPanel.setWidgetVerticalPosition(fragmentPanel, Alignment.STRETCH);
            resizePanel.add(rootPanel);
            resizePanel.setWidth(FULL_WIDTH);
            resizePanel.addPanelResizedListener(new PanelResizeListener() {
                @Override
                public void onResized(int width, int height) {
                    setHeight(height+"px");
                }
            });
            navElements = new ArrayList<>();
            Element element = getElement();
            if (element.getParentElement() != null) {
                Element parentElement = element.getParentElement();
                int height = getInnerHeight(parentElement);
                for (int i=0; i<parentElement.getChildNodes().getLength();i++) {
                    Node node = parentElement.getChildNodes().getItem(i);
                    if (Element.is(node)) {
                        Element childElement = Element.as(node);
                        if (!element.isOrHasChild(childElement)) {
                            height -= childElement.getOffsetHeight();
                        }
                    }
                }
                setHeight(height+"px");
            } else { 
                setHeight(getInnerHeight(element)+"px");
            }
        }
    }
    
    private static int getInnerHeight(Element element) {
        int height = element.getClientHeight();
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
        if (isRoot) {
            if (value != null) {
                value.setNotNull();
            }
        }
        constructFormData(table, value, registrations);
        if (isRoot && (forceNavigation || isNavigationNeeded(value))) {
            initNavigation();
            breadcrumbs.clear();
            navElements.clear();
            fragmentPanel.clear();
            if (value != null) {
                breadcrumbs.setVisible(true);
                showField(value, null);
            } else {
                breadcrumbs.setVisible(false);
            }
            return resizePanel;
        } else {
            if (isRoot) {
                Element element = getElement();
                element.getStyle().clearHeight();
            }
            if (value != null && value.isOverride()) {
                FieldWidgetPanel fieldWidgetPanel = new FieldWidgetPanel(style, value, readOnly, true);
                fieldWidgetPanel.setWidth("700px");
                if (value.isOverride() && !readOnly && !value.isReadOnly()) {
                    fieldWidgetPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Boolean> event) {
                            fireChanged();
                        }
                    });
                }
                fieldWidgetPanel.setContent(table);
                return fieldWidgetPanel;
            } else {
                return table;
            }
        }
    }
    
    @Override
    public void goBack() {
        gotoIndex(navElements.size()-2);
    }

    @Override
    public void gotoIndex(final int index) {
        final NavigationElement navElement = navElements.get(index);
        fragmentPanel.setAnimationCallback(new AnimationCallback() {
            
            @Override
            public void onLayout(Layer layer, double progress) {}
            
            @Override
            public void onAnimationComplete() {
                for (NavigationElement oldNavElement : navElements.subList(index+1, navElements.size())) {
                    breadcrumbs.remove(oldNavElement.getLink());
                    fragmentPanel.remove(oldNavElement.getWidget());
                }
                navElements = navElements.subList(0, index+1);
                navElement.onShown();
                fragmentPanel.setAnimationCallback(null);
            }
        });
        fragmentPanel.showWidget(navElement.getIndex());
    }

    @Override
    public void showField(FormField field, NavigationActionListener listener) {
        NavigationAction action = (readOnly || field.isReadOnly()) ? NavigationAction.VIEW : NavigationAction.EDIT;
        constructNavigationElement(field, action, listener);
    }

    @Override
    public void addNewField(FormField field, NavigationActionListener listener) {
        constructNavigationElement(field, NavigationAction.ADD, listener);
    }
    
    private void constructNavigationElement(FormField field, NavigationAction action, final NavigationActionListener listener) {
        final NavigationElement navElement = new NavigationElement(style, this, navElements.size(), 
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
        breadcrumbs.add(navElement.getLink());
        fragmentPanel.add(navElement.getWidget());
        fragmentPanel.setAnimationCallback(new AnimationCallback() {
            @Override
            public void onLayout(Layer layer, double progress) {}
            
            @Override
            public void onAnimationComplete() {
                navElement.onShown();
                fragmentPanel.setAnimationCallback(null);
            }
        });
        fragmentPanel.showWidget(navElement.getIndex());
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
