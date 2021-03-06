package com.alonglab.he.filesystem.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_fs_file_info")
public class FileInfo implements Serializable {

    public static final int FILE_STATUS_NEWINDEX = 1;
    public static final int FILE_STATUS_DUPLICATE = 2;
    public static final int FILE_STATUS_EXACT_DUPLICATE = 3;
    public static final int FILE_STATUS_DELETED = 4;
    public static final int FILE_STATUS_ERROR = 5;
    public static final int FILE_STATUS_MISSING = 6;
    public static final int FILE_STATUS_UPDATED = 7;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "full_path")
    private String fullPath;
    @Column(name = "file_length")
    private long fileLength;
    @Column(name = "last_modified")
    private Date lastModified;
    @Column(name = "md5")
    private String md5;
    @Column(name = "insert_time")
    private Date insertTime;
    @Column(name = "status")
    private int status;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @Fetch(value = FetchMode.SELECT)
    private FileCategory category;
    @Column(name = "comments")
    private String comments;
    @Column(name = "duplicated_with_id")
    private Long duplicatedWithId;

    @PrePersist
    protected void prePersist() {
        insertTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDuplicatedWithId() {
        return duplicatedWithId;
    }

    public void setDuplicatedWithId(Long duplicatedWithId) {
        this.duplicatedWithId = duplicatedWithId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public FileCategory getCategory() {
        return category;
    }

    public void setCategory(FileCategory category) {
        this.category = category;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
