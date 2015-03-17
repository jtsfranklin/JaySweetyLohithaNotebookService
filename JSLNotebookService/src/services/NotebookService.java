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
            selfHostport = uri.getBaseUri().getHost() + ":" + uri.getBaseUri().getPort();
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
        return Response.ok(note).build();
    }


    @POST
    @Path("/notebook")
    @Produces(MediaType.APPLICATION_XML)
    public Response postNotebook(@PathParam("title") String title) throws NamingException {
        try {
            Directory directory = directoryFactory.Create();
            String notebookId = directory.createNotebook(title, getSelfHostPort());
            Notebook notebook = new Notebook();
            notebook.setTitle(title);
            notebook.setId(notebookId);
            notebookRepository.add(notebook);
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
                             String content) {
        Notebook notebook = notebookRepository.findNotebook(notebookId);
        if (notebook == null) {
            return Response.status(404).build();
        }
        Note note = notebook.createNote(content);
        return Response.ok(note).build();
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
