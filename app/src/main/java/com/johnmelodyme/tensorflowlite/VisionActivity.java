package com.johnmelodyme.tensorflowlite;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class VisionActivity extends AppCompatActivity {
    private static final String TAG = "VisionActivity";


    private void ui_component() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ui_component();
    }


}