package com.qty.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.qty.log.QTLog;

public class MainActivity extends AppCompatActivity {

    private QTLog Log = new QTLog(MainActivity.class);

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("onCreate...");

        findViewById(R.id.exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Name length = " + name.length());
            }
        });
    }
}