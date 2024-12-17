package com.t13max.idplug.wechat.bar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;

/**
 * @author t13max
 * @since 16:59 2024/12/16
 */
public class MarqueeWidgetFactory implements StatusBarWidgetFactory {

    @Override
    public String getId() {
        return "MarqueeStatusBarWidgetFactory";
    }

    @Override
    public  String getDisplayName() {
        return "跑马灯状态栏控件";
    }

    @Override
    public boolean isAvailable(Project project) {
        return true; // 控件始终可用
    }

    @Override
    public StatusBarWidget createWidget( Project project) {
        return new WechatStatusBarWidget();
    }

    @Override
    public void disposeWidget( StatusBarWidget widget) {
        widget.dispose();
    }

    @Override
    public boolean canBeEnabledOn( StatusBar statusBar) {
        return true;
    }
}