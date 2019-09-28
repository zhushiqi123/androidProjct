package com.blue.autoweb;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.RelativeLayout;
import android.widget.Toast;


import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.iflytek.cloud.SpeechSynthesizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    @BindView(R.id.fab_mode)
    FloatingActionButton fabMode;
    @BindView(R.id.fab_refresh)
    FloatingActionButton fabRefresh;
    @BindView(R.id.fab_screen)
    FloatingActionButton fabScreen;
    @BindView(R.id.fab_set)
    FloatingActionButton fabSet;
    @BindView(R.id.fab_menu)
    FloatingActionsMenu fabMenu;

    private static final String[] MODE = new String[]{":3000", ":3000/checkTime", ":3334"};
    private static final String[] MODE_Name = new String[]{"思拓微项目进度", "考勤", "劲源通项目进度"};
    private static final int DAKA_SLEEP_TIME = 600;
    private WebView webView;
    private SpeechSynthesizer mTts;
    private RelativeLayout black;
    private String[] voiceName = {
            "xiaoyan",
            "xiaofeng",
            "xiaoqi",
            "vinn",
            "vils",
            "aisjying",
            "aisbabyxu",
            "aisjinger",
            "yefang",
            "aisduck",
            "aisxmeng",
            "aismengchun",
            "ziqi",
            "aisduoxu",
            "xiaoxin",
            "xiaowanzi",
            "dalong",
            "xiaomei",
            "aisxlin",
            "xiaoqian",
            "aisxrong",
            "xiaokun",
            "aisxqiang",
            "aisxying",
    };
    private boolean flag = true;
    private int modeIndex = 0;
    private String Url = "";
    private boolean projectLight;
    private boolean dakaLight;
    private int dataLightTime = DAKA_SLEEP_TIME;
    private SharedPreferences sp;
    private Disposable webSub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sp = getSharedPreferences("config", MODE_PRIVATE);
        Url = sp.getString("url", "http://192.168.1.206");
        modeIndex = sp.getInt("mode", 1);
        projectLight = sp.getBoolean("projectLight", false);
        dakaLight = sp.getBoolean("dakaLight", true);
        webView = (WebView) findViewById(R.id.webView);
        black = (RelativeLayout) findViewById(R.id.black);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface(this);
        javaScriptInterface.setLightlistener(new JavaScriptInterface.Lightlistener() {
            @Override
            public void open() {
                dataLightTime = DAKA_SLEEP_TIME;
                if (black.getVisibility() == View.VISIBLE) {
                    Message message = new Message();
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }
            }
        });
        webView.addJavascriptInterface(javaScriptInterface, "android");


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });
        webView.setWebViewClient(new MyWebViewClient() {


            @Override
            public void loadTimeOut() {
                subLoad();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                subLoad();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    try {
                        if (projectLight) {
                            if (!visibleTime()) {
                                Message message = new Message();
                                message.arg1 = 0;
                                handler.sendMessage(message);
                            } else if (visibleTime()) {
                                Message message = new Message();
                                message.arg1 = 1;
                                handler.sendMessage(message);
                            }
                        }

                        if (dakaLight) {
                            dataLightTime -= 30;
                            if (dataLightTime < 0) {
                                Message message = new Message();
                                message.arg1 = 0;
                                handler.sendMessage(message);
                            }
                        }
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        loadUrl();
    }

    private void setLight(int brightness) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    private boolean visibleTime() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            return false;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        switch (hour) {
            case 8:
            case 9:
            case 10:
            case 11:
            case 14:
            case 15:
            case 16:
                return true;
            case 13:
                if (minute < 30)
                    return false;
                else
                    return true;
            case 17:
                if (minute < 30)
                    return true;
                else
                    return false;
            default:
                return false;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case 0:
                    if (black.getVisibility() != View.VISIBLE) {
                        setLight(0);
                        black.setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    if (black.getVisibility() != View.GONE) {
                        setLight(255);
                        black.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag = false;
    }

    private void loadUrl() {
        Log.e("xxx", "loadUrl");
        webView.loadUrl(Url + MODE[modeIndex]);
    }

    private void subLoad() {
        if (webSub != null && !webSub.isDisposed())
            webSub.dispose();
        webSub = Observable
                .timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    loadUrl();
                });
    }

    @OnClick({R.id.fab_mode, R.id.fab_refresh, R.id.fab_screen, R.id.fab_set, R.id.fab_menu, R.id.black})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_mode:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setSingleChoiceItems(MODE_Name, modeIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        modeIndex = which;
                        sp.edit().putInt("mode", which).commit();
                        dataLightTime = DAKA_SLEEP_TIME;
                        switch (which) {
                            case 0:
                                dakaLight = false;
                                sp.edit().putBoolean("dakaLight", dakaLight).commit();
                                break;
                            case 1:
                                dakaLight = true;
                                sp.edit().putBoolean("dakaLight", dakaLight).commit();
                                break;
                        }
                        loadUrl();
                    }
                });
                dialog.setTitle("模式选择");
                dialog.show();
                fabMenu.collapse();
                break;
            case R.id.fab_refresh:
                loadUrl();
                fabMenu.collapse();
                break;
            case R.id.fab_screen:
                setLight(0);
                black.setVisibility(View.VISIBLE);
                fabMenu.collapse();
                break;
            case R.id.fab_set:
                AlertDialog.Builder dialog2 = new AlertDialog.Builder(this);
                View dview = View.inflate(this, R.layout.dialog_set, null);
                dialog2.setView(dview);
                final AppCompatCheckedTextView cbLight = (AppCompatCheckedTextView) dview.findViewById(R.id.cb_light);
                cbLight.setChecked(projectLight);
                cbLight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        projectLight = !projectLight;
                        cbLight.setChecked(!cbLight.isChecked());
                        sp.edit().putBoolean("projectLight", projectLight).commit();
                    }
                });
                dialog2.setTitle("设置");
                dialog2.setPositiveButton("确定", null);
                dialog2.show();
                fabMenu.collapse();
                break;
            case R.id.fab_menu:
                break;
            case R.id.black:
                dataLightTime = DAKA_SLEEP_TIME;
                setLight(255);
                black.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (fabMenu.isExpanded()) {

                Rect outRect = new Rect();
                fabMenu.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    fabMenu.collapse();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void changeMode() {
        if (modeIndex < 0)
            modeIndex = MODE_Name.length + modeIndex;
        if (modeIndex >= MODE_Name.length) {
            modeIndex -= MODE_Name.length;
        }
        sp.edit().putInt("mode", modeIndex).commit();
        dataLightTime = DAKA_SLEEP_TIME;
        switch (modeIndex) {
            case 0:
                dakaLight = false;
                sp.edit().putBoolean("dakaLight", dakaLight).commit();
                projectLight = true;
                sp.edit().putBoolean("projectLight", projectLight).commit();
                Toast.makeText(this, "4楼项目展示", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                dakaLight = true;
                sp.edit().putBoolean("dakaLight", dakaLight).commit();
                projectLight = false;
                sp.edit().putBoolean("projectLight", projectLight).commit();
                Toast.makeText(this, "考勤", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                dakaLight = false;
                sp.edit().putBoolean("dakaLight", dakaLight).commit();
                projectLight = true;
                sp.edit().putBoolean("projectLight", projectLight).commit();
                Toast.makeText(this, "5楼项目展示", Toast.LENGTH_SHORT).show();
                break;
        }
        loadUrl();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == 0) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    webView.reload();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    modeIndex--;
                    changeMode();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    modeIndex++;
                    changeMode();
                    break;
            }
            dataLightTime = DAKA_SLEEP_TIME;
            setLight(255);
            black.setVisibility(View.GONE);
        }
        return super.dispatchKeyEvent(event);
    }
}