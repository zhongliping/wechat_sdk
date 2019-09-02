/****************************************************************************
Copyright (c) 2008-2010 Ricardo Quesada
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2011      Zynga Inc.
Copyright (c) 2013-2014 Chukong Technologies Inc.
 
http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package org.cocos2dx.javascript;

import java.util.Map;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.cocos2dx.javascript.SDKWrapper;

import com.totorotec.yueyueqipai.WXAPI;
import com.zxinsight.MLink;
import com.zxinsight.MWConfiguration;
import com.zxinsight.MagicWindowSDK;
import com.zxinsight.mlink.MLinkCallback;

import com.fm.openinstall.OpenInstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

public class AppActivity extends Cocos2dxActivity {
    
    //battery
    private BroadcastReceiver batteryLevelRcvr;
    private IntentFilter batteryLevelFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)
            // Don't need to finish it again since it's finished in super.onCreate .
            return;
        }
        // DO OTHER INITIALIZATION BELOW
        
        //SDKWrapper.getInstance().init(this);
        
        Log.e("initMW", "onCreate..................weixin");
        WXAPI.Init(this);
        // Log.e("initMW", "onCreate..................mochuang");
        // initMW();
        // register();
        // Uri mLink = getIntent().getData();
        // MLink.getInstance(AppActivity.this).deferredRouter();
        // if (mLink != null) {
        //     MLink.getInstance(this).router(mLink);
        // } else {
        //     MLink.getInstance(this).checkYYB();
        // }

        OpenInstall.init(this);
        
        monitorBatteryState(); //`暂时不用 好像要报错

    }
    
    //初始化魔窗
    private void initMW(){
        MWConfiguration config = new MWConfiguration(this);
        //设置渠道，非必须（渠道推荐在AndroidManifest.xml内填写）
        config.setChannel("微信")
        //开启Debug模式，显示Log，release时注意关闭
        .setDebugModel(true)
        //带有Fragment的页面。具体查看2.2.2
        .setPageTrackWithFragment(true)
        //设置分享方式，如果之前有集成sharesdk，可在此开启
        . setSharePlatform (MWConfiguration. ORIGINAL);
        MagicWindowSDK.initSDK(config);
        
    }
    
    
    private void register(){
        Log.e("register", "Context..................注册魔窗");
        MLink mLink = MagicWindowSDK.getMLink();
        mLink.registerDefault(new MLinkCallback() {
            @Override
            public void execute(Map paramMap, Uri uri, Context context) {
                //HomeActivity 为你的首页
                
            }
        });
        Log.e("register", "mLink..................goToRoom");
        // mLinkKey:  mLink 的 key, mLink的唯一标识
        mLink.register("goToRoom", new MLinkCallback() {
            public void execute(Map<String,String> paramMap, Uri uri, Context context) {
                String id = "";
                if (paramMap != null) {
                    id = paramMap.get("roomid");
                } else if(uri!=null) {
                    id = uri.getQueryParameter("id");
                }
                //todo: 此处可以根据获取的动态参数id来做相应的处理
                Log.e("roomid", id);
                //Native.GoToRoom(id);
                //                 Log.e("register", "goToRoom..................注册魔窗");
                //String eval = "cc.sys.localStorage.setItem('mwroomid',"+ id +")";
                //Cocos2dxJavascriptJavaBridge.evalString("asdfdasfadsfas");
                //                 Cocos2dxJavascriptJavaBridge.evalString("cc.sys.localStorage.setItem('mwroomid','646704')");
                //                 final String roomid = id;
                //                 app.runOnGLThread(new Runnable() {
                //                     @Override
                //                     public void run() {
                //                         Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onMWGotoRoom('"+ roomid +"')");
                //                     }
                //                 });
                //                 final String roomid = id;
                //                     Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
                //                        @Override
                //                        public void run() {
                //                            Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onMWGotoRoom('"+ roomid +"')");
                //                        }
                //                    });
                
                //                 Cocos2dxJavascriptJavaBridge.evalString("cc.vv.anysdkMgr.onMWGotoRoom('"+ id +"')");
                if(!"".equals(id)){
                    WXAPI.mwroomid = Integer.parseInt(id);
                }
            }
        });
    }
    
    private void monitorBatteryState() {
        batteryLevelRcvr = new BroadcastReceiver() {
            
            public void onReceive(Context context, Intent intent) {
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int level = -1; // percentage, or -1 for unknown
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                    Log.d("", " battery    " + level + " percent ");
                    WXAPI.betVal = level;
                }
            }
        };
        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelRcvr, batteryLevelFilter);
    }
	
    @Override
    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);

        //SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView);

        return glSurfaceView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //SDKWrapper.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //SDKWrapper.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SDKWrapper.getInstance().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //SDKWrapper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //SDKWrapper.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //SDKWrapper.getInstance().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //SDKWrapper.getInstance().onStop();
    }
        
    @Override
    public void onBackPressed() {
        //SDKWrapper.getInstance().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //SDKWrapper.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //SDKWrapper.getInstance().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        //SDKWrapper.getInstance().onStart();
        super.onStart();
    }
}
