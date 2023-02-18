package com.hnradio.fans.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.manager.ACTION_WX_PAY_RESULT
import com.hnradio.common.util.ToastUtils
import com.hwangjr.rxbus.RxBus
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WXPayEntryActivity : Activity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, UrlConstant.WX_APPID)
        api?.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api?.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq?) {}

    override fun onResp(resp: BaseResp) {
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            if (resp.errCode == 0) {
                //RxBus.get().post(ACTION_WX_PAY_RESULT, resp.errStr)
                val intent = Intent(ACTION_WX_PAY_RESULT)
                intent.putExtra("errCode", resp.errCode)
                sendBroadcast(intent)
                finish()
            } else {
                ToastUtils.show("支付失败")
                finish()
            }
        }
    }
}