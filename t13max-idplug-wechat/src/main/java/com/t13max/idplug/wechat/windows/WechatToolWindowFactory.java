package com.t13max.idplug.wechat.windows;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.*;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.t13max.common.config.BaseConfig;
import com.t13max.common.event.GameEventBus;
import com.t13max.common.event.IEvent;
import com.t13max.common.event.IEventEnum;
import com.t13max.common.event.IEventListener;
import com.t13max.common.manager.ManagerBase;
import com.t13max.common.run.Application;
import com.t13max.idplug.wechat.bar.WechatStatusBarWidget;
import com.t13max.idplug.wechat.entity.ContactsElement;
import com.t13max.util.TimeUtil;
import com.t13max.util.func.Applier;
import com.t13max.wxbot.MessageHandler;
import com.t13max.wxbot.Robot;
import com.t13max.wxbot.RobotApplication;
import com.t13max.wxbot.consts.RobotEventEnum;
import com.t13max.wxbot.consts.RobotStatusEnum;
import com.t13max.wxbot.consts.WxRespConstant;
import com.t13max.wxbot.entity.Contacts;
import com.t13max.wxbot.entity.Message;
import com.t13max.wxbot.manager.RobotManager;
import com.t13max.wxbot.tools.MessageTools;
import com.t13max.wxbot.utils.QRUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * 微信窗口
 *
 * @Author t13max
 * @Date 16:58 2024/12/16
 */
public class WechatToolWindowFactory implements ToolWindowFactory, DumbAware {

    private Robot robot;
    private Frame qrFrame;
    private JComboBox<ContactsElement> comboBox;
    private JTextField textField;
    private JBTextArea textArea;

    private final Map<String, List<Message>> contentMap = new HashMap<>();

