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
    public class SplashActivity extends FlashActivity {

          @Override
          public void onsplashStop() {
             //闪屏结束后会调用该方法，CP可在该方法中跳转到游戏的主Activity
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
    }
}
```

##### 3.1.4. 中间件初始化
调用初始化接口是调用其他接口的前置条件，否则调用其他接口时将抛出RuntimException。初始化接口建议在程序入口处调用，例如：Application或者入口Activity的onCreate。  
接入的示例代码如下：
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.acitvity_main);

            String appKey = ...  // appsecret向“鱼丸”申请。为保证安全，建议将appKey存到游戏服务器
            YW.getInstance().init(this, appKey); // 调用处
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
         * @param appKey 申请的appsecret
         */
        public void init(Context context, String appKey);
    }
```

##### 3.1.5. 登录接口
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
##### 3.1.6. 提交玩家游戏相关信息
在游戏玩家第一次进入游戏创建完成游戏角色时，CP需要提交游戏玩家相关信息。以后在游戏玩家登录完成时，CP都需要提交游戏玩家相关信息（玩家第一次注册登录，尚未创建游戏角色时不用提交）。示例：
```java
    PlayerInfo userExtraData = new PlayerInfo ();
    YW.getInstance().subPlayerInfo (userExtraData, new Response<String>() {
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
    private String viplevel         //玩家的VIP等级   选填
```

##### 3.1.7. 支付接口
支付
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
    /***** 接口声明 *****/
    public class YW {

        /**
         * 支付
         *
         * @param response 支付响应的回调。
         *     response.onSuccess(Null aNull)，支付成功。
         *     response.onFailure(String reason)，支付失败并携带失败的原因
         */
        public void pay(Order order, Response<Null> response);
    }
```

##### 3.1.8. 悬浮窗接入
进入游戏后，调用悬浮窗显示接口，显示悬浮窗。建议在Activity的onResume方法中调用。示例：
```java
   @Override
    protected void onResume() {
        super.onResume();
        YW.getInstance().showFloat(MainActivity.this);
    }
```
游戏退出或需要隐藏悬浮窗时，调用悬浮穿隐藏接口，隐藏显示的悬浮窗。示例：
```java
   @Override
    protected void onStop() {
        super.onStop();
        YW.getInstance().hideFloat(MainActivity.this);
        }
```
```java
   注意：
    在游戏退出时，一定要确保调用了悬浮窗隐藏接口，否则某些渠道会出现游戏退出了，悬浮窗依然还出现在手机桌面的情况。
建议在Activity的onStop方法中调用。
```

##### 3.1.9. 账号切换
如果CP支持游戏内账户切换，提供游戏内账户切换功能时，需要调用switchAccount (Response respnse)。示例：
```java
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

##### 3.1.10. 登出接口
帐号登出
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Some code

            findViewById(R.id.button_logout).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 调用处
                    YW.getInstance().logout();
                }
            });
        }
    }
```
```java
    /***** 接口声明 *****/
    public class YW {

        /**
         * 登出
         */
        public void logout();
    }
```

##### 3.1.11. 销毁SDK
在游戏退出时调用销毁接口，以便释放资源。示例：
```java
   @Override
    protected void onDestory () {
    super.onDestroy();
    YW.getInstance().shutdown (MainActivity.this);
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

##### 3.2.1. HTTP请求参数签名
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
    "channel": "yw_360"  //渠道区分
  }
}

// 失败
{
  "code": 1, // 错误码：0成功，非0失败。
  "message": "Illegal token." // 错误描述
}
```

##### 3.2.3 支付通知接口

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
| result | 支付结果，成功：SUCCESS，失败：非SUCCESS，一般传FAILURE | string | 是 |
| sign | 参数签名 | string | 是 |

* 请求示例

<div style="background:#F7F7F7;padding:15px">
假定appsecret=4d6121503e6c2c8157c98796cc4f598e<br>
假定CP的请求地址是：http://cp.com/pay/nodify<br><br>

curl -X GET 'http://cp.com/pay/nodify?appid=13421341&order_id=1370481703&cp_order_id=1948312434&uid=134124312&real_fee=200&trade_name=stone&product_count=1&pay_time=1440665340&attach=2af34&result=SUCCESS&sign=abb1d204741e93d376b0f17c6f0b5af5'
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

