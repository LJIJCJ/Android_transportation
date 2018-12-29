package com.example.a25726.transportation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapclient.liteapp.LiteActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int SDK_PERMISSION_REQUEST = 127;

    private MapView myMapView = null;//地图控件
    private BaiduMap myBaiduMap;//百度地图对象
    private LocationClient myLocationClient;//定位服务客户对象
    private MylocationListener myListener;//重写的监听类
    private Context context;

    private double myLatitude;//纬度，用于存储自己所在位置的纬度
    private double myLongitude;//经度，用于存储自己所在位置的经度
    private float myCurrentX;
    private double endLatitude;
    private double endLongitude;
    private String endAddress;

    private BitmapDescriptor myIconLocation1;//图标1，当前位置的箭头图标
//    private BitmapDescriptor myIconLocation2;//图表2,前往位置的中心图标

    private MyOrientationListener myOrientationListener;//方向感应器类对象

    private MyLocationConfiguration.LocationMode locationMode;//定位图层显示方式
////    private MyLocationConfiguration.LocationMode locationMode2;//定位图层显示方式

    private LinearLayout myLinearLayout2; //地址搜索区域2

    private LinearLayout mLinearLayout3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            }
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (permissions.size() != 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }else{
                initView();
                initLocation();
            }
        }
    }
    private void initView() {
        myMapView = findViewById(R.id.baiduMapView);

        myBaiduMap = myMapView.getMap();
        //根据给定增量缩放地图级别
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(18.0f);
        myBaiduMap.setMapStatus(msu);
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        myLocationClient = new LocationClient(this);
        myListener = new MylocationListener();

        //注册监听器
        myLocationClient.registerLocationListener(myListener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();//SDK配置参数
        //设置调用位置描述信息
        mOption.setIsNeedLocationDescribe(true);
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        int span = 1000;
        mOption.setScanSpan(span);
        //设置 LocationClientOption
        myLocationClient.setLocOption(mOption);

        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        myIconLocation1 = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
//        myIconLocation2 = BitmapDescriptorFactory.fromResource(R.drawable.icon_target);

        //配置定位图层显示方式,三个参数的构造器
        MyLocationConfiguration configuration
                = new MyLocationConfiguration(locationMode, true, myIconLocation1);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
        myBaiduMap.setMyLocationConfigeration(configuration);

        myOrientationListener = new MyOrientationListener(context);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });

        myBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myLinearLayout2 = findViewById(R.id.linearLayout2);
                mLinearLayout3 = findViewById(R.id.guide);
                myLinearLayout2.setVisibility(View.GONE);
                mLinearLayout3.setVisibility(View.GONE);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    /*
     *创建菜单操作
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            /*
             *第一个功能，返回自己所在的位置，箭头表示
             */
            case R.id.menu_item_mylocation://返回当前位置
                myBaiduMap.clear();
                getLocationByLL(myLatitude, myLongitude);
                break;

            /*
             *第二个功能，根据地址名前往所在的位置
             */
            case R.id.menu_item_sitesearch://根据地址搜索
                myLinearLayout2 = findViewById(R.id.linearLayout2);
                //显示地址搜索区域2
                myLinearLayout2.setVisibility(View.VISIBLE);
                final EditText myEditText_site = findViewById(R.id.editText_site);
                Button button_site = findViewById(R.id.button_sitesearch);

                button_site.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String site_str = myEditText_site.getText().toString();
                        if(site_str.equals("")){
                            Toast.makeText(MainActivity.this,"请输入地址",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Const.EndName = site_str;
                        GeoCoder mSearch = GeoCoder.newInstance();

                        OnGetGeoCoderResultListener lister = new OnGetGeoCoderResultListener() {
                            @Override
                            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                                if(geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                                    Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                endLatitude = geoCodeResult.getLocation().latitude;
                                endLongitude = geoCodeResult.getLocation().longitude;

                                Const.EndLatitude = endLatitude;
                                Const.EndLongitude = endLongitude;
                                String str = String.format("纬度：" + endLatitude + "经度" + endLongitude);
                                //获取地理编码结果
                                Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
                                getLocInfoByLL(endLatitude,endLongitude);

                                myBaiduMap.clear();
                                getLocationByLL(endLatitude,endLongitude);
                                BitmapDescriptor bitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.icon_marka);
                                OverlayOptions options = new MarkerOptions()
                                        .position(new LatLng(endLatitude,endLongitude))
                                        .icon(bitmapDescriptor);
                                myBaiduMap.addOverlay(options);
                            }

                            @Override
                            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                                    Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                endAddress = reverseGeoCodeResult.getAddress();
                                Const.EndAddress = endAddress;

                                Toast.makeText(MainActivity.this,endAddress,Toast.LENGTH_SHORT).show();
                            }
                        };
                        mSearch.setOnGetGeoCodeResultListener(lister);
                        mSearch.geocode(new GeoCodeOption().city(Const.City).address(site_str));
                        mSearch.destroy();

                        //隐藏前面地址输入区域
                        myLinearLayout2.setVisibility(View.GONE);
                        //隐藏输入法键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    }
                });
                break;
            case R.id.menu_item_test:
                mLinearLayout3 = findViewById(R.id.guide);
                mLinearLayout3.setVisibility(View.VISIBLE);
                Button BtGo = findViewById(R.id.bt_go);
                Button BtChange = findViewById(R.id.bt_change);
                BtGo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ToDo
                        if (Const.EndAddress.equals(null)) {
                            Toast.makeText(MainActivity.this,"请输入目的地",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this,String.format("%f,%f"+Const.EndAddress,Const.EndLatitude,Const.EndLongitude),Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this,String.format("%f,%f"+Const.StartAddress,Const.StartLatitude,Const.StartLongitude),Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,LiteActivity.class);
                        startActivity(intent);
                    }
                });

                BtChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLinearLayout3.setVisibility(View.GONE);

                        myLinearLayout2 = findViewById(R.id.linearLayout2);
                        //显示地址搜索区域2
                        myLinearLayout2.setVisibility(View.VISIBLE);
                        final EditText myEditText_site = findViewById(R.id.editText_site);
                        Button button_site = findViewById(R.id.button_sitesearch);

                        button_site.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String site_str = myEditText_site.getText().toString();
                                GeoCoder mSearch = GeoCoder.newInstance();

                                OnGetGeoCoderResultListener lister = new OnGetGeoCoderResultListener() {
                                    @Override
                                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                                        if(geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                                            Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        endLatitude = geoCodeResult.getLocation().latitude;
                                        endLongitude = geoCodeResult.getLocation().longitude;

                                        Const.EndLatitude = endLatitude;
                                        Const.EndLongitude = endLongitude;
                                        String str = String.format("纬度：" + endLatitude + "经度" + endLongitude);
                                        //获取地理编码结果
                                        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
                                        getLocInfoByLL(endLatitude,endLongitude);

                                        myBaiduMap.clear();
                                        getLocationByLL(endLatitude,endLongitude);
                                        BitmapDescriptor bitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.icon_marka);
                                        OverlayOptions options = new MarkerOptions()
                                                .position(new LatLng(endLatitude,endLongitude))
                                                .icon(bitmapDescriptor);
                                        myBaiduMap.addOverlay(options);
                                    }

                                    @Override
                                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                        if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                                            Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        endAddress = reverseGeoCodeResult.getAddress();
                                        Const.EndAddress = endAddress;

                                        Toast.makeText(MainActivity.this,endAddress,Toast.LENGTH_SHORT).show();
                                    }
                                };
                                mSearch.setOnGetGeoCodeResultListener(lister);
                                mSearch.geocode(new GeoCodeOption().city(Const.City).address(site_str));
                                mSearch.destroy();

                                //隐藏前面地址输入区域
                                myLinearLayout2.setVisibility(View.GONE);
                                //隐藏输入法键盘
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                            }
                        });
                    }
                });
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void getLocInfoByLL(double la,double lg){
        LatLng latLng = new LatLng(la,lg);
        GeoCoder mSearch = GeoCoder.newInstance();
        OnGetGeoCoderResultListener lister = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if(geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                    Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                    return;
                }
                endLatitude = geoCodeResult.getLocation().latitude;
                endLongitude = geoCodeResult.getLocation().longitude;
                Const.EndLatitude = endLatitude;
                Const.EndLongitude = endLongitude;
                String str = String.format("纬度：" + endLatitude + "经度" + endLongitude);
                //获取地理编码结果
                Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();

                myBaiduMap.clear();
                getLocationByLL(endLatitude,endLongitude);
                BitmapDescriptor bitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.icon_marka);
                OverlayOptions options = new MarkerOptions()
                        .position(new LatLng(endLatitude,endLongitude))
                        .icon(bitmapDescriptor);
                myBaiduMap.addOverlay(options);
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                    Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_SHORT).show();
                    return;
                }

                endAddress = reverseGeoCodeResult.getAddress();
                Const.EndAddress = endAddress;

                Toast.makeText(MainActivity.this,endAddress,Toast.LENGTH_SHORT).show();
            }
        };
        mSearch.setOnGetGeoCodeResultListener(lister);
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        mSearch.destroy();
    }

    /*
     *根据经纬度前往
     */
    public void getLocationByLL(double la, double lg)
    {
        //地理坐标的数据结构
        LatLng latLng = new LatLng(la, lg);
        //描述地图状态将要发生的变化,通过当前经纬度来使地图显示到该位置

        MyLocationConfiguration configuration
                = new MyLocationConfiguration(locationMode, true, myIconLocation1);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
        myBaiduMap.setMyLocationConfigeration(configuration);

        myOrientationListener = new MyOrientationListener(context);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });

        MapStatus mapStatus = new MapStatus.Builder()
                .target(latLng)
                .zoom(18)
                .build();
        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mapStatus);


        myBaiduMap.setMapStatus(msu);
    }

    /*
     *定位请求回调接口
     */
    public class MylocationListener implements BDLocationListener
    {
        //定位请求回调接口
        private boolean isFirstIn=true;
        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //BDLocation 回调的百度坐标类，内部封装了如经纬度、半径等属性信息
            //MyLocationData 定位数据,定位数据建造器
            /*
             * 可以通过BDLocation配置如下参数
             * 1.accuracy 定位精度
             * 2.latitude 百度纬度坐标
             * 3.longitude 百度经度坐标
             * 4.satellitesNum GPS定位时卫星数目 getSatelliteNumber() gps定位结果时，获取gps锁定用的卫星数
             * 5.speed GPS定位时速度 getSpeed()获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
             * 6.direction GPS定位时方向角度
             * */
            //String locationDescribe = bdLocation.getLocationDescribe();

            myLatitude = bdLocation.getLatitude();
            myLongitude = bdLocation.getLongitude();
            Const.StartAddress = bdLocation.getAddrStr();
            Const.StartName = bdLocation.getBuildingName();

            Const.City = bdLocation.getCity();
            Const.StartLatitude = myLatitude;
            Const.StartLongitude = myLongitude;

            MyLocationData data = new MyLocationData.Builder()
                    .direction(myCurrentX)//设定图标方向
                    .accuracy(bdLocation.getRadius())//getRadius 获取定位精度,默认值0.0f
                    .latitude(myLatitude)//百度纬度坐标
                    .longitude(myLongitude)//百度经度坐标
                    .build();
            //设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
            myBaiduMap.setMyLocationData(data);

            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                //根据当前所在位置经纬度前往
                getLocationByLL(myLatitude, myLongitude);
                isFirstIn = false;
                //提示当前所在地址信息
                Toast.makeText(context, bdLocation.getAddrStr(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch(requestCode){
            case SDK_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                    initLocation();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /*
     *定位服务的生命周期，达到节省
     */
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位，显示位置图标
        myBaiduMap.setMyLocationEnabled(true);
        if(!myLocationClient.isStarted())
        {
            myLocationClient.start();
        }
        myOrientationListener.start();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        myBaiduMap.setMyLocationEnabled(false);
        myLocationClient.stop();
        /*myOrientationListener.stop();*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        myMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        myMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myMapView.onDestroy();
    }
}
