package com.example.coursetracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {
    // Declare the EditTexts
    EditText title_value;
    EditText percent_value;
    EditText description_value;
    EditText prerequisites_value;

    // Declare Buttons
    Button add_button;
    Button remove_button;

    // Get the singleton User class user instance
    User user = User.getInstance();

    // Initialize Strings
    String id;
    String title;
    String percent;
    String username = user.getUsername();
    String description;
    String prerequisites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        DBHelper myDB = new DBHelper(EditActivity.this);

        add_button = findViewById(R.id.add_button);
        remove_button = findViewById(R.id.remove_button);
        percent_value = findViewById(R.id.percent_input);
        title_value = findViewById(R.id.course_input);
        description_value = findViewById(R.id.description_input);
        prerequisites_value = findViewById(R.id.prerequisites_input);

        // Retrieve data from the intent and set the UI texts
        if (getIntent().hasExtra("id") && getIntent().hasExtra("course") &&
                getIntent().hasExtra("description") && getIntent().hasExtra("percent")) {

            id = getIntent().getStringExtra("id");
            title = getIntent().getStringExtra("course");
            description = getIntent().getStringExtra("description");
            percent = getIntent().getStringExtra("percent");
        } else {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }

        // send intent data to the UI
        prerequisites = myDB.getPrerequisites(title);
        myDB.close();
        title_value.setText(title);
        percent_value.setText(percent);
        description_value.setText(description);
        prerequisites_value.setText(prerequisites);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        remove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCourse(title);
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDB.addCourse(title, description, username);
                int totalCompleted = myDB.getTotalCompleted(username);
                myDB.close();
                // start the main activity
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    void removeCourse(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // dialog 1
        builder.setTitle("Remove " + title + " ?");
        // dialog 2
        builder.setMessage("Do you want to remove " + title + " ?");
        User user = User.getInstance();
        String username = user.getUsername();
        // if yes dialog
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialogInterface, int i) {
                DBHelper myDB = new DBHelper(EditActivity.this);
                // remove the course
                myDB.removeCourse(username, title);
                myDB.close();
                // start the main activity
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);

                finish();

            }
        });
        // if no dialog
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}});  // cancel if no

        builder.create().show();
    }

    // Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remove_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.remove_all) {
            confirmRemoveDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Confirm remove dialog
    private void confirmRemoveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // dialog 1
        builder.setTitle("Remove All?");
        // dialog 2
        builder.setMessage("Are you sure you want to remove all courses?");
        // if yes dialog
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            // remove the courses from the database
            DBHelper myDB = new DBHelper(EditActivity.this);
            myDB.removeAllCourses(username);
            myDB.close();
            // start the main activity
            Intent intent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(intent);

            finish();
        });
        // if no dialog
        builder.setNegativeButton("No", (dialogInterface, i) -> {}); // cancel if no
        builder.create().show();
    }

}