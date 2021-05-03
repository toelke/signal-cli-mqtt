package org.asamk.signal.mqtt;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.asamk.signal.json.JsonError;
import org.asamk.signal.json.JsonMessageEnvelope;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;

/**
 * Class that encapsulates an incoming message that can be published on an mqtt broker.
 */
public class MqttJsonMessage {

    public static final String SUBTOPIC_DATA = "data";
    public static final String SUBTOPIC_SYNC = "sync";
    public static final String SUBTOPIC_CALL = "call";
    public static final String SUBTOPIC_TYPING_INFO = "typinginfo";
    public static final String SUBTOPIC_RECEIPT = "receipt";
    public static final String SUBTOPIC_OTHER = "other";

    private static final String SUBTOPIC_ERROR = "error";

    private String subTopic;
    private String content;


    private MqttJsonMessage() {
        // hide public constructor
    }

    private void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }

    /**
     * Returns the subtopic the message fits the most to.
     *
     * @return the subtopic, e.g. data, typinginfo...
     */
    public String getSubTopic() {
        return subTopic;
    }

    private void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the json encoded message.
     *
     * @return json encoded message
     */
    public String getJsonContent() {
        return content;
    }

    /**
     * Builds a Json Message from an incoming signal message and determines the corresponding sub topic
     * for the mqtt broker.
     *
     * @param envelope  the signal service envelope of the message
     * @param content   the content of the message
     * @param exception an exception that might have occurred on the way of processing
     * @return the mqtt json message with assigned sub topic
     */
    public static MqttJsonMessage build(SignalServiceEnvelope envelope, SignalServiceContent content, Throwable exception) {
        MqttJsonMessage message = new MqttJsonMessage();

        ObjectMapper jsonProcessor = new ObjectMapper();
        jsonProcessor.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // disable autodetect
        jsonProcessor.setDefaultPropertyInclusion(
                JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.NON_NULL));

        jsonProcessor.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonProcessor.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        ObjectNode result = jsonProcessor.createObjectNode();
        if (exception != null) {
            result.putPOJO("error", new JsonError(exception));
            message.setSubTopic(SUBTOPIC_ERROR);
        }

        if (envelope != null) {
            result.putPOJO("envelope", new JsonMessageEnvelope(envelope, content, null));
            message.setSubTopic(findSubTopic(envelope, content));
        }
        try {
            message.setContent(jsonProcessor.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            ObjectNode errorMsg = jsonProcessor.createObjectNode();
            result.putPOJO("error", new JsonError(e));
            try {
                message.setSubTopic(SUBTOPIC_ERROR);
                message.setContent(jsonProcessor.writeValueAsString(errorMsg));
            } catch (JsonProcessingException jsonEx) {
                // this should never happen, but well just to be safe
                throw new AssertionError(jsonEx);
            }
        }
        return message;
    }

    /**
     * Finds the designated type of the message and defines the subtopic for the mqtt broker.
     *
     * Possible subtopics: data, synq, call, typinginfo, receipt, other
     *
     * @param envelope the envelope with meta information
     * @param content  the content of the message
     * @return the subtopic as a string
     */
    private static String findSubTopic(final SignalServiceEnvelope envelope, final SignalServiceContent content) {
        if (content != null) {
            if (content.getDataMessage().isPresent()) {
                return SUBTOPIC_DATA;
            } else if (content.getSyncMessage().isPresent()) {
                return SUBTOPIC_SYNC;
            } else if (content.getCallMessage().isPresent()) {
                return SUBTOPIC_CALL;
            } else if (content.getTypingMessage().isPresent()) {
                return SUBTOPIC_TYPING_INFO;
            } else if (content.getReceiptMessage().isPresent()) {
                return SUBTOPIC_RECEIPT;
            } else {
                return SUBTOPIC_OTHER;
            }
        } else {
            if (envelope.isReceipt()) {
                return SUBTOPIC_RECEIPT;
            } else {
                return SUBTOPIC_OTHER;
            }
        }
    }
}
