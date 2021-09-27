package com.example.notebooktest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notebooktest.R;
import com.example.notebooktest.model.NoteData;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder>{

    private final List<NoteData> notesData;


    public NotesAdapter(List<NoteData> notesData) {
        this.notesData = notesData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_my_note,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        NoteData noteData = notesData.get(position);


        holder.noteTextView.setText(noteData.getNoteText());
        holder.timePlaceTextView.setText(noteData.getTimeStamp());
        downloadProfilePicture(holder.noteImage,noteData.getNotePic());
    }

    @Override
    public int getItemCount() {
        return notesData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView noteTextView;
        private final TextView timePlaceTextView;
        private final CircleImageView noteImage;
        public RelativeLayout viewBackground, viewForeground;

        ViewHolder(View itemView) {
            super(itemView);

            noteTextView = itemView.findViewById(R.id.note_text);
            timePlaceTextView = itemView.findViewById(R.id.time_and_place);
            noteImage = itemView.findViewById(R.id.note_picture);

            viewBackground = itemView.findViewById(R.id.view_background_swipe_delete);
            viewForeground = itemView.findViewById(R.id.parent);

        }
    }


    private void downloadProfilePicture(CircleImageView noteImage, String note_pic_path) {

        Glide.with(noteImage.getContext())
                .load(note_pic_path)
                .placeholder(R.drawable.drawable_notebook)
                .thumbnail(
                        Glide.with(noteImage.getContext())
                                .load(note_pic_path)
                                .override(50,50))
                .into(noteImage);

    }
}
