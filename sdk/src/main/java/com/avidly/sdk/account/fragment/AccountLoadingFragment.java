package com.avidly.sdk.account.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.avidly.sdk.account.base.Constants;
import com.avidly.sdk.account.base.utils.LogUtils;
import com.avidly.sdk.account.base.utils.ThreadHelper;
import com.avidly.sdk.account.data.user.Account;
import com.avidly.sdk.account.data.user.LoginUser;
import com.avidly.sdk.account.data.user.LoginUserManager;
import com.avidly.sdk.account.listener.AccountLoadingListener;
import com.sdk.avidly.account.R;

public class AccountLoadingFragment extends Fragment implements View.OnClickListener {
    private AccountLoadingListener mLoadingListener;
    private View mLoginLayout;
    private View mLoadingIcon;
    private View mSwitchUser;

    Runnable timeCountRun = new Runnable() {
        @Override
        public void run() {
            startAutoLogin();
        }
    };

    public void setLoadingListener(AccountLoadingListener listener) {
        mLoadingListener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.avidly_fragment_loading, container, false);
        mLoginLayout = view.findViewById(R.id.avidly_logining_layout);
        mLoadingIcon = view.findViewById(R.id.avidly_icon_loading);
        mSwitchUser = view.findViewById(R.id.avidly_switch_user);
        mSwitchUser.setOnClickListener(this);

        mLoginLayout.setVisibility(View.GONE);
        mSwitchUser.setVisibility(View.VISIBLE);

        TextView userNameText = view.findViewById(R.id.avidly_user_name);
        LoginUser activedUser = LoginUserManager.getCurrentActiveLoginUser();
        if (activedUser != null) {
            boolean isGuest = activedUser.getLoginedMode() == Account.ACCOUNT_MODE_GUEST;
            if (isGuest) {
                userNameText.setText(getString(R.string.avidly_string_usermanger_guest));
            } else {
                userNameText.setText(activedUser.findActivedAccount().nickname);
            }
        }

        ThreadHelper.runOnMainThread(timeCountRun, Constants.AUTO_LOGIN_TIME_OUT_MILLS);

        return view;
    }

    private void startAutoLogin() {
        RotateAnimation rotateAnim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotateAnim.setInterpolator(lin);
        rotateAnim.setDuration(500);//设置动画持续周期
        rotateAnim.setRepeatCount(-1);//设置重复次数
        rotateAnim.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        mLoadingIcon.setAnimation(rotateAnim);

        mLoginLayout.setVisibility(View.VISIBLE);
        mSwitchUser.setVisibility(View.GONE);

        if (mLoadingListener != null) {
            mLoadingListener.onAutoLoginWaitingTimeOut();
        }
    }

    @Override
    public void onClick(View view) {
        synchronized (Thread.currentThread()) {
            if (timeCountRun != null) {
                try {
                    ThreadHelper.removeOnMainThread(timeCountRun);
                } catch (Exception e) {
                    timeCountRun = null;
                }
            }


        }

        int id = view.getId();
        if (mLoadingListener == null) {

            return;
        }

        if (id == R.id.avidly_switch_user) {
            mLoadingListener.onSwitchAccountClicked();
        }
    }
}
