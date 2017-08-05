package com.example.alikshasha.chatapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import data.User;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    public static ListView listview;
    public static ArrayList<User> users = new ArrayList<>();
    RealtimeDatabase realtime_database;
    private boolean view_message = true;
    database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listview = (ListView) findViewById(R.id.requests);
        setSupportActionBar(toolbar);
        users = new ArrayList<>();
        db = new database(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
        realtime_database = new RealtimeDatabase(getApplicationContext());
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                findViewById(R.id.search_box).setVisibility(View.VISIBLE);
            }
        });
        if ((!project_services.service_started) || project_services.context == null) {
            startService(new Intent(getApplicationContext(), project_services.class));
            project_services.set_context(getApplicationContext());
        }
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                intent.putExtra("phone", users.get(position).getPhone());
                intent.putExtra("name", users.get(position).getUsername());
                intent.putExtra("statues", users.get(position).getStatues());
                intent.putExtra("birth", users.get(position).getBirthday());
                startActivity(intent);

            }
        });
        if (!sharedPreferences.getBoolean("logged", false) || sharedPreferences.getString("phone", "-1").equals("-1"))
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("rotate", true);
        if (findViewById(R.id.search_box).getVisibility() == View.VISIBLE) {
            outState.putBoolean("visible", true);
            outState.putString("text", ((EditText) findViewById(R.id.search_box)).getText().toString());
        } else
            outState.putBoolean("visible", false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("rotate")) {
            users = db.restore_friends();
        }
        if (savedInstanceState.getBoolean("visible")) {
            findViewById(R.id.search_box).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.search_box)).setText(savedInstanceState.getString("text"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        project_services.main_activity_started = true;
        findViewById(R.id.search_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((TextView) findViewById(R.id.search_box)).getText().toString();
                if (!text.isEmpty()) {
                    project_services.Send_request(getSharedPreferences("logged", MODE_PRIVATE).getString("username", "not access"), text, "add", sharedPreferences.getString("birth", "- - - "));
                }
            }
        });
        project_services.Monitor_request();
    }

    @Override
    protected void onStop() {
        super.onStop();
        project_services.main_activity_started = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return true;
        }
        if (id == R.id.search) {
            if (view_message) {
                users.clear();
                realtime_database.get_my_contacts();
                view_message = false;
            } else {
                users.clear();
                view_message = true;
                project_services.Monitor_request();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}