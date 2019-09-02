package com.totorotec.yueyueqipai.wxapi;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//import com.tencent.mm.sdk.openapi.BaseReq;
//import com.tencent.mm.sdk.openapi.BaseResp;
//import com.tencent.mm.sdk.openapi.BaseResp.ErrCode;
//import com.tencent.mm.sdk.openapi.ConstantsAPI;
//import com.tencent.mm.sdk.openapi.IWXAPI;
//import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
//import com.tencent.mm.sdk.openapi.SendAuth;
//import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.totorotec.yueyueqipai.Constants;
import com.totorotec.yueyueqipai.WXAPI;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	// IWXAPI �ǵ�����app��΢��ͨ�ŵ�openapi�ӿ�
    private IWXAPI _api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
           finish();
           return;
        }
        //setContentView(R.layout.plugin_entry);
    	_api = WXAPIFactory.createWXAPI(this,null); 
    	_api.registerApp(Constants.APP_ID);
        _api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        _api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.e("onReq", "onReq.................." + req);
		/*
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			//goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			//goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
		*/
		this.finish();
	}

	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		Log.e("onResp", "resp.................." + resp);
		Log.e("onResp", "resp.erroCode.................." + resp.errCode);
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			if(WXAPI.isLogin){
				SendAuth.Resp authResp = (SendAuth.Resp)resp;
				if(authResp != null && authResp.code != null){
					Log.e("onResp", "cc.vv.anysdkMgr.onLoginResp..................:" + authResp.code);
					final String cc = authResp.code;
					Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
					    @Override
					    public void run() {
							Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onLoginResp('"+ cc +"','"+Constants.CHANNEL+"')");
					    }                                
					});
					//Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onLoginResp('"+ authResp.code +"')");
				}				
			}else{
                Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onShareSuccess()");
                    }
                });
			}
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = 2;//R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = 3;//R.string.errcode_deny;
			break;
		default:
			result = 4;//R.string.errcode_unknown;
			break;
		}
		this.finish();
		
		//Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}
}
