package com.github.tvbox.osc.player;

import android.content.Context;
import android.text.TextUtils;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.IJKCode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import xyz.doikki.videoplayer.ijk.IjkPlayer;

public class IjkMediaPlayer extends IjkPlayer {

    private final String codec;

//    public IjkMediaPlayer(Context context, IJKCode codec) {
//        super(context);
//        this.codec = codec;
//    }

    public IjkMediaPlayer(Context context, String codec) {
        super(context);
        this.codec = codec;
    }

    @Override
    public void setOptions() {
        super.setOptions();
//        IJKCode codecTmp = this.codec == null ? ApiConfig.get().getCurrentIJKCode() : this.codec;
//        LinkedHashMap<String, String> options = codecTmp.getOption();
//        if (options != null) {
//            for (String key : options.keySet()) {
//                String value = options.get(key);
//                String[] opt = key.split("\\|");
//                int category = Integer.parseInt(opt[0].trim());
//                String name = opt[1].trim();
//                try {
//                    assert value != null;
//                    long valLong = Long.parseLong(value);
//                    mMediaPlayer.setOption(category, name, valLong);
//                } catch (Exception e) {
//                    mMediaPlayer.setOption(category, name, value);
//                }
//            }
//        }
        if (Objects.equals(codec, "硬解码")) {
            mMediaPlayer.setOption(1, "mediacodec", 1);
            mMediaPlayer.setOption(1, "mediacodec-all-videos", 1);
            mMediaPlayer.setOption(1, "mediacodec-auto-rotate", 1);
            mMediaPlayer.setOption(1, "mediacodec-handle-resolution-change", 1);
        }
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            if (path.contains("rtsp") || path.contains("udp") || path.contains("rtp")) {
                mMediaPlayer.setOption(1, "infbuf", 1);
                mMediaPlayer.setOption(1, "rtsp_transport", "tcp");
                mMediaPlayer.setOption(1, "rtsp_flags", "prefer_tcp");
                mMediaPlayer.setOption(1, "probesize", 512 * 1000);
                mMediaPlayer.setOption(1, "analyzeduration", 2 * 1000 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setDataSource(path, headers);
    }
}
