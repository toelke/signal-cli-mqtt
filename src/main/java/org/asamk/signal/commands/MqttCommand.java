package org.asamk.signal.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.mqtt.MqttSendMessageHandler;
import org.asamk.signal.mqtt.MqttTopicClient;
import org.asamk.signal.mqtt.ReceivedMessageMqttBridge;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.asamk.signal.util.ErrorUtils.handleAssertionError;

/**
 * Command to start a daemon that connects to an Mqtt Broker and waits for incoming messages to provide them to
 * the broker. Additionally the daemon waits for send requests and sends them through Signal.
 */
public class MqttCommand implements LocalCommand {

    private static final String DEFAULT_MQTT_BROKER = "tcp://127.0.0.1:1883";

    @Override
    public void attachToSubparser(final Subparser subparser) {
        subparser.addArgument("-b", "--broker")
                .help("The broker to connect to, default: " + DEFAULT_MQTT_BROKER)
                .action(Arguments.store());
    }

    @Override
    public int handleCommand(final Namespace ns, final Manager m) {
        if (!m.isRegistered()) {
            System.err.println("User is not registered.");
            return 1;
        }

        String brokerInput = ns.getString("broker");
        String broker = brokerInput != null ? brokerInput : DEFAULT_MQTT_BROKER;

        MqttTopicClient mqttTopicClient = null;
        try {
            mqttTopicClient = new MqttTopicClient(broker);
            mqttTopicClient.connect();

            System.out.println("Connected: " + mqttTopicClient.isConnected());

            MqttSendMessageHandler sendHandler = new MqttSendMessageHandler(m);
            mqttTopicClient.addHandler(sendHandler);

            boolean ignoreAttachments = false;
            try {
                m.receiveMessages(1,
                        TimeUnit.HOURS,
                        false,
                        ignoreAttachments,
                        new ReceivedMessageMqttBridge(m, mqttTopicClient));
                return 0;
            } catch (IOException e) {
                System.err.println("Error while receiving messages: " + e.getMessage());
                return 3;
            } catch (AssertionError e) {
                handleAssertionError(e);
                return 1;
            }
        } catch (MqttException me) {
            System.err.println("Error while handling mqtt: " + me.getMessage());
            me.printStackTrace();
            return 1;
        } catch (Exception ex) {
            System.err.println("Error: " + ex);
            ex.printStackTrace();
            return 1;
        } finally {
            if (mqttTopicClient != null) {
                try {
                    System.out.println("Closing mqtt connection");
                    mqttTopicClient.disconnect();
                    return 0;
                } catch (MqttException me) {
                    System.err.println("Error while closing mqtt connection: " + me.getMessage());
                    return 1;
                }
            }
        }
    }
}
