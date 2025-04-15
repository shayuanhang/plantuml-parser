package com.shuzijun.plantumlparser.core;

public interface Code {
    /**
     * 源码
     * @return
     */
    String getCode();

    /**
     * 文件名
     * @return
     */
    String getName();

    default Type getType(){
        return Type.JAVA;
    }

    static enum Type{
        JAVA,KT,OTHER
    }
}
