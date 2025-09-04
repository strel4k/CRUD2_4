package com.crudapp.filestorage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "files")
public class FileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "file", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Event> events = new LinkedHashSet<>();

    public FileEntity() {}
    public FileEntity(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getFilePath() { return filePath; }
    public void setName(String name) { this.name = name; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<Event> getEvents() { return events; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileEntity)) return false;
        FileEntity that = (FileEntity) o;
        return id != null && id.equals(that.id);
    }
    @Override public int hashCode() { return Objects.hashCode(id); }
}