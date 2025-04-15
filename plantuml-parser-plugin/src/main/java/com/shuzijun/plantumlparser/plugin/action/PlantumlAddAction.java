package com.shuzijun.plantumlparser.plugin.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.plantumlparser.core.Code;
import com.shuzijun.plantumlparser.plugin.utils.CodeUtils;
import com.shuzijun.plantumlparser.plugin.utils.Store;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;

public class PlantumlAddAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(PlantumlAddAction.class);
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            List<Code> codeList = CodeUtils.parse(virtualFiles);
            Store.getInstance().addAll(codeList);
        } catch (Exception exception) {
            LOG.error(exception);
            Notifications.Bus.notify(new Notification("plantuml-parser", "", exception.getMessage(), NotificationType.WARNING), e.getProject());
        }

    }
}
