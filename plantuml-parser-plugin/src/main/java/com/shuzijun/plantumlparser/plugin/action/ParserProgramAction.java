package com.shuzijun.plantumlparser.plugin.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.core.ParserConfig;
import com.shuzijun.plantumlparser.core.ParserProgram;
import com.shuzijun.plantumlparser.plugin.utils.CodeUtils;
import com.shuzijun.plantumlparser.plugin.utils.PropertiesUtils;
import com.shuzijun.plantumlparser.plugin.utils.Store;
import com.shuzijun.plantumlparser.plugin.window.ParserConfigPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 解析动作
 *
 * @author shuzijun
 */
public class ParserProgramAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(ParserProgramAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            ParserConfig parserConfig = new ParserConfig();
            Collection<Code> codeCollection = Store.getInstance().getAllData();
            Collection<Code> failCodes = CodeUtils.preCheck(codeCollection);
            if (failCodes.size() > 0) {
                String failCodeNameStr = failCodes.stream().map(code -> code.getName()).collect(Collectors.joining(","));
                Notifications.Bus.notify(new Notification("plantuml-parser", "","文件有误: "+failCodeNameStr, NotificationType.WARNING), e.getProject());
                failCodes.forEach(Store.getInstance()::delete);
                codeCollection.removeAll(failCodes);
                CodeUtils.errorLogCodes(failCodes);
            }
            if (codeCollection.isEmpty()) {
                Notifications.Bus.notify(new Notification("plantuml-parser", "", PropertiesUtils.getInfo("select.empty"), NotificationType.WARNING), e.getProject());
                return;
            }
            ParserConfigDialog parserConfigDialog = new ParserConfigDialog(e.getProject(), parserConfig);
            if (parserConfigDialog.showAndGet()) {
                for (Code code : codeCollection) {
                    parserConfig.addCode(code);
                }
                parserConfig = parserConfigDialog.getParserConfig();
                ParserProgram parserProgram = new ParserProgram(parserConfig);
                parserProgram.execute();
                Notifications.Bus.notify(new Notification("plantuml-parser", "", PropertiesUtils.getInfo("success", parserConfig.getOutFilePath()), NotificationType.INFORMATION), e.getProject());
                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(parserConfig.getOutFilePath()));
                OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
                FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, false);
            }
        } catch (Exception exception) {
            // 清除数据，防止无法打开解析页面
            Store.getInstance().clear();
            LOG.error(exception);
            Notifications.Bus.notify(new Notification("plantuml-parser", "", exception.getMessage(), NotificationType.WARNING), e.getProject());
        }
    }

    class ParserConfigDialog extends DialogWrapper {

        private ParserConfigPanel parserConfigPanel;

        private ParserConfig parserConfig;

        private Project project;

        public ParserConfigDialog(@Nullable Project project, ParserConfig parserConfig) {
            super(project, true);
            parserConfigPanel = new ParserConfigPanel(project);
            this.parserConfig = parserConfig;
            this.project = project;
            setModal(true);
            init();
            setTitle("ParserConfig");
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return parserConfigPanel.getJpanel();
        }

        @Override
        protected @NotNull Action getOKAction() {
            Action action = super.getOKAction();
            action.putValue(Action.NAME, "generate");
            return action;
        }

        @Override
        protected @NotNull Action getCancelAction() {
            return super.getCancelAction();
        }

        @Override
        protected @NotNull Action getHelpAction() {
            Action action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BrowserUtil.browse("https://github.com/shuzijun/plantuml-parser");
                }
            };
            action.putValue(Action.NAME, "help");
            return action;
        }

        @Override
        protected @Nullable ValidationInfo doValidate() {
            try {
                parserConfigPanel.getFilePath();
            } catch (NullPointerException nullPointerException) {
                return new ValidationInfo(nullPointerException.getMessage(), null);
            }
            return null;
        }

        public ParserConfig getParserConfig() {
            parserConfig.setOutFilePath(parserConfigPanel.getFilePath());
            parserConfigPanel.getField().forEach(s -> parserConfig.addFieldModifier(s));
            parserConfigPanel.getMethod().forEach(s -> parserConfig.addMethodModifier(s));
            parserConfig.setLanguageLevel(parserConfigPanel.getLanguageLevel());
            parserConfig.setShowPackage(parserConfigPanel.getShowPackage());
            parserConfig.setShowConstructors(parserConfigPanel.getConstructors());
            parserConfig.setShowComment(parserConfigPanel.getShowComment());
            parserConfig.setProject(this.project);
            parserConfigPanel.getExcludeClass().forEach(s -> parserConfig.addExcludeClassRegex(s));
            return parserConfig;
        }
    }
}
