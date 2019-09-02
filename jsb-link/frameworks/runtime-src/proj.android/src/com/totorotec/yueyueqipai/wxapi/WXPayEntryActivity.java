package com.totorotec.yueyueqipai.wxapi;



import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.totorotec.yueyueqipai.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			switch (resp.errCode) {
			case 0:
				Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
				    @Override
				    public void run() {
						Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onPaymentCompleteWX('','','')");
				    }                                
				});
				break;
			case -1:
				Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
				    @Override
				    public void run() {
						Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onPaymentError()");
				    }                                
				});
				break;
			case -2:
				Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
				    @Override
				    public void run() {
				        Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onPaymentCancel()");
				    }                                
				});
			break;

			default:
				break;
			}
		}

		//用完记得关闭，不然这个activity会一直覆盖在上面
		this.finish();
	}
}