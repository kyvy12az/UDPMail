package com.udpmail.common.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Request {
    private ActionType action;
    private String requestId;
    private Map<String, Object> data = new HashMap<>();

    public Request() {}
    public Request(ActionType action, Map<String, Object> data) {
        this.action = action;
        this.requestId = UUID.randomUUID().toString();
        this.data = data == null ? new HashMap<>() : new HashMap<>(data);
    }
    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public String string(String key) { Object value = data.get(key); return value == null ? null : value.toString(); }
}
