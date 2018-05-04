package com.alonglab.he.filesystem.service.impl;

import com.alonglab.he.filesystem.domain.FileCategory;
import com.alonglab.he.filesystem.domain.FileInfo;
import com.alonglab.he.filesystem.repository.FileInfoRepository;
import com.alonglab.he.filesystem.service.FileProcessor;
import com.alonglab.he.filesystem.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Service
public class FileProcessorImpl implements FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessorImpl.class);

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Override
    public void handleFile(File file, FileCategory category) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                handleFile(f, category);
            }
        } else if (file.isFile()) {
            fileProcess(file, category);
        } else {
            logger.error("++ERROR FILE:" + file.getAbsolutePath() + "+++++");
        }

    }

    @Override
    public void rehandleFile(File file, FileCategory category, Map<String, FileInfo> existedFileInfo) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                rehandleFile(f, category, existedFileInfo);
            }
        } else if (file.isFile()) {
            String fullPath = file.getAbsolutePath();
//            FileInfo fileInfo = fileInfoRepository.findByCategoryAndFullPath(category, fullPath);
            FileInfo fileInfo = existedFileInfo.get(fullPath);
            if (fileInfo == null) {
                FileInfo newFileInfo = fileProcess(file, category);
                if(newFileInfo != null){
                    existedFileInfo.put(fullPath, newFileInfo);
                }
            }
        } else {
            logger.error("++ERROR FILE:" + file.getAbsolutePath() + "+++++");
        }

    }

    private FileInfo fileProcess(File file, FileCategory category) {
        FileInfo info = new FileInfo();
        info.setCategory(category);
        long size = file.length();
        info.setFileLength(size);
        info.setFileName(file.getName());
        info.setFullPath(file.getAbsolutePath());
        long lastModified0 = file.lastModified();
        info.setLastModified(new Date(lastModified0));

        try {
            logger.info(file.getAbsolutePath());
            String md5 = MD5Util.getMD5(size, new FileInputStream(file));
            info.setMd5(md5);
            info.setStatus(FileInfo.FILE_STATUS_NEWINDEX);
            fileInfoRepository.save(info);
            category.setFileNum(category.getFileNum() + 1);
            category.setFolderSize(category.getFolderSize() + info.getFileLength());
            return info;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("file process error:", e);
            return null;
        }
    }
}
