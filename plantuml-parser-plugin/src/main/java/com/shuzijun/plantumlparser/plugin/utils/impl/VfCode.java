package com.shuzijun.plantumlparser.plugin.utils.impl;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.shuzijun.plantumlparser.core.Code;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

public class VfCode implements Code {
    private VirtualFile vf;

    public VfCode(VirtualFile vf) {
        this.vf = vf;
    }

    @Override
    public String getCode() {
        try {
            return new String(vf.contentsToByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type getType() {
        String url = vf.getUrl();
        if (StringUtils.endsWith(url, ".java")) {
            return Type.JAVA;
        } else if (StringUtils.endsWith(url, ".kt")) {
            return Type.KT;
        }else {
            return Type.OTHER;
        }

    }

    @Override
    public String getName() {
        return vf.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VfCode vfCode = (VfCode) o;
        return Objects.equals(vf, vfCode.vf);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vf);
    }
}
