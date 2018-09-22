package com.bsuir.spolks.util;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private String currentClientUUID = "";

    Map<String, Map<String, Integer>> clientsUUID = new HashMap<>();

    public void setClientUUID(String uuid){
        if(!clientsUUID.containsKey(uuid)){
            clientsUUID.put(uuid, null);
        }
        currentClientUUID = uuid;
    }

    public void deleteCurrentClient(){
        clientsUUID.remove(currentClientUUID);
    }

    public void updateFileProgress(String fileName, int length){
        HashMap<String, Integer> fileIndo = new HashMap<>();
        fileIndo.put(fileName, length);
        clientsUUID.put(currentClientUUID, fileIndo);
    }

    public Integer getFileProgress(String line){
        return (clientsUUID.get(currentClientUUID) == null)? 0 : clientsUUID.get(currentClientUUID).get(line);
    }
}
