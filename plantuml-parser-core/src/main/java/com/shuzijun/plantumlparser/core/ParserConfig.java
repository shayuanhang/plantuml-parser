package com.shuzijun.plantumlparser.core;

import com.github.javaparser.ParserConfiguration;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.config.CompilerConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * 解析配置
 *
 * @author shuzijun
 */
public class ParserConfig {
    /**
     * 解析的文件源码
     */
    private Set<Code> codeSet = new HashSet<>();

    /**
     * 输出文件路径
     */
    private String outFilePath;

    private Set<String> excludeClassRegex = new HashSet<>();

    private Set<String> fieldModifier = new HashSet<>();

    private Set<String> methodModifier = new HashSet<>();

    private boolean showPackage = true;

    private boolean showConstructors = false;

    private boolean showDefaultConstructors = false;

    private boolean showComment = false;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private Project project;

    private ParserConfiguration.LanguageLevel languageLevel = ParserConfiguration.LanguageLevel.JAVA_8;

    public String getOutFilePath() {
        return outFilePath;
    }

    public void setOutFilePath(String outFilePath) {
        this.outFilePath = outFilePath;
    }

    public Set<Code> getCodeSet() {
        return new HashSet<>(codeSet);
    }
    public void addCode(Code code) {
        codeSet.add(code);
    }

    public void addFieldModifier(String modifier) {
        fieldModifier.add(modifier);
    }

    public boolean isFieldModifier(String modifier) {
        if (fieldModifier.contains(Constant.VisibilityAll)) {
            return true;
        }
        return fieldModifier.contains(modifier);
    }

    public void addMethodModifier(String modifier) {
        methodModifier.add(modifier);
    }

    public boolean isMethodModifier(String modifier) {
        if (methodModifier.contains(Constant.VisibilityAll)) {
            return true;
        }
        return methodModifier.contains(modifier);
    }

    public boolean isShowPackage() {
        return showPackage;
    }

    public void setShowPackage(boolean showPackage) {
        this.showPackage = showPackage;
    }

    public boolean isShowConstructors() {
        return showConstructors;
    }

    public void setShowConstructors(boolean showConstructors) {
        this.showConstructors = showConstructors;
    }

    public boolean isShowDefaultConstructors() {
        return showDefaultConstructors;
    }

    public void setShowDefaultConstructors(boolean showDefaultConstructors) {
        this.showDefaultConstructors = showDefaultConstructors;
    }

    public ParserConfiguration.LanguageLevel getLanguageLevel() {
        return languageLevel;
    }

    public void setLanguageLevel(ParserConfiguration.LanguageLevel languageLevel) {
        this.languageLevel = languageLevel;
    }

    public boolean isShowComment() {
        return showComment;
    }

    public void setShowComment(boolean showComment) {
        this.showComment = showComment;
    }

    public Set<String> getExcludeClassRegex() {
        return excludeClassRegex;
    }

    public void addExcludeClassRegex(String excludeClassRegex) {
        this.excludeClassRegex.add(excludeClassRegex);
    }

    public boolean isExcludeClass(String className) {
        if (className == null || className.trim().length() == 0) {
            return false;
        }
        for (String regex : excludeClassRegex) {
            try {
                if (className.matches(regex)) {
                    return true;
                }
            } catch (PatternSyntaxException ignore) {
            }
        }
        return false;
    }


}
