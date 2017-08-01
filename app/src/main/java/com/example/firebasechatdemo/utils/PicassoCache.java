package com.example.firebasechatdemo.utils;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Etman on 7/30/2017.
 */
public class PicassoCache {

    /**
     * Static Picasso Instance
     */
    private static Picasso picassoInstance = null;

    /**
     * PicassoCache Constructor
     */
    private PicassoCache(Context context) {
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(new OkHttp3Downloader(context, Integer.MAX_VALUE));
        picassoInstance = builder.build();
    }

    /**
     * Get Singleton Picasso Instance
     *
     * @return Picasso instance
     */
    public static Picasso get(Context context) {

        if (picassoInstance == null) {

            new PicassoCache(context);
            return picassoInstance;
        }

        return picassoInstance;
    }

}
