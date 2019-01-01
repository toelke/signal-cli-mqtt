package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Collection;

/**
 * Interface that should be implemented by all handlers that should handle mqtt messages.
 */
public interface MqttMessageHandler {

    /**
     * Must return true if the handler matches the topic.
     *
     * @param topic the topic that should be checked
     * @return true if the topic is matched by the handler, false if not
     */
    boolean matchesTopic(String topic);

    /**
     * Returns a list of topics - wild cards can be included.
     *
     * @return list of topics to subscribe to
     */
    Collection<String> getTopics();

    /**
     * After {@link #matchesTopic(String)} let the topic pass through it #messageArrived has to process it.
     *
     * @param topic the topic of the message
     * @param message the received message
     * @throws Exception if the message handler runs into errors
     */
    void messageArrived(String topic, MqttMessage message) throws Exception;
}
