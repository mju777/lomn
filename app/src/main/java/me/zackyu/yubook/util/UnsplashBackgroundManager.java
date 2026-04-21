package me.zackyu.yubook.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UnsplashBackgroundManager {

    // 免费版 API（不需要 Access Key）
    private static final String UNSPLASH_RANDOM_URL = "https://source.unsplash.com/random/1080x2400";
    private static final String[] FALLBACK_URLS = {
            "https://picsum.photos/1080/2400?random=1",
            "https://picsum.photos/1080/2400?random=2",
            "https://picsum.photos/1080/2400?random=3"
    };

    private Context context;
    private OnBackgroundLoadedListener listener;
    private ExecutorService executorService;

    public interface OnBackgroundLoadedListener {
        void onBackgroundLoaded(Drawable drawable);
        void onError(String error);
    }

    public UnsplashBackgroundManager(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void setOnBackgroundLoadedListener(OnBackgroundLoadedListener listener) {
        this.listener = listener;
    }

    // 加载随机图片
    public void loadRandomBackground() {
        String randomUrl = UNSPLASH_RANDOM_URL + "?t=" + System.currentTimeMillis();
        loadImageWithGlide(randomUrl);
    }

    // 使用 Glide 加载图片
    private void loadImageWithGlide(String url) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        if (listener != null) {
                            listener.onBackgroundLoaded(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // 清理时的处理
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        // 加载失败时尝试备用URL
                        loadFallbackImage();
                    }
                });
    }

    // 加载备用图片
    private void loadFallbackImage() {
        int index = (int) (Math.random() * FALLBACK_URLS.length);
        String fallbackUrl = FALLBACK_URLS[index] + "&t=" + System.currentTimeMillis();

        Glide.with(context)
                .load(fallbackUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        if (listener != null) {
                            listener.onBackgroundLoaded(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        // 最终还是失败，使用纯色背景
                        loadDefaultColorBackground();
                    }
                });
    }

    // 加载默认纯色背景
    private void loadDefaultColorBackground() {
        if (listener != null) {
            // 使用渐变色作为默认背景
            ColorDrawable defaultDrawable = new ColorDrawable(Color.parseColor("#667eea"));
            listener.onBackgroundLoaded(defaultDrawable);
            if (context != null) {
                listener.onError("使用默认背景");
            }
        }
    }

    // 关闭资源
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}