package com.alonglab.he.filesystem.service.impl;

import com.alonglab.he.filesystem.domain.FileCategory;
import com.alonglab.he.filesystem.domain.FileInfo;
import com.alonglab.he.filesystem.repository.FileCategoryRepository;
import com.alonglab.he.filesystem.repository.FileInfoRepository;
import com.alonglab.he.filesystem.service.CategoryProcessor;
import com.alonglab.he.filesystem.service.FileProcessor;
import com.alonglab.he.filesystem.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatrgoryProcessorImpl implements CategoryProcessor {
    @Autowired
    private FileCategoryRepository fileCategoryRepository;
    @Autowired
    private FileProcessor fileProcessor;
    @Autowired
    private FileInfoRepository fileInfoRepository;
    private static final Logger logger = LoggerFactory.getLogger(FileProcessorImpl.class);

    /**
     * 比较两个Category并标记精确重复的FileInfo
     *
     * @param oldCategoryId
     * @param newCategoryId
     */
    @Override
    public void checkCategoryFileDuplicate(long oldCategoryId, long newCategoryId) {
        Map<String, FileInfo> oldFiles = loadOldFiles(oldCategoryId);
        List<FileInfo> newFileList = fileInfoRepository.findAllByCategory_Id(newCategoryId);

        for (FileInfo fileInfo : newFileList) {
            if (fileInfo.getStatus() == FileInfo.FILE_STATUS_MISSING) {
                continue;
            }
            String key = generateExactDuplicateKey(fileInfo);
            if (oldFiles.containsKey(key)) {
                fileInfo.setStatus(FileInfo.FILE_STATUS_EXACT_DUPLICATE);
                fileInfo.setComments(oldFiles.get(key).getFullPath());
                fileInfoRepository.save(fileInfo);
            }
        }
    }

    /**
     * 将FileInfoList转换为map用于比较
     *
     * @param oldCategoryId
     * @return
     */
    private Map<String, FileInfo> loadOldFiles(long oldCategoryId) {
        List<FileInfo> oldFileList = fileInfoRepository.findAllByCategory_Id(oldCategoryId);
        logger.debug("list size = " + oldFileList.size());
        Map<String, FileInfo> oldFiles = new HashMap<>();
        oldFileList.forEach(fileInfo ->
                oldFiles.put(generateExactDuplicateKey(fileInfo), fileInfo)
        );
        logger.debug("map size = " + oldFiles.size());
        //Assert.isTrue(oldFileList.size() == oldFiles.size(), "old file category should have no duplicated file!");
        return oldFiles;
    }

    private String generateExactDuplicateKey(FileInfo fileInfo) {
        return fileInfo.getFileName() + "#" + fileInfo.getFileLength() + "#" + fileInfo.getMd5();
    }

    /**
     * 删除完全重复的文件(name\length\md5均重复)并将FileInfo更新为已删除。不清理FileInfo
     */
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

    /**
     * 刷新category。仅更新FileInfo和FileCategory至真实状态，不处理真实文件
     */
    @Override
    public String refreshCategory(long categoryId) {
        FileCategory category = fileCategoryRepository.findOne(categoryId);
        Map<String, Integer> result = new HashMap<>();
        checkDeletedFileInfo(category, result);
        checkUnDeletedFileInfo(category, result);
        reindexCategory(category, result);

        fileCategoryRepository.save(category);
        return extractRefreshResult(result);
    }

    /**
     * 检查Status为Deleted的FileInfo，确实已删除的则删掉当前FileInfo
     *
     * @param category
     * @param result
     */
    private void checkDeletedFileInfo(FileCategory category, Map<String, Integer> result) {
        List<FileInfo> deletedList = fileInfoRepository.findAllByCategory_IdAndStatus(category.getId(), FileInfo.FILE_STATUS_DELETED);
        int listSize = deletedList.size();
        int errorNumber = 0;
        int deleteNumber = 0;
        for (FileInfo deletedFile : deletedList) {
            File f = new File(deletedFile.getFullPath());
            if (f.exists()) {
                deletedFile.setStatus(FileInfo.FILE_STATUS_ERROR);
                errorNumber++;
                fileInfoRepository.save(deletedFile);
            } else {
                category.setFileNum(category.getFileNum() - 1);
                category.setFolderSize(category.getFolderSize() - deletedFile.getFileLength());
                deleteNumber++;
                fileInfoRepository.delete(deletedFile.getId());
            }
        }

        result.put("ShouldDeletedSize", listSize);
        result.put("DeletedErrorNumber", errorNumber);
        result.put("RealDeleteNumber", deleteNumber);
    }

    /**
     * 检查所有Status不是删除的FileInfo，对应的文件应存在，不存在的标记为Missing,然后可以手动review后调用recheckMissing方法删除记录（TODO）
     *
     * @param category
     * @param result
     */
    private void checkUnDeletedFileInfo(FileCategory category, Map<String, Integer> result) {
        List<FileInfo> allFileInfoList = fileInfoRepository.findAllByCategory_Id(category.getId());
        int listSize = allFileInfoList.size();
        int missingNum = 0;
//        int updated = 0;
        for (FileInfo fileInfo : allFileInfoList) {
            if (fileInfo.getStatus() == FileInfo.FILE_STATUS_DELETED) {
                continue;
            }
            File f = new File(fileInfo.getFullPath());
            if (!f.exists()) {
                missingNum++;
                fileInfo.setStatus(FileInfo.FILE_STATUS_MISSING);
                fileInfoRepository.save(fileInfo);
            } else {
                //TODO 更新文件信息，暂时不需要。
            }
        }
        result.put("CheckMissingFileListSize", listSize);
        result.put("CheckMissingResult", missingNum);
    }

    private void reindexCategory(FileCategory category, Map<String, Integer> result) {
        File folder = new File(category.getFolderPath());
        int fileNum1 = category.getFileNum();
        List<FileInfo> oldFileList = fileInfoRepository.findAllByCategory_Id(category.getId());
        logger.debug("list size = " + oldFileList.size());
        Map<String, FileInfo> existedFileInfo = new HashMap<>();
        oldFileList.forEach(fileInfo ->
                existedFileInfo.put(fileInfo.getFullPath(), fileInfo)
        );
        fileProcessor.rehandleFile(folder, category, existedFileInfo);
        int fileNum2 = category.getFileNum();
        result.put("NewIndexNumber", fileNum2 - fileNum1);
    }

    private String extractRefreshResult(Map<String, Integer> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("应被删除数：" + result.get("ShouldDeletedSize") + "\n");
        sb.append("实际删除数：" + result.get("RealDeleteNumber") + "\n");
        sb.append("删除出错数：" + result.get("DeletedErrorNumber") + "\n");
        sb.append("检查文件是否存在数：" + result.get("CheckMissingFileListSize") + "\n");
        sb.append("实际文件已不存在数：" + result.get("CheckMissingResult") + "\n");
        sb.append("新索引文件数：" + result.get("NewIndexNumber") + "\n");
        return sb.toString();
    }

    /**
     * 检查同一个category中的文件重复信息
     * 只根据本category中的文件信息进行比较处理，不处理本category中文件与其他category文件相同或相似的问题
     * 由于文件大多按照日期或活动归类到不同的文件夹进行存储，所以本功能只处理同文件夹下的文件查重，跨文件夹的不处理。
     * 典型的情况是下面这种同文件夹下的重复，除了文件名不同，其他都相同：
     * /Users/victor/Documents/照片/我的照片/2015年11月/2015-11-21 175756.jpg
     * /Users/victor/Documents/照片/我的照片/2015年11月/2015-11-21 175756(1).jpg
     *
     * @param categoryId
     */
    @Override
    public String checkCurrentCategoryFileDuplicated(long categoryId) {
        List<FileInfo> fileInfoList = fileInfoRepository.findAllByCategory_Id(categoryId);
        Map<String, List<FileInfo>> allFiles = new HashMap<>(); //<fileParentPath,fileInfoList>
        //首先根据目录进行fileInfo分组
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getStatus() != FileInfo.FILE_STATUS_NEWINDEX) {
                //其他状态不处理，降低本功能的影响，只处理最典型的情况
                continue;
            }
            File f = new File(fileInfo.getFullPath());
            if (!f.exists()) {
                continue;
            }
            String parent = f.getParent();
            List<FileInfo> infoList = allFiles.get(parent);
            if (infoList == null) {
                infoList = new ArrayList<>();
                allFiles.put(parent, infoList);
            }
            infoList.add(fileInfo);
        }
        return "";
    }

    /**
     * fileName很可能相似、但不同
     *
     * @param fileInfo
     * @return
     */
    private String generateDuplicateKey(FileInfo fileInfo) {
        return fileInfo.getFileLength() + "#" + fileInfo.getMd5();
    }
}
