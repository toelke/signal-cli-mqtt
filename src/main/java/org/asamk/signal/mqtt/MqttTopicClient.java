package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import java.util.LinkedList;
import java.util.List;

public class MqttTopicClient implements MqttCallback {

    private final MqttAsyncClient mqttClient;
    private final String brokerUrl;
    private List<MqttMessageHandler> messageHandlers = new LinkedList<MqttMessageHandler>();
    private static int QUALITY_OF_SERVICE = 2;

    public MqttTopicClient(String brokerUrl) throws MqttException {
        this.brokerUrl = brokerUrl;
        // connect to mqtt
        mqttClient = new MqttAsyncClient(brokerUrl, "signal-cli");
        mqttClient.setCallback(this);
    }

    public IMqttToken publish(String topic, MqttMessage message) throws MqttException {
        return mqttClient.publish(topic, message);
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public IMqttToken disconnect() throws MqttException {
        return mqttClient.disconnect();
    }

    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker: " + brokerUrl);
        mqttClient.connect(connOpts).waitForCompletion();
    }

    @Override
    public void connectionLost(final Throwable cause) {

    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        System.out.println("Received message on " + topic);
        for (MqttMessageHandler handler : messageHandlers) {
            if(handler.matchesTopic(topic))
            {
                handler.messageArrived(topic, message);
            }
        }
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {

    }

    public void addHandler(final MqttSendMessageHandler handler) throws MqttException {
        messageHandlers.add(handler);
        for(String topic : handler.getTopics()) {
            System.out.println("Subscribing to " + topic);
            mqttClient.subscribe(topic, QUALITY_OF_SERVICE).waitForCompletion();
        }
    }
}
