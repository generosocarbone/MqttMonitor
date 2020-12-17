package it.systemslab.mqttonbiottest;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.concurrent.atomic.AtomicInteger;

public class MqttHelper implements IMqttActionListener, MqttCallbackExtended {

    private MqttAndroidClient mqttAndroidClient;
    private static final String TAG = "MqttHelper";
    private static MqttHelper instance;
    private Cloud2DeviceMessageCallback cloud2DeviceMessageCallback;
    private Context context;
    private boolean subscribed = false;
    private AtomicInteger coda = new AtomicInteger(0);

    private MqttHelper(Context context, Cloud2DeviceMessageCallback callback){
        this.context = context;
        this.cloud2DeviceMessageCallback = callback;
        String clientId = MqttAsyncClient.generateClientId();
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(
                String.format(
                        "%s/BeamWatch/%s",
                        context.getFilesDir().getAbsolutePath(),
                        clientId
                )
        );
        mqttAndroidClient = new MqttAndroidClient(context, "ws://stream.lifesensor.cloud:9001", clientId, dataStore);
        mqttAndroidClient.setCallback(this);
        connect();
    }

    public static MqttHelper getInstance(Context context, Cloud2DeviceMessageCallback callback){
        if(instance == null){
            instance = new MqttHelper(context, callback);
        }
        return instance;
    }

    public void setCloud2DeviceMessageCallback(Cloud2DeviceMessageCallback cloud2DeviceMessageCallback) {
        this.cloud2DeviceMessageCallback = cloud2DeviceMessageCallback;
    }

    public int getCoda() {
        return coda.get();
    }

    private MqttConnectOptions connectOptions;

    private MqttConnectOptions buildMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout((int)(2.4*60));
        mqttConnectOptions.setKeepAliveInterval((int) (1.8*60));
        mqttConnectOptions.setUserName("GSWALOVI");
        mqttConnectOptions.setPassword("$2a$12$qTiKcFQw6rZoLjUFXb/pF.nmx9G6x2jfqK8xgVZIIagrHKSwsnzYW".toCharArray());
        return mqttConnectOptions;
    }

    public void connect(){
        if(connectOptions == null)
            connectOptions = buildMqttConnectOptions();

        try {
            mqttAndroidClient.connect(connectOptions, "CONNECT", this);

        } catch (MqttException ex){
            Log.d(TAG, "connect: mqtt exception: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        Log.d(TAG, "subscribeToTopic: subscribing");
        try {
            mqttAndroidClient.subscribe("sensorResponse", 1, "SUBSCRIBE", this);

        } catch (MqttException ex) {
            Log.d(TAG, String.format("MQTT Error while subscribing: %s", ex.getMessage()));
            ex.printStackTrace();
        }
    }

    public boolean isConnected(){
        return mqttAndroidClient.isConnected();
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Object userContext = asyncActionToken.getUserContext();
        if(userContext.equals("PUBLISH")) {
            int i = coda.decrementAndGet();
            Log.d(TAG, "onSuccess: message published: " + asyncActionToken.getResponse() + "; coda: " + i);
        } else if(userContext.equals("CONNECT")) {
            Log.d(TAG, "onSuccess: connected");
            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
            disconnectedBufferOptions.setBufferEnabled(true);
            disconnectedBufferOptions.setBufferSize(100);
            disconnectedBufferOptions.setPersistBuffer(false);
            disconnectedBufferOptions.setDeleteOldestMessages(false);
            mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

        } else if(userContext.equals("SUBSCRIBE")) {
            Log.d(TAG,"onSuccess: MQTT Subscribed!");
            subscribed = true;
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Object userContext = asyncActionToken.getUserContext();
        if(userContext.equals("PUBLISH")) {
            Log.d(TAG, "publish: onFailure: message push failure: " + exception + "; " + asyncActionToken);
        } else if(userContext.equals("CONNECT")) {
            Log.d(TAG, String.format("connect: onFailure: MQTT Failed to connect to broker: %s", exception.toString()));
            exception.printStackTrace();
        } else if(userContext.equals("SUBSCRIBE")) {
            Log.d(TAG,"subscribe: onFailure: MQTT Subscribe failed!");
        }

        if(exception != null)
            exception.printStackTrace();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost: Connection to broker lost: " + cause);
        if(cause != null) {
            cause.printStackTrace();
        }

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if(cloud2DeviceMessageCallback != null) {
            cloud2DeviceMessageCallback.newMessage(message.toString());
        } else {
            Log.e(TAG, "messageArrived: message not handled");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete: " + token.getResponse());
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "connected complete. Is it a reconnect? " + reconnect);
//        if(!subscribed)
        try {
            subscribeToTopic();
        } catch (Exception e) {
            Log.d(TAG, "connectComplete: subscribe exception: " + e.toString());
        }
    }
}
