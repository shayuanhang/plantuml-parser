package com.shuzijun.plantumlparser.plugin.utils;


import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shuzijun.plantumlparser.plugin.action.PlantumlAddAction.TMP_DIR_PATH;

public class SimpleDataSource implements DataSource {
    private static String ADD_FILE_PATH = TMP_DIR_PATH + File.separator + ".add";
    private static String BLANK = " ";
    private static String STORE_STR = "STORE";
    private static String DEL_STR = "DEL";
    private static String CLEAR_STR = "CLEAR";

    private static class Inner{
        public static DataSource INTANCE = new SimpleDataSource();
    }
    {
        File file = new File(TMP_DIR_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }
    public static DataSource getSingleton() {
        return Inner.INTANCE;
    }

    @Override
    public Map<String, String> getAllData() {
        HashMap<String, String> data = new HashMap<>();
        try (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(getFile()))
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String[] split = line.split(BLANK);
                if (STORE_STR.equals(split[0])) {
                    data.put(split[1], split[2]);
                } else if (DEL_STR.equals(split[0])) {
                    data.remove(split[1]);
                } else if (CLEAR_STR.equals(split[0])) {
                    data.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public void store(String key, String value) {
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(key, value);
        store(objectObjectHashMap);
    }

    @Override
    public void store(Map<String, String> map) {
        try (
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getFile(), true));
        ) {
            for (String key : map.keySet()) {
                bufferedWriter.write(STORE_STR + BLANK + key + BLANK + map.get(key)+"\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void del(String key) {
        del(Collections.singletonList(key));
    }

    @Override
    public void del(List<String> keys) {
        try (
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getFile(), true));
        ) {
            for (String key : keys) {
                bufferedWriter.write(DEL_STR + BLANK + key +"\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        File file = new File(ADD_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    private File getFile() {
        if (!new File(TMP_DIR_PATH).exists()) {
            try {
                new File(TMP_DIR_PATH).createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File file = new File(ADD_FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
}
