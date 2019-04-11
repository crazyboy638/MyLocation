package com.example.mylocationdemo;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MapActivity extends AppCompatActivity {

    private Context mContext;
    private RxPermissions rxPermissions;

    private MapView mapView;

    private LocationClient mLocationClient;
    private BaiduMap baiduMap;

    private int grantedPermissionNum = 0;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化操作一定要在setContentView之前！！！
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        rxPermissions = new RxPermissions(MapActivity.this);
        mContext = this;
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerNotifyLocationListener(new MyLocationListener());

        mapView = findViewById(R.id.Map_mapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        //设置定位图标是否有箭头
        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true,null));

        checkUserAllPermissions();
        mLocationClient.start();
        initLocation();
    }

    /**
     * 定时更新位置（每隔5s）
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //强制使用GPS
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        //显示详细地址信息
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            new MyLocationAsyncTask().execute(bdLocation);
        }
    }

    /**
     * 获取定位信息的异步任务
     */
    class MyLocationAsyncTask extends AsyncTask<BDLocation, Void, Void> {
        @Override
        protected Void doInBackground(BDLocation... bdLocations) {
            BDLocation bdLocation = bdLocations[0];
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation
                    || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }
            return null;
        }
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData = builder.build();
        baiduMap.setMyLocationData(locationData);
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

                        }
                    }
                });
    }
}
