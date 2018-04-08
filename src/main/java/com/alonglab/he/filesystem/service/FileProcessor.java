package com.alonglab.he.filesystem.service;

import com.alonglab.he.filesystem.domain.FileCategory;

import java.io.File;

public interface FileProcessor {
    void handleFile(File file, FileCategory category);
    void rehandleFile(File file, FileCategory category);
}
