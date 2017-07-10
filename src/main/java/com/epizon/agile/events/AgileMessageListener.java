package com.epizon.agile.events;

import com.epizon.agile.objs.AgileMessage;

/**
 * Created by Comp12 on 04-Aug-16.
 */
public interface AgileMessageListener {
    void onMessageReceivedEvent(AgileMessage receivedMessage);
    void onMessageSentEvent(AgileMessage sentMessage);
    void onMessageDeliveredEvent(AgileMessage deliveredMessage);
    void onMessageReadEvent(AgileMessage readMessage);
    void onMessageSendFailedEvent();
}
