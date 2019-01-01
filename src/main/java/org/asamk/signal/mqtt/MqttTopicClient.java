package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import java.util.LinkedList;
import java.util.List;

/**
 * MqttClient that can handle incoming messages from multiple topics and publish messages.
 */
public class MqttTopicClient implements MqttCallback {

    private final MqttAsyncClient mqttClient;
    private final String brokerUrl;
    private List<MqttMessageHandler> messageHandlers = new LinkedList<>();
    private static int QUALITY_OF_SERVICE = 2;

    /**
     * Instantiates a new client to receive and publish messages on a Mqtt Broker.
     *
     * @param brokerUrl broker url e.g. tcp://127.0.0.1:1883 or ssl://127.0.0.1:1883
     * @throws MqttException when the creation of the instance fails
     */
    public MqttTopicClient(String brokerUrl) throws MqttException {
        this.brokerUrl = brokerUrl;
        mqttClient = new MqttAsyncClient(brokerUrl, "signal-cli");
        mqttClient.setCallback(this);
    }

    /**
     * Publishes a message under the given topic.
     *
     * @param topic   the topic to publish the message to
     * @param message the actual message content
     * @return a token to wait on the result
     * @throws MqttException if the publishing failed
     */
    public IMqttToken publish(String topic, MqttMessage message) throws MqttException {
        return mqttClient.publish(topic, message);
    }

    /**
     * Returns the state of the connection.
     *
     * @return true if the client is connected, false if not
     */
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    /**
     * Disconnects from the broker.
     *
     * @return a token to wait on or evaluate the result
     * @throws MqttException if the disconnect process failed
     */
    public IMqttToken disconnect() throws MqttException {
        return mqttClient.disconnect();
    }

    /**
     * Connects to a broker.
     *
     * @throws MqttException if the connection failed
     */
    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker: " + brokerUrl);
        mqttClient.connect(connOpts).waitForCompletion();
    }

    @Override
    public void connectionLost(final Throwable cause) {
        System.err.println("Connection dropped from mqtt: " + cause.getMessage());
        cause.printStackTrace();
        try {
            connect();
            for (MqttMessageHandler handler : messageHandlers) {
                subscribeHandler(handler);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        System.out.println("Received message on " + topic);
        for (MqttMessageHandler handler : messageHandlers) {
            if (handler.matchesTopic(topic)) {
                handler.messageArrived(topic, message);
            }
        }
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {

    }

    private void subscribeHandler(MqttMessageHandler handler) throws MqttException {
        for (String topic : handler.getTopics()) {
            System.out.println("Subscribing to " + topic);
            mqttClient.subscribe(topic, QUALITY_OF_SERVICE).waitForCompletion();
        }
    }

    /**
     * Adds a new message handler for specific topics to the client.
     *
     * @param handler the handler to add
     * @throws MqttException if the handler's topics could not be subscribed
     */
    public void addHandler(final MqttMessageHandler handler) throws MqttException {
        if (messageHandlers.contains(handler)) {
            // is added already
            return;
        }
        messageHandlers.add(handler);
        subscribeHandler(handler);
    }
}
