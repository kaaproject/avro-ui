package org.kaaproject.avro.ui.gwt.client.widget.nav;

import org.kaaproject.avro.ui.shared.FormField;

public interface NavigationActionListener {

    void onAdded(FormField field);
    
    void onChanged(FormField field);
    
}
