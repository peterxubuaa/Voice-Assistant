package com.fih.featurephone.voiceassistant.speechaction;

import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;

public interface BaseAction {
    boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse);
}
