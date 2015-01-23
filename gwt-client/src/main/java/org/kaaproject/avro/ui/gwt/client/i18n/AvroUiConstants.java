package org.kaaproject.avro.ui.gwt.client.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface AvroUiConstants extends ConstantsWithLookup {

    @DefaultStringValue("add items")
    String appendStrategy();

    @DefaultStringValue("replace items")
    String replaceStrategy();
    
    @DefaultStringValue("There is no data to display")
    String dataGridEmpty();

    @DefaultStringValue("Delete")
    String delete();

    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();
    
    @DefaultStringValue("Add")
    String add();
 
    @DefaultStringValue("Remove")
    String remove();

    @DefaultStringValue("Back")
    String back();
    
    @DefaultStringValue("Create")
    String create();
    
    @DefaultStringValue("Open")
    String open();
    
}
