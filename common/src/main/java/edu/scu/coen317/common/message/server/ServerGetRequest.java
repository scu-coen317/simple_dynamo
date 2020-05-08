package edu.scu.coen317.common.message.server;

import edu.scu.coen317.common.message.MessageType;

public class ServerGetRequest {
    public static final MessageType TYPE = MessageType.QUORUM_GET;

    private String key;

    public ServerGetRequest(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
