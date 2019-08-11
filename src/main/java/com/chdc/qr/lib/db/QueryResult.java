package com.chdc.qr.lib.db;

import java.util.ArrayList;
import java.util.List;

public class QueryResult extends ArrayList<List> {
    private List<String> header = null;

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }
}
