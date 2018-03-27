package com.alonglab.he.filesystem.service;

import com.alonglab.he.filesystem.domain.FileCategory;

import java.io.File;

public interface FileProcessor {
    public void handleFile(File file, FileCategory category);
}
