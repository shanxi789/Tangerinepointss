package com.mzth.tangerinepoints.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
//import android.support.multidex.MultiDex;

import com.mzth.tangerinepoints.bean.UserBean;
import com.mzth.tangerinepoints.util.SharedPreferencesUtil;
import com.mzth.tangerinepoints.util.StringUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class MainApplication extends Application {

    private static ArrayList<Activity> list = new ArrayList<Activity>();

    private static Context context;
    //用户的对象
    private static UserBean userBean;

    public static String APP_INSTANCE_ID;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        String uuid = UUID.randomUUID().toString();
        if (StringUtil.isEmpty((String) SharedPreferencesUtil.getParam(context, SharedName.UUID, ""))) {
            //将UUID保存到本地
            SharedPreferencesUtil.setParam(context, SharedName.UUID, uuid);
            //APP_INSTANCE_ID = (String) SharedPreferencesUtil.getParam(context, SharedName.UUID, "");
        }
            APP_INSTANCE_ID = (String) SharedPreferencesUtil.getParam(context, SharedName.UUID, "");

    }
    /**
     * 添加Activity到集合中
     */
    public void addActivity(Activity activity) {
        list.add(activity);
    }
    public static UserBean getUserBean() {
        return userBean;
    }

    public static void setUserBean(UserBean userBean) {
        MainApplication.userBean = userBean;
    }

    public Context getContext() {
        return context;
    }
    // 用于存放倒计时时间
    public static Map<String, Long> map;
    /**
     * 从集合中移除Activity
     */
    public void removeActivity(Activity activity) {
        list.remove(activity);
    }

    /**
     * 关闭所有的Activity
     */
    public static void closeActivity() {
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
    }


//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(context);
//    }
}
