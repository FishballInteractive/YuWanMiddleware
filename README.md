# YuWanMiddlewar(Android)接入文档

#### [快速接入](#1)
#### [工作流程](#2)
#### [接入指南](#3)
##### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[客户端接入](#3.1)
##### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[服务端接入](#3.2)
#### [资源下载](#4)
#### [常见问题](#5)

<h2 id="1">一.快速接入</h2>
* 只需5步，快速完成接入
![快速接入流程][quick-import]

<h2 id="2">二.工作流程</h2>
* 登录工作流程
![登录工作流程][login-import]
* 支付工作流程
![登录工作流程][pay-import]

<h2 id="3">三.接入指南</h2>

<h4 id="3.1">3.1. 客户端接入</h4> 

##### 3.1.1. 导入鱼丸中间件Jar包
  * 在[“资源下载”][lib-download]中下载最新的鱼丸中间件Jar包
  * 在IDE中导入Jar包。各IDE导入Jar包请点击如下连接
    * [Android Studio][android-studio]
    * [Eclipse][eclipse]

##### 3.1.2. AndroidManifest配置
  * AppId & 分发渠道配置

```xml
    <application>
        <!--在application标签内插入YW_APPID、YW_CHANNEL两个meta-data标签-->
        <!--AppId由鱼丸工作人员分配-->
        <meta-data
            android:name="YW_APPID"
            android:value="54866deefd98c55332000cc7" />
        <!--渠道“占位”标签，供渠道打包工具替换实际的分发渠道号-->
        <meta-data
            android:name="YW_CHANNEL"
            android:value="yuwan" />
    </application>
```

  * 权限配置

```xml
    <manifest>
        <!--在manifest标签内插入鱼丸中间件使用到的必要权限-->
        <!--允许使用网络-->
        <uses-permission android:name="android.permission.INTERNET" />
    </manifest>
```

  * Application配置

```xml
    <manifest>
        <!--在manifest标签内插入鱼丸中间件使用到的必要权限-->
        <application android:name="com.yuwan8.middleware.YWApplication">
        </application>
    </manifest>
```
```
注意：
  如果游戏内代码已经继承了android.app.Application，
  请改为继承com.yuwan8.middleware.YWApplication。
  参考代码如下。
```
```java
    public class GameApplication extends YWApplication {

        // Some fields

        @Override
        public void onCreate() {
            super.onCreate();
            // Some logic
        }
    
        // Some methods
    }
```

##### 3.1.3. 继承闪屏的Activity
游戏需要创建一个Activity来继承中间件提供的FlashActivity，并把该Activity作为游戏的入口，重写FlashActivity的onsplashStop()方法,并在该方法中跳转到游戏的主Activity。 接入的示例代码：
```java
    /***** 调用示例 *****/
    
    public class SplashActivity extends SplashActivity {
       private boolean flag = true;
       @Override
       protected void onCreate(Bundle savedInstanceState) {
        //注：需要把该方法中的代码复制到游戏继承闪屏的Activity的onCreate方法中，且该Activity为游戏的LaunchActivity（游戏入口）。
	     super.onCreate(savedInstanceState);
	     Intent intent = this.getIntent();
	     Set<String> set = intent.getCategories();
	     if(null == set){
	       finish();
	       return;
	      }
		
	     for (String category : set) {
	       if("android.intent.category.LAUNCHER".equals(category)){
	         flag = false;
              }
		 }
		
	     if(flag){
	        finish();						
		 }
	}

        @Override
        protected void onSplashStop() {
        //闪屏结束后会调用该方法，CP可在该方法中跳转到游戏的主Activity
             Intent intent = new Intent(LoginActivity.this,MainActivity.class);
             startActivity(intent);
             finish();
    }
}
```

##### 3.1.4. 中间件初始化
调用初始化接口是调用其他接口的前置条件，否则调用其他接口时将抛出RuntimException。初始化接口建议在游戏的主Activity的onCreate方法中调用。  
接入的示例代码如下：
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.acitvity_main);

            String appKey = ...  // appKey向“鱼丸”申请。为保证安全，建议将appKey存到游戏服务器
            YW.getInstance().initSDK(this, appKey); // 调用处
        }
    }
```
```java
    /***** 接口声明 *****/
    public class YW {

        /**
         * 初始化中间件
         *
         * @param context 上下文
         * @param appKey 申请的appKey
         */
        public void init(Context context, String appKey);
    }
