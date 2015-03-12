package org.kaaproject.avro.ui.sandbox.shared.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AvroUiSandboxServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void generateFormFromSchema( java.lang.String avroSchema, AsyncCallback<org.kaaproject.avro.ui.shared.RecordField> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void getJsonStringFromRecord( org.kaaproject.avro.ui.shared.RecordField field, AsyncCallback<java.lang.String> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void generateFormDataFromJson( java.lang.String avroSchema, java.lang.String json, AsyncCallback<org.kaaproject.avro.ui.shared.RecordField> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void getEmptySchemaForm( AsyncCallback<org.kaaproject.avro.ui.shared.RecordField> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void generateSchemaFormFromSchema( java.lang.String avroSchema, AsyncCallback<org.kaaproject.avro.ui.shared.RecordField> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.kaaproject.avro.ui.sandbox.shared.services.AvroUiSandboxService
     */
    void getJsonStringFromSchemaForm( org.kaaproject.avro.ui.shared.RecordField field, AsyncCallback<java.lang.String> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static AvroUiSandboxServiceAsync instance;

        public static final AvroUiSandboxServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (AvroUiSandboxServiceAsync) GWT.create( AvroUiSandboxService.class );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
