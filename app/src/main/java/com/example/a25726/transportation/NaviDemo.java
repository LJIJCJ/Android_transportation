package com.example.a25726.transportation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NaviDemo extends AppCompatActivity {

    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private static final int authBaseRequestCode = 1;
    static final String ROUTE_PLAN_NODE = "routePlanNode";
    private String mSDCardPath = null;
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean hasInitSuccess = false;

    private BNRoutePlanNode mStartNode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsvi_demo);

        if (initDirs()) {
            initNavi();
        }
    }

    public boolean initDirs(){
        mSDCardPath = getSdCardDir();
        if(mSDCardPath == null){
            return false;
        }
        File file = new File(mSDCardPath,APP_FOLDER_NAME);
        if (!file.exists()) {
            try{
                file.mkdir();
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private String getSdCardDir(){
        if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
    private void initNavi(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr,authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int i, String s) {
                        String result;
                        if (i == 0) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + s;
                        }
                        Toast.makeText(NaviDemo.this,result,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(NaviDemo.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(NaviDemo.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        hasInitSuccess = true;

                        //ToDo
                        routePlanToNavi();
                    }

                    @Override
                    public void initFailed() {
                        Toast.makeText(NaviDemo.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == authBaseRequestCode) {
            for(int ret : grantResults){
                if(ret == 0){
                    continue;
                }else {
                    Toast.makeText(NaviDemo.this,"缺少导航基本的权限",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        }
    }

    private void routePlanToNavi(){
        if (!hasInitSuccess) {
            Toast.makeText(this, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        BNRoutePlanNode sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", "百度大厦", BNRoutePlanNode.CoordinateType.BD09LL);
        BNRoutePlanNode eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", "北京天安门", BNRoutePlanNode.CoordinateType.BD09LL);

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<>();
            list.add(sNode);
            list.add(eNode);

            BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                    list,
                    IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                    null,
                    new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                    Toast.makeText(NaviDemo.this, "算路开始", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                    Toast.makeText(NaviDemo.this, "算路成功", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                    Toast.makeText(NaviDemo.this, "算路失败", Toast.LENGTH_SHORT)
                                            .show();
                                    break;
                                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                    Toast.makeText(NaviDemo.this, "算路成功准备进入导航", Toast.LENGTH_SHORT)
                                            .show();
                                    Intent intent = new Intent(NaviDemo.this,
                                            GuideDemo.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(ROUTE_PLAN_NODE, mStartNode);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    break;
                                default:
                                    // nothing
                                    break;
                            }
                        }
                    });
        }
    }
}
