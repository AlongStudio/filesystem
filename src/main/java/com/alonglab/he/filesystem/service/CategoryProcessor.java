package com.alonglab.he.filesystem.service;

public interface CategoryProcessor {
    void checkCategoryFileDuplicate(long oldCategoryId, long newCategoryId);
}
