package com.shuzijun.plantumlparser.plugin.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataSourceUtils {


    public static final void parseClassNameFromJavaFilesAndStore(List<String> addPaths){
        Map<String, String> map = new HashMap<>();
        try {
            for (String addPath : addPaths) {
                CompilationUnit compilationUnit = null;
                compilationUnit = StaticJavaParser.parse(new File(addPath));
                String packagePre = compilationUnit.getPackageDeclaration().isPresent() ? compilationUnit.getPackageDeclaration().get().getName().toString() + "." : "";
                NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
                if (types.isEmpty()) {
                    continue;
                }
                String ClassName = packagePre + types.get(0).getName().toString();
                map.put(ClassName, addPath);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        DataSource.getInstance().store(map);
    }
}
