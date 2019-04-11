package com.example.mylocationdemo;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BDLocationListener {

    private Context mContext;
    private RxPermissions rxPermissions;

    private Button btnStart;
    private TextView tvLocationInfo;

    private LocationClient mLocationClient;

    private int grantedPermissionNum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseDataInit();
        bindViews();
        viewsAddListener();
        viewsDataInit();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }

    private void baseDataInit() {
        mContext = this;
        mLocationClient = new LocationClient(mContext);
        rxPermissions = new RxPermissions(MainActivity.this);
    }

    private void bindViews() {
        btnStart = findViewById(R.id.Main_btnStart);
        tvLocationInfo = findViewById(R.id.Main_tvLocationInfo);
    }

    private void viewsAddListener() {
        btnStart.setOnClickListener(this);
        mLocationClient.registerNotifyLocationListener(this);
    }

    private void viewsDataInit() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Main_btnStart: {
                checkUserAllPermissions();
                break;
            }
            default:break;
        }
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        initLocation();
        mLocationClient.start();
    }

    /**
     * 定时更新位置（每隔5s）
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //强制使用GPS
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        //显示详细地址信息
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        new MyLocationAsyncTask().execute(bdLocation);
    }

    /**
     * 获取定位信息的异步任务
     */
    class MyLocationAsyncTask extends AsyncTask<BDLocation, Void, String> {
        @Override
        protected String doInBackground(BDLocation... bdLocations) {
            BDLocation bdLocation = bdLocations[0];
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS\n");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络\n");
            }
            //以下显示详细信息
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省份：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("城市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            return currentPosition.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(mContext, "更新完成", Toast.LENGTH_SHORT).show();
            tvLocationInfo.setText(s);
        }
    }

    /**
     * 申请用户权限
     */
    private void checkUserAllPermissions() {
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            grantedPermissionNum ++;
                        } else if (permission.shouldShowRequestPermissionRationale) {
                        } else {
                            Toast.makeText(mContext, "定位服务需要您同意相关权限", Toast.LENGTH_SHORT).show();
                        }
                        if (grantedPermissionNum == 3) {
                            startLocation();
                        }
                    }
                });
    }
}
