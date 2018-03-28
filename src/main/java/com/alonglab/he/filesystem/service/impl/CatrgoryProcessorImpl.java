package com.alonglab.he.filesystem.service.impl;

import com.alonglab.he.filesystem.domain.FileInfo;
import com.alonglab.he.filesystem.repository.FileCategoryRepository;
import com.alonglab.he.filesystem.repository.FileInfoRepository;
import com.alonglab.he.filesystem.service.CategoryProcessor;
import com.alonglab.he.filesystem.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatrgoryProcessorImpl implements CategoryProcessor {
    @Autowired
    private FileCategoryRepository fileCategoryRepository;
    @Autowired
    private FileInfoRepository fileInfoRepository;
    private static final Logger logger = LoggerFactory.getLogger(FileProcessorImpl.class);

    @Override
    public void checkCategoryFileDuplicate(long oldCategoryId, long newCategoryId) {
        Map<String, FileInfo> oldFiles = loadOldFiles(oldCategoryId);
        List<FileInfo> newFileList = fileInfoRepository.findAllByCategory_Id(newCategoryId);

        for (FileInfo fileInfo : newFileList) {
            String key = generateKey(fileInfo);
            if (oldFiles.containsKey(key)) {
                fileInfo.setStatus(FileInfo.FILE_STATUS_EXACT_DUPLICATE);
                fileInfo.setComments(oldFiles.get(key).getFullPath());
                fileInfoRepository.save(fileInfo);
            }
        }
    }

    private Map<String, FileInfo> loadOldFiles(long oldCategoryId) {
        List<FileInfo> oldFileList = fileInfoRepository.findAllByCategory_Id(oldCategoryId);
        logger.debug("list size = " + oldFileList.size());
        Map<String, FileInfo> oldFiles = new HashMap<>();
        oldFileList.forEach(fileInfo ->
                oldFiles.put(generateKey(fileInfo), fileInfo)
        );
        logger.debug("map size = " + oldFiles.size());
        Assert.isTrue(oldFileList.size() == oldFiles.size(), "old file category should have no duplicated file!");
        return oldFiles;
    }

    private String generateKey(FileInfo fileInfo) {
        return fileInfo.getFileName() + "#" + fileInfo.getFileLength() + "#" + fileInfo.getMd5();
    }

    @Override
    public void cleanCategory(long categoryId) {
        List<FileInfo> fileInfoList = fileInfoRepository.findAllByCategory_IdAndStatus(categoryId, FileInfo.FILE_STATUS_EXACT_DUPLICATE);
        fileInfoList.forEach(fileInfo -> {
            logger.info("deal with {},size = {}", fileInfo.getFullPath(), fileInfo.getFileLength());
            File fileToBeDeleted = new File(fileInfo.getFullPath());
            File fileSame = new File(fileInfo.getComments());
            boolean isSame = FileUtil.checkTotallySame(fileToBeDeleted, fileSame);
            if (isSame) {
                boolean deleted = fileToBeDeleted.delete();
                if (deleted) {
                    fileInfo.setStatus(FileInfo.FILE_STATUS_DELETED);
                    fileInfoRepository.save(fileInfo);
                    logger.info(fileInfo.getFileName() + " deleted!");
                }
            }
        });
    }
}
