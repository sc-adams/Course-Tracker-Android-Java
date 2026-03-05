package com.example.coursetracker;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "courseTracker.db";
    private static final int DATABASE_VERSION = 5;  // Note: upgrade the DB with new user
    public static final String COURSE_TABLE = "course_data"; // data table 1
    private static final String COMPLETED_TABLE = "completed_data"; // data table 2
    private static final String USER_TABLE = "user_data"; // data table 3
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "course_title";
    public static final String COLUMN_DESCRIPTION = "course_description";
    public static final String COLUMN_PERCENT = "column_percent";
    public static final String COLUMN_COMPLETED = "column_completed";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_GOAL = "goalCourse";
    private static final String COLUMN_REQUIREMENT = "requirement";
    public static final String COLUMN_PREREQUISITES = "course_prerequisites";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_USERNAME = "user_username";
    private static final String COLUMN_PASSWORD = "user_password";

    String percent = String.valueOf(4.35);
    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String userTableQuery = "CREATE TABLE " + USER_TABLE +
                " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_PHONE + " TEXT)";
        String courseTableQuery = "CREATE TABLE " + COURSE_TABLE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PERCENT + " TEXT, " +
                COLUMN_GOAL + " TEXT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PREREQUISITES + " TEXT, " +
                COLUMN_PHONE + " TEXT)";

        String goalTableQuery = "CREATE TABLE " + COMPLETED_TABLE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PERCENT + " TEXT, " +
                COLUMN_COMPLETED + " TEXT, " +
                COLUMN_GOAL + " TEXT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PREREQUISITES + " TEXT, " +
                COLUMN_REQUIREMENT + " TEXT, " +
                COLUMN_PHONE + " TEXT)";

        db.execSQL(userTableQuery);
        db.execSQL(courseTableQuery);
        db.execSQL(goalTableQuery);
        loadCourses(db);
    }