```

##### 3.1.5. onActivityCreate接口
在接入过程中，需要在游戏的主Activity的生命周期方法onCreate中调用中间件的onActivityCreate接口和wxLoginCallBack接口。
```java
    /***** 调用示例 *****/
        @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		YW.getInstance().onActivityCreate(this);
		YW.getInstance().wxLoginCallBack(getIntent(), this);
	}
```

##### 3.1.6. 登录接口
帐号登录
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Some code

            findViewById(R.id.button_login).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 调用处
                    YW.getInstance().login(new Response<String>() {

                        @Override
                        public void onSuccess(String token) {
                            //注：token的有效期为十分钟
                            // Logic for login success.
                        }

                        @Override
                        public void onFailure(String reason) {
                            // Logic for login failure.
                        }
                    });
                }
            });
        }
    }
```
```java
    /***** 接口声明 *****/
    public class YW {

        /**
         * 登录
         *
         * @param response 登录响应的回调。
         *     response.onSuccess(String token)，登录成功并携带登录成功的登录凭证
         *     response.onFailure(String reason)，登录失败并携带失败的原因
         */
        public void login(Response<String> response);
    }
```
##### 3.1.7. 提交玩家游戏相关信息
在游戏玩家第一次进入游戏创建完游戏角色时，CP需要提交游戏玩家相关信息。以后在游戏玩家登录完成时，CP都需要提交游戏玩家相关信息（玩家第一次注册登录，尚未创建游戏角色时不用提交）。游戏中玩家等级升级时，也需要提交游戏信息。示例：
```java
    PlayerInfo userExtraData = new PlayerInfo ();
    YW.getInstance().submitPlayerInfo (userExtraData, new Response<String>() {
            @Override
            public void onFailure(String arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(String arg0) {
                // TODO Auto-generated method stub

            }
        })
```
CP可在相应的回调方法中拿到信息提交是否成功的结果，处理自己的逻辑

PlayerInfo封装的参数：
```java
    public static final int TYPE_SELECT_SERVER = 1;     //选择服务器   
    public static final int TYPE_CREATE_ROLE = 2;       //创建角色  
    public static final int TYPE_ENTER_GAME = 3;        //进入游戏  
    public static final int TYPE_LEVEL_UP = 4;          //等级提升  
    public static final int TYPE_EXIT_GAME = 5;         //退出游戏  

    private int dataType;       //信息类型，以上5种参数中选择  必填
    private String roleID;       //角色id     必填
    private String roleName;       //角色名称   必填
    private String roleLevel;      //角色等级   必填
    private int serverID;          //服务器id   必填
    private String serverName;  //服务器区服     必填
    private int moneyNum;       //玩家金钱数量    选填
    private String vip;    //玩家的VIP等级  必填
    private long roleCTime;  //游戏角色创建时间 ——毫秒值(10位数)   必填

```

##### 3.1.8. 支付接口
支付（特别需要注意发起支付时签名的生成，否则发起支付会失败）
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Some code

            findViewById(R.id.button_pay).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 调用处
                    Order order = new Order();
                    order.set ... // Invoke some setter methods.

                    YW.getInstance().pay(order, new Response<Null>() {

                        @Override
                        public void onSuccess(Null aNull) {
                            // Logic for pay success.
                        }

                        @Override
                        public void onFailure(String reason) {
                            // Logic for pay failure.
                        }
                    });
                }
            });
        }
    }
```
```java
    /***** order所需的参数：均通过set方法赋值 *****/
   // 必填参数
	private String productId; // 商品id。最大 16 字符。
	// 必填参数
	private String productName;// 商品名称
	private String productDesc;// 商品描述
	// 必填参数
	private String money; // 商品支付总金额。以分为单位，整型值，不能为小数
	private String ratio; // 兑换比例
	private String coinNum;// 用户金钱数量
	// 必填参数
	private String uid;// 登录验证时游戏服务端从中间件服务端获取到的uid
	// 必填参数
	private String GameName;// 游戏名称。最大16个中文字符
	// 必填参数
	private String roleId;// 应用内用户id，如角色名称。最大16个中文字符
	// 必填参数
	private String roleName;// 角色名称
	private int roleLevel;// 角色等级
	// 必填参数
	private String orderID;// 订单id
	//必填参数
	private String sign;//参数签名，规则见下方说明
	private String extension; // 扩展字段
	private String notify_url; //支付回调参数，接入时测试使用
