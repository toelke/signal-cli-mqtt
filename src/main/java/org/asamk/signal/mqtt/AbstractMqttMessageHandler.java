package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class to handle subscription of topics with multiple handlers.
 */
public abstract class AbstractMqttMessageHandler implements MqttMessageHandler {

    private final List<String> topicPatterns = new LinkedList<>();

    /**
     * Adds a topic the handler will be subscribed to. The topic can include wildcards.
     *
     * @param topicPattern the topic to subscribe to
     */
    protected final void addTopic(String topicPattern) {
        topicPatterns.add(topicPattern);
    }

    @Override
    public Collection<String> getTopics() {
        return topicPatterns;
    }

    @Override
    public boolean matchesTopic(final String topic) {
        for (String topicPattern : topicPatterns) {
            if (MqttTopic.isMatched(topicPattern, topic)) {
                return true;
            }
        }
        return false;
    }
}
