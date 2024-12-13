package com.t13max.idplug.replace.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.t13max.idplug.replace.dialog.ReplaceListDialog;
import com.t13max.idplug.replace.settings.AutoReplaceSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author t13max
 * @since 15:07 2024/12/13
 */
public class SettingsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        ReplaceListDialog dialog = new ReplaceListDialog(project);
        if (dialog.showAndGet()) {
            AutoReplaceSettings.getInstance(project).clearGroup();
            Messages.showInfoMessage(project, "Replace list updated!", "Info");
        }
    }

}
