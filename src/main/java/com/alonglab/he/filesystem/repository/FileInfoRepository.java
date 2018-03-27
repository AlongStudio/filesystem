package com.alonglab.he.filesystem.repository;

import com.alonglab.he.filesystem.domain.FileInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends CrudRepository<FileInfo, Long> {

}
