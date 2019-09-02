package com.totorotec.yueyueqipai;

import java.io.File;
import java.util.jar.JarFile;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.text.ClipboardManager;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

//import com.tencent.mm.sdk.modelpay.PayReq;
//import com.tencent.mm.sdk.openapi.IWXAPI;
//import com.tencent.mm.sdk.openapi.SendAuth;
//import com.tencent.mm.sdk.openapi.SendMessageToWX;
//import com.tencent.mm.sdk.openapi.WXAPIFactory;
//import com.tencent.mm.sdk.openapi.WXImageObject;
//import com.tencent.mm.sdk.openapi.WXMediaMessage;
//import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.model.AppData;

public class WXAPI {
	public static IWXAPI api;
	public static Activity instance;
	public static boolean isLogin = false;
	public static boolean isPay = false;
	
	public static int mwroomid;
	//经纬度
	public static double lo;
	public static double la;
	//电池电量
	public static int betVal = 100;
	
	public static LocationManager locationManager;
	
	public static void Init(Activity context){
		WXAPI.instance = context;
        // ͨ��WXAPIFactory��������ȡIWXAPI��ʵ��
		api = WXAPIFactory.createWXAPI(context,null);
        api.registerApp(Constants.APP_ID);
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        // String provider = locationManager.getBestProvider(WXAPI.createFineCriteria(),true);
        // Log.i("GPS", "GPS最佳PROVIDER获取成功"+provider); 
        // if(provider==null){
        // 	Log.i("GPS", "GPS最佳PROVIDER获取不成功，使用默认的NETWORK_PROVIDER"); 
       	//  用网络定位是最快的，而且可以使用getLastKnownLocation方法，GPS反而不行
        	String provider = LocationManager.NETWORK_PROVIDER;
        // }
        locationManager.requestLocationUpdates(provider, 2000, 5, new MyLocationListener());
        Location last = locationManager.getLastKnownLocation(provider);
        if(last!=null){
			Log.i("GPS", "GPS找到了上次的地点"+provider);
			final double lo = last.getLongitude();
			final double la = last.getLatitude();
			WXAPI.lo = lo;
			WXAPI.la = la;
        }
	}
	
	private static String buildTransaction(final String type) {
	    return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	public static void getInstallData(){
		Log.d("OpenInstall", "getInstallData...........................");
		OpenInstall.getInstall(new AppInstallAdapter() {
		    @Override
		    public void onInstall(AppData appData) {
		        //获取渠道数据
		        String channelCode = appData.getChannel();
		        //获取自定义数据
		        String bindData = appData.getData();
		        Log.d("OpenInstall", "channelCode = " + channelCode);
		        Log.d("OpenInstall", "getInstall : installData = " + appData.toString());
		    }
		});
	}
	
	//返回魔窗的房间号
	public static int getMWroomid(){
		return mwroomid;
	}
	
	//获取GPS位置
	public static String getGPS(){
		return lo+","+la;
	}
	
	//获取电池电量
	public static int getCurrentBatteryLevel(){
		return betVal;
	}
    
    //返回底层代码版本
    public static int getCodeVersion(){
        return 1;
    }
	
	//微信支付
	public static void wxPay(String prepayId,String nonceStr,String timestamp,String sign){
//		api.registerApp(Constants.APP_ID);
		PayReq request = new PayReq();
//		request.appId = "wxd930ea5d5a258f4f";
//		request.partnerId = "1900000109";
//		request.prepayId= "1101000000140415649af9fc314aa427";
//		request.packageValue = "Sign=WXPay";
//		request.nonceStr= "1101000000140429eb40476f8896f4c9";
//		request.timeStamp= "1398746574";
//		request.sign= "7FFECB600D7157C5AA49810D2D8F28BC2811827B";
		Log.d("payargs", "appID"+Constants.APP_ID);
		Log.d("payargs", "partnerId"+1483615012);
		Log.d("payargs", "prepayId"+prepayId);
		Log.d("payargs", "nonceStr"+nonceStr);
		Log.d("payargs", "timestamp"+timestamp);
		Log.d("payargs", "sign"+sign);
		
		request.appId = Constants.APP_ID;
		request.partnerId = "1483615012";
		request.prepayId = prepayId;
		request.packageValue = "Sign=WXPay";
		request.nonceStr = nonceStr;
		request.timeStamp = timestamp;
		request.sign= sign;
		api.sendReq(request);
	}
	
	public static void Login(){
		isLogin = true;
		isPay = false;
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "carjob_wx_login";
		api.sendReq(req);
		//instance.finish();
	}
	
	public static void Share(String url,String title,String desc,String type){
		try{
			isLogin = false;
			isPay = false;
			WXWebpageObject webpage = new WXWebpageObject();
			webpage.webpageUrl = url;
			WXMediaMessage msg = new WXMediaMessage(webpage);
			msg.title = title;
			msg.description = desc;
			
			Bitmap bmp = BitmapFactory.decodeResource(WXAPI.instance.getResources(),R.drawable.icon);
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
			msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("webpage");
			req.message = msg;
			req.scene = "1".equals(type)?SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
			api.sendReq(req);
			//instance.finish();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//兼容性，担心找不到方法
	public static void ShareIMG(String path,int width,int height){
		ShareIMG(path,width,height,"0");
	}
	
	public static void ShareIMG(String path,int width,int height,String type){
		try{
			
			isLogin = false;
			isPay = false;
			
			File file = new File(path);
			if (!file.exists()) {
				return;
			}
			Bitmap bmp = BitmapFactory.decodeFile(path);
			
			WXImageObject imgObj = new WXImageObject(bmp);
			//imgObj.setImagePath(path);
			
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = imgObj;
			
			
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, width, height, true);
			bmp.recycle();
			msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("img");
			req.message = msg;
			req.scene = "1".equals(type)?SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
			api.sendReq(req);	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void onClickCopy(final String s) {

		WXAPI.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               	// 从API11开始android推荐使用android.content.ClipboardManager
		        // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
		        ClipboardManager cm = (ClipboardManager) WXAPI.instance.getSystemService(Context.CLIPBOARD_SERVICE);
		        // 将文本内容放到系统剪贴板里。
		        cm.setText(s);
            }
        });
        
    }

}

class MyLocationListener implements LocationListener{

	@Override
	public void onLocationChanged(Location location) {
		Log.i("GPS", "经度："+location.getLongitude()); 
		Log.i("GPS", "纬度："+location.getLatitude());
		final double lo = location.getLongitude();
		final double la = location.getLatitude();
		WXAPI.lo = lo;
		WXAPI.la = la;
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		Log.i("GPS", "GPS被禁用"); 
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		Log.i("GPS", "GPS已启用"); 
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		Log.i("GPS", "GPS状态改变"); 
	}
	
}
