package com.min.aiassistant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.min.aiassistant.utils.CommonUtil;

public class HelpInfoActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpinfo);
        setHelpInfo();

        findViewById(R.id.active_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActiveBaidu();
            }
        });

        findViewById(R.id.helpInfo_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.title_text_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtil.toast(HelpInfoActivity.this, "x_ugang@sohu.com(13911592763)");
                return true;
            }
        });
    }

    private void setHelpInfo() {
        TextView helpInfoTextView = findViewById(R.id.helpInfo_text_view);
        String sb = getString(R.string.baidu_unit_about) + CommonUtil.getVersionName(this) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_dialogue_translate_mode_support_touch) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_face_mode_support_touch) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_ocr_mode_support_touch) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_classify_mode_support_touch) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_keyboard_input) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_extra_fun) +
                "\n\n" +
                getString(R.string.baidu_unit_question_help_other);
        helpInfoTextView.setText(sb);
    }

    private void onActiveBaidu() {
        Uri uri = Uri.parse("https://console.bce.baidu.com/?fromai=1&locale=zh-cn#/aip/overview");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
