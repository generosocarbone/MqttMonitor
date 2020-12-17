package it.systemslab.mqttonbiottest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements Cloud2DeviceMessageCallback{

    private TextView messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = findViewById(R.id.messages);
        MqttHelper.getInstance(this, this);
    }

    @Override
    public void newMessage(String newMessage) {
        messages.post(() -> {
            String s = messages.getText().toString();
            String newText = Calendar.getInstance().getTime().toString()
                    .concat(": ")
                    .concat(newMessage)
                    .concat("\n")
                    .concat(s);
            messages.setText(newText);
        });
    }
}