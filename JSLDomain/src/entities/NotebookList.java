package entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import dino.api.Notebook;

@XmlRootElement(name="notebook-list")
public class NotebookList {

    private List<Notebook> notebooks;

    public NotebookList() {
        notebooks = new ArrayList<Notebook>();
    }

    public NotebookList(NotebookList a, NotebookList b) {
        List<Notebook> notebooks = new ArrayList<Notebook>();
        notebooks.addAll(a.getNotebooks());
        notebooks.addAll(b.getNotebooks());
        this.notebooks = notebooks;
    }

    public NotebookList(List<Notebook> notebooks) {
        this.notebooks = notebooks;
    }

    @XmlElement(name = "notebook")
    public List<Notebook> getNotebooks() {
        return notebooks;
    }

    public Notebook findById(String id) {
        for (Notebook nb : notebooks) {
            if (id.equals(nb.getId())) return nb;
        }
        return null;
    }

    public void addNotebook(Notebook notebook) {
        notebooks.add(notebook);
    }

    public void deleteNotebook(String notebookId) {
        Notebook notebookToDelete = findById(notebookId);
        notebooks.remove(notebookToDelete);
    }

    public Notebook findByTitle(String title) {
        for (Notebook nb : notebooks) {
            if (title.equals(nb.getTitle())) return nb;
        }
        return null;
    }
}
