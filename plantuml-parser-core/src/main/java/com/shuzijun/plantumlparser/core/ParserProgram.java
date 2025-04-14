package com.shuzijun.plantumlparser.core;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;

/**
 * 解析程序
 *
 * @author shuzijun
 */
public class ParserProgram {

    private ParserConfig parserConfig;


    public ParserProgram(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }


    public void execute() throws IOException {
        if(parserConfig.getLanguageLevel()!=null) {
            StaticJavaParser.getConfiguration().setLanguageLevel(parserConfig.getLanguageLevel());
        }else {
            StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        }

        Set<Code> codeSet = this.parserConfig.getCodeSet();
        PUmlView pUmlView = new PUmlView();
        for (Code code : codeSet) {
            if (Code.Type.JAVA.equals(code.getType())) {
                CompilationUnit compilationUnit = StaticJavaParser.parse(code.getCode());
                Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
                VoidVisitor<PUml> classNameCollector = new ClassVoidVisitor(packageDeclaration.isPresent() ? packageDeclaration.get().getNameAsString() : "", parserConfig);
                classNameCollector.visit(compilationUnit, pUmlView);
            } else if (Code.Type.KT.equals(code.getType())) {
                try {
                    KtParserProgram.execute(parserConfig,code,pUmlView);
                }catch (NoClassDefFoundError e){
                    throw new IOException("Environment not support kotlin");
                }
            }
        }


        if (this.parserConfig.getOutFilePath() == null) {
            System.out.println(pUmlView);
        } else {
            File outFile = new File(this.parserConfig.getOutFilePath());
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            FileUtils.write(outFile, pUmlView.toString(), Charset.forName("UTF-8"));
        }

    }

}
