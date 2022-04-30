package com.yong.httpserver.web.util;

import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.servlet.ServletMapping;

import java.util.Comparator;
import java.util.Set;

public class ServletMappingComparator implements Comparator<ServletMapping> {

    private final AntPathMatcher.AntPatternComparator comparator = new AntPathMatcher.AntPatternComparator();

    @Override
    public int compare(ServletMapping o1, ServletMapping o2) {
        int compare = comparator.compare(o1.getPattern(), o2.getPattern());
        if (compare == 0) {
            Set<RequestMethod> methodSet1 = o1.getSupportedMethods();
            Set<RequestMethod> methodSet2 = o2.getSupportedMethods();
            if (methodSet1.size() != methodSet2.size()) {
                return methodSet1.size() - methodSet2.size();
            } else {
                if (methodSet1.containsAll(methodSet2)) {
                    return 0;
                }
                return -1;
            }
        }
        return compare;
    }
}
