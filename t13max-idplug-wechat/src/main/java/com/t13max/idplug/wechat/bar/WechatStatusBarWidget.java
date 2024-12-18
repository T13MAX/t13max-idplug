package com.t13max.idplug.wechat.bar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import java.util.Timer;

import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;

/**
 * @author t13max
 * @since 17:00 2024/12/16
 */
public class WechatStatusBarWidget implements StatusBarWidget {

    public final static String ID = "WechatStatusBarWidget";

    private JLabel label;
    private Timer timer;
    private String currentText = "";
    private String fullText = "";

    public WechatStatusBarWidget(Project project) {
        this.label = new JLabel(" ");
        this.label.setPreferredSize(new Dimension(200, 20)); // 设置宽度和高度
    }

    @Override
    public String ID() {
        return "ScrollingStatusBarWidget";
    }

    @Override
    public void install(StatusBar statusBar) {
        // 安装时做初始化
    }

    @Override
    public void dispose() {
        // 清理定时器等
        if (timer != null) {
            timer.cancel();
        }
    }

    public JComponent getComponent() {
        return label; // 返回显示文本的组件
    }

    public void setText(String text) {
        this.fullText = text;
        this.currentText = text;
        startScrolling();
    }

    private void startScrolling() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 每100ms 更新一次文字
                SwingUtilities.invokeLater(() -> {
                    if (!StringUtil.isEmpty(currentText)) {
                        label.setText(currentText);
                        // 向左滚动一位
                        currentText = currentText.substring(1) + currentText.charAt(0);
                    } else {
                        label.setText(""); // 滚动完成后清空
                    }
                });
            }
        };

        // 启动定时器，100ms更新一次，最多持续5秒
        timer.scheduleAtFixedRate(task, 0, 100);
    }
}