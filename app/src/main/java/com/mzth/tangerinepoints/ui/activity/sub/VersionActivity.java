package com.mzth.tangerinepoints.ui.activity.sub;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mzth.tangerinepoints.R;
import com.mzth.tangerinepoints.common.Constans;
import com.mzth.tangerinepoints.ui.activity.base.BaseBussActivity;
import com.mzth.tangerinepoints.ui.activity.sub.git.GoogleMapActivity;
import com.mzth.tangerinepoints.ui.activity.sub.git.PhotoPreviewActivity;
import com.mzth.tangerinepoints.util.GsonUtil;
import com.mzth.tangerinepoints.util.NetUtil;
import com.mzth.tangerinepoints.util.ToastUtil;
import com.mzth.tangerinepoints.util.WeiboDialogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.data;

/**
 * Created by lenovo on 2017/5/19.
 * 版本页面
 */

public class VersionActivity extends BaseBussActivity {
    private TextView tv_title,tv_version,tv_update;
    private ImageView iv_back;
    private RelativeLayout rl_version;
    private String version;
    @Override
    protected void setCustomLayout(Bundle savedInstanceState) {
        super.setCustomLayout(savedInstanceState);
        _context = VersionActivity.this;
        setContentView(R.layout.activity_version);
    }

    @Override
    protected void initView() {
        super.initView();
        //设置标题
        tv_title = (TextView) findViewById(R.id.tv_title);
        //返回键
        iv_back = (ImageView) findViewById(R.id.iv_back);
        //版本更新
        rl_version = (RelativeLayout) findViewById(R.id.rl_version);
        //版本号
        tv_version = (TextView) findViewById(R.id.tv_version);
        //已经是最新版本  状态
        tv_update = (TextView) findViewById(R.id.tv_update);
    }
    @Override
    protected void initData() {
        super.initData();
        //设置标题
        tv_title.setText("Version Update");
        tv_version.setText("V"+getVersionName());
        GetVersion();//获得当前版本号
    }
    @Override
    protected void BindComponentEvent() {
        super.BindComponentEvent();
        iv_back.setOnClickListener(myonclick);
        rl_version.setOnClickListener(myonclick);
    }

    private View.OnClickListener myonclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_back://返回键监听
                    onBackPressed();
                    break;
                case R.id.rl_version://版本更新
                    if(tv_update.getText().toString().equals("This is the latest version.")){
                        ToastUtil.showShort(_context,"It's the latest version, no need to update");
                    }else{
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException e) {
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("play.google.com/store/apps/details?id=" + appPackageName)));
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("play.google.com/store/apps/details?id=com.Facebook.katana")));
                            Uri uri = Uri.parse("play.google.com/store/apps/details?id=com.Facebook.katana");
                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            startActivity(intent);
                        }
                        //如果发现新版本跳入那个链接
//                        Uri uri = Uri.parse("https://fir.im/g5mq");
//                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
//                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    //获取当前应用的版本号：
    private String getVersionName(){
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }

    //获得服务器版本号
    private void GetVersion(){
        final Dialog dialog = WeiboDialogUtils.createLoadingDialog(_context,"Loading...");
        NetUtil.Request(NetUtil.RequestMethod.GET, Constans.GETVERSION, null, null, null, new NetUtil.RequestCallBack() {
            @Override
            public void onSuccess(int statusCode, String json) {
                version = GsonUtil.getJsonFromKey(json,"current");
                String ver = version.substring(0,3);
                //如果当前版本与服务器上的版本相同
                if(getVersionName().equals(ver)){
                    tv_update.setText("This is the latest version.");
                }else{
                    tv_update.setText("New version available!");
                }
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }

            @Override
            public void onFailure(int statusCode, String errorMsg) {
                ToastUtil.showShort(_context,errorMsg);
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ToastUtil.showShort(_context,errorMsg);
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }
        });
    }

}
