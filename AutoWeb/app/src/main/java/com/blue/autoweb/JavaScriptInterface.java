package com.blue.autoweb;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by macbook on 17/6/9.
 */

public class JavaScriptInterface {
    Context context;
    private SpeechSynthesizer mTts;

    public interface Lightlistener {
        void open();
    }

    private Lightlistener lightlistener;

    public void setLightlistener(Lightlistener lightlistener) {
        this.lightlistener = lightlistener;
    }

    public JavaScriptInterface(Context t) {
        context = t;
        initSpeek();
    }

    private void initSpeek() {
        mTts = SpeechSynthesizer.createSynthesizer(context, null);
        //2.合成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechConstant.SPEED, "25");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    @JavascriptInterface
    public void speek(String txt, String type) {
        mTts.setParameter(SpeechConstant.VOICE_NAME, type);
        mTts.startSpeaking(txt, listener);
    }

    @JavascriptInterface
    public void openLight() {
        if (lightlistener != null)
            lightlistener.open();
    }

    private SynthesizerListener listener = new SynthesizerListener() {


        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
}
