package edu.scu.coen317.common.message.server;

import edu.scu.coen317.common.message.MessageType;

public class ServerGetResponse {
    public static final MessageType TYPE = MessageType.QUORUM_GET_REPLY;

    private String val;

    public ServerGetResponse(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