```
签名说明：
参与签名的参数：productId，productName，money，uid，GameName，orderID</br>
签名规则：参见[3.2.1. HTTP请求参数签名](#3.2.1)

##### 3.1.9. getConfig接口
该接口返回中间件当前所支持的功能的信息，信息封装在一个Config对象中。
```java
 	private boolean isSupportLogin;  //是否支持登录，true:支持，false:不支持
	private boolean isSupportPay;   //支持支付，true:支持，false:不支持
	private boolean isSupportFloat;  //是否有悬浮窗，true:支持，false:不支持
	private boolean isSupportSwitchAccount;  //是否支持账号切换，true:支持，false:不支持
```
```java
    /***** 调用示例 *****/
    Config config = YW.getInstance().getConfig();
    boolean isSupportSwitchAccount = config.isSupportSwitchAccount();   //可根据该值判断是否支持该功能，从而做相应处理。游戏提供账号切换按钮时特别需要注意
```

##### 3.1.10. 账号切换
如果CP支持游戏内账户切换，提供游戏内账户切换功能时，需要调用switchAccount (Response respnse)。示例：
```java
   //注意：若游戏支持游戏内账号切换，在调用账号切换方法时，需要调用getConfig接口来获得渠道SDK是否支持账号切换，若不支持，需要隐藏相应的账号切换的按钮
   switch_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                YW.getInstance().switchAccount (new Response<String>() {
                    @Override
                    public void onFailure(String msg) {
                        // TODO Auto-generated method stub              
                    }
                    @Override
                    public void onSuccess(String msg) {
                        // TODO Auto-generated method stub
                    }
                });

            }
        });
```
CP可在回调成功或失败的方法中处理自己的逻辑

##### 3.1.11. 设置游戏退出的监听
在初始化中间件后，需要设置游戏退出的回调监听。在相应的方法中实现游戏退出的逻辑
```java
    /***** 调用示例 *****/
  @Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    YW.getInstance().setExitStrategy(new YW.IExitStrategy() {
			@Override
			public void onShowExitDialog() {
			  //CP需实现逻辑：弹出自己的退出提示框，根据玩家的选择退出或留在游戏	
			}

			@Override
			public void onExitDirectly() {
			  //CP需实现逻辑：直接退出游戏
			}
		});
	
		
	}
```

##### 3.1.12. 退出方法
在游戏玩家触发退出事件时，CP需要调用该方法，游戏退出的逻辑放在退出回调的监听中来实现。
```java
   /***** 调用示例 *****/
      //注：示例模拟玩家点击手机返回键的情况。若游戏拦截了手机的返回键事件，该方法应该在触发手机的Back键事件时调用，
      //而不是放在Activity的onBackPressed方法中。具体情况需要CP根据自己的情况来灵活处理。需要保证要退出时调用了该方法，
      //该方法与setExitStrategy方法配合使用，两者都需要保证被调用到
 	@Override
	public void onBackPressed() {
	  YW.getInstance().tryExit(this);

	}
```

##### 3.1.13. 其他所需接口
在游戏的主Activity中，还需要在对应的生命周期方法中分别调用以下方法。所有方法均需调用
```java
   /***** 调用示例 *****/
        @Override
	protected void onResume() {
	  super.onResume();
	  YW.getInstance().onActivityResume();
	}
```

```java
  	@Override
	protected void onPause() {
	  super.onPause();
	  YW.getInstance().onActivityPause();
	}
```
```java
  	@Override
	protected void onStart() {
	  super.onStart();
          YW.getInstance.onActivityStart();
	}
```
```java
 	@Override
	protected void onRestart() {
	  super.onRestart();
	  YW.getInstance().onActivityReStart();
	}
```

```java
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    YW.getInstance().wxLoginCallBack(intent, this);
	}
```

```java
	@Override
	protected void onStop() {
	  super.onStop();
	  YW.getInstance().onActivityStop();
	}
```
```java
 	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  YW.getInstance().onActivityResult(requestCode, resultCode, data);
	}
```
```java
 	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  YW.getInstance().onConfigurationChanged(newConfig);
	}

```

```java
 	@Override
	protected void onDestroy() {
	  super.onDestroy();
	  YW.getInstance().onActivityDestroy();
	}
