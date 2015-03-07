package com.couchbasehelloworld;

import com.couchbase.lite.*;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Source: 
 * http://developer.couchbase.com/mobile/get-started/get-started-mobile/android/get-started-studio/index.html#troubleshooting
 * http://developer.couchbase.com/mobile/develop/training/build-first-android-app/create-manager-db/index.html
 * http://developer.couchbase.com/mobile/develop/training/build-first-android-app/do-crud/index.html
 */
public class CouchbaseMainActivity extends ActionBarActivity {

    // Get class name
    public static String TAG = CouchbaseMainActivity.class.getName();

    // Couchbase Manager Instance. The manager is used in the app to access the database.
    private Manager manager;

    /**
     *  create a name for the database and make sure the name is legal
     *  Rules:
     *  - The database name must begin with a lowercase letter.
     *  - The database name must contain only valid characters. The following characters are valid in database names:
     *      - Lowercase letters: a-z
     *      - Numbers: 0-9
     *      - Special characters: _$()+-/
     *      
     *  Note: The file for the database has a .cblite extension.
     */
    private String dbname = "hello";

    // Create a new couchbase database
    private Database database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couchbase_main);
        
        // Create a manager
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            Log.d(TAG, "Manager created");
        } catch (IOException e) {
            Log.e(TAG, "Cannot create manager object");
            return;
        }

        // If you database is invalid we exit from application.
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e(TAG, "Bad database name");
            return;
        } 

        try {
            database = manager.getDatabase(dbname);
            Log.d(TAG, "Database created");
            
            // Create a new database record
            String documentID = createDocument();
            
            // Retrieve Document
            retrieveDocument(documentID);
            
            // Update a document
            updateDocument(documentID);
            
            // Delete document
            deleteDocument(documentID);

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get database");
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_couchbase_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This section of the HelloWord tutorial code generates some data for a new document,
     * and then creates the new document and writes it to the database. It also outputs some messages to the console.
     * @return String documentID
     */
    private String createDocument() {

        // get the current date and time
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // Create a new Gregorian Calendar
        Calendar calendar = GregorianCalendar.getInstance();

        String currentTimeString = dateFormatter.format(calendar.getTime());

        /**
         * HashMap objects provide JSON-compatible representations of data that are suitable for 
         * creating documents that you can store in the database. The content for the document created by this snippet
         * is created in a HashMap object named docContent that contains these keys: message and currentTimeString.
         * Each key has a value associated with it.
         * The value for the message key contains the string “Hello Couchbase Lite!”, 
         * and the value for the currentTimeString key contains the time and date the document was created.
         * The document content is written out to the console to log the generated keys and values.
         */
        Map<String, Object> documentContent = new HashMap<String, Object>();
        documentContent.put("message", "Hello Couchbase Lite");
        documentContent.put("creationDate", currentTimeString);

        // display the data for the new document
        Log.d(TAG, "docContent=" + String.valueOf(documentContent));

        // Create an empty document
        Document document = database.createDocument();

        // add content to document and write the document to the database
        try {
            // Put document context in our database document
            document.putProperties(documentContent);
            
            Log.d (TAG, "Document written to database named " + database.getName() + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        /**
         * When a document is saved to the database, Couchbase Lite generates a document identifier 
         * property named _id and a revision identifier property named _rev, which are added to the stored document.
         */
        String documentID = document.getId();
        
        return documentID;
    }

    /**
     * The retrieved document includes the _id and _rev properties created by Couchbase Lite, 
     * in addition to the keys and values written by the HelloWorld code.
     * @param documentID
     * @return
     */
    private Document retrieveDocument(String documentID) {
        // retrieve the document from the database
        Document retrievedDocument = database.getDocument(documentID);

        // display the retrieved document
        Log.d(TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
        
        return retrievedDocument;
    }

    /**
     * When a document is updated, Couchbase Lite creates a new revision of the document that 
     * contains a new revision identifier in the _rev property. The document identifier in _id always remains the same.
     * @param documentID
     */
    private void updateDocument(String documentID) {
        // retrieve the document from the database
        Document retrievedDocument = retrieveDocument(documentID);

        /**
         * The code first makes a new HashMap object and copies the existing properties of retrievedDocument into it. 
         * Working on a copy of the document properties is helpful, 
         * because to update a document successfully you need to include the current revision identifier. 
         * Then the code modifies the value of the message key and adds a new key, temperature.
         */
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        updatedProperties.putAll(retrievedDocument.getProperties());
        updatedProperties.put ("message", "We're having a heat wave!");
        updatedProperties.put ("temperature", "95");

        try {
            // Put document context in our database document
            retrievedDocument.putProperties(updatedProperties);
            Log.d(TAG, "updated retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
        } catch (CouchbaseLiteException e) {
            Log.e (TAG, "Cannot update document", e);
        }
    }

    /**
     * The document is deleted by calling the delete() method on retrievedDocument.
     * @param documentID
     */
    private void deleteDocument(String documentID) {
        // retrieve the document from the database
        Document retrievedDocument = retrieveDocument(documentID);

        // delete the document
        try {
            retrievedDocument.delete();
            
            // Then to verify the deletion, it logs the value returned by the isDeleted() method.
            Log.d (TAG, "Deleted document, deletion status = " + retrievedDocument.isDeleted());
        } catch (CouchbaseLiteException e) {
            Log.e (TAG, "Cannot delete document", e);
        }
    }
}
