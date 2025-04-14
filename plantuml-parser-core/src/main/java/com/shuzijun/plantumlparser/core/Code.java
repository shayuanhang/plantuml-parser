package com.shuzijun.plantumlparser.core;

public interface Code {
    String getCode();

    String getName();

    default Type getType(){
        return Type.JAVA;
    }

    static enum Type{
        JAVA,KT,OTHER
    }
}
