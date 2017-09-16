package com.example.cannabisitterapp;

import android.content.Context;
import android.util.LruCache;

import com.microsoft.windowsazure.mobileservices.*;

import java.net.MalformedURLException;

/**
 * Created by noamg on 15/09/2017.
 */

public class AzureServiceAdapter {
    private static final String mMobileBackendUrl = "https://cannabisitterapp.azurewebsites.net";
    private Context mContext;
    private MobileServiceClient mClient;
    private static AzureServiceAdapter mInstance = null;

    public static LruCache<Integer, String> mUserIdCache;
    private static final int LRU_CACHE_SIZE = 256;

    private AzureServiceAdapter(Context context) throws MalformedURLException {
        mContext = context;
        mClient = new MobileServiceClient(mMobileBackendUrl, mContext);
        mUserIdCache = new LruCache<Integer, String>(LRU_CACHE_SIZE);
    }

    public static void Initialize(Context context) throws MalformedURLException {
        if (mInstance == null) {
            mInstance = new AzureServiceAdapter(context);
        } else {
            throw new IllegalStateException("AzureServiceAdapter is already initialized");
        }
    }

    public static AzureServiceAdapter getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("AzureServiceAdapter is not initialized");
        }
        return mInstance;
    }

    public MobileServiceClient getClient() {
        return mClient;
    }

// Place any public methods that operate on mClient here.
}


