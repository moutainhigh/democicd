package com.uwallet.pay.core.common;

import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SortingContext implements Serializable {
    private static final long serialVersionUID = 2930414009172099746L;
    private static final Pattern PATTERN_SC = Pattern.compile("(?<field>[a-zA-z]+)\\((?<sort>[a-zA-z]+)\\)");
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final SortingContext DEFAULT = new SortingContext("created_date", "DESC");
    public static final SortingContext EMPTY = new SortingContext("", "");
    private String field;
    private String order = "DESC";

    public SortingContext() {
    }

    public SortingContext(String f, String o) {
        this.field = f;
        this.order = o;
    }

    public SortingContext(String sc) {
        Matcher matcher = PATTERN_SC.matcher(sc);
        if (matcher.find()) {
            this.field = matcher.group("field");
            this.order = matcher.group("sort");
        }

    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public static Vector<SortingContext> getSortingContexts(String vsc) {
        Vector<SortingContext> scs = new Vector();
        Matcher matcher = PATTERN_SC.matcher(vsc);

        while(matcher.find()) {
            SortingContext sc = new SortingContext();
            sc.field = matcher.group("field");
            sc.order = matcher.group("sort");
            scs.add(sc);
        }

        return scs;
    }

    @Override
    public String toString() {
        return String.format("ORDER BY %s %s", this.field, this.order);
    }

}
