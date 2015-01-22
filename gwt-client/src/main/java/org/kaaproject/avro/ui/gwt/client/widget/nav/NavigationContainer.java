package org.kaaproject.avro.ui.gwt.client.widget.nav;

import org.kaaproject.avro.ui.shared.FormField;

public interface NavigationContainer {
    
    void goBack();
    
    void gotoIndex(int index);

    void showField(FormField field, NavigationActionListener listener);

    void addNewField(FormField field, NavigationActionListener listener);
    
}
