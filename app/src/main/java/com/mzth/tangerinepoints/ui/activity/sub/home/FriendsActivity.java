package com.mzth.tangerinepoints.ui.activity.sub.home;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.mzth.tangerinepoints.R;
import com.mzth.tangerinepoints.bean.ContactsBean;
import com.mzth.tangerinepoints.bean.FriendBean;
import com.mzth.tangerinepoints.bean.RFriendBean;
import com.mzth.tangerinepoints.bean.UnregisteredBean;
import com.mzth.tangerinepoints.common.Constans;
import com.mzth.tangerinepoints.common.MainApplication;
import com.mzth.tangerinepoints.common.SharedName;
import com.mzth.tangerinepoints.ui.activity.base.BaseBussActivity;
import com.mzth.tangerinepoints.ui.activity.sub.LoginActivity;
import com.mzth.tangerinepoints.ui.adapter.sub.FriendAdapter;
import com.mzth.tangerinepoints.ui.adapter.sub.RegisteredUserAdapter;
import com.mzth.tangerinepoints.util.GsonUtil;
import com.mzth.tangerinepoints.util.NetUtil;
import com.mzth.tangerinepoints.util.SharedPreferencesUtil;
import com.mzth.tangerinepoints.util.StringUtil;
import com.mzth.tangerinepoints.util.ToastUtil;
import com.mzth.tangerinepoints.util.WeiboDialogUtils;
import com.mzth.tangerinepoints.widget.MyListView;
import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/19.
 * 通讯录
 */

public class FriendsActivity extends BaseBussActivity {
    private ImageView iv_back;
    private TextView tv_title,tv_friend_phone;
    private MyListView lv_members,lv_members_no;
    private EditText et_search;
    private NestedScrollView scroll_friends;
    private ArrayList<ContactsBean> list;//获取手机联系人的电话号码
    private ContactsBean bean;//手机联系人
    private HashMap<String,String> nameMap;//这个集合是为了  根据 电话号码 找到对应的 名字
    private Dialog mWeiboDialog;//加载的动画
    @Override
    protected void setCustomLayout(Bundle savedInstanceState) {
        super.setCustomLayout(savedInstanceState);
        _context = FriendsActivity.this;
        setContentView(R.layout.activity_friends);
    }

    @Override
    protected void initView() {
        super.initView();
        //已经注册的联系人
        lv_members = (MyListView) findViewById(R.id.lv_members);
        //没有注册的联系人
        lv_members_no = (MyListView) findViewById(R.id.lv_members_no);
        //搜索联系人
        et_search = (EditText) findViewById(R.id.et_search);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        // 强制隐藏软键盘
//        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
//        et_search.setInputType(InputType.TYPE_NULL);

        scroll_friends = (NestedScrollView) findViewById(R.id.scroll_friends);
        //返回键
        iv_back = (ImageView) findViewById(R.id.iv_back);
        //标题
        tv_title= (TextView) findViewById(R.id.tv_title);
        //显示一下  获取到的所有联系人
        tv_friend_phone = (TextView) findViewById(R.id.tv_friend_phone);
        scroll_friends.setFocusable(true);
        lv_members.setFocusable(false);
        lv_members_no.setFocusable(false);
    }

