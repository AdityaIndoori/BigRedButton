package com.example.indoo.mixslack_1;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SingletonRequestQueue {
    private static SingletonRequestQueue mInstance;
    private RequestQueue mRequestQueue;

    private SingletonRequestQueue(Context context){
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized SingletonRequestQueue getInstance(Context context){
        if (mInstance == null)
            mInstance = new SingletonRequestQueue(context);
        return mInstance;
    }

    public RequestQueue getmRequestQueue() {
        return mRequestQueue;
    }
}
