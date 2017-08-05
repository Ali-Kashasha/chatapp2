package com.example.alikshasha.chatapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import data.message;

public class MessageActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    EditText messag;
    Button send;
    database db;
    String username;
    String next_phone = null, myphone, birthday;
    SharedPreferences sharedPreferences;
    public static ListView listView;
    public static ArrayList<message> list_message;
    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        username = getIntent().getStringExtra("name");
        toolbar.setTitle(username);
        birthday = getIntent().getStringExtra("birth");
        setSupportActionBar(toolbar);
        list_message = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        messag = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        listView = (ListView) findViewById(R.id.list_message);
        next_phone = getIntent().getStringExtra("phone");
        myphone = sharedPreferences.getString("phone", "-1");
        next_phone = next_phone.replace("+2", "");
        next_phone = next_phone.replaceAll("\\s", "");
        storage = FirebaseStorage.getInstance();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messag.getText().toString().isEmpty() || messag.getText().equals(" ")) {
                    project_services.writeMessage(messag.getText().toString(), next_phone, sharedPreferences.getString("birth", "---"), username);
                    messag.setText("");
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list_message.get(position).getBody().contains("fileuploadedtoyourfiendthismessagenotify")) {
                    download_file(list_message.get(position).getBody().replace("fileuploadedtoyourfiendthismessagenotify", "").trim().toString());
                    Toast.makeText(MessageActivity.this, R.string.donwloading_file, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        project_services.message_activity_started = true;
        project_services.read_message(next_phone);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("message", messag.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        project_services.message_activity_started = false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profileactivity) {
            Intent intent = new Intent(getApplicationContext(), friend_details.class);
            intent.putExtra("phone", next_phone);
            intent.putExtra("name", username);
            intent.putExtra("birth", birthday);
            startActivity(intent);
        } else if (id == R.id.block) {
            project_services.block(next_phone);
        }
        return super.onOptionsItemSelected(item);
    }

    public void upload_files(View view) {
        Intent intent = new Intent(this, FilePicker.class);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    private void start_upload(String path) {
        Log.d("path  ", path);
        RealtimeDatabase realtime_database = new RealtimeDatabase(getApplicationContext());
        final Uri file = Uri.fromFile(new File(path));
        StorageReference strageRef = storage.getReferenceFromUrl("gs://chatapp-21cbe.appspot.com/");
        StorageReference riversRef = strageRef.child(realtime_database.detemined_node(next_phone) + "/" + file.getLastPathSegment().trim());
        UploadTask uploadTask = riversRef.putFile(file);
        project_services.writeMessage("fileuploadingtoyourfiendthismessagenotify" + file.getLastPathSegment().trim(), next_phone, sharedPreferences.getString("birth", "---").toString(), username);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MessageActivity.this, R.string.fail_upload, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                project_services.writeMessage("fileuploadedtoyourfiendthismessagenotify" + file.getLastPathSegment().trim(), next_phone, sharedPreferences.getString("birth", "---"), username);
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }

    private void download_file(final String file) {
        RealtimeDatabase realtime_database = new RealtimeDatabase(getApplicationContext());
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));

        storageRef.child(realtime_database.detemined_node(next_phone) + "/" + file.trim())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(MessageActivity.this, R.string.downloaded, Toast.LENGTH_SHORT).show();
                Log.d("thisfile_todownload" + file, "");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MessageActivity.this, R.string.cant_down, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_FILE:
                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        start_upload(selectedFile.getPath());
                    }
                    break;
            }
        }
    }

    public void add_emoji(View view) {
        if (view.getId() == R.id.em1)
            messag.setText(messag.getText() + "\uD83D\uDE02");
        else if (view.getId() == R.id.em2)
            messag.setText(messag.getText() +"\uD83D\uDE0D");
        else if (view.getId() == R.id.em3)
            messag.setText(messag.getText() + "\uD83D\uDE13");
        else if (view.getId() == R.id.em4)
            messag.setText(messag.getText() +"\uD83D\uDC4D");
    }
    public void show_emoji(View view) {
        if (findViewById(R.id.emoji_layout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.emoji_layout).setVisibility(View.INVISIBLE);
        } else
            findViewById(R.id.emoji_layout).setVisibility(View.VISIBLE);
    }
}