    @Override
    protected void initData() {
        super.initData();

        //设置标题
        tv_title.setText("Friends");
        //Constans.hideSoftKeyboard(et_search,_context);
        //检查是否拥有读取联系人的权限
        int permissionCheck = ContextCompat.checkSelfPermission(_context,
                Manifest.permission.READ_CONTACTS);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions
                       (_context,new String[]{Manifest.permission.
                               READ_CONTACTS},1);
        }else {

            try {
                //获取手机联系人
                GetContacts();
            } catch (SecurityException e) {
                //e.printStackTrace();
                //跳入到设置权限的页面
                //AndPermission.defaultSettingDialog(_context, 1).show();
                ToastUtil.showShort(_context, "Please check that you have this permission");
            }
            int count = (Integer) SharedPreferencesUtil.getParam(_context,SharedName.ContactsNum,0);
            String contact = (String) SharedPreferencesUtil.getParam(_context,SharedName.Contacts,"");
            if(!StringUtil.isEmpty(contact)){//先查看本地是否有联系人信息
                //判断用户是否添加联系人
                if(count != list.size()){
                    FriendRequest();
                }else {
                    String contacts = GsonUtil.getJsonFromKey(contact, "contacts");
                    String registered = GsonUtil.getJsonFromKey(contacts, "registered");
                    String unregistered = GsonUtil.getJsonFromKey(contacts, "unregistered");
                    List<RFriendBean> list = GsonUtil.getListFromJson(registered, new TypeToken<List<RFriendBean>>() {
                    });
                    List<UnregisteredBean> list2 = GsonUtil.getListFromJson(unregistered, new TypeToken<List<UnregisteredBean>>() {
                    });
                    //注册的用户
                    lv_members.setAdapter(new RegisteredUserAdapter(_context, list, R.layout.friends_item, nameMap));
                    //未注册的用户
                    lv_members_no.setAdapter(new FriendAdapter(_context, list2, R.layout.friends_no_item, nameMap, Authorization));
                }
                }else {
                if (!StringUtil.isEmpty(list)) {//判断这个集合是否为空
                    FriendRequest();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    GetContacts();//获取联系人信息
                    if(!StringUtil.isEmpty(list)) {//判断这个集合是否为空
                        FriendRequest();
                    }
                    //ToastUtil.showShort(_context,"Permissions set successfully");
                }else{
                    ToastUtil.showShort(_context,"Permission settings failed");
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void BindComponentEvent() {
        super.BindComponentEvent();
        iv_back.setOnClickListener(myclick);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = et_search.getText().toString();
                if(!StringUtil.isEmpty(search)){
                    SearchRequest();
                }else{
                    FriendRequest();
                }

            }
        });
    }

