package org.kaaproject.avro.ui.gwt.client.i18n;

/**
 * Interface to represent the messages contained in resource bundle:
 * 	D:/git/avro-ui/gwt-client/src/main/java/org/kaaproject/avro/ui/gwt/client/i18n/AvroUiMessages.properties'.
 */
public interface AvroUiMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "Add new {0}".
   * 
   * @return translated "Add new {0}"
   */
  @DefaultMessage("Add new {0}")
  @Key("addNewEntry")
  String addNewEntry(String arg0);

  /**
   * Translated "{0} of {1} max characters".
   * 
   * @return translated "{0} of {1} max characters"
   */
  @DefaultMessage("{0} of {1} max characters")
  @Key("charactersLength")
  String charactersLength(String arg0,  String arg1);

  /**
   * Translated "Are you sure you want to delete nested {0} which is value of field ''{1}''?".
   * 
   * @return translated "Are you sure you want to delete nested {0} which is value of field ''{1}''?"
   */
  @DefaultMessage("Are you sure you want to delete nested {0} which is value of field ''{1}''?")
  @Key("deleteNestedEntryQuestion")
  String deleteNestedEntryQuestion(String arg0,  String arg1);

  /**
   * Translated "Delete nested entry".
   * 
   * @return translated "Delete nested entry"
   */
  @DefaultMessage("Delete nested entry")
  @Key("deleteNestedEntryTitle")
  String deleteNestedEntryTitle();

  /**
   * Translated "Are you sure you want to delete selected entry?".
   * 
   * @return translated "Are you sure you want to delete selected entry?"
   */
  @DefaultMessage("Are you sure you want to delete selected entry?")
  @Key("deleteSelectedEntryQuestion")
  String deleteSelectedEntryQuestion();

  /**
   * Translated "Delete entry".
   * 
   * @return translated "Delete entry"
   */
  @DefaultMessage("Delete entry")
  @Key("deleteSelectedEntryTitle")
  String deleteSelectedEntryTitle();

  /**
   * Translated "You have unsaved changes for ''{0}''. If you navigate away from this form without first saving, all changes will be lost.".
   * 
   * @return translated "You have unsaved changes for ''{0}''. If you navigate away from this form without first saving, all changes will be lost."
   */
  @DefaultMessage("You have unsaved changes for ''{0}''. If you navigate away from this form without first saving, all changes will be lost.")
  @Key("detailsMayCloseMessage")
  String detailsMayCloseMessage(String arg0);

  /**
   * Translated "Nested {0}".
   * 
   * @return translated "Nested {0}"
   */
  @DefaultMessage("Nested {0}")
  @Key("nestedEntry")
  String nestedEntry(String arg0);

  /**
   * Translated "Page {0} of {1}".
   * 
   * @return translated "Page {0} of {1}"
   */
  @DefaultMessage("Page {0} of {1}")
  @Key("pagerText")
  String pagerText(String arg0,  String arg1);
}
