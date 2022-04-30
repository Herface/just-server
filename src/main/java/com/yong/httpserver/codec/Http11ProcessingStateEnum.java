package com.yong.httpserver.codec;

import java.util.Map;

public enum Http11ProcessingStateEnum {
    INIT,
    CHAR,
    CR,
    LF,
    END_CR,
    END_LF;
    public static final Map<Http11ProcessingStateEnum, Http11ProcessingStateEnum> STATE_MAP;

    static {
        STATE_MAP = null;
    }

}
