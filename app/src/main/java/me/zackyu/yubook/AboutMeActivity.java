package me.zackyu.yubook;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Random;

public class AboutMeActivity extends AppCompatActivity {

 private View rootView;
 private Handler mainHandler;

 // 多个在线图片源
 private final String[] IMAGE_URLS = {
         "https://picsum.photos/1080/2400",
         "https://picsum.photos/1080/2400?random=1",
         "https://picsum.photos/1080/2400?random=2",
         "https://picsum.photos/1080/2400?random=3"
 };

 // 预设渐变背景颜色
 private final int[][] GRADIENT_COLORS = {
         {0xFF667eea, 0xFF764ba2},
         {0xFFf093fb, 0xFFf5576c},
         {0xFF4facfe, 0xFF00f2fe},
         {0xFF43e97b, 0xFF38f9d7}
 };

 private int currentUrlIndex = 0;
 private boolean isLoadingImage = false;

 @Override
 protected void onCreate(@Nullable Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  setupTransparentWindow();
  applyAppSettings();
  setContentView(R.layout.activity_about_me);

  rootView = findViewById(android.R.id.content);
  mainHandler = new Handler(Looper.getMainLooper());

  // 加载在线图片背景
  loadOnlineBackground();

  initViews();

  Button buttonAboutBack = findViewById(R.id.button_about_me_back);
  buttonAboutBack.setOnClickListener(v -> finish());
 }

 private void setupTransparentWindow() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
   Window window = getWindow();
   window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
   window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
   window.setStatusBarColor(Color.TRANSPARENT);
   window.setNavigationBarColor(Color.TRANSPARENT);

   View decorView = window.getDecorView();
   decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
  }
 }

 private void applyAppSettings() {
  SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
  String fontSize = prefs.getString("font_size", "medium");
  applyFontSize(fontSize);
 }

 private void applyFontSize(String fontSize) {
  Resources res = getResources();
  Configuration config = new Configuration(res.getConfiguration());
  switch (fontSize) {
   case "small":
    config.fontScale = 0.85f;
    break;
   case "medium":
    config.fontScale = 1.0f;
    break;
   case "large":
    config.fontScale = 1.15f;
    break;
   case "xlarge":
    config.fontScale = 1.3f;
    break;
  }
  res.updateConfiguration(config, res.getDisplayMetrics());
 }

 private void loadOnlineBackground() {
  if (isLoadingImage) return;
  isLoadingImage = true;

  String imageUrl = IMAGE_URLS[currentUrlIndex] + "?t=" + System.currentTimeMillis();
  loadImageWithGlide(imageUrl);
 }

 private void loadImageWithGlide(String url) {
  Glide.with(this)
          .load(url)
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .skipMemoryCache(true)
          .timeout(15000)
          .into(new CustomTarget<Drawable>() {
           @Override
           public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
            if (rootView != null && resource != null) {
             rootView.setBackground(resource);
             isLoadingImage = false;
            }
           }

           @Override
           public void onLoadFailed(Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            currentUrlIndex++;
            if (currentUrlIndex < IMAGE_URLS.length) {
             loadOnlineBackground();
            } else {
             isLoadingImage = false;
             setGradientBackground();
            }
           }

           @Override
           public void onLoadCleared(Drawable placeholder) {}
          });
 }

 private void setGradientBackground() {
  Random random = new Random();
  int[] colors = GRADIENT_COLORS[random.nextInt(GRADIENT_COLORS.length)];

  GradientDrawable gradient = new GradientDrawable(
          GradientDrawable.Orientation.TL_BR,
          colors
  );
  gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);

  if (rootView != null) {
   rootView.setBackground(gradient);
  }
 }

 private void initViews() {
  // 设置复制功能
  TextView textWechatAccount = findViewById(R.id.tv_wechat_account);
  setUpCopyClickListener(textWechatAccount, "微信");

  TextView textQqAccount = findViewById(R.id.tv_qq_account);
  setUpCopyClickListener(textQqAccount, "QQ");

  TextView textEmail = findViewById(R.id.tv_email);
  setUpCopyClickListener(textEmail, "邮箱");

  TextView textAuthorName = findViewById(R.id.tv_author_name);
  setUpCopyClickListener(textAuthorName, "联系人");
 }

 private void setUpCopyClickListener(TextView textView, String appName) {
  if (textView != null) {
   textView.setOnClickListener(v -> {
    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    String text = textView.getText().toString();
    cm.setPrimaryClip(android.content.ClipData.newPlainText("label", text));
    Toast.makeText(AboutMeActivity.this, "已复制 " + text + "，打开" + appName + "粘贴", Toast.LENGTH_LONG).show();
   });
  }
 }
}