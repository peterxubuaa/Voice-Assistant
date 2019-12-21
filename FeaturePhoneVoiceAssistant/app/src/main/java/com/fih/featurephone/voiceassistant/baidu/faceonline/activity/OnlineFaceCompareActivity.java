package com.fih.featurephone.voiceassistant.baidu.faceonline.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceCompare;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class OnlineFaceCompareActivity extends OnlineFaceDoubleBaseActivity {

    private FaceCompare mFaceCompare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_face_compare);

        initView();
        mFaceCompare = new FaceCompare(this, mFaceOnlineListener);
    }

    void initTouchScreenView() {
        super.initTouchScreenView();
        findViewById(R.id.compare_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCompare(mFirstFaceFilePath, mSecondFaceFilePath);
            }
        });
    }

    private BaiduFaceOnlineAI.OnFaceOnlineListener mFaceOnlineListener = new BaiduFaceOnlineAI.OnFaceOnlineListener() {
        @Override
        public void onError(String msg) {
            CommonUtil.toast(OnlineFaceCompareActivity.this, msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    ((TextView)findViewById(R.id.compare_score_text_view)).setText((String)result);
                }
            });
        }
    };

    public void onCompare(final String templateFilePath, final String targetFilePath) {
        showProgressDialog(getString(R.string.baidu_unit_working));
        new Thread() {
            @Override
            public void run() {
                mFaceCompare.request(templateFilePath, targetFilePath);
            }
        }.start();
    }
}
