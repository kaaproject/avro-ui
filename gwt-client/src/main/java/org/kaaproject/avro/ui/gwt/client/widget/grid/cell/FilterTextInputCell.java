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

package org.kaaproject.avro.ui.gwt.client.widget.grid.cell;

import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FilterTextInputCell extends
    AbstractInputCell<String, FilterTextInputCell.ViewData> {
    
  public static final String PASTE = "paste";

  interface Template extends SafeHtmlTemplates {
    @Template("<span class=\"{0}\"><input type=\"text\" value=\"{1}\" style=\"{2}\" tabindex=\"-1\"></input></span>")
    SafeHtml input(String filterInputClass, String value, SafeStyles style);

    @Template("<span class=\"{0}\"><input type=\"text\" style=\"{1}\" tabindex=\"-1\"></input></span>")
    SafeHtml emptyInput(String filterInputClass, SafeStyles style);
  }
  
  public static class ViewData {

    private String lastValue;

    private String curValue;

    public ViewData(String value) {
      this.lastValue = value;
      this.curValue = value;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ViewData)) {
        return false;
      }
      ViewData vd = (ViewData) other;
      return equalsOrNull(lastValue, vd.lastValue)
          && equalsOrNull(curValue, vd.curValue);
    }

    public String getCurrentValue() {
      return curValue;
    }

    public String getLastValue() {
      return lastValue;
    }

    @Override
    public int hashCode() {
      return (lastValue + "_*!@HASH_SEPARATOR@!*_" + curValue).hashCode();
    }

    protected void setCurrentValue(String curValue) {
      this.curValue = curValue;
    }

    protected void setLastValue(String lastValue) {
      this.lastValue = lastValue;
    }

    private boolean equalsOrNull(Object a, Object b) {
      return (a != null) ? a.equals(b) : ((b == null) ? true : false);
    }
  }

  private static Template template;
  
  private SafeStyles inputStyle = SafeStylesUtils.fromTrustedString("");
  
  public FilterTextInputCell() {
      this(0, null);
  }

  public FilterTextInputCell(double width, Unit widthUnit) {
    super(BrowserEvents.BLUR, BrowserEvents.CHANGE, BrowserEvents.KEYUP, PASTE);
    if (template == null) {
      template = GWT.create(Template.class);
    }
    if (width > 0) {
        inputStyle = SafeStylesUtils.forWidth(width, widthUnit);
    }
  }
  
  @Override
  public void onBrowserEvent(Context context, Element parent, String value,
      NativeEvent event, ValueUpdater<String> valueUpdater) {
      
    super.onBrowserEvent(context, parent, value, event, valueUpdater);

    // Ignore events that don't target the input.
    InputElement input = getInputElement(parent);
    Element target = event.getEventTarget().cast();
    if (!input.isOrHasChild(target)) {
      return;
    }

    String eventType = event.getType();
    Object key = context.getKey();
    if (BrowserEvents.BLUR.equals(eventType)) {
      finishEditing(parent, value, key, valueUpdater);
    } else if (BrowserEvents.CHANGE.equals(eventType) || 
            BrowserEvents.KEYUP.equals(eventType) ||
            PASTE.equals(eventType)) {
      ViewData vd = getViewData(key);
      if (vd == null) {
        vd = new ViewData(value);
        setViewData(key, vd);
      }
      
      String newValue = input.getValue();
      
      vd.setCurrentValue(input.getValue());
      
      if (valueUpdater != null && !vd.getCurrentValue().equals(vd.getLastValue())) {
          vd.setLastValue(newValue);
          valueUpdater.update(newValue);
      }
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    Object key = context.getKey();
    ViewData viewData = getViewData(key);
    if (viewData != null && viewData.getCurrentValue().equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    String s = (viewData != null) ? viewData.getCurrentValue() : value;
    if (s != null) {
      sb.append(template.input(Utils.avroUiStyle.filterInput(), s, inputStyle));
    } else {
      sb.append(template.emptyInput(Utils.avroUiStyle.filterInput(), inputStyle));
    }
  }

  @Override
  protected void finishEditing(Element parent, String value, Object key,
      ValueUpdater<String> valueUpdater) {
    String newValue = getInputElement(parent).getValue();

    ViewData vd = getViewData(key);
    if (vd == null) {
      vd = new ViewData(value);
      setViewData(key, vd);
    }
    vd.setCurrentValue(newValue);

    if (valueUpdater != null && !vd.getCurrentValue().equals(vd.getLastValue())) {
      vd.setLastValue(newValue);
      valueUpdater.update(newValue);
    }

    super.finishEditing(parent, newValue, key, valueUpdater);
  }

  @Override
  protected InputElement getInputElement(Element parent) {
    return parent.getFirstChildElement().<SpanElement> cast().
            getFirstChildElement().<InputElement> cast();
  }
}
