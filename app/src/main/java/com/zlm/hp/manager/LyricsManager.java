package com.zlm.hp.manager;

import android.content.Context;
import android.content.Intent;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.net.api.DownloadLyricsUtil;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import base.lyrics.LyricsReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.utils.LyricsIOUtils;
import base.lyrics.utils.LyricsUtils;
import base.utils.LoggerUtil;
import base.utils.ThreadUtil;

/**
 * 歌词管理器
 * Created by zhangliangming on 2017/8/13.
 */

public class LyricsManager {
    /**
     *
     */
    private LoggerUtil logger;
    /**
     *
     */
    private static Context mContext;

    /**
     *
     */
    private static Map<String, LyricsReader> mLyricsUtils = new HashMap<String, LyricsReader>();

    private static LyricsManager _LyricsManager;

    public LyricsManager(Context context) {
        //
        logger = LoggerUtil.getZhangLogger(context);
        this.mContext = context;
    }

    public static LyricsManager getLyricsManager(Context context) {
        if (_LyricsManager == null) {
            _LyricsManager = new LyricsManager(context);
        }
        return _LyricsManager;
    }

    /**
     * @param fileName
     * @param keyword
     * @param duration
     * @param hash
     * @return
     */
    public void loadLyricsUtil(final String fileName, final String keyword, final String duration, final String hash) {
        //1.从缓存中获取
        //2.从本地文件中获取
        //3.从网络中获取
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                AudioMessage audioMessage = new AudioMessage();
                audioMessage.setHash(hash);
                //发送搜索中广播
                Intent searchingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEARCHING);
                searchingIntent.putExtra(AudioMessage.KEY, audioMessage);
                searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(searchingIntent);

                if (mLyricsUtils.containsKey(hash)) {
                    //发送加载完成广播
                    Intent loadedIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCLOADED);
                    loadedIntent.putExtra(AudioMessage.KEY, audioMessage);
                    loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(loadedIntent);
                    return;
                }
                //
                File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));
                if (lrcFile != null) {
                    LyricsReader lyricsUtil = new LyricsReader();
                    lyricsUtil.loadLrc(lrcFile);
                    mLyricsUtils.put(hash, lyricsUtil);
                } else {

                    //发送下载中广播
                    Intent downloadingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCDOWNLOADING);
                    downloadingIntent.putExtra(AudioMessage.KEY, audioMessage);
                    downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(downloadingIntent);

                    //下载歌词
                    File saveLrcFile = new File(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, fileName + ".krc"));
                    byte[] base64ByteArray = DownloadLyricsUtil.downloadLyric(mContext, keyword, duration, hash);
                    if (base64ByteArray != null && base64ByteArray.length > 1024) {
                        LyricsReader lyricsUtil = new LyricsReader();
                        lyricsUtil.loadLrc(base64ByteArray, saveLrcFile, saveLrcFile.getName());
                        mLyricsUtils.put(hash, lyricsUtil);
                    }
                }

                //发送加载完成广播
                Intent loadedIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCLOADED);
                loadedIntent.putExtra(AudioMessage.KEY, audioMessage);
                loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(loadedIntent);
            }
        });
    }

    public LyricsReader getLyricsUtil(String hash) {
        return mLyricsUtils.get(hash);
    }

    /**
     * 使用该歌词
     *
     * @param hash
     * @param lyricsUtil
     */
    public void setUseLrcUtil(String hash, LyricsReader lyricsUtil) {
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setHash(hash);
        //发送搜索中广播
        Intent searchingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEARCHING);
        searchingIntent.putExtra(AudioMessage.KEY, audioMessage);
        searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(searchingIntent);

        if (mLyricsUtils.containsKey(hash)) {
            mLyricsUtils.remove(hash);
        }
        mLyricsUtils.put(hash, lyricsUtil);

        //保存歌词文件
        saveLrcFile(lyricsUtil.getLrcFilePath(), lyricsUtil.getLyricsInfo());

        //发送加载完成广播
        Intent loadedIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCLOADED);
        loadedIntent.putExtra(AudioMessage.KEY, audioMessage);
        loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(loadedIntent);
    }

    /**
     * 保存歌词文件
     *
     * @param lrcFilePath lrc歌词路径
     * @param lyricsInfo  lrc歌词数据
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo) {
        new Thread() {

            @Override
            public void run() {

                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }
}
