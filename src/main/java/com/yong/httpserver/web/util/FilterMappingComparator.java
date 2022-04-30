package com.yong.httpserver.web.util;

import com.yong.httpserver.web.filter.FilterMapping;

import java.util.Comparator;

public class FilterMappingComparator implements Comparator<FilterMapping> {

    private final AntPathMatcher.AntPatternComparator comparator = new AntPathMatcher.AntPatternComparator();

    @Override
    public int compare(FilterMapping o1, FilterMapping o2) {
        return comparator.compare(o1.pattern(), o2.pattern());
    }
}
