package domain;

import dino.api.BadAddressException;
import dino.api.Directory;
import dino.api.NotebookAlreadyExistsException;
import dino.api.NotebookNotFoundException;
import dino.api.Notebook;
import entities.NotebookList;

import javax.ejb.Stateless;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jay on 3/11/2015.
 */
@Stateless
public class DirectoryBean implements Directory {

    private static NotebookList notebooks = new NotebookList();

    @Override
    public List<Notebook> getAllNotebooks() {
        return notebooks.getNotebooks();
    }

    @Override
    public Notebook getNotebook(String id) {
        return notebooks.findById(id);
    }

    @Override
    public String createNotebook(String title, String primaryUrl) throws NotebookAlreadyExistsException, BadAddressException {

        if(notebooks.findByTitle(title) != null)
        {
            throw new NotebookAlreadyExistsException();
        }

        String notebookId = UUID.randomUUID().toString();

        Notebook notebook = new Notebook();
        notebook.setTitle(title);
        notebook.setPrimaryNotebookUrl(primaryUrl);
        notebook.setId(notebookId);
        notebooks.addNotebook(notebook);

        return notebookId;
    }

    @Override
    public void deleteNotebook(String id) throws NotebookNotFoundException {
        notebooks.deleteNotebook(id);
    }
}
