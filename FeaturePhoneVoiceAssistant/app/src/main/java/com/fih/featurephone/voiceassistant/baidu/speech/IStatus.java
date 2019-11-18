package com.fih.featurephone.voiceassistant.baidu.speech;

public interface IStatus {
    int STATUS_NONE = 0;
    int STATUS_READY = 1;
    int STATUS_SPEAKING = 2;
    int STATUS_RECOGNITION = 3;
    int STATUS_FINISHED = 4;
    int STATUS_PARTIAL_FINISHED = 5;
    int STATUS_LONG_SPEECH_FINISHED = 6;
    int STATUS_STOPPED = 7;
    int STATUS_OFFLINE_LOAD = 8;
    int STATUS_OFFLINE_UNLOAD = 9;
    int STATUS_FINAL_RESULT = 10;
    int STATUS_FINISHED_ERROR = 11;
}
