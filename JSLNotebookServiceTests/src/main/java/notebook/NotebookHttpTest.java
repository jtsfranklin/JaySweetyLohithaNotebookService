package notebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import edu.franklin.comp655.util.Is;
import entity.Note;
import entity.Notebook;
import entity.NotebookList;

import javax.ws.rs.core.Response;

public class NotebookHttpTest {
	
	// useful constants
	public static final String      team = System.getProperty("team", "JSLNotebookService");
	public static final String  dinoUrl1 = System.getProperty("url1", "http://localhost:8080/" + team);
	public static final String  dinoUrl2 = System.getProperty("url2", "http://localhost:8080/" + team + "2");
	public static final String  dinoUrl3 = System.getProperty("url3", "http://localhost:8080/" + team + "3");
	public static final String titleBase = System.getProperty("titleBase", "DiNo testing");
	public static final String    fakeId = "--fake--";
	
	public static final String  notebookUri = "/notebook";
	public static final String     notesUri = "/notes";
	public static final String secondaryUri = "/secondary";
	public static final String      xmlType = "text/xml";
	
	// using the Jersey client library
	private static Client client = Client.create();
	private static WebResource resource1 = client.resource(dinoUrl1);
	private static WebResource resource2 = client.resource(dinoUrl2);
	private static WebResource resource3 = client.resource(dinoUrl3);
	
	// miscellaneous resources
	private static int contentSequence = 1;
	private static List<String> idList1 = new ArrayList<String>();
	private static List<String> idList2 = new ArrayList<String>();
	private static List<String> idList3 = new ArrayList<String>();
	private static long         random = new Random().nextLong();
	private static int   titleSequence = 1;
	
	@Before
	public void setup() {
		
	}
	
	@AfterClass
	static public void cleanup() {
		// comment this out if your notebook-delete feature isn't working
		deleteNotebooks(resource1,idList1);
		deleteNotebooks(resource2,idList2);
		deleteNotebooks(resource3,idList3);
	}
	
	@Test
	public void manageNotebooks() {
		// create four notebooks
		Notebook notebook1 = addNotebook(resource1,generateTitle());
		idList1.add(notebook1.getId());
		Notebook notebook2 = addNotebook(resource1,generateTitle());
		idList1.add(notebook2.getId());
		Notebook notebook3 = addNotebook(resource1,generateTitle());
		idList1.add(notebook3.getId());
		Notebook notebook4 = addNotebook(resource1,generateTitle());
		idList1.add(notebook4.getId());
		
		// verify that the notebooks are returned by GET /notebook
		NotebookList expected = new NotebookList();
		expected.getNotebooks().add(notebook1);
		expected.getNotebooks().add(notebook2);
		expected.getNotebooks().add(notebook3);
		expected.getNotebooks().add(notebook4);
		compareNotebookLists(expected,getNotebookList(resource1));
		
		// delete a notebook and verify things
		deleteNotebook(resource1,notebook2.getId());
		verifyAbsentNotebook(resource1,notebook2.getId());
		idList1.remove(notebook2.getId());
		expected.getNotebooks().remove(notebook2);
		compareNotebookLists(expected,getNotebookList(resource1));
	}
	
