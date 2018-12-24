package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttMessageHandler {

    /**
     * Must return true if the handler matches the topic.
     * @param topic the topic that should be checked
     * @return true if the topic is matched by the handler, false if not
     */
    boolean matchesTopic(String topic);

    /**
     * After {@link #matchesTopic(String)} let the topic pass through it #messageArrived has to process it.
     * @param topic
     * @param message
     * @throws Exception
     */
    void messageArrived(String topic, MqttMessage message) throws Exception;
}
