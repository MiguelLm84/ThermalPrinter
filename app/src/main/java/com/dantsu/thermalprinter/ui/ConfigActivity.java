package com.dantsu.thermalprinter.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.dantsu.thermalprinter.R;


public class ConfigActivity extends AppCompatActivity {

    public static final String PREFERENCES_DATA = "preferences_data";
    public static final String IP = "ip";
    public static final String PORT = "port";
    EditText ipAddress, portAddress;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        init();
        buttonEvent();
    }

    private void init() {

        ipAddress = (EditText) this.findViewById(R.id.edittext_tcp_ip);
        portAddress = (EditText) this.findViewById(R.id.edittext_tcp_port);
        btnSave = (Button) this.findViewById(R.id.btnConfig);
    }

    private void buttonEvent() {

        btnSave.setOnClickListener(v -> saveData());
    }

    private void saveData() {

        String ip = ipAddress.getText().toString();
        String port = portAddress.getText().toString();

        if(!ip.equals("") && !port.equals("")) {
            saveSharedPreferences(ip, port);
            //Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
            goToMainActivity(ip, port);
        }
    }

    private void saveSharedPreferences(String ip, String port) {

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES_DATA, MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IP, ip);
        editor.putString(PORT, port);
        editor.apply();
    }

    private void goToMainActivity(String ip, String port) {

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(IP, ip);
        i.putExtra(IP, port);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}