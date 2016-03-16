package com.yuwan.middle.demo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yuwan8.middleware.Config;
import com.yuwan8.middleware.Null;
import com.yuwan8.middleware.Order;
import com.yuwan8.middleware.PlayerInfo;
import com.yuwan8.middleware.Response;
import com.yuwan8.middleware.YW;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends Activity {

	private TextView tokenTV;
	private TextView playinfoTV;
    private TextView switchAccountTV;
	//测试时该值需替换为游戏的appsecret
	private String mAppsecret = "";
    //游戏的appkey
    private String appkey = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.test_activity_main);

		tokenTV = (TextView) findViewById(R.id.token_tv);
		playinfoTV = (TextView) findViewById(R.id.playInfo_tv);
        switchAccountTV = (TextView) findViewById(R.id.switch_account_tv);

        //游戏自己的appkey
        YW.getInstance().initSDK(MainActivity.this, appkey);
        YW.getInstance().onActivityCreate(this);
        YW.getInstance().setExitStrategy(mIExitStrategy);
        YW.getInstance().onActivityCreate(this);

		View login_btn = findViewById(R.id.login_btn);
		login_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				YW.getInstance().login(new Response<String>() {

					@Override
					public void onFailure(String msg) {
						Toast.makeText(MainActivity.this, "login msg = " + msg,
								Toast.LENGTH_LONG).show();
						tokenTV.setText("error login:" + msg);
					}

					@Override
					public void onSuccess(String data) {
						tokenTV.setText("token:" + data);
						subPlayerInfo();
					}
				});

			}
		});
	
		View pay_btn = findViewById(R.id.pay_btn);
		pay_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Order order = getOrder();
				YW.getInstance().pay(order, new Response<Null>() {

					@Override
					public void onFailure(String msg) {
						Toast.makeText(MainActivity.this, "pay msg = " + msg,
								Toast.LENGTH_LONG).show();

					}

					@Override
					public void onSuccess(Null arg0) {
						Toast.makeText(MainActivity.this, "pay success",
								Toast.LENGTH_LONG).show();
					}
				});

				System.out.println("");
			}
		});


		View logout_btn = findViewById(R.id.logout_btn);
		logout_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIExitStrategy.onExitDirectly();
			}
		});

		View exit_show_dialog = findViewById(R.id.exit_show_dialog);
		exit_show_dialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIExitStrategy.onShowExitDialog();

			}
		});

        View switchAccount_btn = findViewById(R.id.switch_account_btn);
        Config config = YW.getInstance().getConfig();
        boolean isSupportSwitchAccount = config.isSupportSwitchAccount();
        if (isSupportSwitchAccount) {
            switchAccount_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    YW.getInstance().switchAccount(new Response<String>() {

                        @Override
                        public void onFailure(String msg) {
                            switchAccountTV.setText("" + msg);

                        }

                        @Override
                        public void onSuccess(String msg) {
                            switchAccountTV.setText("" + msg);
                        }
                    });

                }
            });
        } else {
            switchAccount_btn.setVisibility(View.GONE);
        }
	}

	private void subPlayerInfo() {
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setDataType(PlayerInfo.TYPE_ENTER_GAME);
		playerInfo.setRoleID("1001");
		playerInfo.setRoleName("悟空");
		playerInfo.setRoleLevel("22");
		playerInfo.setServerID(1000);
		playerInfo.setServerName("花果山");
		playerInfo.setMoneyNum(500);

		YW.getInstance().submitPlayerInfo(playerInfo, new Response<String>() {

			@Override
			public void onFailure(String arg0) {
				Toast.makeText(MainActivity.this, "游戏信息提交失败", Toast.LENGTH_LONG)
						.show();

			}

			@Override
			public void onSuccess(String msg) {
				playinfoTV.setText("玩家信息提交：" + msg);

			}
		});
	}

	private Order getOrder() {
		Order order = new Order();
		order.setProductId("12321");
		order.setProductName("300天晶石");

		order.setMoney("100");
		order.setRatio("10");
		order.setCoinNum("222");
        //测试的uid
		order.setUid("999999");
		order.setGameName("大圣传");
		order.setRoleName("齐天大圣");
		order.setRoleId("1001");

		order.setRoleLevel(11);
		order.setOrderID("2001");
		order.setExtension("1");
		order.setNotify_url("http://www.baidu00.com");

		String src_sign = getSignString(order);
		String sign = encryption(src_sign);

		System.out.println("order src_sign = " + src_sign);
		System.out.println("order sign = " + sign);

		order.setSign(sign);

		return order;
	}

	public String encryption(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

	public String getSignString(Order order) {
		String signString = "";
		try {
			signString += "GameName="
					+ URLEncoder.encode(order.getGameName(), "utf-8");
			signString += "&money=" + order.getMoney();
			signString += "&orderID=" + order.getOrderID();
			signString += "&productId=" + order.getProductId();
			signString += "&productName="
					+ URLEncoder.encode(order.getProductName(), "utf-8");
			signString += "&uid=" + order.getUid();
			signString += "&appsecret=" + mAppsecret;

			Log.d("pay_sign", signString);
			return signString;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		YW.getInstance().onActivityResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		YW.getInstance().onActivityPause();

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		YW.getInstance().onActivityReStart();

	}

	@Override
	protected void onStop() {
		super.onStop();
		YW.getInstance().onActivityStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		YW.getInstance().onActivityDestroy();

	}

	@Override
	public void onBackPressed() {
		YW.getInstance().tryExit(MainActivity.this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		YW.getInstance().onActivityResult(requestCode, resultCode, data);
	}

	private YW.IExitStrategy mIExitStrategy = new YW.IExitStrategy() {
		@Override
		public void onShowExitDialog() {
			new Handler(MainActivity.this.getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setTitle("提示");
					builder.setMessage("您确定要退出吗？");
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									dialog.dismiss();
									finish();

								}
							});
					builder.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									dialog.dismiss();
								}
							});
					builder.create().show();
				}
			});
		}
		@Override
		public void onExitDirectly() {
			finish();
		}
	};
}