	@Test
	public void createNotebookAndAddNote() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		addNote(resource1,notebook.getId());
	}
	
	@Test
	public void manageNotes() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		
		// add some notes and check that they come back when retrieved
		Note note1 = addNote(resource1,notebook.getId());
		Note note2 = addNote(resource1,notebook.getId());
		Note note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
        Notebook notebookFromServer = getNotebook(resource1,notebook.getId());
		compareNotebooks(notebook,notebookFromServer);
		
		// delete a note and check things
		deleteNote(resource1,notebook.getId(),note2.getId());
		notebook.getNotes().remove(note2);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		
		// add a few more notes and verify
		Note note4 = addNote(resource1,notebook.getId());
		Note note5 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note4);
		notebook.getNotes().add(note5);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));

		// replace a note and verify
		String newContent = generateContent();
		replaceNote(resource1,notebook.getId(),note3.getId(),newContent);
		note3.setContent(newContent);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
	}
	
	@Test
	public void secondaryCopy() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		
		// add some notes and check that they come back when retrieved
		Note note1 = addNote(resource1,notebook.getId());
		Note note2 = addNote(resource1,notebook.getId());
		Note note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		
		// create a secondary copy
		addSecondaryCopy(resource2,notebook.getId());

        Notebook notebookFromServer2 = getNotebook(resource2,notebook.getId());
		compareNotebooks(notebook,notebookFromServer2);
		
		// add a couple of notes and verify that they are available at the secondary
		Note note4 = addNote(resource1,notebook.getId());
		Note note5 = addNote(resource1,notebook.getId());
		
		// wait to allow the new notes to propagate
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note4,getNote(resource2,notebook.getId(),note4.getId()));
		compareNotes(note5,getNote(resource2,notebook.getId(),note5.getId()));
		notebook.getNotes().add(note4);
		notebook.getNotes().add(note5);
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
	}
	
	@Test
	public void secondaryCopy2() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		
		// add a notes and check that it comes back when retrieved
		Note note1 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		
		// create a secondary copy
		addSecondaryCopy(resource2,notebook.getId());
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// add a couple more notes
		Note note2 = addNote(resource1,notebook.getId());
		Note note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// change a couple of the notes and verify that they are available at the secondary
		note1.setContent(generateContent());
		note2.setContent(generateContent());
		replaceNote(resource1,notebook.getId(),note1.getId(),note1.getContent());
		replaceNote(resource1,notebook.getId(),note2.getId(),note2.getContent());
		
		// wait to allow the revised notes to propagate
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note1,getNote(resource2,notebook.getId(),note1.getId()));
		compareNotes(note2,getNote(resource2,notebook.getId(),note2.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// more changes, this time via the secondary
		note1.setContent(generateContent());
		note3.setContent(generateContent());
		replaceNote(resource2,notebook.getId(),note1.getId(),note1.getContent());
		replaceNote(resource2,notebook.getId(),note3.getId(),note3.getContent());
		
		// wait to allow the revised notes to propagate
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note1,getNote(resource1,notebook.getId(),note1.getId()));
		compareNotes(note1,getNote(resource2,notebook.getId(),note1.getId()));
		compareNotes(note2,getNote(resource1,notebook.getId(),note2.getId()));
		compareNotes(note2,getNote(resource2,notebook.getId(),note2.getId()));
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
	}
	
	@Test
	public void removeSecondaryCopy() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		
		// don't add this notebook to the idList, because we're going to remove it in the test ...
		
		// create a secondary copy
		addSecondaryCopy(resource2,notebook.getId());
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// remove the secondary copy and check that it's gone in both places
		deleteNotebook(resource1, notebook.getId());
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		verifyAbsentNotebook(resource1, notebook.getId());
		verifyUnlistedNotebook(resource1, notebook.getId());
		verifyAbsentNotebook(resource2, notebook.getId());
		verifyUnlistedNotebook(resource2, notebook.getId());
		
		// now do the same thing when the notebook has some notes in it
		// add a notes and check that it comes back when retrieved
		notebook = addNotebook(resource1,generateTitle());
		Note note1 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		addSecondaryCopy(resource2,notebook.getId());
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// add a couple more notes
		Note note2 = addNote(resource1,notebook.getId());
		Note note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// remove the secondary copy and check that it's gone in both places
		deleteNotebook(resource1, notebook.getId());
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		verifyAbsentNotebook(resource1, notebook.getId());
		verifyUnlistedNotebook(resource1, notebook.getId());
		verifyAbsentNotebook(resource2, notebook.getId());
		verifyUnlistedNotebook(resource2, notebook.getId());
		
		// finally do it again, but this time do the delete via the secondary
		// add a notes and check that it comes back when retrieved
		notebook = addNotebook(resource1,generateTitle());
		note1 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		addSecondaryCopy(resource2,notebook.getId());
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// add a couple more notes
		note2 = addNote(resource1,notebook.getId());
		note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// remove the secondary copy using the secondary, and check that it's gone in both places
		deleteNotebook(resource2, notebook.getId());
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		verifyAbsentNotebook(resource1, notebook.getId());
		verifyUnlistedNotebook(resource1, notebook.getId());
		verifyAbsentNotebook(resource2, notebook.getId());
		verifyUnlistedNotebook(resource2, notebook.getId());
	}
	
	@Test
	public void maxTransparency() {
		// this test is very similar to the secondary copy test ...
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		
		// add some notes and check that they come back when retrieved
		Note note1 = addNote(resource1,notebook.getId());
		Note note2 = addNote(resource1,notebook.getId());
		Note note3 = addNote(resource1,notebook.getId());
		notebook.getNotes().add(note1);
		notebook.getNotes().add(note2);
		notebook.getNotes().add(note3);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		
		// check that the notebook is available somewhere else
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// create a secondary, add a couple of notes and verify that they are available at the secondary
		addSecondaryCopy(resource2,notebook.getId());
		Note note4 = addNote(resource1,notebook.getId());
		Note note5 = addNote(resource1,notebook.getId());
		
		// wait to allow the new notes to propagate
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note4,getNote(resource2,notebook.getId(),note4.getId()));
		compareNotes(note5,getNote(resource2,notebook.getId(),note5.getId()));
		notebook.getNotes().add(note4);
		notebook.getNotes().add(note5);
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		
		// add a note at the secondary copy and verify that it is visible elsewhere
		Note note6 = addNote(resource2,notebook.getId(),500);
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note6,getNote(resource1,notebook.getId(),note6.getId()));
		compareNotes(note6,getNote(resource3,notebook.getId(),note6.getId()));
		notebook.getNotes().add(note6);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource3,notebook.getId()));
		
		// add a note at the third server (neither primary nor secondary) and verify
		Note note7 = addNote(resource3,notebook.getId());
		try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
		compareNotes(note7,getNote(resource1,notebook.getId(),note7.getId()));
		compareNotes(note7,getNote(resource2,notebook.getId(),note7.getId()));
		notebook.getNotes().add(note7);
		compareNotebooks(notebook,getNotebook(resource1,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource2,notebook.getId()));
		compareNotebooks(notebook,getNotebook(resource3,notebook.getId()));
	}
	
	@Test
	public void checkTitleUniqueness() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		Notebook duplicate = new Notebook();
		duplicate.setTitle(notebook.getTitle());
		
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		Notebook check = null;
		
		// try to create the duplicate
		try {
			check = resource1.path(notebookUri).entity(duplicate).type(xmlType).post(Notebook.class);
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNull("unexpected notebook",check);
		Assert.assertNotNull("creating duplicate notebook should have returned 409",uie);
		Assert.assertEquals("creating duplicate notebook should have returned 409", Response.Status.CONFLICT.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	@Test
	public void nonExistentNotebook() {
		verifyAbsentNotebook(resource1,fakeId);
		verifyAbsentNotebook(resource2,fakeId);
	}
	
	@Test
	public void nonExistentNote() {
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		verifyAbsentNote(resource1,notebook.getId(),fakeId);
		addNote(resource1,notebook.getId());
		verifyAbsentNote(resource1,notebook.getId(),fakeId);
	}
	
	@Test
	public void secondaryErrors() {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to create a secondary copy of a non-existent notebook
		String uri = secondaryUri + "/" + fakeId;
		try {
			resource2.path(uri).post();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("creating secondary copy of non-existent notebook should have returned 404",uie);
		Assert.assertEquals("creating secondary copy of non-existent notebook should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// now try creating a secondary copy at the primary server
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		uri = secondaryUri + "/" + notebook.getId();
		try {
			resource1.path(uri).post();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("creating secondary copy at the primary should have returned 409",uie);
		Assert.assertEquals("creating secondary copy at the primary should have returned 409",Response.Status.CONFLICT.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// next, try creating two secondary copies at the same server
		addSecondaryCopy(resource2,notebook.getId());
		try {
			resource2.path(uri).post();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("creating second secondary copy at the same server should have returned 409",uie);
		Assert.assertEquals("creating second secondary copy at the same server should have returned 409",Response.Status.CONFLICT.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	@Test
	public void missingSecondaries() {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try deleting a secondary copy of a non-existent notebook	
		String uri = secondaryUri + "/" + fakeId;
		try {
			resource1.path(uri).delete();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("deleting secondary copy of non-existent notebook should have returned 404",uie);
		Assert.assertEquals("deleting secondary copy of non-existent notebook should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// try deleting a secondary copy from the wrong host
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		addSecondaryCopy(resource2,notebook.getId());
		uri = secondaryUri + "/" + notebook.getId();
		try {
			resource3.path(uri).delete();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("deleting secondary copy at the wrong server should have returned 404",uie);
		Assert.assertEquals("deleting secondary copy at the wrong server should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// try deleting a secondary copy at the primary
		try {
			resource1.path(uri).delete();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("deleting secondary copy at the primary should have returned 409",uie);
		Assert.assertEquals("deleting secondary copy at the wrong server should have returned 409",Response.Status.CONFLICT.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	@Test
	public void moreMissingNotes() {
		// try posting a note to a non-existent notebook
		Note note = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		String uri = notesUri + "/" + fakeId;
		Note input = new Note();
		input.setContent(generateContent());
		try {
			note = resource1.path(uri).entity(input).type(xmlType).post(Note.class);
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNull("unexpected note",note);
		Assert.assertNotNull("adding a note to a non-existent notebook should have returned 404",uie);
		Assert.assertEquals("adding a note to a non-existent notebook should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// try replacing a non-existent note
		Notebook notebook = addNotebook(resource1,generateTitle());
		idList1.add(notebook.getId());
		addNote(resource1,notebook.getId());
		note = addNote(resource1,notebook.getId());
		deleteNote(resource1,notebook.getId(),note.getId());
		input.setContent(generateContent());
		uri = notesUri + "/" + notebook.getId() + "/" + note.getId();
		try {
			resource1.path(uri).entity(input).type(xmlType).put();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		Assert.assertNotNull("replacing a non-existent note should have returned 404",uie);
		Assert.assertEquals("replacing a non-existent note should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
		
		// try deleting a non-existent note
		try {
			resource1.path(uri).delete();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		Assert.assertNotNull("deleting a non-existent note should have returned 404",uie);
		Assert.assertEquals("deleting a non-existent note should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	// try deleting a non-existent notebook
	@Test
	public void deleteNonExistentNotebook() {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try deleting a non-existent note
		String uri = notebookUri + "/" + fakeId;
		try {
			resource1.path(uri).delete();
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		Assert.assertNotNull("deleting a non-existent notebook should have returned 404",uie);
		Assert.assertEquals("deleting a non-existent notebook should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	// utility functions
	private Notebook addNotebook(WebResource resource, String title) {
		Notebook notebook = null;
		Notebook check = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// create the input noteboook
		Notebook input = new Notebook();
		input.setTitle(title);
		
		// try to create the notebook
		try {
			notebook = resource.path(notebookUri).entity(input).type(xmlType).post(Notebook.class);
		} catch (UniformInterfaceException e) {
			reportFailure("POST",notebookUri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("notebook missing",notebook);
		Assert.assertEquals("wrong notebook title", input.getTitle(),notebook.getTitle());
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);

		String uri = notebookUri + "/" + notebook.getId();
		try {
			check = resource.path(uri).get(Notebook.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("unable to retrieve notebook",check);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		compareNotebooks(notebook,check);

		return notebook;
	}
	private Note addNote(WebResource resource, String notebookId) {
		return addNote(resource,notebookId,0);
	}
	private Note addNote(WebResource resource, String notebookId, int delay) {
		Note note = null;
		Note check = null;
		String uri = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// add a note
		uri = notesUri + "/" + notebookId;
		Note input = new Note();
		input.setContent(generateContent());
		try {
			note = resource.path(uri).entity(input).type(xmlType).post(Note.class);
		} catch (UniformInterfaceException e) {
			reportFailure("POST",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNotNull("note missing",note);
		Assert.assertEquals("wrong note content", input.getContent(),note.getContent());
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);

		uri = notesUri + "/" + notebookId + "/" + note.getId();
		try {
			if( delay > 0 ) try { Thread.sleep(delay); } catch(InterruptedException ignore) {}
			check = resource.path(uri).get(Note.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNotNull("unable to retrieve note",check);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		compareNotes(note,check);
		
		return note;
	}
	private void addSecondaryCopy(WebResource resource,String notebookId) {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to create the secondary
		String uri = secondaryUri + "/" + notebookId;
		try {
			resource.path(uri).post();
		} catch (UniformInterfaceException e) {
			reportFailure("POST",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);

	}
	private void compareNotebookLists(NotebookList nbList1, NotebookList nbList2)  {
		if( nbList1 == null && nbList2 == null ) return;
		Assert.assertNotNull("notebook list is missing",nbList1);
		Assert.assertNotNull("notebook list is missing",nbList2);
		Assert.assertEquals("list lengths don't match",nbList1.getNotebooks().size(),nbList2.getNotebooks().size());
		for( Notebook nb : nbList1.getNotebooks() ) {
			compareNotebooks(nb,nbList2.findById(nb.getId()));
		}
	}
	private void compareNotebooks(Notebook nb1, Notebook nb2)  {
		if( nb1 == null && nb2 == null ) return;
		Assert.assertNotNull("notebook is missing",nb1);
		Assert.assertNotNull("notebook is missing",nb2);
		Assert.assertEquals("notebook ids don't match",nb1.getId(),nb2.getId());
		Assert.assertEquals("notebook titles don't match",nb1.getTitle(),nb2.getTitle());
		compareNotes(nb1,nb2);
	}
	private void compareNotes(Notebook nb1, Notebook nb2) {
		if( nb1 == null && nb2 == null ) return;
		Assert.assertNotNull("notebook is missing",nb1);
		Assert.assertNotNull("notebook is missing",nb2);
		Assert.assertEquals("list lengths don't match",nb1.getNotes().size(),nb2.getNotes().size());
		for( Note n : nb1.getNotes() ) {
			compareNotes(n,nb2.find(n.getId()));
		}
	}
	private void compareNotes(Note n1, Note n2)  {
		if( n1 == null && n2 == null ) return;
		Assert.assertNotNull("note is missing",n1);
		Assert.assertNotNull("note is missing",n2);
		Assert.assertEquals("note ids don't match",n1.getId(),n2.getId());
		Assert.assertEquals("note contents don't match",n1.getContent(),n2.getContent());
	}
	private void deleteNotebook(WebResource resource,String id) {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to get the notebook
		String uri = notebookUri + "/" + id;
		try {
			resource.path(uri).delete();
		} catch (UniformInterfaceException e) {
			reportFailure("DELETE",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		verifyAbsentNotebook(resource,id);
	}
	private void deleteNote(WebResource resource, String notebookId, String noteId) {
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to get the notebook
		String uri = notesUri + "/" + notebookId + "/" + noteId;
		try {
			resource.path(uri).delete();
		} catch (UniformInterfaceException e) {
			reportFailure("DELETE",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		verifyAbsentNote(resource,notebookId,noteId);
	}
	private NotebookList getNotebookList(WebResource resource) {
		NotebookList nbl = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to get the notebook
		try {
			nbl = resource.path(notebookUri).get(NotebookList.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",notebookUri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("notebook list missing",nbl);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		
		return nbl;
	}
	private Notebook getNotebook(WebResource resource, String id) {
		Notebook notebook = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to get the notebook
		String uri = notebookUri + "/" + id;
		try {
			notebook = resource.path(uri).get(Notebook.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("notebook missing",notebook);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		
		return notebook;
	}
	private Note getNote(WebResource resource, String notebookId, String noteId) {
		Note note = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// try to get the note
		String uri = notesUri + "/" + notebookId + "/" + noteId;
		try {
			note = resource.path(uri).get(Note.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		Assert.assertNotNull("note missing",note);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		
		return note;
	}
	private static void deleteNotebooks(WebResource resource, List<String> ids) {
		if( Is.missing(ids) ) return;
		for( String id : ids) {
			String uri = notebookUri + "/" + id;
			try {
				resource.path(uri).delete();
			} catch (UniformInterfaceException uie) {
				reportFailure("DELETE",uri,uie);
			} catch (RuntimeException re) {
				re.printStackTrace();
			}
		}
	}
	private void replaceNote(WebResource resource,String notebookId,String noteId,String content) {
		Note check = null;
		String uri = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		// replace the note's content
		uri = notesUri + "/" + notebookId + "/" + noteId;
		Note input = new Note();
		input.setContent(content);
		try {
			resource.path(uri).entity(input).type(xmlType).put();
		} catch (UniformInterfaceException e) {
			reportFailure("PUT",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);

		try {
			check = resource.path(uri).get(Note.class);
		} catch (UniformInterfaceException e) {
			reportFailure("GET",uri,e);
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNotNull("unable to retrieve note",check);
		Assert.assertNull("unexpected exception",uie);
		Assert.assertNull("unexpected exception",re);
		input.setId(noteId);
		compareNotes(input,check);
	}

	private void verifyAbsentNote(WebResource resource, String notebookId, String noteId) {
		Note note = null;
		String uri = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		uri = notesUri + "/" + notebookId + "/" + noteId;
		try {
			note = resource.path(uri).get(Note.class);
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNull("unexpected note",note);
		Assert.assertNotNull("GET" + uri + " should have returned 404",uie);
		Assert.assertEquals("GET" + uri + " should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}
	
	private void verifyAbsentNotebook(WebResource resource, String id) {
		Notebook notebook = null;
		String uri = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		uri = notebookUri + "/" + id;
		try {
			notebook = resource.path(uri).get(Notebook.class);
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNull("unexpected notebook",notebook);
		Assert.assertNotNull("GET" + uri + " should have returned 404",uie);
		Assert.assertEquals("GET" + uri + " should have returned 404",Response.Status.NOT_FOUND.getStatusCode(),uie.getResponse().getStatus());
		Assert.assertNull("unexpected exception",re);
	}

	private void verifyUnlistedNotebook(WebResource resource, String id) {
		NotebookList notebookList = null;
		Notebook notebook = null;
		String uri = null;
		UniformInterfaceException uie = null;
		RuntimeException re = null;
		
		uri = notebookUri;
		try {
			notebookList = resource.path(uri).get(NotebookList.class);
		} catch (UniformInterfaceException e) {
			uie = e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			re = e;
		}
		
		// check things
		Assert.assertNotNull("GET /notebook returned null", notebookList);
		if( Is.present(notebookList.getNotebooks()) ) {
			for( Notebook nb : notebookList.getNotebooks() ) {
				if( id.equals(nb.getId()) ) {
					notebook = nb;
					break;
				}
			}
		}
		Assert.assertNull("unexpected notebook notebook/" + id + " at " + resource1.toString(),notebook);
		Assert.assertNull("unexpected uniform interface exception",uie);
		Assert.assertNull("unexpected exception",re);
	}

	
	// misc
	private static String generateTitle() {
		return titleBase + " " + random + " " + titleSequence++;
	}
	
	private static String generateContent() {
		return "Content " + contentSequence++;
	}
	private static void reportFailure(String verb, String uri, UniformInterfaceException uie) {
		System.out.println("unable to " + verb + " " + uri + " ... " + uie.getResponse().getStatus() + " " + uie.getResponse().getEntity(String.class));
	}
}
