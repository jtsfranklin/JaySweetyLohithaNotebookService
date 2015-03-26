package domain;

import dino.api.NotebookAlreadyExistsException;
import dino.api.NotebookNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jay on 3/22/2015.
 */
public class SecondaryServerRepository {

    // Maps a notebookID to a list of secondary URLs
    private HashMap<String, List<String>> map = new HashMap<String, List<String>>();

    public void add(String notebookId, String secondaryUrl) throws NotebookAlreadyExistsException {
        List<String> serverList = map.get(notebookId);
        if (serverList == null) serverList = new ArrayList<String>();
        if (serverList.contains(secondaryUrl)) {
            throw new NotebookAlreadyExistsException();
        }
        serverList.add(secondaryUrl);
        map.put(notebookId, serverList);
    }

    public void delete(String notebookId, String secondaryUrl) throws NotebookNotFoundException {
        List<String> serverList = map.get(notebookId);
        if (serverList == null) serverList = new ArrayList<String>();
        if (!serverList.contains(secondaryUrl)) {
            throw new NotebookNotFoundException();
        }
        serverList.remove(secondaryUrl);
        map.put(notebookId, serverList);
    }

    public List<String> getServersForNotebook(String notebookId) {
        return (List<String>)map.get(notebookId);
    }
}
