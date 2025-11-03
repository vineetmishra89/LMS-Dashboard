package com.example.lms.dto;

import java.time.OffsetDateTime;

/**
 * DTO representing a file in SharePoint/OneDrive.
 * Contains file metadata including name, path, size, and URLs.
 */
public class FileNode {
    private String name;
    private String path;
    private String webUrl;
    private Long size;
    private OffsetDateTime lastModified;

    public FileNode() {
    }

    public FileNode(String name, String path, String webUrl, Long size, OffsetDateTime lastModified) {
        this.name = name;
        this.path = path;
        this.webUrl = webUrl;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
