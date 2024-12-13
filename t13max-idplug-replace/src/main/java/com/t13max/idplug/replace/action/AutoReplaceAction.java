package com.t13max.idplug.replace.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.t13max.idplug.replace.settings.AutoReplaceSettings;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author t13max
 * @since 15:07 2024/12/13
 */
public class AutoReplaceAction extends AnAction {

    private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm yyyy/MM/dd");

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 获取当前项目和编辑器
        Project project = event.getProject();
        Editor editor = event.getDataContext().getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getDataContext().getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);

        if (editor == null || psiFile == null || !psiFile.getFileType().getName().equals("JAVA")) {
            Messages.showErrorDialog(project, "Please open a Java file", "Invalid File");
            return;
        }

        //替换
        String newText = replace(project, editor.getDocument().getText());

        //格式化注释
        newText = formatAnnotation(newText);

        final String finalText = newText;
        ApplicationManager.getApplication().runWriteAction(() -> {
            editor.getDocument().setText(finalText);  // 替换文本
        });
        Messages.showInfoMessage(project, "Text replacement done", "Info");
    }

    private String formatAnnotation(String text) {
        String[] lines = text.split("\n");
        StringBuilder newText = new StringBuilder();
        boolean annotationHead = false;
        for (String line : lines) {
            if (line.contains("/**")) {
                annotationHead = true;
            }
            if (!annotationHead) {
                newText.append(line).append("\n");
                continue;
            }
            if (line.contains("* @param ") || line.contains(" * @author ")
                    || line.contains(" * @since ") || line.contains("* @return ")
                    || line.contains(" * @Author ") || line.contains("* @Date ")
            ) {
                //newText.append(line).append("\n");
            } else if (line.contains("*/")) {
                newText.append("     * @Author t13max").append("\n");
                newText.append("     * @Date ").append(SDF.format(new Date())).append("\n");
                newText.append(line).append("\n");
                annotationHead = false;
            } else {
                newText.append(line).append("\n");
            }
        }
        return newText.toString();
    }

    private String replace(Project project, String text) {
        Map<String, String> replaceList = AutoReplaceSettings.getInstance(project).getReplaceList();
        for (Map.Entry<String, String> entry : replaceList.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }
}
