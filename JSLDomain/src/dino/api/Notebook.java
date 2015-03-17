package dino.api;

import entities.Note;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Notebook implements Serializable {

	private static final long serialVersionUID = 3757893817259090136L;
	private String id;
	private String title;
	private String primaryNotebookUrl;

    private List<Note> notes = new ArrayList<Note>();
    private long lastNoteId = 0;

    @XmlElement
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
    @XmlElement
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    @XmlElement
	public String getPrimaryNotebookUrl() {
		return primaryNotebookUrl;
	}
	public void setPrimaryNotebookUrl(String primaryNotebookUrl) {
		this.primaryNotebookUrl = primaryNotebookUrl;
	}

    public Note find(String id) {
        for( Note n : notes ) {
            if( id.equals(n.getId()) ) {
                return n;
            }
        }
        return null;
    }

    public Note createNote(String content){
        String newId = Long.toString(lastNoteId+1);
        Note newNote = new Note();
        newNote.setContent(content);
        newNote.setId(newId);
        notes.add(newNote);
        ++lastNoteId;
        return newNote;
    }
}