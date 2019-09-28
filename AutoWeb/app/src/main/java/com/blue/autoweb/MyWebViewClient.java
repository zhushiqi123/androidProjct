package com.blue.autoweb;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by macbook on 17/7/24.
 */

public class MyWebViewClient extends WebViewClient {
    boolean timeout;

    public MyWebViewClient() {
        timeout = true;
    }

    public void loadTimeOut() {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            loadTimeOut();
        }
    };

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        timeout = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (timeout) {
                    // do what you want
                    Message message = new Message();
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        timeout = false;
    }
}