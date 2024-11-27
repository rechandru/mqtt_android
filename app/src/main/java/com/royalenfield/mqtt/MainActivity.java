package com.royalenfield.mqtt;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    String TAG = "Main Activity";

    /*

    String user_name = "reos_test";
    String password = "123456789";
    String topic = "reos_data";

    String broker = "tcp://wa81da6a.ala.dedicated.aws.emqxcloud.com:1883";
    String clientId = "reos_123";
     */

    String user_name = "pd2admin";
    String password = "pd2admin@";
    String topic = "pd/vehicle/data";

    String broker = "tcp://i7e1598e.ala.dedicated.gcp.emqxcloud.com:1883";
    String clientId = "poc_pd2admin";

    char[] sample_data = new char[] { 0xA5, 0xA5, //start of the frame
            0x0D, 0x01, // size and sending device id
            0, 0, 0x03, 0x61, // can id
            0xB8, 0x02, 0, 0, 0x41, 0, 0, 0, // payload data
            0, //checksum
            0x5A, 0x5A // end of the frame
    };

    int qos = 2;

    MemoryPersistence persistence = new MemoryPersistence();
    MqttConnectOptions connOpts = new MqttConnectOptions();

    MqttClient sampleClient;
    Button send_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        connOpts.setCleanSession(true);
        connOpts.setUserName(user_name);
        connOpts.setPassword(password.toCharArray());
        connOpts.setAutomaticReconnect(true);

        send_data = findViewById(R.id.send_data);
        send_data.setOnClickListener(v -> {
            sendData(sample_data);
        });
    }

    private void createConnection() {
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            Log.d(TAG, "Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            Log.d(TAG, "Connected");

            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection lost "+cause.getMessage());
                    try {
                        sampleClient.reconnect();
                        Log.d(TAG, "Reconnected");
                    } catch (MqttException e) {
                        Log.d("Reconnection error", e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Receive message: "+message.toString()+" from topic: "+topic);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Delivery Complete");
                }
            });
        } catch (MqttException me) {
            printException(me);
        }
    }

    private void sendData(char[] charArray) {
        try {
            Log.d(TAG, "Publishing message: " + Arrays.toString(charArray));

            byte[] byteArray = new byte[charArray.length];
            for (int i = 0; i < charArray.length; i++) {
                byteArray[i] = (byte) charArray[i];
            }

            String str = new String(byteArray, StandardCharsets.UTF_8); // for UTF-8 encoding

            Log.d(TAG, "Receiver Side : " + str);

            MqttMessage message = new MqttMessage(byteArray);
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Log.d(TAG, "Message published");
        } catch (MqttException me) {
            printException(me);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            sampleClient.disconnect();
        } catch (MqttException e) {
            printException(e);
        }
    }

    private void printException(MqttException me) {

        Log.d(TAG, "reason "+me.getReasonCode());
        Log.d(TAG, "msg "+me.getMessage());
        Log.d(TAG, "loc "+me.getLocalizedMessage());
        Log.d(TAG, "cause "+me.getCause());
        Log.d(TAG, "excep "+me);
        me.printStackTrace();
    }
}