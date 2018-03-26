package com.alonglab.he.filesystem.restful;

import com.alonglab.he.filesystem.domain.FileCategory;
import com.alonglab.he.filesystem.domain.FileInfo;
import com.alonglab.he.filesystem.repository.FileCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("FileResource")
@RequestMapping("/file")
public class FileResource {
    @Autowired
    private FileCategoryRepository fileCategoryRepository;

    @RequestMapping(value = "/test0",method = RequestMethod.GET)
    public @ResponseBody String testSave(){
        FileCategory fileCategory = new FileCategory();
        fileCategory.setCode("test");
        fileCategory.setName("test");
        fileCategory.setDescription("test");
        fileCategoryRepository.save(fileCategory);
        return ""+fileCategory.getId();
    }
}
