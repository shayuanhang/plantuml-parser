package com.shuzijun.plantumlparser.plugin.utils.impl;

import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.plugin.utils.Store;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleStore implements Store<Code> {
    private Set<Code> codeSet = new HashSet<>();

    public static Store getInstance() {
        return Inner.INSTANCE;
    }

    @Override
    public void add(Code code) {
        codeSet.add(code);
    }

    @Override
    public void addAll(Collection<Code> collection) {
        codeSet.addAll(collection);
    }

    @Override
    public @NotNull Set<Code> getAllData() {
        return this.codeSet;
    }

    @Override
    public boolean delete(Code code) {
        return this.codeSet.remove(code);
    }

    @Override
    public boolean clear() {
        this.codeSet.clear();
        return true;
    }

    static class Inner {
        static Store INSTANCE = new SimpleStore();
    }
}
