package com.example.getoffer.common;

import java.util.List;

import org.springframework.data.domain.Page;

public class PageResult<T> {

    private List<T> list;
    private int page;
    private int pageSize;
    private long total;
    private boolean hasMore;

    public static <T> PageResult<T> from(Page<T> pageData, int page, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(pageData.getContent());
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotal(pageData.getTotalElements());
        result.setHasMore(pageData.getTotalPages() > page);
        return result;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
