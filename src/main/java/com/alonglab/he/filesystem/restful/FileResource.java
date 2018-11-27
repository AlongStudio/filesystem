package com.alonglab.he.filesystem.restful;

import com.alonglab.he.filesystem.domain.FileCategory;
import com.alonglab.he.filesystem.domain.FileInfo;
import com.alonglab.he.filesystem.dto.CheckInput;
import com.alonglab.he.filesystem.dto.IndexInput;
import com.alonglab.he.filesystem.local.RecordFilesProcessor;
import com.alonglab.he.filesystem.repository.FileCategoryRepository;
import com.alonglab.he.filesystem.repository.FileInfoRepository;
import com.alonglab.he.filesystem.service.CategoryProcessor;
import com.alonglab.he.filesystem.service.FileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Controller("FileResource")
@RequestMapping("/file")
public class FileResource {
    @Autowired
    private FileCategoryRepository fileCategoryRepository;
    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private FileProcessor fileProcessor;
    @Autowired
    private CategoryProcessor categoryProcessor;

    @RequestMapping(value = "/test0", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    String testSave() {
        int random = (int) (Math.random() * 10000000);
        FileCategory fileCategory = new FileCategory();
        fileCategory.setCode("test" + random);
        fileCategory.setName("test" + random);
        fileCategory.setDescription("test");
        fileCategoryRepository.save(fileCategory);

        for (int i = 0; i < 10; i++) {
            FileInfo info = new FileInfo();
            info.setCategory(fileCategory);
            info.setFileLength(i);
            info.setFileName("abc" + i);
            info.setFullPath("c://" + i);
            info.setMd5("md" + i);
            fileInfoRepository.save(info);
        }
        return "" + fileCategory.getId();
    }

    @RequestMapping(value = "/index", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public @ResponseBody
    String index(@RequestBody IndexInput input) {

        FileCategory fileCategory = new FileCategory();
        fileCategory.setCode(input.getCode());
        fileCategory.setName(input.getName());
        fileCategory.setFolderPath(input.getIndexPath());
        fileCategory.setDescription(input.getDescription());
        fileCategoryRepository.save(fileCategory);

        File folder = new File(fileCategory.getFolderPath());
        fileProcessor.handleFile(folder, fileCategory);
        fileCategoryRepository.save(fileCategory);
        return "" + fileCategory.getId();
    }

    @RequestMapping(value = "/check/duplicate", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public @ResponseBody
    String checkCategoryDuplicate(@RequestBody CheckInput input) {
        categoryProcessor.checkCategoryFileDuplicate(input.getOldCategoryId(), input.getNewCategoryId());
        return "Done";
    }

    @RequestMapping(value = "/clean/{categoryId}", method = RequestMethod.DELETE)
    @Transactional
    public @ResponseBody
    String deleteCategory(@PathVariable("categoryId") long categoryId) {
        categoryProcessor.cleanCategory(categoryId);
        return "Done";
    }

    @RequestMapping(value = "/refresh/{categoryId}", method = RequestMethod.PUT)
    @Transactional
    public @ResponseBody
    String refreshCategory(@PathVariable("categoryId") long categoryId) {
        String result = categoryProcessor.refreshCategory(categoryId);
        return result;
    }

    @RequestMapping(value = "/check/current/duplicate/{categoryId}", method = RequestMethod.PUT)
    @Transactional
    public @ResponseBody
    String checkCurrentCategoryFileDuplicated(@PathVariable("categoryId") long categoryId) {
        String result = categoryProcessor.checkCurrentCategoryFileDuplicated(categoryId);
        return result;
    }
}
