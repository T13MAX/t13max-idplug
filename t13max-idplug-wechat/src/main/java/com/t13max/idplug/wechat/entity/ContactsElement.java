package com.t13max.idplug.wechat.entity;

import com.t13max.wxbot.entity.Contacts;
import lombok.Getter;
import lombok.Setter;

/**
 * @author t13max
 * @since 14:16 2024/12/17
 */
public class ContactsElement {

    private boolean newMsg;

    private final Contacts contacts;

    public ContactsElement(Contacts contacts) {
        this.contacts = contacts;
    }

    public boolean isNewMsg() {
        return newMsg;
    }

    public void setNewMsg(boolean newMsg) {
        this.newMsg = newMsg;
    }

    public Contacts getContacts() {
        return contacts;
    }

    @Override
    public String toString() {
        String remarkName = contacts.getRemarkname();
        return (newMsg?"(*)":"")+(remarkName != null && !remarkName.isEmpty() ? remarkName : contacts.getNickname());
    }
}
