package com.alonglab.he.filesystem.service;

public interface CategoryProcessor {
    void checkCategoryFileDuplicate(long oldCategoryId, long newCategoryId);
    void cleanCategory(long categoryId);
    String refreshCategory(long categoryId);
    String checkCurrentCategoryFileDuplicated(long categoryId);
}
