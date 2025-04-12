package com.shuzijun.plantumlparser.plugin.utils;

import java.util.List;
import java.util.Map;

public interface DataSource {
    Map<String, String> getAllData();

    void store(String key, String value);

    void store(Map<String, String> map);

    void del(String key);

    void del(List<String> keys);
    void clear();
    static DataSource getInstance(){
        return SimpleDataSource.getSingleton();
    }
}
