package com.crudapp.filestorage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    @JsonIgnore
    private FileEntity file;


    @Column(name = "event_type", length = 16)
    private String eventType;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Transient
    @JsonProperty("userId")
    public Long getUserId() { return user != null ? user.getId() : null; }

    @Transient
    @JsonProperty("fileId")
    public Long getFileId() { return file != null ? file.getId() : null; }

    public Event() {}
    public Event(User user, FileEntity file, String eventType) {
        this.user = user;
        this.file = file;
        this.eventType = eventType;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public FileEntity getFile() { return file; }
    public String getEventType() { return eventType; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setFile(FileEntity file) { this.file = file; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event that = (Event) o;
        return id != null && id.equals(that.id);
    }
    @Override public int hashCode() { return Objects.hashCode(id); }
}
