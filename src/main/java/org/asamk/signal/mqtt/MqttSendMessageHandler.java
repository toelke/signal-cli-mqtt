package org.asamk.signal.mqtt;

import org.asamk.signal.manager.Manager;
import org.eclipse.paho.client.mqttv3.*;
import org.whispersystems.signalservice.api.push.exceptions.EncapsulatedExceptions;

import java.util.ArrayList;
import java.util.List;

public class MqttSendMessageHandler implements MqttCallback {

    private final Manager manager;
    private final MqttAsyncClient mqttClient;

    public MqttSendMessageHandler(Manager manager, MqttAsyncClient mqttClient)
    {
        this.manager = manager;
        this.mqttClient = mqttClient;
        try {
            mqttClient.subscribe("signal-cli/messages/send", 1);
            mqttClient.setCallback(this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(final Throwable cause) {
        System.err.println("Connection to Mqtt lost");
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        System.out.println(topic);
        final List<String> attachments = new ArrayList<>();

        try {
            manager.sendMessage(message.toString(), attachments, "+4917691403039");
        } catch (EncapsulatedExceptions encapsulatedExceptions) {
            encapsulatedExceptions.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {
        System.out.println("mqtt delivered id: " + token.getMessageId());
    }
}
