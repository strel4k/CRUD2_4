package com.crudapp.filestorage.dto;

public class EventUpdateRequest {
    private String eventType;

    public EventUpdateRequest() {}

    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