56

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + COURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMPLETED_TABLE);
        onCreate(db);
    }

    // TODO: implement password hashing
    // method to add user and hashed password to the database
    void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the db
        ContentValues values = new ContentValues();
        if (username != null) {  // first check for null username
            values.put(COLUMN_USERNAME, username);  // add if not null
        }
        values.put(COLUMN_PASSWORD, password);  // add the password to the db
        // check for insertion
        long result = db.insert(USER_TABLE, null, values);
        if (result == -1) {  // failed to add
            Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show();
        } else {  // successfully added
            Toast.makeText(context, "User added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    // TODO: implement password hashing
    // method to check user credentials against the database
    boolean checkUser(String username, String password) {
        if (username == null || password == null) {  // first check for nulls
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase(); // initialize the db
        // define the search query
        String[] columns = {COLUMN_USER_ID, COLUMN_PASSWORD};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {  // move to the first result row
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (passwordColumnIndex != -1) {  // if password column index is found
                String storedPassword = cursor.getString(passwordColumnIndex);  // get the stored password
                cursor.close();  // close the cursor
                return password.equals(storedPassword);  // return true if the password matches
            }
        }
        cursor.close();  // close the cursor
        return false;
    }

    Cursor readAllData() {
        String query = "SELECT * FROM " + COURSE_TABLE;   // select all the data in the course table
        SQLiteDatabase db = this.getReadableDatabase();   // initialize the db
        Cursor cursor = null;
        try {
            if (db != null) { // check that the db is not null
                cursor = db.rawQuery(query, null);
            }
        } catch (Exception e) {  // catch exceptions and log the error
            e.printStackTrace();
        }
        return cursor;
    }
    void removeCourse(String username, String title) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        // check for null or empty values
        if ((title == null || title.isEmpty()) || (username == null || username.isEmpty())) {
            Toast.makeText(context, "Invalid title and or username", Toast.LENGTH_SHORT).show();
            return;
        }
        // define the search
        String whereClause = COLUMN_USERNAME + " = ? AND " + COLUMN_TITLE + " = ?";
        String[] whereArgs = {username, title};
        Cursor cursorQuery = db.query(COMPLETED_TABLE, null, whereClause, whereArgs, null, null, null);
        if (cursorQuery.getCount() > 0) {  // if the search is found
            ContentValues values = new ContentValues();
            values.put(COLUMN_COMPLETED, "-1");  // mark the row for deletion
            long result = db.delete(COMPLETED_TABLE, whereClause, whereArgs);  // delete
            if (result == -1) {  // if the delete result is failure, make a toast message
                Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
            } else {  // successfully deleted
                Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
            }
            // send the broadcast of the change and push that data to the intent
            pushBroadcast(username);
        } else {
            Toast.makeText(context, "Course not found.", Toast.LENGTH_SHORT).show();
        }
        cursorQuery.close();   // close the cursor
    }

    public void addCourse(String course, String description, String username) {
        SQLiteDatabase db = this.getWritableDatabase();   // initialize the db
        ContentValues values = new ContentValues();
        if (!username.isEmpty()) {  // only add if not empty
            values.put(COLUMN_USERNAME, username);
        }
        if (!course.isEmpty()) {   // only add if not empty
            values.put(COLUMN_TITLE, course);
        }
        if (!description.isEmpty()) {   // only add if not empty
            values.put(COLUMN_DESCRIPTION, description);
        }
        values.put(COLUMN_PERCENT, percent);  // add the calculated values
        values.put(COLUMN_COMPLETED, "1");
        // define the search
        String whereClause = COLUMN_USERNAME + " = ? AND " + COLUMN_TITLE + " = ?";
        String[] whereArgs = {username, course};
        Cursor cursor = db.query(COMPLETED_TABLE, null, whereClause, whereArgs, null, null, null);
        if (cursor.getCount() == 0) {  // if the course does not exist in the completed table
            long result = db.insert(COMPLETED_TABLE, null, values); // insert it
            if (result != -1) {   // if successfully added, make a toast message
                Toast.makeText(context, "New row inserted", Toast.LENGTH_SHORT).show();
            }
        } else {  // if duplicate, not added
            Toast.makeText(context, "Course exists already.", Toast.LENGTH_SHORT).show();
        }
        // send the broadcast of the change and push that data to the intent
        pushBroadcast(username);
        cursor.close();  // close the cursor
        if (db.isOpen()){
            db.close();
        }
    }

    public void pushBroadcast(String username){
        // send the broadcast of the change and push that data to the intent
        Intent intent = new Intent("data_updated");
        intent.putExtra("username", username);
        context.sendBroadcast(intent);
    }

    String getPrerequisites(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String prerequisites = null;
        if (db != null && title != null) {  // check for nulls first
            // search the course title provided
            String query = "SELECT " + COLUMN_PREREQUISITES + " FROM " + COURSE_TABLE +
                    " WHERE " + COLUMN_TITLE + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{title});
            if (cursor != null && cursor.moveToFirst()) {  // check for null cursor and move to the first result if it exists
                int columnIndexPrerequisites = cursor.getColumnIndex(COLUMN_PREREQUISITES);  // define the column index
                if (columnIndexPrerequisites != -1) {  // if the column index is found
                    prerequisites = cursor.getString(columnIndexPrerequisites);  // get the value at the column index
                }
                cursor.close();   // close the cursor
            }
        }
        return prerequisites;
    }


    void removeAllCourses(String username) {
        SQLiteDatabase db = this.getWritableDatabase(); // initialize the db
        if (db != null) {  // first check for null db
            db.delete(COMPLETED_TABLE, COLUMN_USERNAME + "=?", new String[]{username});
        }
    }
    public int getTotalCompleted(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalCompleted = 0;
        if (db != null && username != null) {   // check for null values before proceeding
            // get the sum of the values in the completed column for the current user from the completed table
            String query = "SELECT SUM(" + COLUMN_COMPLETED + ") FROM " + COMPLETED_TABLE +
                    " WHERE " + COLUMN_USERNAME + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{username});
            if (cursor != null) {  // if cursor is not null
                if (cursor.moveToFirst()) {  // move to the first result if it exists
                    totalCompleted = cursor.getInt(0); // get the integer value of the first result
                }
                cursor.close();   // close the cursor
            }
        }
        return totalCompleted;
    }

    public int checkForUpdates() {
        String fileName = "courses.txt"; // get the text file from the assets folder
        AssetManager assetManager = context.getAssets();
        BufferedReader bufferedReader = null;
        try {   // Open the file
            InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open(fileName));
            bufferedReader = new BufferedReader(inputStreamReader);
            long lastModified = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            if (bufferedReader != null) {
                bufferedReader.close();  // close the reader
            }
            if (currentTime - lastModified < 6000) {  // if the file was modified in the past 6 seconds
                return 1;
            } else {  // if not modified recently
                return 0;
            }
        } catch (IOException e) {  // Handle the exceptions
            Log.e("DBHelper", "Error opening or reading file: " + fileName, e);
        }
        return 0;
    }

    boolean checkIfIsInDatabase(String username, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            // check if the provided title is in the user's table
            String query = "SELECT " + COLUMN_TITLE + " FROM " + COMPLETED_TABLE +
                    " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_TITLE + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{username != null ? username : "", title != null ? title : ""});
            if (cursor != null && cursor.moveToFirst()) {  // if the cursor is not null, move to the first result if it exists
                return true; // record is in the database, return true
            }
            if (cursor != null) {  // if the cursor is not null
                cursor.close();   // close the cursor
            }
        }
        return false; // if no results, return false
    }

    // TODO: finish this method to recommend courses to the user
    public List<String> suggestCourses(String username){
        List<String> recommendedList = new ArrayList<>();
        return recommendedList;
    }
}
