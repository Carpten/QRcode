package com.ysq.qrcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ysq.qrlib.QrFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QrFragment qrFragment = (QrFragment) getSupportFragmentManager().findFragmentById(R.id.qr_fragment);
        qrFragment.setOnCodeGetListener(new QrFragment.OnCodeGetListener() {
            @Override
            public void onCodeGet(String code) {
                Toast.makeText(MainActivity.this, "code:" + code, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
