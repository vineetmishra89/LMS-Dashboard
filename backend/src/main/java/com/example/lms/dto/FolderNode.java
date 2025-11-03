package com.example.lms.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a folder in SharePoint/OneDrive with its contents.
 * Contains folder metadata and lists of child folders and files.
 */
public class FolderNode {
    private String name;
    private String path;
    private String webUrl;
    private List<FileNode> files;
    private List<FolderNode> folders;

    public FolderNode() {
        this.files = new ArrayList<>();
        this.folders = new ArrayList<>();
    }

    public FolderNode(String name, String path, String webUrl) {
        this.name = name;
        this.path = path;
        this.webUrl = webUrl;
        this.files = new ArrayList<>();
        this.folders = new ArrayList<>();
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

    public List<FileNode> getFiles() {
        return files;
    }

    public void setFiles(List<FileNode> files) {
        this.files = files;
    }

    public List<FolderNode> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderNode> folders) {
        this.folders = folders;
    }

    public void addFile(FileNode file) {
        this.files.add(file);
    }

    public void addFolder(FolderNode folder) {
        this.folders.add(folder);
    }
}
