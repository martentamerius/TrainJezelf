package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.ronald.trainjezelf.R;
import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

public class ReminderEditActivity extends Activity {

    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        // Fill activity with data to be edited
        Intent intent = getIntent();
        int messageId = intent.getExtras().getInt("messageId");
        //reminder = DataStore.getReminder(messageId);

        // Populate form
        EditText messageEditText = (EditText) findViewById(R.id.messageEditText);
        messageEditText.setText("Message");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            // TODO
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
