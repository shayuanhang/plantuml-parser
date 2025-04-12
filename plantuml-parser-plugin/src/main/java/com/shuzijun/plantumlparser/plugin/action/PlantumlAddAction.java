package com.shuzijun.plantumlparser.plugin.action;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.plantumlparser.plugin.utils.DataSource;
import com.shuzijun.plantumlparser.plugin.utils.DataSourceUtils;
import com.shuzijun.plantumlparser.plugin.utils.PropertiesUtils;
import com.shuzijun.plantumlparser.plugin.utils.SimpleDataSource;
import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlantumlAddAction extends AnAction {
    public static String TMP_DIR_PATH = System.getProperty("user.home") + File.separator + "cfrtmp";
    private DataSource dataSource = DataSource.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            List<String> addPaths = new ArrayList<>();
            // 如果是JAVA源码，直接加入addPaths
            Arrays.stream(virtualFiles).filter(vf -> vf.getPath().endsWith(".java")).forEach(vf -> addPaths.add(vf.getPath()));
            // 处理选的是从jar包中的class文件
            List<VirtualFile> classVfs = Arrays.stream(virtualFiles).filter(vf -> vf.getPath().endsWith(".class") && vf.getPath().contains("!/")).collect(Collectors.toList());
            addPaths.addAll(getJavaFilePathFromClassFile(classVfs));
            if (addPaths.size() == 0) {
                throw new RuntimeException(PropertiesUtils.getInfo("select.empty"));
            }
            // 储存选中数据
            DataSourceUtils.parseClassNameFromJavaFilesAndStore(addPaths);
        } catch (Exception exception) {
            Notifications.Bus.notify(new Notification("plantuml-parser", "", exception.getMessage(), NotificationType.WARNING), e.getProject());
        }

    }

    private List<String> getJavaFilePathFromClassFile(List<VirtualFile> classVfs) {
        if (classVfs.size() == 0) {
            return new ArrayList<>();
        }
        // 将选中的jar包中的class文件写入本地
        for (VirtualFile virtualFile : classVfs) {
            try {
                byte[] bytes = virtualFile.contentsToByteArray();
                String genFileName = getGenFileName(virtualFile);
                File file = new File(genFileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        // 对上一步的生成的class文件生成源码
        List<String> classFilePaths = classVfs.stream().map(vf -> getGenFileName(vf)).collect(Collectors.toList());
        // 生成java源码
        List<String> javaPaths = parseToJava(classFilePaths);

        return javaPaths;
    }

    public static @NotNull Map<String, String> parseClassNameFromJavaFiles(List<String> addPaths) throws FileNotFoundException {
        Map<String, String> map = new HashMap<>();
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
        return map;
    }

    /**
     * @param virtualFile
     * @return
     */
    private String getGenFileName(VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (!path.endsWith(".class")) {
            throw new RuntimeException("not support this file type[" + path.substring(path.lastIndexOf(".")) + "]!");
        } else {
            return TMP_DIR_PATH + File.separator + path.substring(path.lastIndexOf("!") + 1).replace("/", "_");
        }

    }

    private String getGenCfrJavaFilePath(VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (!path.endsWith(".class")) {
            throw new RuntimeException("not support this file type[" + path.substring(path.lastIndexOf(".")) + "]!");
        } else {
            return TMP_DIR_PATH + File.separator + path.substring(path.lastIndexOf("!/") + 2).replace("/", File.separator).replace(".class", ".java");
        }
    }

    public static List<String> parseToJava(List<String> classList) {
        CustomOutputSinkFactory output = new CustomOutputSinkFactory();
        CfrDriver driver = new CfrDriver.Builder().withOutputSink(output).build();
        driver.analyse(classList);
        return output.getJavaPaths();
    }

    public static List<String> parseFilePath(List<String> classList) {
        ArrayList<String> pathList = new ArrayList<>();
        for (String className : classList) {
            String fileName = TMP_DIR_PATH + File.separator + className.replace(".", File.separator) + ".java";
            pathList.add(fileName);
        }
        return pathList;
    }

    public static void delFiles(List<String> classList) {
        List<String> filePaths = parseFilePath(classList);
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                boolean delete = file.delete();
                if (!delete) {
                    System.out.println("删除文件失败：" + filePath);
                }
            }
        }
    }

    static class CustomOutputSinkFactory implements OutputSinkFactory{
        private List<String> javaPaths = new ArrayList<>();

        public List<String> getJavaPaths() {
            return javaPaths;
        }

        public void setJavaPaths(List<String> javaPaths) {
            this.javaPaths = javaPaths;
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
            return ignore -> {};
        }

        public String genJavaPath(String packageName, String className) {
            return TMP_DIR_PATH+File.separator+packageName.replace(".","_")+"_"+className+".java"        ;
        }
        Consumer<SinkReturns.Decompiled> dumpDecompiled = d -> {
            String javaPath = genJavaPath(d.getPackageName(), d.getClassName());
            File javaFile = new File(javaPath);
            try (BufferedWriter bw=new BufferedWriter(new FileWriter(javaFile)))
            {
              bw.write(d.getJava());
              this.javaPaths.add(javaPath);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
