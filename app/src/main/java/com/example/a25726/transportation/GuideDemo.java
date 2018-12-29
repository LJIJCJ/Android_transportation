package com.example.a25726.transportation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.baidu.navi.sdkdemo.EventHandler;
import com.baidu.navi.sdkdemo.newif.DemoGuideActivity;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.map.BNItemizedOverlay;
import com.baidu.navisdk.adapter.map.BNOverlayItem;

public class GuideDemo extends AppCompatActivity {

    private static final String TAG = DemoGuideActivity.class.getName();

    private static final int MSG_RESET_NODE = 3;

    private BNRoutePlanNode mBNRoutePlanNode = null;

    private IBNRouteGuideManager mRouteGuideManager;

    private Handler hd = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createHandler();
        setContentView(R.layout.activity_guide_demo);

        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        View view = mRouteGuideManager.onCreate(this, mOnNavigationListener);

        if (view != null) {
            setContentView(view);
        }
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mBNRoutePlanNode = (BNRoutePlanNode)
                        bundle.getSerializable(NaviDemo.ROUTE_PLAN_NODE);
            }
        }

        routeGuideEvent();
    }

    private void routeGuideEvent(){
        EventHandler.getInstance().getDialog(this);
        EventHandler.getInstance().showDialog();

        BaiduNaviManagerFactory.getRouteGuideManager().setRouteGuideEventListener(
                new IBNRouteGuideManager.IRouteGuideEventListener() {
                    @Override
                    public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
                        EventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
                    }
                }
        );
    }
    @Override
    protected void onStart() {
        super.onStart();
        mRouteGuideManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRouteGuideManager.onResume();
        // 自定义图层
        showOverlay();
    }

    private void showOverlay() {
        BNOverlayItem item =
                new BNOverlayItem(2563047.686035, 1.2695675172607E7, BNOverlayItem.CoordinateType.BD09_MC);
        BNItemizedOverlay overlay = new BNItemizedOverlay(
                GuideDemo.this.getResources().getDrawable(R.drawable
                        .navi_guide_turn));
        overlay.addItem(item);
        overlay.show();
    }

    protected void onPause() {
        super.onPause();
        mRouteGuideManager.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mRouteGuideManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRouteGuideManager.onDestroy(false);
        EventHandler.getInstance().disposeDialog();
    }

    @Override
    public void onBackPressed() {
        mRouteGuideManager.onBackPressed(false, true);
    }

    private void createHandler(){
        if (hd == null) {
            hd = new Handler(getMainLooper()){
                public void handleMessage(android.os.Message msg){
                    if (msg.what == MSG_RESET_NODE) {
                        mRouteGuideManager.resetEndNodeInNavi(
                                new BNRoutePlanNode(116.21142, 40.85087, "百度大厦11",
                                        null, BNRoutePlanNode.CoordinateType.GCJ02));
                    }
                }
            };
        }
    }




    private IBNRouteGuideManager.OnNavigationListener mOnNavigationListener =
            new IBNRouteGuideManager.OnNavigationListener() {

                @Override
                public void onNaviGuideEnd() {
                    // 退出导航
                    finish();
                }

                @Override
                public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {
                    if (actionType == 0) {
                        // 导航到达目的地 自动退出
                        Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
                        mRouteGuideManager.forceQuitNaviWithoutDialog();
                    }
                }
            };
}
