package entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.franklin.comp655.util.Is;

@XmlRootElement
public class Notebook {
	private String id;
	private String title;
	private List<Note> notes = new ArrayList<Note>();
	
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
	
	@XmlElement(name="note")
	public List<Note> getNotes() {
		return notes;
	}
	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}
	
	public Note find(String id) {
		if( Is.missing(id) ) return null;
		for( Note n : notes ) {
			if( id.equals(n.getId()) ) {
				return n;
			}
		}
		return null;
	}
}
