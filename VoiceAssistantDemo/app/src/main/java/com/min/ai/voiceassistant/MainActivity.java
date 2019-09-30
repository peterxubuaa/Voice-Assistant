package com.min.ai.voiceassistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
    public static final boolean DEBUG = false;
    private final int WINDOW_OVERLAY_PERMISSION = 1;
    private final int REQUEST_MULTIPLE_PERMISSION = 2;
    private BaiduSpeechAI mBaiduSpeechAI;
    private SpeechAction mSpeechAction;
    private BaiduUnitAI mBaiduUnitAI;
    private BaiduTTSAI mBaiduTTSAI;
    private LinearLayout mFloatingWindow;
    private boolean mSpeechEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyOverlayPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAssistant();
    }

    private void initAssistant() {
//        mBaiduSpeechAI = new BaiduSpeechAI(this, mSpeechListener);
//        mBaiduSpeechAI.initBaiduSpeech();
//        mSpeechAction = new SpeechAction(this);

        mBaiduUnitAI = new BaiduUnitAI(this, mUnitListener);
        mBaiduUnitAI.initBaiduUnit();

        mBaiduTTSAI = new BaiduTTSAI(this, mTTSListener);
        mBaiduTTSAI.initBaiduTTS();

        initFloatingComponent();
        if (null != mFloatingWindow) mFloatingWindow.findViewById(R.id.iv_voice).setVisibility(View.VISIBLE);
    }

    private void releaseAssistant() {
        releaseFloatingButton();
        if (null != mBaiduSpeechAI) mBaiduSpeechAI.releaseBaiduSpeech();
        if (null != mBaiduUnitAI) mBaiduUnitAI.releaseBaiduUnit();
        if (null != mBaiduTTSAI) mBaiduTTSAI.releaseBaiduTTS();
    }

    private void applyApkPermissions() {
        final String[] requiredPermissions = new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECORD_AUDIO};
        ArrayList<String> denyPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
                continue;
            denyPermissions.add(permission);
        }
        if (denyPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, denyPermissions.toArray(new String[0]), REQUEST_MULTIPLE_PERMISSION);
        } else {
            initAssistant();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_MULTIPLE_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    Toast.makeText(this, "Must allow all permissions", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            initAssistant();
        }
    }

    private void applyOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            applyApkPermissions();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, WINDOW_OVERLAY_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (WINDOW_OVERLAY_PERMISSION == requestCode) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Must allow Display over other apps", Toast.LENGTH_LONG).show();
                finish();
            } else {
                applyApkPermissions();
            }
        }
    }

    private void initFloatingComponent() {
        mFloatingWindow = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.floatbutton, null).findViewById(R.id.floating_toast);
        ViewGroup vg = (ViewGroup)mFloatingWindow.getParent();
        if (vg != null) vg.removeAllViews();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ((WindowManager)getSystemService(WINDOW_SERVICE)).addView(mFloatingWindow, layoutParams);

        final ImageView iv = mFloatingWindow.findViewById(R.id.iv_voice);
        iv.setOnTouchListener(new SingleDoubleClickListener (new SingleDoubleClickListener.ClickCallBack() {
            @Override
            public void onOneClick(View v) {
                if (mBaiduTTSAI.isTTSRunning()) {
                    mBaiduTTSAI.stopTTSSpeak();
                    return;
                }
                if (mSpeechEnable) {
                    if (null != mBaiduSpeechAI) mBaiduSpeechAI.stopBaiduSpeechRecognize();
                    if (null != mBaiduUnitAI) mBaiduUnitAI.stopBaiduUnit();
                } else {
                    if (null != mBaiduSpeechAI) mBaiduSpeechAI.startBaiduSpeechRecognize();
                    if (null != mBaiduUnitAI) mBaiduUnitAI.startBaiduUnit();
                }
                mSpeechEnable = !mSpeechEnable;
                iv.setImageResource(mSpeechEnable? R.drawable.audio_record_enable : R.drawable.audio_record_disable);
            }

            @Override
            public void onDoubleClick(View v) {
                finish();
            }
        }));
        moveTaskToBack(true); //key point
    }

    private void releaseFloatingButton() {
        if (null != mFloatingWindow) {
            ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mFloatingWindow);
        }
    }

    private BaiduSpeechAI.onSpeechListener mSpeechListener = new BaiduSpeechAI.onSpeechListener() {
        @Override
        public void onShowDebugInfo(String info) {
            showDebugInfo(info, false);
        }

        @Override
        public void onExit() {
            mSpeechEnable = false;
            ((ImageView)mFloatingWindow.findViewById(R.id.iv_voice)).setImageResource(R.drawable.audio_record_disable);
        }

        @Override
        public void onFinalResult(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            if (null != mSpeechAction) {
                mSpeechAction.parseAction(filterPunctuation(result));
            }
        }
    };

    private BaiduUnitAI.onUnitListener mUnitListener = new BaiduUnitAI.onUnitListener() {
        @Override
        public void onShowDebugInfo(String info, boolean reset) {
            showDebugInfo(info, reset);
        }

        @Override
        public void onExit() {
            mSpeechEnable = false;
            ((ImageView)mFloatingWindow.findViewById(R.id.iv_voice)).setImageResource(R.drawable.audio_record_disable);
        }

        @Override
        public void onFinalResult(String question, String answer) {
            if (TextUtils.isEmpty(answer)) return;

            showFinalResponse(question, answer);
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            final int MAX_TTS_LENGTH = 1024 / 2; //chines character is two bytes
            int startPos = 0;
//            int size = result.length();
            while (true) {
                if ((startPos + MAX_TTS_LENGTH) > answer.length()) {
                    mBaiduTTSAI.startTTSSpeak(answer.substring(startPos));
                    break;
                } else {
                    mBaiduTTSAI.startTTSSpeak(answer.substring(startPos, MAX_TTS_LENGTH));
                    startPos += MAX_TTS_LENGTH;
                }
            }
        }
    };

    private BaiduTTSAI.onTTSListener mTTSListener = new BaiduTTSAI.onTTSListener() {
        @Override
        public void start() {
        }

        @Override
        public void end() {
        }
    };

    private void showDebugInfo(String info, boolean reset) {
        final int MAX_LINE = 500;
        TextView tv = mFloatingWindow.findViewById(R.id.bottom_tv_info);
        if (reset) tv.setText("");
        tv.append(info);
        if (tv.getLineCount() > MAX_LINE) {
            Layout layout = tv.getLayout();
            int start = layout.getLineEnd(tv.getLineCount() - MAX_LINE - 1);
            int end;
            String text = tv.getText().toString();
            StringBuilder sb = new StringBuilder();
            for (int i = tv.getLineCount() - MAX_LINE; i < tv.getLineCount(); i++) {
                end = layout.getLineEnd(i);
                String line = text.substring(start, end);
                start = end;
                sb.append(line);
            }
            tv.setText(sb.toString());
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ScrollView scrollView = mFloatingWindow.findViewById(R.id.bottom_scrollview);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void showFinalResponse(String question, String answer) {
        TextView tv = mFloatingWindow.findViewById(R.id.top_tv_info);
        tv.setText("");
        tv.append(question);
        tv.append("\n");
        tv.append(answer);
    }

    private String filterPunctuation(String result) {
        return result.replaceAll("[\\p{Punct}\\s]+", "");
    }
}
