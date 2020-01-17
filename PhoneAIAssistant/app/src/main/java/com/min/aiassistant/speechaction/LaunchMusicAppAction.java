package com.min.aiassistant.speechaction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LaunchMusicAppAction implements BaseAction {
    private final String TAG = LaunchMusicAppAction.class.getSimpleName();
    private final String[] REGEX_LAUNCH_MUSIC;
    private Context mContext;
    private List<MediaEntity> mMusicFileList;

    public LaunchMusicAppAction(final Context context) {
        mContext = context;
        REGEX_LAUNCH_MUSIC = mContext.getResources().getStringArray(R.array.launch_music_regex);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!CommonUtil.checkRegexMatch(query, REGEX_LAUNCH_MUSIC)) return false;

        if (null == mMusicFileList) {
            mMusicFileList = getAllMediaList(mContext);
        }

        String musicName = CommonUtil.getRegexMatch(query, REGEX_LAUNCH_MUSIC, 1);
        if (TextUtils.isEmpty(musicName)) musicName = ""; //如果没有指定歌曲，设置为“”，则通过查询会获取第一首歌曲
        String musicFilePath = "";// = Environment.getExternalStorageDirectory() + "/Music/default.mp3";
        String musicTitle = "";// = "测试歌曲";
        for (MediaEntity mediaEntity : mMusicFileList){
            assert musicName != null;
            if (musicName.contains(mediaEntity.title) || mediaEntity.title.contains(musicName)) {
                musicFilePath = mediaEntity.path;
                musicTitle = mediaEntity.title;
                break;
            }
            if (musicName.contains(mediaEntity.display_name) || mediaEntity.display_name.contains(musicName)) {
                musicFilePath = mediaEntity.path;
                musicTitle = mediaEntity.display_name;
                break;
            }
        }

        if (TextUtils.isEmpty(musicFilePath)) {
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_play_music_fail);
        } else {
            playAudio(musicFilePath);

            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_playing_music) + musicTitle;
            bestResponse.mHint = mContext.getString(R.string.baidu_unit_hint_playing_music_list) + getSongList();
        }

        return true;
    }

    private String getSongList() {
        final int MAX_NUM_LIST = 20;//最多显示20首歌曲
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mMusicFileList.size(); i++) {
            MediaEntity mediaEntity = mMusicFileList.get(i);
            sb.append(mediaEntity.display_name);
            sb.append("\n");
            if (i > MAX_NUM_LIST) break;
        }
        return sb.toString();
    }

    private void playAudio(String audioPath){
        final Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        //        Uri uri = Uri.parse("file://" + audioPath);//"file:///sdcard/a.mp3"
        File file = new File(audioPath);
        Uri data;
        data = Uri.fromFile(file);

        intent.setDataAndType(data , "audio/mp3");

        mContext.startActivity(intent);
    }

/*    private void launchSysMusic() {
        Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");
        mContext.startActivity(intent);
    }*/

    private class MediaEntity implements Serializable {
        private static final long serialVersionUID = 1L;

        int id; //id标识
        String title; // 显示名称
        String display_name; // 文件名称
        String path; // 音乐文件的路径
        int duration; // 媒体播放总时间
//        String albums; // 专辑
        String artist; // 艺术家
//        String singer; //歌手
        long size;
        String durationStr;
    }

    private List<MediaEntity> getAllMediaList(Context context) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        List<MediaEntity> mediaList = new ArrayList<>();
        try (Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE},
                selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
            if (cursor == null) {
                Log.d(TAG, "The getMediaList cursor is null.");
                return mediaList;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                Log.d(TAG, "The getMediaList cursor count is 0.");
                return mediaList;
            }
            mediaList = new ArrayList<>();
            MediaEntity mediaEntity;
//			String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                mediaEntity = new MediaEntity();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaEntity.durationStr = String.valueOf(mediaEntity.duration);
//                if(!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
//                    continue;
//                }
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mediaList.add(mediaEntity);
            }
        } catch (Exception e) {
            Log.i(TAG, "查找音乐文件出错 " + e.toString());
        }
        return mediaList;
    }

    /*
     * 根据时间和大小，来判断所筛选的media 是否为音乐文件，具体规则为筛选小于30秒和1m一下的
     */
 /*   private boolean checkIsMusic(int time, long size) {
        if(time <= 0 || size <= 0) {
            return  false;
        }

        time /= 1000;
        int minute = time / 60;
        //	int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return ((minute <= 0 && second <= 30) || size <= 1024 * 1024);
    }*/
}
