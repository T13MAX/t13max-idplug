package com.t13max.idplug.replace.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.t13max.idplug.replace.settings.AutoReplaceSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReplaceListDialog extends DialogWrapper {
    private final AutoReplaceSettings state;
    private JPanel mainPanel;
    private JComboBox<String> groupSelector;
    private DefaultListModel<String> ruleListModel;
    private JList<String> ruleList;
    private boolean isUserAction = true; // 标志是否是用户操作

    public ReplaceListDialog(Project project) {
        super(project);
        setTitle("Settings");
        state = AutoReplaceSettings.getInstance(project);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mainPanel = new JPanel(new BorderLayout());

        // 分组选择
        JPanel groupPanel = new JPanel(new BorderLayout());
        groupPanel.add(new JLabel("Group:"), BorderLayout.WEST);

        groupSelector = new JComboBox<>();
        groupSelector.addActionListener(e -> {
            if (isUserAction) {
                String selectedGroup = (String) groupSelector.getSelectedItem();
                state.setCurrentGroup(selectedGroup); // 保存当前分组
                loadGroupRules();
            }
        });
        groupPanel.add(groupSelector, BorderLayout.CENTER);

        JButton addGroupButton = new JButton("Add Group");
        addGroupButton.addActionListener(e -> addGroup());
        groupPanel.add(addGroupButton, BorderLayout.EAST);

        mainPanel.add(groupPanel, BorderLayout.NORTH);

        // 规则列表
        ruleListModel = new DefaultListModel<>();
        ruleList = new JList<>(ruleListModel);
        JScrollPane scrollPane = new JScrollPane(ruleList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮
        JPanel buttonPanel = new JPanel();
        JButton addRuleButton = new JButton("Add Rule");
        addRuleButton.addActionListener(e -> addRule());
        buttonPanel.add(addRuleButton);

        JButton batchAddButton = new JButton("Batch Add");
        batchAddButton.addActionListener(e -> batchAddRules());
        buttonPanel.add(batchAddButton);

        JButton deleteRuleButton = new JButton("Delete Rule");
        deleteRuleButton.addActionListener(e -> deleteRule());
        buttonPanel.add(deleteRuleButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loadGroups(); // 初始化加载分组
        return mainPanel;
    }

    private void loadGroups() {
        isUserAction = false;
        try {
            groupSelector.removeAllItems();
            for (String groupName : state.getReplaceGroups().keySet()) {
                groupSelector.addItem(groupName);
            }

            // 选中上次保存的分组
            String currentGroup = state.getCurrentGroup();
            if (currentGroup != null) {
                groupSelector.setSelectedItem(currentGroup);
            }
        } finally {
            isUserAction = true;
        }

        loadGroupRules();
    }

    private void loadGroupRules() {
        ruleListModel.clear();
        String selectedGroup = (String) groupSelector.getSelectedItem();
        if (selectedGroup != null && state.getReplaceGroups().containsKey(selectedGroup)) {
            Map<String, String> rules = state.getReplaceGroups().get(selectedGroup);
            for (Map.Entry<String, String> entry : rules.entrySet()) {
                ruleListModel.addElement(entry.getKey() + " -> " + entry.getValue());
            }
        }
    }

    private void addGroup() {
        String groupName = JOptionPane.showInputDialog("Enter new group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            state.getReplaceGroups().putIfAbsent(groupName.trim(), new java.util.HashMap<>());
            state.setCurrentGroup(groupName);
            loadGroups();
        }
    }

    private void addRule() {
        String find = JOptionPane.showInputDialog("Find string:");
        String replace = JOptionPane.showInputDialog("Replace with:");
        if (find != null && replace != null) {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            if (selectedGroup != null) {
                state.getReplaceGroups().get(selectedGroup).put(find, replace);
                loadGroupRules();
            }
        }
    }

    private void batchAddRules() {
        String input = JOptionPane.showInputDialog("Enter rules (format: xxx->yyy,aaa->bbb):");
        if (input != null) {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            if (selectedGroup != null) {
                Map<String, String> rules = state.getReplaceGroups().get(selectedGroup);
                String[] rulePairs = input.split(",");
                for (String pair : rulePairs) {
                    String[] keyValue = pair.split("->");
                    if (keyValue.length == 2) {
                        rules.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
                loadGroupRules();
            }
        }
    }

    private void deleteRule() {
        String selectedValue = ruleList.getSelectedValue();
        if (selectedValue != null) {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            if (selectedGroup != null) {
                Map<String, String> rules = state.getReplaceGroups().get(selectedGroup);
                String find = selectedValue.split(" -> ")[0];
                rules.remove(find);
                loadGroupRules();
            }
        }
    }
}