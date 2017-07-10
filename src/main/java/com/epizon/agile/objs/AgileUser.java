package com.epizon.agile.objs;

import java.io.Serializable;

/**
 * Created by Comp12 on 31-Jul-16.
 */
public class AgileUser implements Serializable{

    String status = "", email = "", name = "";
    AgileMessage lastMessage;
    String unreadCount = "";
    Boolean isTyping = false;

    public AgileUser(){
    }

    public AgileUser(String email, String name, String status){
        this.email = email;
        this.name = name;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String id) {
        this.status = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AgileMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(AgileMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUnreadCount(String unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getUnreadCount() {
        return unreadCount;
    }

    public Boolean getTyping() {
        return isTyping;
    }

    public void setTyping(Boolean typing) {
        isTyping = typing;
    }
}
