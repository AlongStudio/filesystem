package com.alonglab.he.filesystem.dto;

public class CheckInput {
    private long oldCategoryId;
    private long newCategoryId;

    public long getOldCategoryId() {
        return oldCategoryId;
    }

    public void setOldCategoryId(long oldCategoryId) {
        this.oldCategoryId = oldCategoryId;
    }

    public long getNewCategoryId() {
        return newCategoryId;
    }

    public void setNewCategoryId(long newCategoryId) {
        this.newCategoryId = newCategoryId;
    }
}
