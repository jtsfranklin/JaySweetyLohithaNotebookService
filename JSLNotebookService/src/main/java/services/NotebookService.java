package services;


import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.client.*;

import com.sun.jersey.api.client.WebResource;
import dino.api.*;
import domain.DirectoryFactory;
import domain.NotebookRepository;
import domain.SecondaryServerRepository;
import entities.Note;
import entities.NotebookList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jay on 3/6/2015.
 */

@Path("/")
public class NotebookService {

    // Contains a list of notebooks for which we are a primary server
    private static NotebookRepository primaryNotebookRepository = new NotebookRepository();

    // Contains a list of notebooks for which we are a secondary server
    private static NotebookRepository secondaryNotebookRepository = new NotebookRepository();

    // Contains a list of servers that are registered as secondaries for us
    private static SecondaryServerRepository secondaryServerRepository = new SecondaryServerRepository();

    // Service locator to provide a Directory EJB
    private static DirectoryFactory directoryFactory = new DirectoryFactory();

    // Points to our own URI (defaults to calling context URI)
    private static String selfHostport = null;

    @Context
    UriInfo uri;

    @Context
    ServletContext context;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    private String getSelfHostPort() {
        if (selfHostport == null) {
            selfHostport = uri.getBaseUri().toString();
        }
        return selfHostport;
    }

    @GET
    @Path("/notebook/all")
    @Produces(MediaType.TEXT_XML)
    public Response getAll() throws NamingException {
        Directory directory = directoryFactory.Create();
        NotebookList notebookList = new NotebookList(directory.getAllNotebooks());
        return Response.ok(notebookList).build();
    }


    @DELETE
    @Path("/notes/{notebookId}/{noteId}")
    @Produces(MediaType.TEXT_XML)
    public Response deleteNote(@PathParam("notebookId") String notebookId, @PathParam("noteId") String noteId) {

        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);

