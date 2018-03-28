package com.alonglab.he.filesystem.repository;

import com.alonglab.he.filesystem.domain.FileInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileInfoRepository extends CrudRepository<FileInfo, Long> {
    List<FileInfo> findAllByCategory_Id(long categoryId);
}
