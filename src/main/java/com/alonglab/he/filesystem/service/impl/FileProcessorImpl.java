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

@Service
public class FileProcessorImpl implements FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessorImpl.class);

    @Autowired
    private FileInfoRepository fileInfoRepository;

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

    private void fileProcess(File file, FileCategory category) {
        FileInfo info = new FileInfo();
        info.setCategory(category);
        long size = file.length();
        info.setFileLength(size);
        info.setFileName(file.getName());
        info.setFullPath(file.getAbsolutePath());

        try {
            logger.info(file.getAbsolutePath());
            String md5 = MD5Util.getMD5(size, new FileInputStream(file));
            info.setMd5(md5);
            info.setStatus("NewIndex");
            fileInfoRepository.save(info);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("file process error:", e);
        }
    }
}