        // If notebook doesn't exist, return 404
        if (notebook == null) {

            return Response.status(404).build();

        } else {
            // If note doesn't exist, return 404
            if (notebook.find(noteId) == null) {
                return Response.status(404).build();
            }

            // Delete the note
            notebook.deleteNoteById(noteId);

            // OK
            return Response.ok().build();
        }
    }

    @DELETE
    @Path("/notebook/{notebookId}")
    public Response deleteNotebook(@PathParam("notebookId") String notebookId) {

        Client client = new Client();
        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
        Notebook secondaryNotebook = secondaryNotebookRepository.findNotebook(notebookId);

        if (notebook != null) {

            // Delete the primary copy
            primaryNotebookRepository.deleteNotebook(notebookId);

            // Inform all secondaries
            List<String> secondaryServers = secondaryServerRepository.getServersForNotebook(notebookId);
            for(String secondaryServer : secondaryServers) {
                client.resource(secondaryServer)
                        .path("/norecurse/notebook/" + notebook.getId())
                        .delete();
            }

            // OK
            return Response.ok().build();

        } else if (secondaryNotebook != null) {

            // Inform the primary server
            return client.resource(notebook.getPrimaryNotebookUrl())
                    .path("/notebook/" + notebook.getId())
                    .delete(Response.class);
        } else {
            // Not found
            return Response.status(404).build();
        }
    }


    @DELETE
    @Path("/norecurse/notebook/{notebookId}")
    public Response noRecurseDeleteNotebook(@PathParam("notebookId") String notebookId) {

        Notebook secondaryNotebook = secondaryNotebookRepository.findNotebook(notebookId);
        Notebook primaryNotebook = primaryNotebookRepository.findNotebook(notebookId);

        if(primaryNotebook != null) {
            return Response.status(409).build();
        }
        else if (secondaryNotebook != null) {

            // Delete the secondary copy
            primaryNotebookRepository.deleteNotebook(notebookId);
            return Response.ok().build();

        } else {
            return Response.status(404).build();
        }
    }


    @DELETE
    @Path("/secondary/notebook/{notebookId}")
    public Response secondaryDeleteNotebook(@PathParam("notebookId") String notebookId) {

        Client client = new Client();
        Notebook secondaryNotebook = secondaryNotebookRepository.findNotebook(notebookId);

        // Redirect to the primary
        return client.resource(secondaryNotebook.getPrimaryNotebookUrl())
                .path("/notebook/" + secondaryNotebook.getId())
                .delete(Response.class);
    }

    @GET
    @Path("/notebook")
    @Produces(MediaType.TEXT_XML)
    public Response getNotebooks() {
        NotebookList mergedList =
                new NotebookList(primaryNotebookRepository.getNotebooks(), secondaryNotebookRepository.getNotebooks());
        return Response.ok(mergedList).build();
    }

    @GET
    @Path("/notebook/{notebookId}")
    @Produces(MediaType.TEXT_XML)
    public Response getNotebook(@PathParam("notebookId") String notebookId) {

        Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
        if (notebook == null) {

            Notebook secondaryNotebook = secondaryNotebookRepository.findNotebook(notebookId);
            if(secondaryNotebook == null) {
                return Response.status(404).build();
            } else {
                return Response.ok(secondaryNotebook).build();
            }
        } else {
            return Response.ok(notebook).build();
        }
    }


    @GET
    @Path("/notes/{notebookId}")
    @Produces(MediaType.TEXT_XML)
    public Response getNotes(@PathParam("notebookId") String notebookId) {
        return getNotebook(notebookId);
    }

    @GET
    @Path("/notes/{notebookId}/{noteId}")
    @Produces(MediaType.TEXT_XML)
    public Response getNote(@PathParam("notebookId") String notebookId,
                            @PathParam("noteId") String noteId) {

        Note note = primaryNotebookRepository.findNote(notebookId, noteId);
        if (note == null) {

            Note secondaryNote = secondaryNotebookRepository.findNote(notebookId, noteId);
            if(secondaryNote == null) {
                return Response.status(404).build();
            } else {
                return Response.ok(secondaryNote).build();
            }

        } else {
            return Response.ok(note).build();
        }
    }

    @POST
    @Path("/config/secondary/{notebookId}")
    @Produces(MediaType.TEXT_XML)
    public Response configPostSecondaryNotebook(@PathParam("notebookId") String notebookId,
                                                String secondaryUrl) {
        try {
            secondaryServerRepository.add(notebookId, secondaryUrl);
        } catch (NotebookAlreadyExistsException e) {
            return Response.status(409).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/config/secondary/{notebookId}/{secondaryUrl}")
    @Produces(MediaType.TEXT_XML)
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
    @DELETE
    @Path("/secondary/{notebookId}")
    public Response deleteSecondaryNotebook(@PathParam("notebookId") String notebookId) {

        Notebook notebook = secondaryNotebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        } else {

            // Inform the primary server
            Client client = new Client();
            client.resource(notebook.getPrimaryNotebookUrl())
                    .path("/notebook/" + notebook.getId())
                    .delete();

            // Delete the local copy
            secondaryNotebookRepository.deleteNotebook(notebookId);

            return Response.ok().build();
        }
    }


    // Creates a secondary copy of a notebook in the server that receives the request.
    // The secondary server is responsible for notifying the primary that the secondary copy has been created.
    @POST
    @Path("/secondary/{notebookId}")
    @Produces(MediaType.TEXT_XML)
    public Response postSecondaryNotebook(@PathParam("notebookId") String notebookId) {
        try {

            // Make sure we're not already a primary or secondary server
            if (primaryNotebookRepository.findNotebook(notebookId) != null
                    || secondaryNotebookRepository.findNotebook(notebookId) != null) {
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
            Client client = Client.create();
            Notebook notebook = client
                    .resource(primaryNotebookUrl)
                    .path("/notebook/" + notebookFromDirectory.getId())
                    .get(Notebook.class);

            // Add complete noteboook to our secondary repository
            secondaryNotebookRepository.add(notebook);

            // Inform primary server that we're now a secondary server:
            //     PUT {primaryUrl}/config/secondary/{notebookId}
            //      {secondaryUrl}
            client.resource(primaryNotebookUrl)
                    .path("/config/secondary/" + notebookId)
                    .post(getSelfHostPort());

            return Response.ok().build();

        } catch (NamingException e) {
            return Response.status(400).build();
        }
    }

    @POST
    @Path("/notebook")
    @Produces(MediaType.TEXT_XML)
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


    @PUT
    @Path("/secondaryNote/{notebookId}/{noteId}")
    @Produces(MediaType.TEXT_XML)
    public Response putSecondaryNote(@PathParam("notebookId") String notebookId,
                                     @PathParam("noteId") String noteId,
                                     Note note) throws ServletException, IOException {

        // The request content must be a <note> element containing only a <content> element.
        if (note.getContent() == null
                || noteId == null) {
            return Response.status(400).build();
        }
        Notebook notebookForWhereWeAreASecondary = secondaryNotebookRepository.findNotebook(notebookId);
        notebookForWhereWeAreASecondary.createNote(note.getContent(),note.getId());
        return Response.ok().build();
    }




    @PUT
    @Path("/notes/{notebookId}/{noteId}")
    @Produces(MediaType.TEXT_XML)
    public Response postNote(@PathParam("notebookId") String notebookId,
                             @PathParam("noteId") String noteId,
                             Note note) throws ServletException, IOException {

        // The request content must be a <note> element containing only a <content> element.
        if (note.getContent() == null
                || noteId == null) {
            return Response.status(400).build();
        }
        return postOrPutNote(notebookId, note, noteId);
    }

    @POST
    @Path("/notes/{notebookId}")
    @Produces(MediaType.TEXT_XML)
    public Response postNote(@PathParam("notebookId") String notebookId,
                             Note note) throws ServletException, IOException {

        // The request content must be a <note> element containing only a <content> element.
        if (note.getContent() == null
                || note.getId() != null) {
            return Response.status(400).build();
        }
        return postOrPutNote(notebookId, note, null);
    }

    private Response postOrPutNote(String notebookId, Note note, String noteId) throws ServletException, IOException {

        Client client = Client.create();

        // If a secondary server for the notebook receives this request, it should re-submit it to the
        // notebook's primary server
        Notebook notebookForWhereWeAreASecondary = secondaryNotebookRepository.findNotebook(notebookId);
        if (notebookForWhereWeAreASecondary != null) {

            // We are a secondary server
            // Forward the request to the primary server

            String primaryUri = notebookForWhereWeAreASecondary.getPrimaryNotebookUrl();

            if(noteId == null) {
                Response response = client.resource(primaryUri)
                        .path("/notes/" + notebookId)
                        .entity(note)
                        .type(MediaType.TEXT_XML)
                        .accept(MediaType.TEXT_XML).accept(MediaType.APPLICATION_XML)
                        .post(Response.class);
                return response;
            } else {
                Response response = client.resource(primaryUri)
                        .path("/notes/" + notebookId + "/" + noteId)
                        .entity(note)
                        .type(MediaType.TEXT_XML)
                        .accept(MediaType.TEXT_XML).accept(MediaType.APPLICATION_XML)
                        .put(Response.class);
                return response;
            }

        } else {

            // We are a primary server
            // Create the note in the given notebook

            Notebook notebook = primaryNotebookRepository.findNotebook(notebookId);
            if (notebook == null) {
                return Response.status(404).build();
            }
            Note newNote;
            if(noteId == null) {
                newNote = notebook.createNote(note.getContent());
            } else {
                newNote = notebook.createNote(note.getContent(), noteId);
            }

            // When a note is created, the notebook's primary server is responsible for informing any
            // secondary copies about the new note. Your team is responsible for designing a way to make this happen.
            List<String> secondaries = secondaryServerRepository.getServersForNotebook(notebookId);
            if(secondaries != null) {
                for (String secondary : secondaries) {
                    client.resource(secondary)
                            .path("/secondaryNote/" + notebookId + "/" + newNote.getId())
                            .entity(newNote)
                            .type("text/xml")
                            .put();
                }
            }

            // The response is the new note, including the noteId assigned by the primary server.
            return Response.ok(newNote).type(MediaType.TEXT_XML).build();

        }
    }


    @GET
    @Path("/config/self")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSelfHostPortApi() {
        return Response.ok(getSelfHostPort()).build();
    }


    @PUT
    @Path("/config/self/{hostport}")
    @Produces(MediaType.TEXT_XML)
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
    @Produces(MediaType.TEXT_XML)
    public Response setJndiHostPort(@PathParam("hostport") String hostport) {
        directoryFactory.setJndiHostPort(hostport);
        return Response.ok().build();
    }

}