    public WechatToolWindowFactory() {

    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {

        runApplication();

        // 主面板
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 顶部下拉框
        comboBox = new ComboBox<>();

        // 中间文本区域
        textArea = new JBTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);

        // 滚动条
        JBScrollPane scrollPane = new JBScrollPane(textArea);

        // 创建 JPanel
        JPanel underPanel = new JPanel();
        underPanel.setLayout(new BorderLayout());

        // 创建输入框
        textField = new JTextField();
        underPanel.add(textField, BorderLayout.NORTH);  // 将输入框放置在北部

        // 创建按钮
        JPanel buttonPanel = new JPanel();  // 创建一个新的 JPanel 用来放置按钮
        buttonPanel.setLayout(new FlowLayout());  // 使用 FlowLayout 来水平排列按钮

        JButton loginButton = new JButton("Login");
        JButton sendButton = new JButton("Send");
        buttonPanel.add(loginButton);
        buttonPanel.add(sendButton);

        underPanel.add(buttonPanel, BorderLayout.SOUTH);  // 将按钮面板放置在南部

        // 添加组件到面板
        panel.add(comboBox, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(underPanel, BorderLayout.SOUTH);

        // 下拉框事件
        comboBox.addActionListener(e -> {
            ContactsElement selected = (ContactsElement) comboBox.getSelectedItem();
            if (selected != null) {
                selected.setNewMsg(false);
                updateTextArea(selected.getContacts().getUsername());
            }
        });
        comboBox.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                updateComboBox(comboBox.getSelectedIndex());
            }
        });

        // 按钮事件
        sendButton.addActionListener(e -> {
            ContactsElement selected = (ContactsElement) comboBox.getSelectedItem();
            if (selected == null) {
                return;
            }
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                // 执行耗时任务
                String text = textField.getText();
                textField.setText("");
                String msgId = MessageTools.randomMessageId();
                Message message = Message.builder().isSend(false)
                        .id(msgId)
                        .content(text)
                        .plaintext(text)
                        .createTime(TimeUtil.formatTimestamp(TimeUtil.nowMills()))
                        .fromUsername(robot.getUserName())
                        .toUsername(selected.getContacts().getUsername())
                        .msgType(WxRespConstant.WXReceiveMsgCodeEnum.MSG_TYPE_TEXT.getCode())
                        .fromNickname(robot.getNickName())
                        .progress(50)
                        .messageTime(LocalDateTime.now())
                        .deleted(false)
                        .isSend(true)
                        .isNeedToResend(false)
                        .build();
                MessageTools.sendMsgByUserId(robot, message);
                newMessage(message);
            });
        });
        loginButton.addActionListener(e -> {
            if (robot == null) {
                robot = RobotManager.inst().createRobot();
            }
            if (robot.getStatusEnum() != RobotStatusEnum.IDLE) {
                return;
            }
            try {
                BufferedImage bufferedImage = robot.getQR();
                qrFrame = QRUtils.createFrame(bufferedImage);
                GameEventBus.inst().register(new IEventListener() {
                    @Override
                    public Set<IEventEnum> getInterestedEvent() {
                        return Set.of(RobotEventEnum.QR_SUCCESS);
                    }

                    @Override
                    public void onEvent(IEvent iEvent) {
                        qrFrame.dispose();
                    }

                    @Override
                    public int getPriority() {
                        return 0;
                    }
                });
                GameEventBus.inst().register(new IEventListener() {
                    @Override
                    public Set<IEventEnum> getInterestedEvent() {
                        return Set.of(RobotEventEnum.LOGIN_SUCCESS);
                    }

                    @Override
                    public void onEvent(IEvent iEvent) {
                        updateComboBox();
                    }

                    @Override
                    public int getPriority() {
                        return 0;
                    }
                });

            } catch (Exception ignore) {
                return;
            }

            robot.register(new MessageHandler() {
                @Override
                public void handle(Message message) {
                    newMessage(message);
                }
            });

            robot.register(new MessageHandler() {
                @Override
                public void handle(Message message) {
                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    // 通过 StatusBar 获取 StatusBarWidgetFactory 实例
                    StatusBarWidget statusBarWidget = statusBar.getWidget(WechatStatusBarWidget.ID);
                    if (statusBarWidget instanceof WechatStatusBarWidget wechatStatusBarWidget) {
                        String fromNickName = message.getFromNickname();
                        if (fromNickName.length() > 3) {
                            fromNickName = fromNickName.substring(0, 3);
                        }
                        wechatStatusBarWidget.setText(fromNickName + ":" + message.getFromMemberOfGroupNickname() + ":" + message.getContent());
                    }
                }
            });
        });

        // 将面板添加到ToolWindow
        toolWindow.getComponent().add(panel);
    }

    private void runApplication() {
        try {
            Application.run(RobotApplication.class, new String[]{}, new Applier() {
                @Override
                public void apply() {
                    BaseConfig config = Application.config();
                    config.getSundryMap().put("PACK_CLASS_LOADER", this.getClass().getClassLoader());
                    ManagerBase.initAllManagers();
                    Application.addShutdownHook(ManagerBase::shutdown);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateTextArea(String selected) {
        List<Message> messageList = contentMap.getOrDefault(selected, Collections.emptyList());
        StringBuilder sb = new StringBuilder();
        for (Message message : messageList) {
            String fromNickName = message.getFromNickname();
            if (fromNickName.length() > 3) {
                fromNickName = fromNickName.substring(0, 3);
            }
            sb.append(fromNickName).append(":").append(message.getFromMemberOfGroupNickname()).append(":").append(message.getContent()).append("\n");
        }
        textArea.setText(sb.toString());
    }

    private void updateComboBox(int index) {
        Set<String> recentContacts = robot.getRecentContacts();
        DefaultComboBoxModel<ContactsElement> model = (DefaultComboBoxModel<ContactsElement>) comboBox.getModel();
        ContactsElement selectElement = null;
        if (index < model.getSize()) {
            selectElement = model.getElementAt(index);
        }
        model.removeAllElements();
        List<ContactsElement> sortList = new ArrayList<>();
        Map<String, Contacts> memberMap = robot.getMemberMap();
        for (String option : recentContacts) {
            Contacts contacts = memberMap.get(option);
            if (contacts == null) {
                continue;
            }
            if (selectElement != null && selectElement.getContacts().getUsername().equals(contacts.getUsername())) {
                continue;
            }
            sortList.add(new ContactsElement(contacts));
        }
        sortList.sort(Comparator.comparing(ContactsElement::isNewMsg));
        if (selectElement != null) {
            model.addElement(selectElement);
            model.setSelectedItem(selectElement);
        }
        for (ContactsElement contactsElement : sortList) {
            model.addElement(contactsElement);
        }
        if (selectElement == null) {
            comboBox.setSelectedIndex(index); // 默认选中第一个选项
        }
    }

    private void updateComboBox() {
        updateComboBox(0);
    }

    private void newMessage(Message message) {
        List<Message> messageList = this.contentMap.computeIfAbsent(message.getFromUsername(), k -> new LinkedList<>());
        messageList.add(message);
        if (messageList.size() > 100) {
            messageList = messageList.subList(50, messageList.size());
            this.contentMap.put(message.getToUsername(), messageList);
        }
        ContactsElement selected = (ContactsElement) comboBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        String username = selected.getContacts().getUsername();
        if (username.equals(message.getFromUsername()) || username.equals(message.getToUsername())) {
            updateTextArea(username);
        } else if (!username.equals(message.getToUsername())) {
            // 遍历 JComboBox 中的元素
            for (int i = 0; i < comboBox.getModel().getSize(); i++) {
                ContactsElement element = comboBox.getModel().getElementAt(i);
                element.setNewMsg(true);
            }
        }
    }

    private void test() {

    }
}
