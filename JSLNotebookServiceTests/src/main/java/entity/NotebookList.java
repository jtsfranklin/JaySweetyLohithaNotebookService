package entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.franklin.comp655.util.Is;

@XmlRootElement(name="notebook-list")
public class NotebookList {
	private List<Notebook> notebooks = new ArrayList<Notebook>();
	
	@XmlElement(name="notebook")
	public List<Notebook> getNotebooks() {
		return notebooks;
	}

	public Notebook findById(String id) {
		if( Is.missing(id) ) return null;
		for( Notebook nb : notebooks ) {
			if( id.equals(nb.getId()) ) return nb;
		}
		return null;
	}
}
