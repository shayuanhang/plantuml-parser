package com.shuzijun.plantumlparser.core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileCode implements Code{
    private File file;

    public FileCode(File file) {
        this.file = file;
    }

    public FileCode(String filePath) {
        this.file = new File(filePath);
    }
    @Override
    public String getCode() {
        String codeStr = "";
        try {
            codeStr = FileUtils.readFileToString(this.file, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return codeStr;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Type getType() {
        if (file.getName().endsWith(".java")) {
            return Type.JAVA;
        } else if (file.getName().endsWith(".kt")) {
            return Type.KT;
        }else {
            return Type.OTHER;
        }
    }
}
