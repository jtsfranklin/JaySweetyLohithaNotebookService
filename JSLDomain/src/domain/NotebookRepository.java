package domain;

import dino.api.Notebook;
import entities.Note;
import entities.NotebookList;

/**
 * Created by Jay on 3/9/2015.
 */
public class NotebookRepository {

    private NotebookList notebooks = new NotebookList();

    public Notebook findNotebook(String notebookId) {
        Notebook notebook = notebooks.findById(notebookId);
        return notebook;
    }

    public Note findNote(String notebookId, String noteId) {
        Notebook notebook = notebooks.findById(notebookId);
        if (notebook == null) {
            return null;
        }
        return notebook.find(noteId);
    }

    public NotebookList getNotebooks() {
        return notebooks;
    }

    public void add(Notebook notebook) {
        notebooks.addNotebook(notebook);
    }
}
