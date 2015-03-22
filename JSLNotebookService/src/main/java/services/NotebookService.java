package services;


import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dino.api.*;
import domain.DirectoryFactory;
import domain.NotebookRepository;
import entities.Note;
import entities.NotebookList;

/**
 * Created by Jay on 3/6/2015.
 */

@Path("/")
@Stateless
public class NotebookService {

    private static NotebookRepository notebookRepository = new NotebookRepository();
    private static DirectoryFactory directoryFactory = new DirectoryFactory();
    private static String selfHostport = null;

    @Context
    UriInfo uri;

    private String getSelfHostPort() {
        if (selfHostport == null) {
            selfHostport = uri.getBaseUri().toString();
        }
        return selfHostport;
    }

    @GET
    @Path("/notebook/all")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAll() throws NamingException {
        Directory directory = directoryFactory.Create();
        NotebookList notebookList = new NotebookList(directory.getAllNotebooks());
        return Response.ok(notebookList).build();
    }

    @DELETE
    @Path("/notebook/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response deleteNotebook(@PathParam("notebookId") String notebookId) {

        Notebook notebook = notebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        } else {
            notebookRepository.deleteNotebook(notebookId);
            return Response.ok().build();
        }
    }

    @GET
    @Path("/notebook")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNotebooks() {
        return Response.ok(notebookRepository.getNotebooks()).build();
    }

    @GET
    @Path("/notebook/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNotebook(@PathParam("notebookId") String notebookId) {

        Notebook notebook = notebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        } else {
            return Response.ok(notebook).build();
        }
    }


    @GET
    @Path("/notes/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNotes(@PathParam("notebookId") String notebookId) {
        return getNotebook(notebookId);
    }

    @GET
    @Path("/notes/{notebookId}/{noteId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNote(@PathParam("notebookId") String notebookId,
                            @PathParam("noteId") String noteId) {

        Note note = notebookRepository.findNote(notebookId, noteId);
        if(note == null) {
            return Response.status(404).build();
        }
        else {
            return Response.ok(note).build();
        }
    }


    @POST
    @Path("/notebook")
    @Produces(MediaType.APPLICATION_XML)
    public Response postNotebook(Notebook notebook) throws NamingException {


        // The request content consists of the new notebook's header, with only a title.
        if(notebook.getTitle() == null || notebook.getTitle() == "") {
            return Response.status(400).build();
        }

        try {

            // Locate the directory service
            Directory directory = directoryFactory.Create();

            // Create a new notebook (i.e., add it to our repository)
            String notebookId = directory.createNotebook(notebook.getTitle(), getSelfHostPort());
            notebook.setId(notebookId);
            notebook.setPrimaryNotebookUrl(this.getSelfHostPort());
            notebookRepository.add(notebook);

            // For a successful request, the response content is the notebook's header,
            // updated to include the newly-assigned id and the URL of the primary server.
            return Response.ok(notebook).build();

        } catch (NotebookAlreadyExistsException e) {
            return Response.status(409).build();
        } catch (BadAddressException e) {
            return Response.status(400).build();
        }
    }

    @POST
    @Path("/notes/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response postNote(@PathParam("notebookId") String notebookId,
                             Note note) {

        // The request content must be a <note> element containing only a <content> element.
        if(note.getContent() == null
                || note.getId() != null) {
            return Response.status(400).build();
        }

        // Create the note in the given notebook
        Notebook notebook = notebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        }
        Note newNote = notebook.createNote(note.getContent());

        // TODO: If a secondary server for the notebook receives this request, it should re-submit it to the
        // notebook's primary server, and return the response code and content received.


        // TODO: When a note is created, the notebook's primary server is responsible for informing any
        // secondary copies about the new note. Your team is responsible for designing a way to make this happen.

        // The response is the new note, including the noteId assigned by the primary server.
        return Response.ok(newNote).build();
    }


    @GET
    @Path("/config/self")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSelfHostPortApi() {
        return Response.ok(getSelfHostPort()).build();
    }


    @PUT
    @Path("/config/self/{hostport}")
    @Produces(MediaType.APPLICATION_XML)
    public Response setSelfHostPort(@PathParam("hostport") String hostport) {
        selfHostport = hostport;
        return Response.ok().build();
    }

    @GET
    @Path("/config/jndi")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJndiHostPortApi() {
        return Response.ok(directoryFactory.getJndiHostPort()).build();
    }


    @PUT
    @Path("/config/jndi/{hostport}")
    @Produces(MediaType.APPLICATION_XML)
    public Response setJndiHostPort(@PathParam("hostport") String hostport) {
        directoryFactory.setJndiHostPort(hostport);
        return Response.ok().build();
    }

}
