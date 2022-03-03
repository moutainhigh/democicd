package com.uwallet.pay.core.common;

import java.io.Serializable;

public class PagingContext implements Serializable {
    private static final long serialVersionUID = -5707123660530858477L;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PAGE_WINDOW = 10;
    public static final PagingContext DEFAULT = new PagingContext();
    private int pageIndex;
    private int pageSize;
    private int pageWindow;
    private int maxPages;
    private int total;
    private int startIndex;

    public PagingContext() {
        this.pageIndex = 1;
        this.pageSize = 20;
        this.setPageWindow(10);
        this.maxPages = 1;
    }

    public PagingContext(PagingContext context) {
        this.pageIndex = context.getPageIndex();
        this.pageSize = context.getPageSize();
        this.pageWindow = context.getPageWindow();
        this.total = context.getTotal();
        this.maxPages = context.getMaxPages();
        this.startIndex = context.getStartIndex();
        this.calculatePages();
    }

    private void calculatePages() {
        if (this.pageSize > 0) {
            if (this.total % this.pageSize == 0) {
                this.maxPages = this.total / this.pageSize;
            } else {
                this.maxPages = this.total / this.pageSize + 1;
            }
        }

    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.calculatePages();
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(int p) {
        if (p >= this.maxPages) {
            this.pageIndex = this.maxPages;
        } else if (p <= 1) {
            this.pageIndex = 1;
        } else {
            this.pageIndex = p;
        }

    }

    public int getMaxPages() {
        return this.maxPages;
    }

    public void setMaxPages(int i) {
        this.maxPages = i;
        this.calculatePages();
    }

    public void setPageWindow(int pageWindow) {
        this.pageWindow = pageWindow;
    }

    public int getPageWindow() {
        return this.pageWindow;
    }

    public int getPreviousPage() {
        return this.pageIndex > 1 ? this.pageIndex - 1 : 0;
    }

    public int getNextPage() {
        return this.pageIndex < this.maxPages ? this.pageIndex + 1 : 0;
    }

    public int getStartPageIndex() {
        return this.pageIndex > this.pageWindow / 2 ? this.pageIndex - this.pageWindow / 2 : 1;
    }

    public int getEndPageIndex() {
        int spi = this.getStartPageIndex();
        return spi + this.pageWindow < this.maxPages ? spi + this.pageWindow : this.getMaxPages();
    }

    public void setTotal(int total) {
        this.total = total;
        this.calculatePages();
    }

    public int getTotal() {
        return this.total;
    }

    public int getStartIndex() {
        int i = (this.pageIndex - 1) * this.pageSize;
        return i > 0 ? i : 0;
    }

    public int getEndIndex() {
        int i = this.pageIndex * this.pageSize;
        return i > 0 ? i : this.pageSize;
    }
}