```

##### 7. 混淆配置
如果要混淆java代码，请不要混淆鱼丸中间件中的类。可以添加以下类到proguard配置，排除在混淆之外：
```java
-keep class com.yuwan8.middleware.** {*;}
```

<h4 id="3.2">3.2. 服务端接入</h4>

阅读下述“服务端接入指南”前，请先理解鱼丸中间件的[工作流程][workflow]。  
工作流程中，跟CP相关的有两个HTTP接口：①登录验证接口、②支付通知接口。   
具体的说明及接口定义见下述描述。

<h5 id="3.2.1">3.2.1. HTTP请求参数签名</h5>
为验证请求的合法性，引入参数签名机制。参数签名的步骤如下：
1. 对查询参数对（URL中“?”后的名值对（name-value）进行字典排序，数字由小到大，字母按字母表顺序；
2. 非ASCII的查询参数值进行UTF-8 UrlEncode；
3. 排序好的查询参数对以“&”拼接。最后拼接appsecret，即拼接“&appsecret=d1f5458832fc283249a27aaaa65dbeaa”（实际采用申请的appsecret）；
4. 将拼接结果进行MD5加密；
5. 将MD5加密结果作为1个查询参数拼接到<span style="color:red;">未拼接appsecret</span>的串后面，即拼接“&sign=33e78d60bc1f9dcc7291c891e6f069e4”。
6. 示例如下：

例如：发送1个GET请求，该请求需要携带如下参数：

  1. id=666666
  2. name=鱼丸
  3. gender=male
  4. mail=support@yuwan8.com

appsecret为4d6121503e6c2c8157c98796cc4f598e

添加签名的基本步骤如下：

  1. 对查询参数对排序，结果为：gender=male、id=666666、mail=support@yuwan8.com、name=鱼丸；
  2. UTF-8 UrlEncode：name=%e9%b1%bc%e4%b8%b8（注意：空格“ ”编码为“%20”，而不是“+”）；
  3. 拼接：gender=male&id=666666&mail=support@yuwan8.com&name=%e9%b1%bc%e4%b8%b8&appsecret=4d6121503e6c2c8157c98796cc4f598e；
  4. MD5加密：sign=fd2d8dcd085111afc789c8da22e2659c；
  5. 最后的结果：gender=male&id=666666&mail=support@yuwan8.com&name=%e9%b1%bc%e4%b8%b8&sign=fd2d8dcd085111afc789c8da22e2659c （<span style="color:red;">注：没有appsecret</span>）


##### 3.2.2. 登录验证接口

* 请求方 => 响应方：游戏服务器 => 鱼丸中间件服务器
* 请求地址：http://api.yuwan8.com/1.0/pay/user/profile
* HTTP方法：GET
* 请求参数（<span style="color:red;">必须参数签名</span>）：

| 参数名称 | 描述 | 参数类型 | 必填 |
| ---- | :---- | ---- | :----: |
| appid | 申请的appid | string | 是 |
| token | 登录凭证，由鱼丸中间件回调给游戏，游戏回传至游戏服务器 | string | 是 |
| sign | 参数签名 | string | 是 |

* 请求示例

<div style="background:#F7F7F7;padding:15px">
假定appsecret=4d6121503e6c2c8157c98796cc4f598e<br><br>

curl -X GET 'http://api.yuwan8.com/1.0/pay/user/profile?appid=13421341&token=17b98e854de15b1bd613571f43aa9a85&sign=35c70fb65838eaba29b4bd3a1348f763'
</div>

* 响应结果

```json
// 成功
{
  "code": 0, // 错误码：0成功，非0失败
  "data": { // 响应的数据
    "uid": "134124312",
    "name": "鱼丸",
    "avator": "http://xxxx.xxx.xxx/xxx.jpg",
    "channel": "yw_360",  //渠道区分-英文标识
    "channel_zh": "鱼丸_360"  //渠道区分-中文名
  }
}

