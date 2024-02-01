package xyz.doikki.videoplayer.exo;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.AdtsExtractor;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.MimeTypes;

import java.io.File;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public final class ExoMediaSourceHelper {

    private Cache mCache;
    private final Context mAppContext;
    private static volatile ExoMediaSourceHelper sInstance;
    private HttpDataSource.Factory mHttpDataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private OkHttpClient mOkClient = null;
    private DataSource.Factory mDataSourceFactory;

    private ExoMediaSourceHelper(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static ExoMediaSourceHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ExoMediaSourceHelper.class) {
                if (sInstance == null) {
                    sInstance = new ExoMediaSourceHelper(context);
                }
            }
        }
        return sInstance;
    }

    public void setOkClient(OkHttpClient client) {
        mOkClient = client;
    }

    public MediaSource getMediaSource(String uri) {
        return getMediaSource(uri, null, false);
    }

    public MediaSource getMediaSource(String uri, Map<String, String> headers) {
        return getMediaSource(uri, headers, false);
    }

    public MediaSource getMediaSource(String uri, boolean isCache) {
        return getMediaSource(uri, null, isCache);
    }

    public MediaSource getMediaSource(String uri, Map<String, String> headers, boolean isCache) {
        Uri contentUri = Uri.parse(uri);
        if ("rtmp".equals(contentUri.getScheme())) {
            return new ProgressiveMediaSource.Factory(new RtmpDataSource.Factory())
                    .createMediaSource(MediaItem.fromUri(contentUri));
        } else if ("rtsp".equals(contentUri.getScheme())) {
            return new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(contentUri));
        }
        if (mHttpDataSourceFactory != null && headers != null) {
            mHttpDataSourceFactory.setDefaultRequestProperties(headers);
        }
        return new DefaultMediaSourceFactory(getDataSourceFactory(), getExtractorsFactory()).createMediaSource(getMediaItem(contentUri, MimeTypes.APPLICATION_M3U8));
    }

    private MediaItem getMediaItem(Uri uri, String mimeType) {
        MediaItem.Builder builder = new MediaItem.Builder().setUri(uri);
        if (mimeType != null) builder.setMimeType(mimeType);
        return builder.build();
    }

    private ExtractorsFactory getExtractorsFactory() {
        if (extractorsFactory == null) {
            extractorsFactory = new DefaultExtractorsFactory()
                    .setConstantBitrateSeekingEnabled(true)
                    .setConstantBitrateSeekingAlwaysEnabled(true)
                    .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES | DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS | DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM | DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS)
                    .setTsExtractorMode(TsExtractor.MODE_MULTI_PMT)
                    .setTsExtractorTimestampSearchBytes(TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES * 3)
                    .setAdtsExtractorFlags(AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING);
        }
        return extractorsFactory;
    }

    private CacheDataSource.Factory buildReadOnlyCacheDataSource(DataSource.Factory upstreamFactory, Cache cache) {
        return new CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(upstreamFactory).setCacheWriteDataSinkFactory(null).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }

    private Cache getCache() {
        if (mCache == null) {
            mCache = new SimpleCache(
                    new File(mAppContext.getExternalCacheDir(), "exo-video-cache"),
                    new LeastRecentlyUsedCacheEvictor(512 * 1024 * 1024),
                    new StandaloneDatabaseProvider(mAppContext)
            );
        }
        return mCache;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @return A new DataSource factory.
     */
    private DataSource.Factory getDataSourceFactory() {
        if (mDataSourceFactory == null) {
            mDataSourceFactory = buildReadOnlyCacheDataSource(new DefaultDataSource.Factory(mAppContext, getHttpDataSourceFactory()), getCache());
        }
        return mDataSourceFactory;
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory getHttpDataSourceFactory() {
        if (mHttpDataSourceFactory == null) {
            if (mOkClient == null) {
                mHttpDataSourceFactory = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);
            } else {
                mHttpDataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) mOkClient);
            }
        }
        return mHttpDataSourceFactory;
    }

    public void reset() {
        mHttpDataSourceFactory = null;
        mDataSourceFactory = null;
        extractorsFactory = null;
        if (mCache != null) {
            mCache.release();
            mCache = null;
        }
    }
}
