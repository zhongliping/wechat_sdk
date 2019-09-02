cc.Class({
    extends: cc.Component,

    properties: {
        // foo: {
        //    default: null,      // The default value will be used only when the component attaching
        //                           to a node for the first time
        //    url: cc.Texture2D,  // optional, default is typeof default
        //    serializable: true, // optional, default is true
        //    visible: true,      // optional, default is true
        //    displayName: 'Foo', // optional
        //    readonly: false,    // optional, default is false
        // },
        // ...
        _isCapturing: false,
        dataEventHandler: null,
        _shareType: 0, //分享类型

        //分享用的图片
    },

    // use this for initialization
    onLoad: function () {},

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },

    init: function () {
        this.ANDROID_API = "com/szbj/yueyueqipai/WXAPI";
        this.IOS_API = "AppController";
        //安卓
        if (cc.sys.os == cc.sys.OS_ANDROID) {
            var mwroomid = jsb.reflection.callStaticMethod(this.ANDROID_API, "getMWroomid", "()I");
            console.log("mwroomid", mwroomid);
            if (mwroomid && mwroomid != '0') {
                cc.sys.localStorage.setItem("mwroomid", mwroomid);
            }
        }
    },

    getAndroidMWroomid: function () {
        // var mwroomid = jsb.reflection.callStaticMethod(this.ANDROID_API, "getMWroomid", "()I");
        // console.log("mwroomid:",mwroomid);
        //    //因为Java层getMWroomid方法一定会返回一个房间号，所以要控制不能一直进，进一次就再也不能进了
        //    if(mwroomid&&mwroomid!='0'){
        //         return mwroomid;
        //    }else{
        //    		return 0;
        //    }
    },

    getGPS: function () {
        if (cc.sys.isNative && cc.sys.os == cc.sys.OS_ANDROID) {
            var gps = jsb.reflection.callStaticMethod(this.ANDROID_API, "getGPS", "()Ljava/lang/String;");
            console.log("收到GPS位置信息 经度,纬度", gps);
            cc.vv.userMgr.gps = gps;
        } else if (cc.sys.isNative && cc.sys.os == cc.sys.OS_IOS) {
            var gps = jsb.reflection.callStaticMethod(this.IOS_API, "getGPS");
            console.log("收到GPS位置信息 经度,纬度", gps);
            cc.vv.userMgr.gps = gps;
        } else {
            var a = 106 + 0.1 * Math.random();
            var b = 29 + 0.1 * Math.random();
            cc.vv.userMgr.gps = a + "," + b;
            console.log("坐标：" + cc.vv.userMgr.gps);
        }
    },

    getWechatSign: function () {
        var self = this;
        if (cc.sys.isBrowser && cc.sys.isMobile) {
            // if(cc.sys.browserType  == cc.sys.BROWSER_TYPE_WECHAT){这个判断在安卓上无效，我艹
            var data = {
                account: cc.vv.userMgr.account,
                url: new Buffer(location.href, 'utf8').toString('base64'),
                channel: cc.args.web_args.channel,
                sign: cc.vv.userMgr.sign
            }
            var fn = function (ret) {
                console.log(ret);
                if (ret.errcode !== 0) {
                    console.log(ret.errmsg);
                } else {
                    self.initH5(ret.timestamp, ret.noncestr, ret.signature);
                }
            };
            cc.vv.http.sendRequest("/web_wechat_signature", data, fn);
            // }
        }
    },

    //进入大厅后会把当前的url发给向服务器获得签名信息，然后调用这个方法，就能通过H5公众号分享了
    initH5: function (timestamp, nonceStr, signature) {
        //初始化微信接口
        wx.config({
            debug: false,
            appId: 'wx9a626fafe7e866cd',
            timestamp: timestamp,
            nonceStr: nonceStr,
            signature: signature,
            jsApiList: [
                'checkJsApi',
                'onMenuShareTimeline',
                'onMenuShareAppMessage',
                'onMenuShareQQ',
                'onMenuShareWeibo',
                'hideMenuItems',
                'showMenuItems',
                'hideAllNonBaseMenuItem',
                'showAllNonBaseMenuItem',
                'translateVoice',
                'startRecord',
                'stopRecord',
                'onRecordEnd',
                'playVoice',
                'pauseVoice',
                'stopVoice',
                'uploadVoice',
                'downloadVoice',
                'chooseImage',
                'previewImage',
                'uploadImage',
                'downloadImage',
                'getNetworkType',
                'openLocation',
                'getLocation',
                'hideOptionMenu',
                'showOptionMenu',
                'closeWindow',
                'scanQRCode',
                'chooseWXPay',
                'openProductSpecificView',
                'addCard',
                'chooseCard',
                'openCard'
            ]
        });

        wx.ready(function () {
            //判断是否在房间里
            var link1 = 'http://assets-0.totorotec.com/web-mobile/index.html?web_args=channel$' + cc.args.web_args.channel;
            var desc1 = '约约棋牌,斗地主跑得快炸金花斗牛德州扑克十三水玩法合集,分享领取红包话费iphone，快一起来玩吧!';
            if (cc.vv.gameNetMgr.roomId) {
                link1 = 'http://assets-0.totorotec.com/web-mobile/index.html?web_args=channel$' + cc.args.web_args.channel + '$$roomid$' + cc.vv.gameNetMgr.roomId;
                desc1 = "约约棋牌 " + cc.vv.gameNetMgr.getGameTypeStr() + " 房号:" + cc.vv.gameNetMgr.roomId + " 玩法:" + cc.vv.gameNetMgr.getWanfa();
            }

            wx.onMenuShareAppMessage({
                title: '约约棋牌',
                desc: desc1,
                link: link1,
                imgUrl: 'http://firicon.fir.im/7ece9bb7eb7a8181cb17185975e58e87e0e31418',
                success: function (res) {
                    //alert('已分享');
                },
                cancel: function (res) {
                    // alert('已取消');
                }
            });

            wx.onMenuShareTimeline({
                title: '约约棋牌,斗地主跑得快炸金花斗牛德州扑克十三水玩法合集,分享领取红包话费iphone，快一起来玩吧!',
                link: 'http://assets-0.totorotec.com/web-mobile/index.html?web_args=channel$' + cc.args.web_args.channel,
                imgUrl: 'http://firicon.fir.im/7ece9bb7eb7a8181cb17185975e58e87e0e31418',
                success: function (res) {
                    // alert('已分享');
                },
                cancel: function (res) {
                    // alert('已取消');
                }
            });
        });

        wx.error(function (res) {
            // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。  
            alert("微信config失败");
        });
    },

    login: function () {
        cc.eventManager.removeCustomListeners(cc.game.EVENT_HIDE);
        if (cc.sys.isBrowser) {
            if (cc.sys.isMobile) {
                //公众号登录,从location中解析出code直接调用onLoginResp
                // var str = location.href;
                // if(str.indexOf("code=")==-1){
                //     return;
                // }
                // var code = str.substring(str.indexOf("code=")+5,str.indexOf("&state"));
                var code = cc.args.code;
                var fn = function (ret) {
                    if (ret.errcode == 0) {
                        cc.sys.localStorage.setItem("wx_account", ret.account);
                        cc.sys.localStorage.setItem("wx_sign", ret.sign);
                    }
                    cc.vv.userMgr.onAuth(ret);
                }
                cc.vv.http.sendRequest("/login", {
                    code: code,
                    os: 'wechat_default'
                }, fn);
                //cc.vv.http.sendRequest("/login",{login_type:2,code:code,channel:ch,os:cc.sys.os},fn);
            }
        } else {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                jsb.reflection.callStaticMethod(this.ANDROID_API, "Login", "()V");
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                jsb.reflection.callStaticMethod(this.IOS_API, "login");
            } else {
                console.log("platform:" + cc.sys.os + " dosn't implement share.");
            }
        }
    },

    shareInHall: function (title, desc, type, roomid) {
        console.log("分享类型", type)
        if (type == 1) {
            this._shareType = 1;
        }
        if (cc.sys.isBrowser) {
            //网页版只有点浏览器右上角分享
            cc.vv.alert.show("提示", "网页版请点击微信浏览器右上角分享或邀请");
            cc.vv.alert.showArrow();
        } else {
            //如果是朋友圈就分享下载链接，好友就发魔窗mlink链接
            var url = cc.SHARELINK + "?roomid=" + roomid;
            if (type == 1) {
                url = cc.DOWNLOAD_URL;
            }
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                jsb.reflection.callStaticMethod(this.ANDROID_API, "Share", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", url, title, desc, type + "");
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                jsb.reflection.callStaticMethod(this.IOS_API, "share:shareTitle:shareDesc:shareType:", url, title, desc, type + "");
            } else {
                console.log("platform:" + cc.sys.os + " dosn't implement share.");
            }
        }
    },

    share: function (title, desc, type) {
        console.log("分享类型", title, desc, type)
        if (type == 1) {
            this._shareType = 1;
        }
        if (cc.sys.isBrowser) {
            //网页版只有点浏览器右上角分享
            cc.vv.alert.show("提示", "网页版请点击微信浏览器右上角分享或邀请");
            cc.vv.alert.showArrow();
        } else {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                jsb.reflection.callStaticMethod(this.ANDROID_API, "Share", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", cc.SHARELINK + "?roomid=" + cc.vv.gameNetMgr.roomId, title, desc, type + "");
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                jsb.reflection.callStaticMethod(this.IOS_API, "share:shareTitle:shareDesc:shareType:", cc.SHARELINK + "?roomid=" + cc.vv.gameNetMgr.roomId, title, desc, type + "");
            } else {
                console.log("platform:" + cc.sys.os + " dosn't implement share.");
            }
        }
    },

    shareUrl: function (url, title, desc, type) {
        console.log("分享类型", type)
        if (type == 1) {
            this._shareType = 1;
        }
        if (cc.sys.isBrowser) {
            //网页版只有点浏览器右上角分享
            cc.vv.alert.show("提示", "网页版请点击微信浏览器右上角分享或邀请");
            cc.vv.alert.showArrow();
        } else {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                jsb.reflection.callStaticMethod(this.ANDROID_API, "Share", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", url, title, desc, type);
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                jsb.reflection.callStaticMethod(this.IOS_API, "share:shareTitle:shareDesc:shareType:", url, title, desc, type + "");
            } else {
                console.log("platform:" + cc.sys.os + " dosn't implement share.");
            }
        }
    },

    // //分享成功由SDK回调
    // onShareOK:function(){
    //     //if(this._shareType==1){
    //         console.log("分享成功")
    //     //}
    // },

    paymentRequest: function (goodId) {
        if (cc.sys.os == cc.sys.OS_IOS) {
            //苹果IAP
            cc.vv.wc.show("订单请求中...");
            var self = this;
            var fn = function (ret) {
                console.log("pay_order  errcode:", ret.errcode);
                if (ret.errcode == 0) {
                    console.log(ret.pay_id);
                    jsb.reflection.callStaticMethod(self.IOS_API, "paymentRequest:", goodId);
                    self.pay_id = ret.pay_id;
                } else {
                    cc.vv.wc.hide();
                    console.log(ret);
                    if (ret.errcode == 8004) {
                        cc.vv.alert.show("提示", "订单提交过于频繁，请5分钟后尝试");
                    } else {
                        cc.vv.alert.show("提示", "订单请求失败错误码" + ret.errcode);
                    }
                }
            }
            var data = {
                account: cc.vv.userMgr.account,
                good_sn: goodId,
                good_count: 1 + "",
                pay_platform: "IOS"
            };
            // var crypt_mac =this.hmac(JSON.stringify(data));
            // data.mask = crypt_mac; HTTP外层已经有签名了
            cc.vv.http.sendRequest("/pay_order", data, fn);
        }
        //测试用，下发钻石
        // var self = this;
        // var de = {user_id:cc.vv.userMgr.userId};
        // var fn2 = function(ret){
        //      if(ret.errcode == 0){
        //        //刷新当前钻石
        //        cc.vv.userMgr.gems += 10;
        //        self.dispatchEvent("freshUserInfo");
        //     }
        // }
        // cc.vv.http.sendRequest("/pay_debug",de,fn2);
    },

    //IOS会回传3个参数，微信支付不需要
    onPaymentComplete: function (transationID, goodID, receiptString) {
        console.log(transationID);
        console.log(goodID);
        console.log(receiptString);
        if (cc.sys.os == cc.sys.OS_IOS) {
            console.log(cc.vv.userMgr.account);
            var sb = true;
            if (cc.vv.userMgr.account.indexOf("guest_") == -1) {
                sb = false;
            }
            var data = {
                account: cc.vv.userMgr.account,
                pay_id: this.pay_id,
                order_id: transationID,
                good_sn: goodID,
                pay_platform: "IOS",
                is_sandbox: sb + "",
                check: receiptString
            };
            this.commitOrder(data);
        }
    },

    //把缓存的订单交给服务器去处理
    commitOrder: function (data) {
        console.log("正在验证订单....");
        cc.vv.wc.show("正在验证订单...");
        var self = this;
        var fn = function (ret) {
            console.log(ret.errcode);
            cc.vv.wc.hide();
            if (ret.errcode == 0 || ret.errcode == 8107) { //8107表示已经验证通过了 直接删除即可
                cc.sys.localStorage.removeItem("checkOrder");
                if (ret.errcode == 0) {
                    cc.vv.userMgr.coins = ret.total_gold;
                    self.dispatchEvent("freshUserInfo");
                }
            } else {
                cc.vv.alert.show("提示", "订单验证失败错误码" + ret.errcode);
            }
        };
        //提交苹果验证之前先本地保存
        var s = JSON.stringify(data);
        cc.sys.localStorage.setItem("checkOrder", s);
        console.log("订单已缓存....", s);
        cc.vv.http.sendRequest("/pay_success", data, fn);
    },

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    paymentRequestWX: function (goodId) {
        if (cc.sys.isNative) {
            //安卓微信支付
            cc.vv.wc.show("订单请求中...");
            var self = this;
            var fn = function (ret) {
                console.log("pay_order  errcode:", ret.errcode);
                if (ret.errcode == 0) {
                    console.log(ret.pay_id);
                    jsb.reflection.callStaticMethod(self.ANDROID_API, "wxPay", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", ret.prepayid, ret.noncestr, ret.timestamp + "", ret.sign);
                    self.out_trade_no = ret.out_trade_no;
                    self.attach = ret.attach;
                } else {
                    cc.vv.wc.hide();
                    console.log(ret);
                    if (ret.errcode == 8004) {
                        cc.vv.alert.show("提示", "订单提交过于频繁，请5分钟后尝试");
                    } else {
                        cc.vv.alert.show("提示", "订单请求失败错误码" + ret.errcode);
                    }
                }
            }
            var data = {
                account: cc.vv.userMgr.account,
                good_sn: goodId,
                good_count: 1 + "",
                platform: "Wechat_Android"
            };
            //http://192.168.0.152:9001/wehchat_pay_order?account=guest_aaa1&platform=Android&good_sn=20001&&good_count=1
            cc.vv.http.sendRequest("/wehchat_pay_order", data, fn);
        }
    },

    onPaymentCompleteWX: function () {
        cc.log(this.out_trade_no);
        cc.log(this.attach);
        if (cc.sys.os == cc.sys.OS_ANDROID) {
            var data = {
                account: cc.vv.userMgr.account,
                out_trade_no: this.out_trade_no,
                attach: this.attach
            };
            this.commitOrderWX(data);
        }
    },

    //把缓存的订单交给服务器去处理
    commitOrderWX: function (data) {
        console.log("正在验证订单....");
        // cc.vv.wc.show("正在验证订单...");
        var self = this;
        var fn = function (ret) {
            console.log("订单验证结果errcode", ret.errcode);
            cc.vv.wc.hide();
            if (ret.errcode == 0) {
                cc.sys.localStorage.removeItem("checkOrder");
                cc.vv.userMgr.coins = ret.total_gold;
                self.dispatchEvent("freshUserInfo");
            } else {
                cc.vv.alert.show("提示", "订单验证失败错误码" + ret.errcode);
            }
        };
        var s = JSON.stringify(data);
        cc.sys.localStorage.setItem("checkOrder", s);
        console.log("订单已缓存....", s);
        cc.vv.http.sendRequest("/wechat_pay_success", data, fn);
        console.log("订单验证已发出....");
    },

    onPaymentError: function () {
        cc.vv.wc.hide();
        cc.vv.alert.show("提示", "支付失败");
    },

    onPaymentCancel: function () {
        cc.vv.wc.hide();
        //cc.vv.alert.show("提示","订单取消");
    },


    //分享大结算截图,第二参数可以在分享完成后让节点隐藏
    shareResult: function (type, node) {
        // if(this._isCapturing){
        //     return;
        // }
        var self = this;
        // this._isCapturing = true;
        var size = cc.director.getWinSize();
        var currentDate = new Date();
        var fileName = "result_share.jpg";
        var fullPath = jsb.fileUtils.getWritablePath() + fileName;
        if (jsb.fileUtils.isFileExist(fullPath)) {
            jsb.fileUtils.removeFile(fullPath);
        }
        var texture = new cc.RenderTexture(Math.floor(size.width), Math.floor(size.height), cc.Texture2D.PIXEL_FORMAT_RGBA8888, gl.DEPTH24_STENCIL8_OES);
        texture.setPosition(cc.p(size.width / 2, size.height / 2));
        texture.begin();
        cc.director.getRunningScene().visit();
        texture.end();
        texture.saveToFile(fileName, cc.IMAGE_FORMAT_JPG, function () {
            var height = 100;
            var scale = height / size.height;
            var width = Math.floor(size.width * scale);
            console.log(fullPath, width, height);
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                if (type) {
                    jsb.reflection.callStaticMethod(self.ANDROID_API, "ShareIMG", "(Ljava/lang/String;IILjava/lang/String;)V", fullPath, width, height, type + "");
                } else {
                    jsb.reflection.callStaticMethod(self.ANDROID_API, "ShareIMG", "(Ljava/lang/String;II)V", fullPath, width, height);
                }
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                jsb.reflection.callStaticMethod(self.IOS_API, "shareIMG:width:height:shareType:", fullPath, width, height, type + "");
            } else {
                console.log("platform:" + cc.sys.os + " dosn't implement share.");
            }
            // self._isCapturing = false;
            if (node) {
                node.active = false;
            }
        });
    },

    //下载一个图片，然后分享出去
    shareImg: function (url, type) {
        // var height = 100;
        // var scale = height/size.height;
        // var width = Math.floor(size.width * scale);
        // if(cc.sys.os == cc.sys.OS_ANDROID){
        //     jsb.reflection.callStaticMethod(self.ANDROID_API, "ShareIMG", "(Ljava/lang/String;II)V",fullPath,width,height);
        // }
        // else if(cc.sys.os == cc.sys.OS_IOS){
        //     jsb.reflection.callStaticMethod(self.IOS_API, "shareIMG:width:height:shareType:",fullPath,width,height,type+"");
        // }
        // else{
        //     console.log("platform:" + cc.sys.os + " dosn't implement share.");
        // }
        cc.vv.wc.show("正在生成分享图片");

    },

    onLoginResp: function (code, channel) {
        console.log("onLoginResp !!!11");
        cc.eventManager.addCustomListener(cc.game.EVENT_HIDE, function () {
            cc.game.emit(cc.game.EVENT_HIDE, cc.game);
        });

        var fn = function (ret) {
            console.log("onLoginResp !!!");
            console.log(ret);
            if (ret.errcode == 0) {
                cc.sys.localStorage.setItem("wx_account", ret.account);
                cc.sys.localStorage.setItem("wx_sign", ret.sign);
            }
            cc.vv.userMgr.onAuth(ret);
        }
        var ch = channel ? channel : cc.CHANNEL;
        cc.vv.http.sendRequest("/login", {
            login_type: 2,
            code: code,
            channel: ch,
            os: cc.sys.os
        }, fn);

    },

    //分享成功回调
    onShareSuccess: function (arg) {
        console.log("分享成功而不是取消，至于分享到哪里去了js分享前自己判断")
        // http://192.168.0.152:9001/daily_share?account=guest_aaa1&sign=xxx

        var self = this;
        var onGet = function (ret) {
            if (ret.errcode !== 0) {
                console.log(ret.errmsg);
            } else {
                console.log("分享成功向服务器发送消息");
                cc.vv.userMgr.share_time = Math.floor(Date.now() / 1000);
                this.dispatchEvent("ShareSuccess");
            }
        };

        var data = {
            account: cc.vv.userMgr.account,
            sign: cc.vv.userMgr.sign,
        };
        cc.vv.http.sendRequest("/daily_share", data, onGet.bind(this));
    },

    //更新房卡和钻石
    updateMoney: function () {
        this.dispatchEvent("updateMoney");
    },

    onMWGotoRoom: function (roomid) {
        console.log("----------魔窗传递roomid:", roomid);
        cc.sys.localStorage.setItem("mwroomid", roomid);
        this.dispatchEvent("goToRoom", roomid);
    },
    //更新电池信息,在需要的时候调用 getBatteryLevel
    // updateBaterry(val){
    //    console.log("----------电池信息更新",val) 
    //    this.dispatchEvent("updateBaterry",val);
    // },

    hmac: function (content) {
        var crypto = require('crypto');
        var token = "1234567890123456" //crypto.randomBytes(16).toString('hex');
        var token_str = new Buffer(token, 'hex').toString('base64');
        var signtrue = crypto.createHmac('sha1', token);
        signtrue.update(content);
        var crypt = signtrue.digest().toString('base64');
        crypt += '?';
        crypt += token_str;
        return crypt;
    },

    checkhmac: function (content, token) {
        var crypto = require('crypto');
        var token_hex = new Buffer(token, 'base64').toString('hex');
        var signtrue = crypto.createHmac('sha1', token_hex);
        signtrue.update(content);
        var crypt = signtrue.digest().toString('base64');
        return crypt;
    },

    // // //生成公钥和私钥
    // dh64_gen_key:function(){
    //     var crypto = require('crypto');
    //     var blob =crypto.getDiffieHellman('modp5');
    //     blob.generateKeys();
    //     return blob;
    // }

    //复制到剪贴板
    copyToClipBord: function (string) {
        if (cc.sys.os == cc.sys.OS_ANDROID) {
            jsb.reflection.callStaticMethod(this.ANDROID_API, "onClickCopy", "(Ljava/lang/String;)V", string);
        } else if (cc.sys.os == cc.sys.OS_IOS) {
            jsb.reflection.callStaticMethod(this.IOS_API, "addStringToPasteboard:", string);
        }
    },

    //获得电池电量
    getBatteryLevel: function () {
        var val = 100;
        if (cc.sys.os == cc.sys.OS_ANDROID) {
            val = jsb.reflection.callStaticMethod(this.ANDROID_API, "getCurrentBatteryLevel", "()I");
        } else if (cc.sys.os == cc.sys.OS_IOS) {
            val = jsb.reflection.callStaticMethod(this.IOS_API, "getCurrentBatteryLevel");
            console.log("电池电量", val);
        }
        return val;
    },

    dispatchEvent: function (event, data) {
        if (this.dataEventHandler) {
            this.dataEventHandler.emit(event, data);
        }
    },
});