package org.menesty.ikea.domain;

import java.util.List;

public class PagingResult<Data> {
    private List<Data> data;

    private int count;

    public PagingResult() {

    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Data> getData() {
        return data;
    }

    public int getCount() {
        return count;
    }
}
