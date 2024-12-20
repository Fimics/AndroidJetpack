/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package com.hnradio.fans.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cn.jiguang.share.wechat.WeChatHandleActivity;

/** 微信客户端回调activity示例 */
public class WXEntryActivity extends WeChatHandleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int type = intent.getIntExtra("_wxapi_command_type", 0);
        Log.e("Ivanwu","type:" + type);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int type = intent.getIntExtra("_wxapi_command_type", 0);
        Log.e("Ivanwu","type:" + type);
    }
}
