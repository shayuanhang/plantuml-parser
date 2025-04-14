package com.shuzijun.plantumlparser.plugin.utils;

import com.shuzijun.plantumlparser.plugin.utils.impl.SimpleStore;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Store<T> {

    static Store getInstance() {
        return SimpleStore.getInstance();
    }

    void add(T data);

    void addAll(Collection<T> dataList);

    @NotNull
    Collection<T> getAllData();

    boolean delete(T data);

    boolean clear();
}
