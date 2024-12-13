package com.t13max.idplug.replace.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自动替换设置
 *
 * @author t13max
 * @since 16:00 2024/12/13
 */
@State(name = "AutoReplaceSettings", storages = @Storage("AutoReplaceSettings.xml"))
@Service(Service.Level.PROJECT)
public final class AutoReplaceSettings implements PersistentStateComponent<AutoReplaceSettings> {

    // 替换配置列表
    private Map<String, Map<String, String>> replaceGroups = new HashMap<>();

    //当前选择的分组
    private String curGroup;

    // 获取当前服务实例
    public static AutoReplaceSettings getInstance(com.intellij.openapi.project.Project project) {
        return project.getService(AutoReplaceSettings.class);
    }

    // 返回当前状态（持久化时调用）
    @Override
    public AutoReplaceSettings getState() {
        return this;
    }

    // 加载状态（恢复时调用）
    @Override
    public void loadState(AutoReplaceSettings state) {
        this.replaceGroups = state.replaceGroups;
        this.curGroup = state.curGroup;
    }

    // 获取替换列表
    public Map<String, String> getReplaceList() {
        return replaceGroups.get(curGroup);
    }

    public Map<String, Map<String, String>> getReplaceGroups() {
        return this.replaceGroups;
    }

    public void clearGroup() {
        this.replaceGroups.entrySet().removeIf(next -> !Objects.equals(next.getKey(), curGroup) && next.getValue().isEmpty());
    }

    public String getCurrentGroup() {
        return curGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.curGroup = currentGroup;
    }
}