package com.github.catvod.crawler;

import android.content.Context;
import android.util.Base64;

import com.alibaba.fastjson.JSONObject;
import com.github.tvbox.osc.util.okhttp.OKCallBack;
import com.github.tvbox.osc.util.okhttp.OkHttpUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;

public abstract class Spider {

    public static JSONObject empty = new JSONObject();

    public void init(Context context) {
    }

    public void init(Context context, String extend) {
        init(context);
    }

    /**
     * 首页数据内容
     *
     * @param filter 是否开启筛选
     * @return
     */
    public String homeContent(boolean filter) {
        return "";
    }

    /**
     * 首页最近更新数据 如果上面的homeContent中不包含首页最近更新视频的数据 可以使用这个接口返回
     *
     * @return
     */
    public String homeVideoContent() {
        return "";
    }

    /**
     * 分类数据
     *
     * @param tid
     * @param pg
     * @param filter
     * @param extend
     * @return
     */
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        return "";
    }

    /**
     * 详情数据
     *
     * @param ids
     * @return
     */
    public String detailContent(List<String> ids) {
        return "";
    }

    /**
     * 搜索数据内容
     *
     * @param key
     * @param quick
     * @return
     */
    public String searchContent(String key, boolean quick) {
        return "";
    }

    /**
     * 播放信息
     *
     * @param flag
     * @param id
     * @return
     */
    public String playerContent(String flag, String id, List<String> vipFlags) {
        return "";
    }

    /**
     * webview解析时使用 可自定义判断当前加载的 url 是否是视频
     *
     * @param url
     * @return
     */
    public boolean isVideoFormat(String url) {
        return false;
    }

    /**
     * 是否手动检测webview中加载的url
     *
     * @return
     */
    public boolean manualVideoCheck() {
        return false;
    }


    public static String textUtilsJoin(CharSequence delimiter, Iterable tokens) {
        final Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
        return sb.toString();
    }

    public static Object[] loadPic(String refer, String pic) {
        try {
            pic = new String(Base64.decode(pic, Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
            OKCallBack.OKCallBackDefault callBack = new OKCallBack.OKCallBackDefault() {
                @Override
                protected void onFailure(Call call, Exception e) {}

                @Override
                protected void onResponse(Response response) {

                }
            };
            HashMap<String, String> header = new HashMap<>();
            header.put("referer", refer);
            header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
            OkHttpUtil.get(OkHttpUtil.defaultClient(), pic, null, header, callBack);
            if (callBack.getResult().code() == 200) {
                Headers headers = callBack.getResult().headers();
                String type = headers.get("Content-Type");
                if (type == null) {
                    type = "application/octet-stream";
                }
                Object[] result = new Object[3];
                result[0] = 200;
                result[1] = type;
                System.out.println(pic);
                System.out.println(type);
                result[2] = callBack.getResult().body().byteStream();
                return result;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }
}
