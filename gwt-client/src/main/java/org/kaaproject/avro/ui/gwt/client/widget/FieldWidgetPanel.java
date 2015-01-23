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

import org.kaaproject.avro.ui.gwt.client.AvroUiResources;
import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.FormField.ChangeListener;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class FieldWidgetPanel extends Composite implements HasValue<Boolean>, ClickHandler, ChangeListener {

    interface FieldWidgetPanelUiBinder extends UiBinder<Widget, FieldWidgetPanel> { }
    private static FieldWidgetPanelUiBinder uiBinder = GWT.create(FieldWidgetPanelUiBinder.class);
    
    private static final int OPEN_ANIMATION_DURATION = 350;
    
    private static ContentAnimation contentAnimation;
    
    @UiField public FlowPanel fieldSet;
    @UiField public Label legendLabel;
    @UiField public Label legendNotesLabel;
    @UiField public Image arrowImage;
    @UiField public CheckBox legendBox;
    @UiField public SimplePanel legendPanel;
    @UiField public SimplePanel contentPanel;
    
    @UiField (provided=true) public final AvroUiResources avroUiResources;
    @UiField (provided=true) public final AvroUiStyle avroUiStyle;
    
    /** Signals whether the fieldset is opened. */
    private boolean isOpen;
    
    private FormField field;
    
    public FieldWidgetPanel(AvroUiStyle style, FormField field, boolean readOnly, boolean openByDefault) {        
        
        this.field = field;
        
        avroUiResources = Utils.resources;
        avroUiStyle = style;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        contentPanel.getElement().getStyle().setProperty("padding", "0px");
        contentPanel.getElement().getStyle().setProperty("overflow", "hidden");

        legendLabel.setText(field.getDisplayName());
        if (!field.isOptional()) {
            legendLabel.addStyleName(avroUiStyle.requiredField());
        }
        
        if (field.isOverride()) {
            arrowImage.setVisible(false);
            legendBox.setVisible(true);
            legendBox.setEnabled(!readOnly && !field.isReadOnly());
            if (!readOnly && !field.isReadOnly()) {
                legendBox.addClickHandler(this);
                field.addChangeListener(this);
            } 
            setOpen(field.isChanged(), false);
        } else {
            legendBox.setVisible(false);
            arrowImage.setVisible(true);
            arrowImage.addClickHandler(this);
            setOpen(openByDefault, false);
        }
        setContentDisplay(false);
    }
    
    public void setContent(Widget widget) {
        contentPanel.setWidget(widget);
        setContentDisplay(false);
    }
    
    public Widget getContent() {
        return contentPanel.getWidget();
    }
    
    public void setLegendWidget(Widget widget) {
        legendPanel.setVisible(true);
        legendPanel.setWidget(widget);
    }
    
    public void setLegendNotes(String notes) {
        legendNotesLabel.setVisible(true);
        legendNotesLabel.setText(notes);
    }

    @Override
    public void onChanged(boolean changed) {
        setValue(changed, false, true);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Boolean getValue() {
        return isOpen;
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        setValue(value, fireEvents, false);
    }    
    
    public void setValue(final Boolean value, final boolean fireEvents, boolean animate) {
        if (value == this.isOpen) {
            return;
        }
        Boolean before = this.isOpen;        
        field.setChanged(value, fireEvents);
        setOpen(value, animate);
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void setOpen(boolean isOpen, boolean animate) {
        if (this.isOpen != isOpen) {
            this.isOpen = isOpen;
            if (field.isOverride()) {
                legendBox.setValue(isOpen);
            }
            setContentDisplay(animate);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        setValue(!isOpen, true, true);
    }
    
    private void setContentDisplay(boolean animate) {
        if (contentAnimation == null) {
            contentAnimation = new ContentAnimation();
        }
        contentAnimation.setOpen(this, animate);
    }
    
    private void updateStyles() {
        if (isOpen) {
            fieldSet.removeStyleName(avroUiStyle.fieldsetInvisible());
            fieldSet.addStyleName(avroUiStyle.fieldsetVisible());
            arrowImage.setResource(avroUiResources.arrowBottomImage());
        } else {
            fieldSet.removeStyleName(avroUiStyle.fieldsetVisible());
            fieldSet.addStyleName(avroUiStyle.fieldsetInvisible());
            arrowImage.setResource(avroUiResources.arrowRightImage());
        }
    }
    
    /**
     * An {@link Animation} used to open the content.
     */
    private static class ContentAnimation extends Animation {
      /**
       * Whether the item is being opened or closed.
       */
      private boolean opening;

      /**
       * The {@link DisclosurePanel} being affected.
       */
      private FieldWidgetPanel curPanel;

      /**
       * Open or close the content.
       * 
       * @param panel the panel to open or close
       * @param animate true to animate, false to open instantly
       */
      public void setOpen(FieldWidgetPanel panel, boolean animate) {
        // Immediately complete previous open
        cancel();

        // Open the new item
        if (animate) {
          curPanel = panel;
          opening = panel.isOpen;
          run(OPEN_ANIMATION_DURATION);
        } else {
          panel.updateStyles();
          panel.contentPanel.setVisible(panel.isOpen);
          if (panel.isOpen && panel.getContent() != null) {
              panel.getContent().setVisible(true);
          }
        }
      }

      @Override
      protected void onComplete() {
        if (!opening) {
          curPanel.contentPanel.setVisible(false);
          curPanel.updateStyles();  
        }
        curPanel.contentPanel.getElement().getStyle().setProperty("height", "auto");
        curPanel = null;
      }

      @Override
      protected void onStart() {
        super.onStart();
        if (opening) {
          curPanel.updateStyles();  
          curPanel.contentPanel.setVisible(true);
          if (curPanel.getContent() != null) {
              curPanel.getContent().setVisible(true);
          }
        }
      }

      @Override
      protected void onUpdate(double progress) {
        int scrollHeight = curPanel.contentPanel.getElement().getPropertyInt("scrollHeight");
        int height = (int) (progress * scrollHeight);
        if (!opening) {
          height = scrollHeight - height;
        }
        height = Math.max(height, 1);
        curPanel.contentPanel.getElement().getStyle().setProperty("height", height + "px");
        //curPanel.contentPanel.getElement().getStyle().setProperty("width", "auto");
      }
    }

}
