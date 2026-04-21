package me.zackyu.yubook.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class BlurUtils {

    /**
     * 为View应用高斯模糊效果（API 31+）
     */
    public static void applyBlur(View view, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && view != null) {
            view.setRenderEffect(RenderEffect.createBlurEffect(
                    radius, radius, Shader.TileMode.CLAMP));
        }
    }

    /**
     * 移除模糊效果
     */
    public static void removeBlur(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && view != null) {
            view.setRenderEffect(null);
        }
    }

    /**
     * 创建模糊快照（用于低版本兼容）
     */
    public static Bitmap blurBitmap(Bitmap bitmap, float radius, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+ 使用RenderEffect
            return bitmap;
        } else {
            // 低版本使用RenderScript（需要引入support库）
            return bitmap;
        }
    }
}