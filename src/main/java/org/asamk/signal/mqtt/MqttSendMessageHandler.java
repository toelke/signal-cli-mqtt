package org.asamk.signal.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asamk.signal.manager.Manager;
import org.eclipse.paho.client.mqttv3.*;
import org.whispersystems.signalservice.api.push.exceptions.EncapsulatedExceptions;

import java.util.ArrayList;
import java.util.List;

public class MqttSendMessageHandler extends AbstractMqttMessageHandler {

    private final Manager manager;
    private final ObjectMapper json = new ObjectMapper();
    public static final String MQTT_TOPIC_SEND = "signal-cli/messages/send";

    public MqttSendMessageHandler(Manager manager)
    {
        this.manager = manager;
        addTopic(MQTT_TOPIC_SEND
                + "/" + MqttUtils.stripIllegalTopicCharacters(manager.getUsername()));
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        System.out.println(topic);

        JsonNode jsonMessage = json.readTree(message.toString());

        JsonNode recipientNode = jsonMessage.get("recipient");
        String recipient = recipientNode.textValue();

        JsonNode messageNode = jsonMessage.get("message");
        String messageText = messageNode.textValue();

        final List<String> attachments = new ArrayList<>();

        try {
            manager.sendMessage(messageText, attachments, recipient);
        } catch (EncapsulatedExceptions encapsulatedExceptions) {
            encapsulatedExceptions.printStackTrace();
        }
    }
}
