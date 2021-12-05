package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Receiver implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    public Receiver() {
    }

    @Override
    public void onMessage(Message message) {
        LOG.info("-- Receiving message: " + new String(message.getBody()));
    }

    @Override
    public void containerAckMode(AcknowledgeMode mode) {
        MessageListener.super.containerAckMode(mode);
    }

    @Override
    public void onMessageBatch(List<Message> messages) {
        MessageListener.super.onMessageBatch(messages);
    }
}
