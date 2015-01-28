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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.util.Utils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBox;

public abstract class NumberBox<T extends Number> extends ValueBox<T> implements BlurHandler,
                                    FocusHandler, ClickHandler, KeyDownHandler {

    private AvroUiStyle style;
    private String promptText;
    private boolean isDecimal = false;
    private Renderer<T> renderer;
    
    protected NumberBox(AvroUiStyle style, Element element, String promptText,
            final Renderer<T> renderer, final Parser<T> parser) {
        super(element, renderer, parser);
        sinkEvents(Event.ONPASTE);
        this.style = style;
        this.promptText = promptText;
        this.renderer = renderer;
        
        if (Utils.isNotBlank(promptText)) {
            this.addFocusHandler(this);
            this.addClickHandler(this);
            setPrompts();
        }
        
        this.addKeyDownHandler(this);
        this.addBlurHandler(this);
    }
    
    @Override
    public void onBlur(BlurEvent event) {
        if (Utils.isNotBlank(promptText) && Utils.isBlank(super.getText())) {
            setPrompts();
        } else {
            setText(renderer.render(getValue()));
        }
    }
    
    @Override
    public void onFocus(FocusEvent event) {
        this.setSelectionRange(0, 0);
    }
    
    @Override
    public void onClick(ClickEvent event) {
        if (promptText.equals(super.getText())) {
            removePrompts();
        }
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (DOM.eventGetType(event)) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (Utils.isNotBlank(promptText) && Utils.isBlank(NumberBox.super.getText())) {
                            setPrompts();
                        } else {
                            setText(renderer.render(getValue()));
                        }                    }
                });
                break;
        }
    }
    
    @Override
    public void onKeyDown(KeyDownEvent event) {
        
        if ( !isEnabled( ) || isReadOnly( ) )
            return;

        if (Utils.isNotBlank(promptText) && promptText.equals(super.getText())
                && !(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB)) {
            removePrompts();
        }
        
        int keyCode = event.getNativeEvent().getKeyCode();

        // allow special keys
        switch(keyCode) {
        case KeyCodes.KEY_TAB: 
        case KeyCodes.KEY_BACKSPACE: 
        case KeyCodes.KEY_DELETE: 
        case KeyCodes.KEY_LEFT: 
        case KeyCodes.KEY_RIGHT: 
        case KeyCodes.KEY_UP: 
        case KeyCodes.KEY_DOWN: 
        case KeyCodes.KEY_END: 
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_ESCAPE:
        case KeyCodes.KEY_PAGEDOWN:
        case KeyCodes.KEY_PAGEUP:
        case KeyCodes.KEY_HOME:
        case KeyCodes.KEY_SHIFT:
        case KeyCodes.KEY_ALT:
        case KeyCodes.KEY_CTRL:
            return;
        default:
            
            if (event.isAltKeyDown() || (event.isControlKeyDown() && 
                    (keyCode == KeyCodes.KEY_C || keyCode == KeyCodes.KEY_V 
                    || keyCode == KeyCodes.KEY_X )))
                return;
            
            if (!event.isShiftKeyDown()) {
                // check for decimal '.'
                if (isDecimal() && isDot(keyCode) && !getText().contains("."))
                    return;
    
                // check for negative sign '-'
                if (getCursorPos() == 0 && isDash(keyCode) && !getText().startsWith("-"))
                    return;
                
                // filter out non-digits
                if (isDigit(keyCode)) {
                    return;
                }
            }
        }

        cancelKey();
    }
    
    private static boolean isDot(int keyCode) {
        return keyCode == 190 || keyCode == 110;
    }
    
    private static boolean isDash(int keyCode) {        
        return (keyCode == (isFirefox() ? 173 : 189)) || keyCode == 109;
    }
    
    private static boolean isDigit(int keyCode) {
        return keyCode >= 48 && keyCode <= 57 || keyCode >= 96 && keyCode <= 105;
    }
    
    private static native boolean isFirefox() /*-{
        return navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
    }-*/;
    
    public boolean isDecimal() {
        return isDecimal;
    }

    public void setDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
    
    @Override
    public String getText() {
        String text = super.getText();
        if (Utils.isNotBlank(promptText) && promptText.equals(text)) {
            return "";
        } else {
            return text;
        }
    }

    @Override
    public void setText(String text) {
        if (Utils.isNotBlank(promptText)) {
            if (Utils.isBlank(text)) {
                setPrompts();
            } else {
                removePrompts();
                super.setText(text);
            }
        } else {
            super.setText(text);
        }
    }
    
    private void setPrompts() {
        this.addStyleName(style.prompt());
        super.setText(promptText);
    }

    private void removePrompts() {
        this.removeStyleName(style.prompt());
        super.setText("");
    }

    static class NumberRenderer<N extends Number> extends AbstractRenderer<N> {

        private NumberFormat numberFormat;
        
        private boolean hasFraction;

        public NumberRenderer(String numberFormatPattern, boolean hasFraction) {
            numberFormat = NumberFormat.getFormat(numberFormatPattern);
            this.hasFraction = hasFraction;
            if (!hasFraction) {
                numberFormat.overrideFractionDigits(0);
            } 
        }

        public String render(N object) {
            if (null == object) {
                return "";
            }
            try {
                if (hasFraction) {
                    int precision = 0;
                    String strNumber = object.toString();
                    precision = strNumber.length();
                    if (strNumber.contains(".")) {
                        precision--;
                    }
                    return numberFormat.format(new BigDecimal(object.doubleValue(), new MathContext(precision, RoundingMode.HALF_EVEN)));
                } else {
                    return numberFormat.format(object);
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }
    }    
    
    static abstract class NumberParser<N extends Number> implements Parser<N> {
        
        private NumberFormat numberFormat;
        
        public NumberParser(String numberFormatPattern, boolean hasFraction) {
            numberFormat = NumberFormat.getFormat(numberFormatPattern);
            if (!hasFraction) {
                numberFormat.overrideFractionDigits(0);
            }
        }

        public N parse(CharSequence object) throws ParseException {
          if ("".equals(object.toString())) {
            return null;
          }

          try {              
            return toNumber(numberFormat.parse(object.toString()));
          } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage(), 0);
          }
        }
        
        protected abstract N toNumber(double val);
     }
    
    static class IntegerParser extends NumberParser<Integer> {

        public IntegerParser(String numberFormatPattern) {
            super(numberFormatPattern, false);
        }

        @Override
        protected Integer toNumber(double val) {
            return (int) Math.rint(val);
        }
        
    }
    
    static class LongParser extends NumberParser<Long> {

        public LongParser(String numberFormatPattern) {
            super(numberFormatPattern, false);
        }

        @Override
        protected Long toNumber(double val) {
            return (long) val;
        }
        
    }
    
    static class FloatParser extends NumberParser<Float> {

        public FloatParser(String numberFormatPattern) {
            super(numberFormatPattern, true);
        }

        @Override
        protected Float toNumber(double val) {
            return (float) val;
        }
        
    }
    
    static class DoubleParser extends NumberParser<Double> {

        public DoubleParser(String numberFormatPattern) {
            super(numberFormatPattern, true);
        }

        @Override
        protected Double toNumber(double val) {
            return val;
        }
        
    }
}
