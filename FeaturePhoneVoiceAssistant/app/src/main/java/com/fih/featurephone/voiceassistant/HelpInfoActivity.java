package com.fih.featurephone.voiceassistant;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class HelpInfoActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpinfo);
        setHelpInfo();

        findViewById(R.id.helpInfo_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setHelpInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.baidu_unit_question_help));
        if (!CommonUtil.isSupportMultiTouch(this)) {
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_dialogue_translate_mode));
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_face_mode));
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_ocr_mode));
        } else {
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_dialogue_translate_mode_support_touch));
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_face_mode_support_touch));
            sb.append("\n\n");
            sb.append(getString(R.string.baidu_unit_question_help_ocr_mode_support_touch));
        }
        sb.append("\n\n");
        sb.append(getString(R.string.baidu_unit_question_help_keyboard_input));

        TextView helpInfoTextView = findViewById(R.id.helpInfo_text_view);
        helpInfoTextView.setText(sb.toString());
    }
}
