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

import static org.kaaproject.avro.ui.gwt.client.util.Utils.isBlank;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.input.HasInputEventHandlers;
import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.util.Utils;
import org.kaaproject.avro.ui.shared.StringField.InputType;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SizedTextBox extends VerticalPanel implements HasValue<String>, HasInputEventHandlers {

    private TextBox text;
    private HTML textLabel;
    private Label charactersLabel;
    private final int maxChars;
    private final boolean editable;

    private final AvroUiStyle style;

    private static AvroUiStyle getDefaultStyle() {
        return Utils.avroUiStyle;
    }
    
    public SizedTextBox(int maxChars, InputType inputType, String prompt) {
        this(getDefaultStyle(), inputType, prompt, maxChars);
    }

    public SizedTextBox(int maxChars, InputType inputType, String prompt, boolean editable) {
        this(getDefaultStyle(), inputType, prompt, maxChars, editable, true);
    }
    
    public SizedTextBox(int maxChars, InputType inputType, String prompt, boolean editable, boolean addNotes) {
        this(getDefaultStyle(), inputType, prompt, maxChars, editable, addNotes);
    }

    public SizedTextBox(AvroUiStyle style, InputType inputType, String prompt, int maxChars) {
        this(style, inputType, prompt, maxChars, true, true);
    }
    
    public SizedTextBox(AvroUiStyle style, InputType inputType, String prompt, int maxChars, boolean editable) {
        this(style, inputType, prompt, maxChars, editable, true);
    }

    public SizedTextBox(AvroUiStyle style, InputType inputType, String prompt, int maxChars, boolean editable, boolean addNotes) {
        
        // Inject the stylesheet.
        this.style = style;
        this.style.ensureInjected();
        
        this.maxChars = maxChars;
        this.editable = editable;
        if (editable) {
            if (inputType == InputType.PASSWORD) {
                text = new ExtendedPasswordTextBox();
            } else {
                text = new ExtendedTextBox(prompt);
            }
            add(text);
        }
        else {
            textLabel = new HTML("&nbsp;");
            textLabel.setHeight("100%");
            textLabel.setStyleName(this.style.secondaryLabel());
            add(textLabel);
        }
        if (editable && maxChars > -1) {
            charactersLabel = new Label();
            charactersLabel.setStyleName(this.style.fieldNotes());
            text.setMaxLength(maxChars);
            add(charactersLabel);
            updateCharactersLabel();
        }
        else if (addNotes) {
            add(new HTML("&nbsp;"));
        }
        addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                fireInputEvent();
            }
        });
        if (editable) {
            text.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent  event) {
                    updateCharactersLabel();
                    fireInputEvent();
                }
            });
        }
    }

    private void updateCharactersLabel() {
        if (editable && maxChars > -1) {
            int currentLength = text.getText().length();
            charactersLabel.setText(Utils.messages.charactersLength(String.valueOf(currentLength), String.valueOf(maxChars)));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {
        if (editable) {
            return text.addValueChangeHandler(handler);
        }
        else {
            return null;
        }
    }

    @Override
    public String getValue() {
        if (editable) {
            return text.getValue();
        }
        else {
            if (textLabel.getHTML().equals("&nbsp;")) {
                return "";
            }
            else {
                return textLabel.getText();
            }
        }
    }

    @Override
    public void setValue(String value) {
        if (editable) {
            text.setValue(value);
        }
        else {
            if (isBlank(value)) {
                textLabel.setHTML("&nbsp;");
            }
            else {
                textLabel.setText(value);
            }
        }
        updateCharactersLabel();
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        if (editable) {
            text.setValue(value, fireEvents);
        }
        else {
            textLabel.setText(value);
        }
    }

    public void setFocus(boolean focused) {
        if (editable) {
            text.setFocus(focused);
        }
    }

    public void setWidth(String width) {
        super.setWidth(width);
        if (editable) {
            text.setWidth(width);
        }
        else {
            textLabel.setWidth(width);
        }
    }

    public void setHeight(String height) {
        super.setHeight(height);
        if (editable) {
            text.setHeight(height);
        }
        else {
            textLabel.setHeight(height);
        }
    }

    public void setEnabled(boolean enabled) {
        if (editable) {
            text.setEnabled(enabled);
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (editable) {
            text.setReadOnly(readOnly);
        }
    }
    
    public void setInvalid(boolean invalid) {
        if (editable) {
            if (invalid) {
                text.addStyleName(style.invalidField());
            } else {
                text.removeStyleName(style.invalidField());
            }
        }
    }

    private class ExtendedTextBox extends TextBox implements BlurHandler,
                                                        FocusHandler, ClickHandler, KeyPressHandler {

        private String promptText;
        
        public ExtendedTextBox(String promptText) {
            super();
            this.promptText = promptText;
            sinkEvents(Event.ONPASTE);
            if (Utils.isNotBlank(promptText)) {
                this.addKeyPressHandler(this);
                this.addFocusHandler(this);
                this.addClickHandler(this);
                this.addBlurHandler(this);
                setPrompts();
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
                            ValueChangeEvent.fire(ExtendedTextBox.this, getText());
                        }
                    });
                    break;
            }
        }
        
        @Override
        public void onBlur(BlurEvent event) {
            if (Utils.isBlank(super.getText())) {
                setPrompts();
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
        public void onKeyPress(KeyPressEvent event) {
            if (promptText.equals(super.getText())
                    && !(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB)) {
                removePrompts();
            }
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
    }
    
    private class ExtendedPasswordTextBox extends PasswordTextBox {

        public ExtendedPasswordTextBox() {
            super();
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
                case Event.ONPASTE:
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            ValueChangeEvent.fire(ExtendedPasswordTextBox.this, getText());
                        }
                    });
                    break;
            }
        }
    }

    private void fireInputEvent() {
        InputEvent event = new InputEvent(this);
        fireEvent(event);
    }

    @Override
    public HandlerRegistration addInputHandler(InputEventHandler handler) {
        return this.addHandler(handler, InputEvent.TYPE);
    }
}
