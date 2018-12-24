package org.asamk.signal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMqttMessageHandler implements MqttMessageHandler {

    private List<String> topicPatterns = new LinkedList<String>();

    protected final void addTopic(String topicPattern) {
        topicPatterns.add(topicPattern);
    }

    public Collection<String> getTopics() {
        return topicPatterns;
    }

    @Override
    public boolean matchesTopic(final String topic) {
        for (String topicPattern : topicPatterns) {
            System.out.println("Pattern: " + topicPattern + " Topic: " + topic);
            if (MqttTopic.isMatched(topicPattern, topic)) {
                return true;
            }
        }
        return false;
    }
}
