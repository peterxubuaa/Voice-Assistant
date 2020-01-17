package com.min.aiassistant.speechaction;

import com.min.aiassistant.baidu.unit.BaiduUnitAI;

public interface BaseAction {
    boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse);
}
