package com.alonglab.he.filesystem.service;

import com.alonglab.he.filesystem.domain.FileCategory;
import com.alonglab.he.filesystem.domain.FileInfo;

import java.io.File;
import java.util.Map;

public interface FileProcessor {
    void handleFile(File file, FileCategory category);
    void rehandleFile(File file, FileCategory category, Map<String, FileInfo> existedFileInfo);
}
