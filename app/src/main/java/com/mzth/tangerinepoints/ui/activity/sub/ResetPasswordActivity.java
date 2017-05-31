package com.mzth.tangerinepoints.ui.activity.sub;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mzth.tangerinepoints.R;
import com.mzth.tangerinepoints.common.Constans;
import com.mzth.tangerinepoints.ui.activity.base.BaseBussActivity;
import com.mzth.tangerinepoints.util.CountDownTimerUtils;
import com.mzth.tangerinepoints.util.NetUtil;
import com.mzth.tangerinepoints.util.StringUtil;
import com.mzth.tangerinepoints.util.ToastUtil;
import com.mzth.tangerinepoints.util.WeiboDialogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lenovo on 2017/5/19.
 * 找回密码
 */

public class ResetPasswordActivity extends BaseBussActivity {
    private TextView tv_title,tv_commit_phone,tv_commit_code,
            tv_find_password_commit;
    private ImageView iv_back;
    private LinearLayout ll_reset02,ll_reset01;
    private EditText et_find_phone,et_find_code,et_find_password,
            et_find_password_again;
    private CountDownTimerUtils timerUtils;
    @Override
    protected void setCustomLayout(Bundle savedInstanceState) {
        super.setCustomLayout(savedInstanceState);
        _context = ResetPasswordActivity.this;
        setContentView(R.layout.activity_reset_password01);
    }

    @Override
    protected void initView() {
        super.initView();
        //设置标题
        tv_title = (TextView) findViewById(R.id.tv_title);
        //返回键
        iv_back = (ImageView) findViewById(R.id.iv_back);
        //找回密码第二部
        ll_reset02 = (LinearLayout) findViewById(R.id.ll_reset02);
        //找回密码第一步
        ll_reset01 = (LinearLayout) findViewById(R.id.ll_reset01);
        //手机号
        et_find_phone = (EditText) findViewById(R.id.et_find_phone);
        //确认手机号
        tv_commit_phone = (TextView) findViewById(R.id.tv_commit_phone);
        //验证码
        et_find_code = (EditText) findViewById(R.id.et_find_code);
        //确认验证码
        tv_commit_code = (TextView) findViewById(R.id.tv_commit_code);
        //第一次的密码
        et_find_password = (EditText) findViewById(R.id.et_find_password);
        //第二次的密码
        et_find_password_again = (EditText) findViewById(R.id.et_find_password_again);
        //确认密码
        tv_find_password_commit = (TextView) findViewById(R.id.tv_find_password_commit);
    }
    @Override
    protected void initData() {
        super.initData();
        //设置标题
        tv_title.setText("Reset Password");
    }
    //重置密码发生短信
    private void ResetPwdSendCode(){
        final Dialog dialog = WeiboDialogUtils.createLoadingDialog(_context,"Loading...");
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("mobile_number",et_find_phone.getText().toString());
        NetUtil.Request(NetUtil.RequestMethod.GET, Constans.RESETPWDCODE, map, null, null, new NetUtil.RequestCallBack() {
            @Override
            public void onSuccess(int statusCode, String json) {
                ToastUtil.showShort(_context,json);
                //发生验证码倒计时
                timerUtils = new CountDownTimerUtils(_context, tv_commit_phone, 1000*30, 1000, "s seconds", "Press to send verification code to this phone.", false);
                timerUtils.start();
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }

            @Override
            public void onFailure(int statusCode, String errorMsg) {
                switch (errorMsg){
                    case "INVALID_MOBILE_NUMBER":
                        ToastUtil.showShort(_context,Constans.INVALID_MOBILE_NUMBER);
                        break;
                    case "MOBILE_NUMBER_COUNTRY_NOT_ALLOWED":
                        ToastUtil.showShort(_context,Constans.MOBILE_NUMBER_COUNTRY_NOT_ALLOWED);
                        break;
                    case "SMS_EXCEPTION":
                        ToastUtil.showShort(_context,Constans.SMS_EXCEPTION);
                        break;
                    default:
                        ToastUtil.showShort(_context,errorMsg);
                        break;
                }
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ToastUtil.showShort(_context,errorMsg);
                WeiboDialogUtils.closeDialog(dialog);//关闭动画
            }
        });
    }

    @Override
    protected void BindComponentEvent() {
        super.BindComponentEvent();
        iv_back.setOnClickListener(myonclick);
        tv_commit_phone.setOnClickListener(myonclick);
        tv_commit_code.setOnClickListener(myonclick);
        tv_find_password_commit.setOnClickListener(myonclick);
    }

    private View.OnClickListener myonclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_back://返回键监听
                    onBackPressed();
                    break;
                case R.id.tv_commit_phone://确认手机号
                    if(StringUtil.isEmpty(et_find_phone.getText().toString())){
                        ToastUtil.showShort(_context,"The cell phone number cannot be empty");
                        return;
                    }
                    //发生验证码
                    ResetPwdSendCode();

                    break;
                case R.id.tv_commit_code://确认验证码
                    if(StringUtil.isEmpty(et_find_phone.getText().toString())){
                        ToastUtil.showShort(_context,"The cell phone number cannot be empty");
                        return;
                    }
                    if(StringUtil.isEmpty(et_find_code.getText().toString())){
                        ToastUtil.showShort(_context,"The verification code cannot be null");
                        return;
                    }
                    //消失手机号和验证码布局
                    ll_reset01.setVisibility(View.GONE);
                    //显示设置密码页面
                    ll_reset02.setVisibility(View.VISIBLE);
                    break;
                case R.id.tv_find_password_commit://确认密码
                    String pwd = et_find_password.getText().toString();
                    String pwd02 = et_find_password_again.getText().toString();
                    if(StringUtil.isEmpty(pwd)){
                        ToastUtil.showShort(_context,"Please input a password");
                        return;
                    }
                    if(StringUtil.isEmpty(pwd02)){
                        ToastUtil.showShort(_context,"Please enter your password again");
                        return;
                    }
                    if(!pwd.equals(pwd02)){
                        ToastUtil.showShort(_context,"The password entered for the two time is inconsistent");
                        return;
                    }
                    //重置密码
                    ResetPwdRequest();
                    break;
            }
        }
    };
    //重置密码
    private void ResetPwdRequest(){
        final Dialog dialog = WeiboDialogUtils.createLoadingDialog(_context,"Loading...");
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("mobile_number",et_find_phone.getText().toString());
        map.put("verification_code",et_find_code.getText().toString());
        map.put("password",et_find_password.getText().toString());
        NetUtil.Request(NetUtil.RequestMethod.POST, Constans.RESETPWD, map, null, null, new NetUtil.RequestCallBack() {
            @Override
            public void onSuccess(int statusCode, String json) {
                ToastUtil.showShort(_context,"Password reset succeeded");
                onBackPressed();
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
