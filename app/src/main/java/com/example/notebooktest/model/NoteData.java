package com.example.notebooktest.model;

public class NoteData {

    String noteId;
    String noteText;
    String notePic;
    String   timeStamp;

    public NoteData(){}

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
    public void setNotePic(String notePic) {
        this.notePic = notePic;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


    public String getNoteId() {
        return noteId;
    }
    public String getNoteText(){
        return noteText;
    }
    public String getNotePic() {
        return notePic;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
}

