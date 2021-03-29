package com.smartwasp.assistant.app.jpush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class PushMessageReceiver extends JPushMessageReceiver{
    private static final String TAG = "PushMessageReceiver";
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        Log.d(TAG,"[onMessage] "+customMessage);
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        Log.d(TAG,"[onNotifyMessageOpened] "+message);

    }

    @Override
    public void onMultiActionClicked(Context context, Intent intent) {
        Log.d(TAG, "[onMultiActionClicked] 用户点击了通知栏按钮");
        String nActionExtra = intent.getExtras().getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA);

        //开发者根据不同 Action 携带的 extra 字段来分配不同的动作。
        if(nActionExtra==null){
            Log.d(TAG,"ACTION_NOTIFICATION_CLICK_ACTION nActionExtra is null");
            return;
        }
        if (nActionExtra.equals("my_extra1")) {
            Log.d(TAG, "[onMultiActionClicked] 用户点击通知栏按钮一");
        } else if (nActionExtra.equals("my_extra2")) {
            Log.d(TAG, "[onMultiActionClicked] 用户点击通知栏按钮二");
        } else if (nActionExtra.equals("my_extra3")) {
            Log.d(TAG, "[onMultiActionClicked] 用户点击通知栏按钮三");
        } else {
            Log.d(TAG, "[onMultiActionClicked] 用户点击通知栏按钮未定义");
        }
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
        Log.d(TAG,"[onNotifyMessageArrived] "+message);
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage message) {
        Log.d(TAG,"[onNotifyMessageDismiss] "+message);
    }

    @Override
    public void onRegister(Context context, String registrationId) {
        Log.d(TAG,"[onRegister] "+registrationId);
    }

    @Override
    public void onConnected(Context context, boolean isConnected) {
        Log.d(TAG,"[onConnected] "+isConnected);
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        Log.d(TAG,"[onCommandResult] "+cmdMessage);
    }

    @Override
    public void onTagOperatorResult(Context context,JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
    }
    @Override
    public void onCheckTagOperatorResult(Context context,JPushMessage jPushMessage){
        super.onCheckTagOperatorResult(context, jPushMessage);
    }
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public void onNotificationSettingsCheck(Context context, boolean isOn, int source) {
        super.onNotificationSettingsCheck(context, isOn, source);
        Log.d(TAG,"[onNotificationSettingsCheck] isOn:"+isOn+",source:"+source);
    }
    
}
