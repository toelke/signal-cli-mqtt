package org.asamk.signal.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asamk.signal.util.Util;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.whispersystems.signalservice.api.push.exceptions.EncapsulatedExceptions;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.groups.NotAGroupMemberException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler to react on send requests from the broker on {@link #MQTT_TOPIC_SEND}.
 */
public class MqttMsgToSignalBridge extends AbstractMqttMessageHandler {

    private final Manager manager;
    private final ObjectMapper json = new ObjectMapper();
    private static final String MQTT_TOPIC_SEND = "signal-cli/messages/send";

    public MqttMsgToSignalBridge(Manager manager) {
        this.manager = manager;
        addTopicForSubscription(MQTT_TOPIC_SEND
                + "/" + MqttUtils.stripIllegalTopicCharacters(manager.getUsername()));
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        JsonNode jsonMessage = json.readTree(message.toString());

        List<String> recipients = new ArrayList<>();

        if (jsonMessage.has("recipient")) {
            JsonNode recipientNode = jsonMessage.get("recipient");
            recipients.add(recipientNode.textValue());
        }

        if (jsonMessage.has("recipients")) {
            JsonNode valuesNode = jsonMessage.get("recipients");
            for (JsonNode node : valuesNode) {
                recipients.add(node.asText());
            }
        }

        String groupId = null;
        if (jsonMessage.has("groupId")) {
            JsonNode valuesNode = jsonMessage.get("groupId");
            groupId = valuesNode.asText();
            System.out.println("Found groupdId: " + groupId);
        }

        JsonNode messageNode = jsonMessage.get("message");
        String messageText = messageNode.textValue();

        final List<String> attachments = new ArrayList<>();

        try {
            if (recipients.size() > 0) {
                manager.sendMessage(messageText, attachments, recipients);
            }
            if (groupId != null) {
                System.out.println("Trying to send to group: " + groupId);
                try {
                    manager.sendGroupMessage(messageText, attachments, Util.decodeGroupId(groupId));
                } catch (NotAGroupMemberException ex) {
                    System.err.println("User not in group " + groupId);
                }
            }
        //} catch (EncapsulatedExceptions encapsulatedExceptions) {
        //    encapsulatedExceptions.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
        }
    }
}
