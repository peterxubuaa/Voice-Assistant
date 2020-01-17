package com.min.aiassistant.baidu.nlp;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.nlp.model.CorrectText;
import com.min.aiassistant.baidu.nlp.model.DNNSentence;
import com.min.aiassistant.baidu.nlp.model.NewsSummary;

//https://ai.baidu.com/ai-doc/NLP/Yk3h7h9o5
public class BaiduNLPAI extends BaiduBaseAI {
    public static final int CORRECT_TEXT_TYPE = 1;
    public static final int NEWS_SUMMARY_TYPE = 2;
    public static final int DNN_SENTENCE_TYPE = 3;

    public static final int CORRECT_TEXT_ACTION = 1;
    public static final int NEWS_SUMMARY_ACTION = 2;
    public static final int DNN_SENTENCE_ACTION = 3;

    private CorrectText mCorrectText;
    private NewsSummary mNewsSummary;
    private DNNSentence mDNNSentence;

    public BaiduNLPAI(Context context, IBaiduBaseListener listener) {
        mCorrectText = new CorrectText(context, listener);
        mNewsSummary = new NewsSummary(context, listener);
        mDNNSentence = new DNNSentence(context, listener);
    }

    public void action(final int type, final String text) {
        new Thread() {
            @Override
            public void run() {
                switch (type) {
                    case CORRECT_TEXT_TYPE:
                        mCorrectText.request(text);
                        break;
                    case NEWS_SUMMARY_TYPE:
                        mNewsSummary.request(text);
                        break;
                    case DNN_SENTENCE_TYPE:
                        mDNNSentence.request(text);
                        break;
                }
            }
        }.start();
    }
}
