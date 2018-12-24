package org.asamk.signal.mqtt;

public final class MqttUtils {

    private MqttUtils() {
        // hide constructor
    }


    /**
     * Removes spaces and wildcard signs (*, +) from a given string.
     * @param topic the topic to clean
     * @return the cleaned topic
     */
    public static String stripIllegalTopicCharacters(String topic)
    {
        return topic.replace("+", "")
                .replace(" ", "");
    }
}
