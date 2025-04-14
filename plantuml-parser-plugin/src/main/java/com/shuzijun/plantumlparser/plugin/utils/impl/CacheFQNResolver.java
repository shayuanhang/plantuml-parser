package com.shuzijun.plantumlparser.plugin.utils.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.plugin.utils.FQNResolver;

import java.util.HashMap;
import java.util.Map;

public class CacheFQNResolver implements FQNResolver {

    Map<Code, String> cacheMap = new HashMap<Code, String>();
    @Override

    public String getFQN(Code code) {
       return cacheMap.computeIfAbsent(code,code1 -> {
           if (Code.Type.JAVA.equals(code1.getType())) {
               CompilationUnit compilationUnit;
               try {
                   compilationUnit = StaticJavaParser.parse(code.getCode());
               } catch (Exception e) {
                   throw new RuntimeException(e);
               }
               String packagePre = compilationUnit.getPackageDeclaration().isPresent() ? compilationUnit.getPackageDeclaration().get().getName().toString() + "." : "";
               NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
               if (types.isEmpty()) {
                   return null;
               }
               return packagePre + types.get(0).getName().toString();
           }else {
               return code1.getName();
           }
        });

    }

    public static FQNResolver getInstance() {
        return Inner.INSTANCE;
    }
    static class Inner{
        public static final FQNResolver INSTANCE = new CacheFQNResolver();
    }
}