    private View.OnClickListener myclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_back://返回键
                    onBackPressed();
                    break;
            }
        }
    };
    //发现通讯录中已经注册的朋友
    private void FriendRequest(){
        mWeiboDialog = WeiboDialogUtils.createLoadingDialog(_context, "Loading...");
        Map<String,Object> map =new HashMap<String,Object>();
        String phone_num="";//初次得到的手机
        String phone="";//多个电话号码的字符串，逗号为分隔符
        for (int i = 0 ; i<list.size(); i++){
            phone_num = list.get(i).getPhone().trim()+",";
            phone +=phone_num;
        }
        //传入多个电话的时候phone.substring(0,phone.length()-1)是为了删掉最后一个,号
        String SumPhone = phone.substring(0,phone.length()-1);
        Log.e("SumPhone------------",SumPhone);
        map.put("phone_numbers",SumPhone);
        NetUtil.Request(NetUtil.RequestMethod.POST, Constans.COUPONS_PHONE_NUMBERS, map, Authorization, MainApplication.APP_INSTANCE_ID, new NetUtil.RequestCallBack() {
            @Override
            public void onSuccess(int statusCode, String json) {
                Log.e("json------------------",json);
                SharedPreferencesUtil.setParam(_context, SharedName.Contacts,json);
                //保存联系人数量
                SharedPreferencesUtil.setParam(_context,SharedName.ContactsNum,list.size());
                String contacts = GsonUtil.getJsonFromKey(json,"contacts");
                String registered = GsonUtil.getJsonFromKey(contacts,"registered");
                String unregistered = GsonUtil.getJsonFromKey(contacts,"unregistered");
                List<RFriendBean> list = GsonUtil.getListFromJson(registered,new TypeToken<List<RFriendBean>>(){});
                List<UnregisteredBean> list2 = GsonUtil.getListFromJson(unregistered,new TypeToken<List<UnregisteredBean>>(){});
                //注册的用户
                lv_members.setAdapter(new RegisteredUserAdapter(_context,list,R.layout.friends_item,nameMap));
                //未注册的用户
                lv_members_no.setAdapter(new FriendAdapter(_context,list2,R.layout.friends_no_item,nameMap,Authorization));
                WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }
            @Override
            public void onFailure(int statusCode, String errorMsg) {
                ToastUtil.showShort(_context,errorMsg);
                if(statusCode==401){
                    ToastUtil.showLong(_context,"For security reason, please login again.");
                    startActivity(LoginActivity.class,null);
                    onBackPressed();
                }
                WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ToastUtil.showShort(_context,errorMsg);
                WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }
        });

    }
    //搜索联系人
    private void SearchRequest() {
        //mWeiboDialog = WeiboDialogUtils.createLoadingDialog(_context, "Loading...");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("phone_numbers", et_search.getText().toString().trim());
        NetUtil.Request(NetUtil.RequestMethod.POST, Constans.COUPONS_PHONE_NUMBERS, map, Authorization, MainApplication.APP_INSTANCE_ID, new NetUtil.RequestCallBack() {
            @Override
            public void onSuccess(int statusCode, String json) {
                SharedPreferencesUtil.setParam(_context, SharedName.Contacts, json);
                String contacts = GsonUtil.getJsonFromKey(json, "contacts");
                String registered = GsonUtil.getJsonFromKey(contacts, "registered");
                String unregistered = GsonUtil.getJsonFromKey(contacts, "unregistered");
                List<RFriendBean> list = GsonUtil.getListFromJson(registered, new TypeToken<List<RFriendBean>>() {
                });
                List<UnregisteredBean> list2 = GsonUtil.getListFromJson(unregistered, new TypeToken<List<UnregisteredBean>>() {
                });
                //注册的用户
                lv_members.setAdapter(new RegisteredUserAdapter(_context, list, R.layout.friends_item, nameMap));
                //未注册的用户
                lv_members_no.setAdapter(new FriendAdapter(_context, list2, R.layout.friends_no_item, nameMap, Authorization));
                //WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }

            @Override
            public void onFailure(int statusCode, String errorMsg) {
                ToastUtil.showShort(_context, errorMsg);
                if (statusCode == 401) {
                    ToastUtil.showLong(_context, "For security reason, please login again.");
                    startActivity(LoginActivity.class, null);
                    onBackPressed();
                }
                //WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ToastUtil.showShort(_context, errorMsg);
                //WeiboDialogUtils.closeDialog(mWeiboDialog);//关闭动画
            }
        });
    }
    //获取手机联系人
    private void GetContacts(){
        nameMap = new HashMap<String,String>();
        list = new ArrayList<ContactsBean>();
        ContactsBean bean = null;
        Cursor cursor = null;
        String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        cursor = _context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    cols, null, null, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                // 取得联系人名字
                String name = cursor.getString(nameFieldColumnIndex);
                Log.e("name------------------",name);
                // 取得联系人电话
                String number = cursor.getString(numberFieldColumnIndex);
                Log.e("number------------------",number);
                bean = new ContactsBean();
                bean.setName(name);
                //去除中间空格
                String t=" ";
                String phone = number.replace(t,"");
                //判断是否包含+86
                String s = "+86";
                String phone_number="";//带有+86的手机号
                if(phone.indexOf("+86")!=-1){
                    phone_number = phone.replace(s,"");
                    bean.setPhone(phone_number);
                    nameMap.put(phone_number,name);
                }else if(phone.indexOf("+")!=-1){
                    phone_number = phone.replace("+","");
                    bean.setPhone(phone_number);
                    nameMap.put(phone_number,name);
                }else if(phone.indexOf("-")!=-1){
                    phone_number = phone.replace("-","");
                    bean.setPhone(phone_number);
                    nameMap.put(phone_number,name);
                }else{
                    bean.setPhone(phone);
                    //将电话号码 和  姓名保存在map中
                    nameMap.put(phone,name);
                }
                list.add(bean);
                //Log.e("listsize----------",list.size()+"");
                //保存联系人的数量

            }
            cursor.close();//使用完后一定要将cursor关闭，不然会造成内存泄露等问题
            int listsize = list.size();




    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
