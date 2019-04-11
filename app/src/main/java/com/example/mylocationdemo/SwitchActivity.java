package com.example.mylocationdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SwitchActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

    private Button btnLocation, btnMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        mContext = this;
        btnLocation = findViewById(R.id.Switch_btnLocation);
        btnMap = findViewById(R.id.Switch_btnMap);

        btnLocation.setOnClickListener(this);
        btnMap.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Switch_btnLocation: {
                startActivity(new Intent(mContext, MainActivity.class));
                break;
            }
            case R.id.Switch_btnMap: {
                startActivity(new Intent(mContext, MapActivity.class));
                break;
            }
            default:break;
        }
    }
}
