/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tvbox.osc.picasso;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.squareup.picasso.Downloader;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A {@link Downloader} which uses OkHttp to download images.
 */
public final class OkHttpDownLoader implements Downloader {
    @VisibleForTesting
    final Call.Factory client;
    private final Cache cache;
    private final boolean sharedClient = true;

    /**
     * Create a new downloader that uses the specified OkHttp instance. A response cache will not be
     * automatically configured.
     */
    public OkHttpDownLoader(OkHttpClient client) {
        this.client = (Call.Factory) client;
        this.cache = client.cache();
    }

    /** Create a new downloader that uses the specified {@link Call.Factory} instance. */
    public OkHttpDownLoader(Call.Factory client) {
        this.client = client;
        this.cache = null;
    }

    @NonNull @Override public Response load(@NonNull Request request) throws IOException {
        Request.Builder builder = request.newBuilder().addHeader("Referer", request.url().host());
        return client.newCall(builder.build()).execute();
    }

    @Override public void shutdown() {
        if (!sharedClient && cache != null) {
            try {
                cache.close();
            } catch (IOException ignored) {
            }
        }
    }
}
