package com.t13max.idplug.wechat.bar;

import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * @author t13max
 * @since 17:00 2024/12/16
 */
public class WechatStatusBarWidget implements StatusBarWidget {

    private String text = "跑马灯效果的状态栏文字 ";
    private int position = 0;
    private Timer timer;

    public WechatStatusBarWidget() {
        startMarquee();
    }

    private void startMarquee() {
        timer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                position++;
                if (position > text.length()) {
                    position = 0;
                }
            }
        });
        timer.start();
    }

    public void setText(String newText) {
        this.text = newText + " ";
        this.position = 0;
    }

    @Override
    public String ID() {
        return "MarqueeStatusBarWidget";
    }

    @Override
    public WidgetPresentation getPresentation() {
        return new TextPresentation() {
            @Override
            public String getText() {
                return text.substring(position) + text.substring(0, position);
            }

            @Override
            public float getAlignment() {
                return Component.CENTER_ALIGNMENT;
            }

            @Override
            public String getTooltipText() {
                return "滚动状态栏";
            }

            @Override
            public Consumer<MouseEvent> getClickConsumer() {
                return null;
            }
        };
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
        }
    }
}