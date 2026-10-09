package com.muxcraft.common;

import java.util.List;

/**
 * 统一分页响应结构
 *
 * @param <T> 记录类型
 */
public class PageResult<T> {

    private List<T> records;
    private int page;
    private int size;
    private long total;
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> records, int page, int size, long total) {
        this.records = records;
        this.page = page;
        this.size = size;
        this.total = total;
        this.pages = (int) Math.ceil((double) total / size);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}