// 失败
{
  "code": 1, // 错误码：0成功，非0失败。
  "message": "Illegal token." // 错误描述
}
```

##### 3.2.3 支付通知接口
###### 3.2.3.1  一般SDK支付接口

* 请求方 => 响应方：鱼丸中间件服务器 => 游戏服务器
* 请求地址：CP提供
* HTTP方法：GET
* 请求参数（<span style="color:red;">必须参数签名</span>）：

| 参数名称 | 描述 | 参数类型 | 必填 |
| ---- | :---- | ---- | :----: |
| appid | 申请的appid | string | 是 |
| order_id | 鱼丸中间件生成的订单id | string | 是 |
| cp_order_id | CP生成的订单id | string | 是 |
| uid | 用户id | string | 是 |
| real_fee | 支付金额（单位：分） | int | 是 |
| trade_name | 商品名称 | string | 是 |
| product_count | 商品数量 | int | 是 |
| pay_time | 支付时间，时间戳 | long | 是 |
| attach | 透传参数 | string | 是 |
| return_code | 支付结果，成功：SUCCESS，失败：非SUCCESS，一般传FAILURE | string | 是 |
| sign | 参数签名 | string | 是 |

* 请求示例

<div style="background:#F7F7F7;padding:15px">
假定appsecret=4d6121503e6c2c8157c98796cc4f598e<br>
假定CP的请求地址是：http://cp.com/pay/nodify<br><br>

curl -X GET 'http://cp.com/pay/nodify?appid=13421341&order_id=1370481703&cp_order_id=1948312434&uid=134124312&real_fee=200&trade_name=stone&product_count=1&pay_time=1440665340&attach=2af34&result_code=SUCCESS&sign=abb1d204741e93d376b0f17c6f0b5af5'
</div>

* 响应结果

```
// 成功
SUCCESS

// 失败
FAILURE

```

* 重试机制
游戏服务器处理订单成功则直接返回SUCCESS，否则返回FAILURE。如果游戏服务器未返回SUCCESS，鱼丸SDK平台会在1分钟、10分钟、30分钟尝试重新通知。


##### 3.2.3.2  应用宝支付接口
由于应用宝要托管游戏货币，也就是说用户的游戏货币由应用宝保存管理。每当用户使用游戏币购买道具时，CP需发送请求到应用宝查询用户账户余额，如果足够则调用扣费接口扣除游戏币相应数量，并立即发货；如果不够则响应前端提示或进入充值游戏币页面，让用户先充值游戏币。另外，可调用直接赠送接口给用户赠送游戏币，赠送的游戏币消耗不参与结算。

注意：与应用宝每月结算是按游戏币消耗数量，而不是充值了多少游戏币。

1.查询余额接口
地址：http://api.yuwan8.com/1.0/pay/order/myapp/balance
请求方式 GET

请求参数：
uid   用户账户
appid 应用id
sign  签名

响应结果：
{ 
   code: 0,        // 状态码，0：表示成功， 1018：登陆校验失败 其它：失败
   balance: 0,     // 游戏币个数（包含了赠送游戏币）
   gen_balance: 0, // 赠送游戏币个数
   first_save: 1,  // 是否满足首次充值，1：满足，0：不满足 
   save_amt: 0     // 累计充值金额(单位：游戏币)
}


2.扣费接口
地址：http://api.yuwan8.com/1.0/pay/order/myapp/pay
请求方式 GET

请求参数：
appid         应用id
uid           用户账户 
cp_order_id   CP订单号
product_id    商品序列号（可选参数）
product_count 商品数量，默认1
trade_name    交易名称
trade_detail  交易详细（可选参数）
total_fee     游戏币数量，与人民币存在兑换比例
sign:         签名

响应结果：
{
    code: 0,       // 状态码，0：表示成功, >=1000 表示失败 1004：余额不足 1018：登陆校验失败 其它：失败
    balance: 1,    // 扣费后的余额
    billno: 123456 // 扣费流水号
}



3.直接赠送接口
地址：http://api.yuwan8.com/1.0/pay/order/myapp/present
请求方式 GET

请求参数：
appid         应用id
uid           用户账户 
cp_order_id   CP订单号
total_fee     要赠送游戏币的个数，赠送的游戏币不参与结算
sign:         签名

响应结果：
{
    code: 0,       // 状态码，0：表示成功, 1018：登陆校验失败 其它：失败
}

<h2 id="4">四.资源下载</h2>
* [点击下载][lib-download]接入所需资源

<h2 id="5">五.常见问题</h2> 
* 未收集到接入时的常见问题。如果您在接入时遇到了问题，欢迎您随时向我们咨询及反馈。

[resource-download]: ../04.resource/00.download.md
[android-studio]: http://www.cnblogs.com/neozhu/p/3458759.html
[eclipse]: http://jingyan.baidu.com/article/466506580baf2ef549e5f8e8.html
[quick-import]:image/quick-import.png  "快速接入"
[login-import]:image/login.png  "登录流程"
[pay-import]:image/pay.png  "支付流程"
[lib-download]:lib/

