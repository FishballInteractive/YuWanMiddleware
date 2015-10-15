# YuWanMiddlewar(Android)接入文档

### [快速接入]()
### [工作流程]()
### [接入指南]()
  #### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [Android接入]()
  #### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [服务端接入]()
### [资源下载]()
### [常见问题]（)

##### 1. 导入鱼丸中间件Jar包
  * 在[“资源下载”][resource-download]中下载最新的鱼丸中间件Jar包
  * 在IDE中导入Jar包。各IDE导入Jar包请点击如下连接
    * [Android Studio][android-studio]
    * [Eclipse][eclipse]

##### 2. AndroidManifest配置
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

##### 3. 中间件初始化
调用初始化接口是调用其他接口的前置条件，否则调用其他接口时将抛出RuntimException。初始化接口建议在程序入口处调用，例如：Application或者入口Activity的onCreate。  
接入的示例代码如下：
```java
    /***** 调用示例 *****/
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.acitvity_main);

            String appKey = ...  // appKey向“鱼丸”申请。为保证安全，建议将appKey存到游戏服务器
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
         * @param appKey 申请的appKey
         */
        public void init(Context context, String appKey);
    }
```

##### 4. 登录接口
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

##### 5. 支付接口
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

##### 6. 登出接口
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

##### 7. 混淆配置
如果要混淆java代码，请不要混淆鱼丸中间件中的类。可以添加以下类到proguard配置，排除在混淆之外：
```java
-keep class com.yuwan8.middleware.** {*;}
```

[resource-download]: ../04.resource/00.download.md
[android-studio]: http://www.cnblogs.com/neozhu/p/3458759.html
[eclipse]: http://jingyan.baidu.com/article/466506580baf2ef549e5f8e8.html

