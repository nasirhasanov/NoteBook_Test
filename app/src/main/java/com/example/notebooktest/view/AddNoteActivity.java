package com.example.notebooktest.view;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.notebooktest.R;
import com.example.notebooktest.helper.ProgressDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddNoteActivity extends AppCompatActivity {

    private CircleImageView noteImageView;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private EditText noteEdittext;
    private File uploadImageFile;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        noteImageView = findViewById(R.id.circleView);
        noteImageView.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                openGalleryActivityForResult();
            }
        });

        noteEdittext = findViewById(R.id.note_editText);
        noteEdittext.requestFocus();
        progressDialog = ProgressDialog.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }
    }

    public void attemptToWriteToDb(){
        try {
            progressDialog.showProgress(AddNoteActivity.this,getString(R.string.please_wait));
            String newNoteId = db.collection("notes").document().getId();

            File newFile = new File(this.getFilesDir(),"fjvnds");
            String filePath = SiliCompressor.with(this).compress(uploadImageFile.getPath(), newFile);
            File uploadFile = new File(filePath);


            final StorageReference notePicRef = storageReference.child("noteImages/" + newNoteId);


            UploadTask uploadTask = notePicRef.putFile(Uri.fromFile(uploadFile));
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return notePicRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Map<String, Object> noteDoc = new HashMap<>();
                    noteDoc.put("note_text", noteEdittext.getText().toString().trim());
                    noteDoc.put("note_pic", String.valueOf(downloadUri));
                    noteDoc.put("timestamp", FieldValue.serverTimestamp());
                    noteDoc.put("user_id", currentUser.getUid());
                    db.collection("notes").document(newNoteId).set(noteDoc)
                            .addOnSuccessListener(unused -> {
                                progressDialog.hideProgress();
                                Toast.makeText(getApplicationContext(),"Note saved!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }



    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        uploadImageFile = new File(getPath(intent.getData()));
                        noteImageView.setImageURI(intent.getData());

                    }
                }
            });

    public void openGalleryActivityForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_save) {
            if (uploadImageFile!=null)
            attemptToWriteToDb();
            else{
                Toast.makeText(getApplicationContext(), "Please select a photo", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            openGalleryActivityForResult();        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            Toast.makeText(getApplicationContext(), "File not found!", Toast.LENGTH_SHORT).show();
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }
}
