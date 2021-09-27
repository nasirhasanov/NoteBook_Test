package com.example.notebooktest.view;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebooktest.R;
import com.example.notebooktest.adapter.NotesAdapter;
import com.example.notebooktest.helper.RecyclerItemTouchHelper;
import com.example.notebooktest.model.NoteData;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyNotesActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private FirebaseUser currentUser;
    private SpinKitView progressBarCenter;
    private FirebaseFirestore db;
    private TextView noNotesTextView;
    private List<NoteData> noteData;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_log_out);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MyNotesActivity.this, LoginActivity.class));
            finish();
        }

        noNotesTextView = findViewById(R.id.no_notes_yet);
        progressBarCenter = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        noteData = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MyNotesActivity.this,LoginActivity.class));
                finish();
            }
        };

        fetchNewNotesFirst();

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    private void fetchNewNotesFirst() {
        progressBarCenter.setVisibility(View.VISIBLE);
        Query first = db.collection("notes")
                .whereEqualTo("user_id", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);
        first.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            noteData.clear();
            progressBarCenter.setVisibility(View.GONE);
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                NoteData newNoteData = new NoteData();

                newNoteData.setNoteId(document.getId());
                newNoteData.setNoteText(String.valueOf(document.get("note_text")));
                newNoteData.setNotePic(String.valueOf(document.get("note_pic")));
                newNoteData.setTimeStamp(String.valueOf(document.getDate("timestamp")));

                noteData.add(newNoteData);

            }

            if (!noteData.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new NotesAdapter(noteData);
                recyclerView.setAdapter(adapter);
                noNotesTextView.setVisibility(View.GONE);
            } else {
                progressBarCenter.setVisibility(View.GONE);
                noNotesTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_note, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mAuth.signOut();
        }
        if (item.getItemId() == R.id.action_new_note) {
            startActivity(new Intent(MyNotesActivity.this, AddNoteActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesAdapter.ViewHolder) {

            NoteData deletedNote = noteData.get(position);
            db.collection("notes").document(deletedNote.getNoteId()).delete();
        }
    }


}
