package com.shuzijun.plantumlparser.plugin.utils.impl;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.shuzijun.plantumlparser.core.Code;

public class VfCode implements Code {
    private VirtualFile vf;

    @Override
    public String getCode() {
        return " vf.getFileType()";
    }

    @Override
    public String getName() {
        VirtualFileManager.getInstance();
        return "";
    }
}
