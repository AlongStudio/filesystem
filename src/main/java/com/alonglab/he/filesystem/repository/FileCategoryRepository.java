package com.alonglab.he.filesystem.repository;

import com.alonglab.he.filesystem.domain.FileCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileCategoryRepository extends CrudRepository<FileCategory, Long> {

}
