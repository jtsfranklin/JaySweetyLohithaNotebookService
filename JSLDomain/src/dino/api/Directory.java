package dino.api;

import java.util.List;

import javax.ejb.Remote;

/**
 * Directory is the API for the DiNo directory service.
 * 
 * @author Don Swartwout
 */
@Remote
public interface Directory {
	/**
	 * Get the directory information for all notebooks currently in the system
	 * @return a list of Notebook objects, one for each notebook currently
	 * in the system. Returns a list of length 0 if there are no notebooks in the system.
	 */
	List<Notebook> getAllNotebooks();
	
	/**
	 * Get the directory information for one notebook
	 * @param id the notebook's id
	 * @return a Notebook object that represents the given notebook; returns null if
	 * there is no notebook in the system with the given id
	 */
	Notebook getNotebook(String id);
	
	/**
	 * Create a new notebook, if possible
	 * @param title the new notebook's title
	 * @param primaryUrl the base URL of the notebook service that will host the notebook's primary copy.
	 * The base URL for a notebook service consists of the service host and context root,
	 * for example, "http://mypc:8080/dino"
	 * @return if successful, an id for the new notebook selected by the directory service
	 * @throws dino.api.NotebookAlreadyExistsException if a notebook with the same title already exists in the system
	 * @throws dino.api.BadAddressException if the primary URL is invalid
	 */
	String createNotebook(String title, String primaryUrl)
		throws NotebookAlreadyExistsException, BadAddressException;
	/**
	 * Delete the notebook with the given id
	 * @param id identifier of the notebook to be deleted
	 * @throws dino.api.NotebookNotFoundException if there is no notebook with the given id
	 */
	void deleteNotebook(String id)
		throws NotebookNotFoundException;
}
