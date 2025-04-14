package com.shuzijun.plantumlparser.plugin.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.core.FileCode;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class CodeUtils {
    public static List<Code> parse(VirtualFile[] virtualFiles) {
        List<Code> codeList = new ArrayList<>();
        List<VirtualFile> classVfs = new ArrayList<>();
        // 如果是JAVA源码，直接加入addPaths
        for (VirtualFile virtualFile : virtualFiles) {
            File file = new File(virtualFile.getPath());
            if (file.exists()) {
                if (file.isDirectory()) {
                    for (VirtualFile child : virtualFile.getChildren()) {
                        if (!child.isDirectory() && child.getPath().endsWith(".java")) {
                            codeList.add(new FileCode(child.getPath()));
                        }
                    }
                } else {
                    if (virtualFile.getPath().endsWith(".java")) {
                        codeList.add(new FileCode(virtualFile.getPath()));
                    }
                }
            } else {
                // vfs
                if (virtualFile.isDirectory()) {
                    List<VirtualFile> collect = Arrays.stream(virtualFile.getChildren()).filter(vf -> !vf.isDirectory() && vf.getPath().endsWith(".class")).collect(Collectors.toList());
                    classVfs.addAll(collect);
                } else if (virtualFile.getPath().endsWith(".class")) {
                    classVfs.add(virtualFile);
                }
            }
        }
        // 将class的VirtualFile 解析出Code
        codeList.addAll(parseCodeFromClassVfs(classVfs));
        if (codeList.isEmpty()) {
            throw new RuntimeException(PropertiesUtils.getInfo("select.empty"));
        }
        return codeList;
    }

    /**
     * 将class 文件解析出Code
     *
     * @param
     * @return
     */
    public static List<Code> parseCodeFromClassVfs(List<VirtualFile> classVfs) {
        List<String> vfUrls = classVfs.stream().map(vf -> vf.getUrl()).collect(Collectors.toList());
        Map<String, String> anonymousClassUrlMap = new HashMap<>();
        for (VirtualFile classVf : classVfs) {
            List<VirtualFile> anonymousClassVFs = Arrays.stream(classVf.getParent().getChildren()).filter(vf -> vf.getUrl().contains(classVf.getUrl().replace(".class", "$"))).collect(Collectors.toList());
            for (VirtualFile anonymousClassVF : anonymousClassVFs) {
                anonymousClassUrlMap.put(anonymousClassVF.getUrl().split("!/")[1], anonymousClassVF.getUrl());
            }
        }
        // org/springframework/util/function/ThrowingBiFunction$1.class
        CustomOutputSinkFactory output = new CustomOutputSinkFactory();
        VfClassFileSource vfClassFileSource = new VfClassFileSource(anonymousClassUrlMap);
        CfrDriver driver = new CfrDriver.Builder().withOutputSink(output).withClassFileSource(vfClassFileSource).build();
        driver.analyse(vfUrls);
        return output.getCodes();
    }

    /**
     * 自定义输出源码位置，无需生成源码文件，减少IO
     */
    static class CustomOutputSinkFactory implements OutputSinkFactory {
        private List<Code> codes = new ArrayList<>();

        public List<Code> getCodes() {
            return codes;
        }


        @Override
        public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
            return Collections.singletonList(SinkClass.DECOMPILED);
        }

        @Override
        public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
            if (sinkClass == SinkClass.DECOMPILED) {
                return x -> dumpDecompiled.accept((SinkReturns.Decompiled) x);
            }
            return ignore -> {
            };
        }

        Consumer<SinkReturns.Decompiled> dumpDecompiled = d -> {
            ParsedCode parsedCode = new ParsedCode(d.getJava(), d.getPackageName() + "." + d.getClassName() + ".java");
            codes.add(parsedCode);
        };
    }

    /**
     * 从vfs中获取class文件
     */
    static class VfClassFileSource implements ClassFileSource {
        /**
         * 用于处理匿名类，将frc自动查找的匿名类的path，遇敌到时VirtualFile的url
         */
        private Map<String ,String> anonymousClassUrlMap = new HashMap<>();

        public VfClassFileSource(Map<String, String> anonymousClassUrlMap) {
            this.anonymousClassUrlMap.putAll(anonymousClassUrlMap);
        }

        @Override
        public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {

        }

        @Override
        public Collection<String> addJar(String jarPath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPossiblyRenamedPath(String path) {
            return path;
        }

        @Override
        public org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair<byte[], String> getClassFileContent(String path) throws IOException {
            if (anonymousClassUrlMap.containsKey(path)) {
                path = anonymousClassUrlMap.get(path);
            }
            VirtualFile fileByUrl = VirtualFileManager.getInstance().findFileByUrl(path);
            if (fileByUrl == null) {
                throw new IOException ("not find [" + path + "] vf");
            }
            return Pair.make(fileByUrl.contentsToByteArray(), path);
        }
    }

    static class ParsedCode implements Code {
        private String code;
        private String name;

        public ParsedCode(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ParsedCode that = (ParsedCode) o;
            return Objects.equals(code, that.code) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, name);
        }
    }
}
