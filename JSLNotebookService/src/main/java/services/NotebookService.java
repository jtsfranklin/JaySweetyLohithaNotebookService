package services;


import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.client.WebResource;
import dino.api.*;
import domain.DirectoryFactory;
import domain.NotebookRepository;
import domain.SecondaryServerRepository;
import entities.Note;
import entities.NotebookList;

import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jay on 3/6/2015.
 */

@Path("/")
@Stateless
public class NotebookService {

    private static NotebookRepository primaryNotebookRepository = new NotebookRepository();
    private static NotebookRepository secondaryNotebookRepository = new NotebookRepository();
    private static SecondaryServerRepository secondaryServerRepository = new SecondaryServerRepository();

    private static DirectoryFactory directoryFactory = new DirectoryFactory();
    private static String selfHostport = null;

    @Context
    UriInfo uri;

    @Context
    ServletContext context;

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

        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        } else {
            primaryNotebookRepository.deleteNotebook(notebookId);
            return Response.ok().build();
        }
    }

    @GET
    @Path("/notebook")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNotebooks() {
        return Response.ok(primaryNotebookRepository.getNotebooks()).build();
    }

    @GET
    @Path("/notebook/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getNotebook(@PathParam("notebookId") String notebookId) {

        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
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

        Note note = primaryNotebookRepository.findNote(notebookId, noteId);
        if (note == null) {
            return Response.status(404).build();
        } else {
            return Response.ok(note).build();
        }
    }

    @POST
    @Path("/config/secondary/{notebookId}/{secondaryUrl}")
    @Produces(MediaType.APPLICATION_XML)
    public Response configPostSecondaryNotebook(@PathParam("notebookId") String notebookId,
                                            @PathParam("secondaryUrl") String secondaryUrl) {
        try {
            secondaryServerRepository.add(notebookId, secondaryUrl);
        } catch (NotebookAlreadyExistsException e) {
            return Response.status(409).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/config/secondary/{notebookId}/{secondaryUrl}")
    @Produces(MediaType.APPLICATION_XML)
    public Response configDeleteSecondaryNotebook(@PathParam("notebookId") String notebookId,
                                            @PathParam("secondaryUrl") String secondaryUrl) {
        try {
            secondaryServerRepository.delete(notebookId, secondaryUrl);
        } catch (NotebookNotFoundException e) {
            return Response.status(404).build();
        }
        return Response.ok().build();
    }


    // Creates a secondary copy of a notebook in the server that receives the request.
    // The secondary server is responsible for notifying the primary that the secondary copy has been created.
    @POST
    @Path("/secondary/{notebookId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response postSecondaryNotebook(@PathParam("notebookId") String notebookId) {
        try {
            // Make sure it doesn't already exist
            if (secondaryNotebookRepository.findNotebook(notebookId) != null) {
                return Response.status(409).build();
            }

            // Get the notebook details from the directory
            Directory directory = directoryFactory.Create();
            Notebook notebookFromDirectory = directory.getNotebook(notebookId);

            // Make sure the notebook exists
            if (notebookFromDirectory == null) {
                return Response.status(404).build();
            }

            // Extract the notebook's primary Url
            String primaryNotebookUrl = notebookFromDirectory.getPrimaryNotebookUrl();

            // Get the complete notebook from primary
            Client client = ClientBuilder.newClient();
            Notebook notebook =
                    client.target(Paths.get(primaryNotebookUrl, "notebook").toUri())
                            .request()
                            .get(Notebook.class);

            // Add complete noteboook to our secondary repository
            secondaryNotebookRepository.add(notebook);

            // Inform primary server that we're now a secondary server:
            //     PUT {primaryUrl}/config/secondary/{notebookId}/{secondaryUrl}
            client.target(Paths.get(primaryNotebookUrl, "config", "secondary", notebookId, getSelfHostPort()).toUri())
                    .request()
                    .post(null);

            return Response.ok().build();

        } catch (NamingException e) {
            return Response.status(400).build();
        }
    }

    @POST
    @Path("/notebook")
    @Produces(MediaType.APPLICATION_XML)
    public Response postNotebook(Notebook notebook) throws NamingException {


        // The request content consists of the new notebook's header, with only a title.
        if (notebook.getTitle() == null || notebook.getTitle() == "") {
            return Response.status(400).build();
        }

        try {

            // Locate the directory service
            Directory directory = directoryFactory.Create();

            // Create a new notebook (i.e., add it to our repository)
            String notebookId = directory.createNotebook(notebook.getTitle(), getSelfHostPort());
            notebook.setId(notebookId);
            notebook.setPrimaryNotebookUrl(this.getSelfHostPort());
            primaryNotebookRepository.add(notebook);

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
                             Note note,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        // The request content must be a <note> element containing only a <content> element.
        if (note.getContent() == null
                || note.getId() != null) {
            return Response.status(400).build();
        }

        // TODO: If a secondary server for the notebook receives this request, it should re-submit it to the
        // notebook's primary server, and return the response code and content received.

        // If we're a secondary server, we need to redirect the request to the primary
        if (secondaryNotebookRepository.findNotebook(notebookId) != null) {
            //context.getRequestDispatcher().forward(request,response);
        }

        // Create the note in the given notebook
        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        }
        Note newNote = notebook.createNote(note.getContent());


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
