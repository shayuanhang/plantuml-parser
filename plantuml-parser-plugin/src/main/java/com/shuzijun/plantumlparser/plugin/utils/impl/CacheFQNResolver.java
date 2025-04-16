package com.shuzijun.plantumlparser.plugin.utils.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.plugin.utils.FQNResolver;

import java.util.HashMap;
import java.util.Map;

public class CacheFQNResolver implements FQNResolver {
    private static final Logger LOG = Logger.getInstance(CacheFQNResolver.class);
    Map<Code, String> cacheMap = new HashMap<Code, String>();
    private JavaParserAdapter javaParserAdapter;

    {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        JavaParser parser = new JavaParser(config);
        javaParserAdapter = JavaParserAdapter.of(parser);
    }

    @Override
    public String getFQN(Code code) {
        return cacheMap.computeIfAbsent(code, code1 -> {
            if (Code.Type.JAVA.equals(code1.getType())) {
                CompilationUnit compilationUnit;
                try {
                    compilationUnit = javaParserAdapter.parse(code.getCode());
                } catch (Exception e) {
                    LOG.error(code1.getName()+" : 解析有误");
                    Notifications.Bus.notify(new Notification("plantuml-parser", "", e.getMessage(), NotificationType.WARNING));
                    LOG.error(e);
                    return null;
                }
                String packagePre = compilationUnit.getPackageDeclaration().isPresent() ? compilationUnit.getPackageDeclaration().get().getName().toString() + "." : "";
                NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
                if (types.isEmpty()) {
                    return null;
                }
                return packagePre + types.get(0).getName().toString();
            } else {
                return code1.getName();
            }
        });
    }

    public static FQNResolver getInstance() {
        return Inner.INSTANCE;
    }

    static class Inner {
        public static final FQNResolver INSTANCE = new CacheFQNResolver();
    }
}
