package com.t13max.idplug.wechat.windows;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信窗口
 *
 * @Author t13max
 * @Date 16:58 2024/12/16
 */
public class WechatToolWindowFactory implements ToolWindowFactory, DumbAware {

    private final Map<String, String> contentMap = new HashMap<>();

    public WechatToolWindowFactory() {
        // 初始化下拉框对应的内容
        contentMap.put("Option 1", "This is the content for Option 1.\nLine 1\nLine 2");
        contentMap.put("Option 2", "This is the content for Option 2.\nAnother line\nMore lines");
        contentMap.put("Option 3", "This is the content for Option 3.\nContent keeps going...");
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // 主面板
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 顶部下拉框
        JComboBox<String> comboBox = new JComboBox<>(contentMap.keySet().toArray(new String[0]));

        // 中间文本区域
        JBTextArea textArea = new JBTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);

        // 滚动条
        JBScrollPane scrollPane = new JBScrollPane(textArea);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        // 添加组件到面板
        panel.add(comboBox, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 下拉框事件
        comboBox.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            if (selected != null) {
                textArea.setText(contentMap.getOrDefault(selected, "No content available."));
            }
        });

        // 按钮事件
        clearButton.addActionListener(e -> textArea.setText(""));
        refreshButton.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            if (selected != null) {
                textArea.setText(contentMap.getOrDefault(selected, "No content available."));
            }
        });

        // 初始显示第一个选项内容
        comboBox.setSelectedIndex(0);

        // 将面板添加到ToolWindow
        toolWindow.getComponent().add(panel);


    }
}
