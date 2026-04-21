package me.zackyu.yubook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
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
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import me.zackyu.yubook.db.iDBHelper;

public class MainActivity extends AppCompatActivity {
    private Button buttonNewRecord;
    private Button buttonRecords;
    private TextView textIncome;
    private TextView textPay;
    private TextView textTotal;
    private iDBHelper iDBHelper;
    private Button buttonAboutMe;
    private Button buttonPlsRcd;
    private View rootView;
    private Handler mainHandler;

    // 多个在线图片源（国内可访问）
    private final String[] IMAGE_URLS = {
            "https://picsum.photos/1080/2400",
            "https://picsum.photos/1080/2400?random=1",
            "https://picsum.photos/1080/2400?random=2",
            "https://picsum.photos/1080/2400?random=3",
            "https://picsum.photos/1080/2400?random=4",
            "https://picsum.photos/1080/2400?random=5"
    };

    // 备用图片源（必应每日壁纸）
    private final String[] BING_URLS = {
            "https://bing.img.run/1920x1080.php",
            "https://bing.img.run/1366x768.php",
            "https://bingw.jasonzeng.dev/?resolution=1920x1080"
    };

    // 预设渐变背景颜色（最终保底方案）
    private final int[][] GRADIENT_COLORS = {
            {0xFF667eea, 0xFF764ba2},  // 紫罗兰
            {0xFFf093fb, 0xFFf5576c},  // 粉红
            {0xFF4facfe, 0xFF00f2fe},  // 天空蓝
            {0xFF43e97b, 0xFF38f9d7},  // 薄荷绿
            {0xFFfa709a, 0xFFfee140},  // 日落
            {0xFF30cfd0, 0xFF330867},  // 海洋
            {0xFFa8edea, 0xFFfed6e3},  // 樱花
            {0xFFff9a9e, 0xFFfecfef},  // 糖果
            {0xFFffecd2, 0xFFfcb69f},  // 暖橙
            {0xFFa1c4fd, 0xFFc2e9fb}   // 淡蓝
    };

    private int currentUrlIndex = 0;
    private boolean isLoadingImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTransparentWindow();
        setContentView(R.layout.activity_main);

        rootView = findViewById(android.R.id.content);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setListeners();
        initDatabase();
        showData();

        // 加载在线图片背景
        loadOnlineBackground();

        Button button_main_back = findViewById(R.id.button_main_back);
        button_main_back.setOnClickListener(v -> finish());
    }

    /**
     * 加载在线图片背景（多层降级）
     */
    private void loadOnlineBackground() {
        if (isLoadingImage) return;
        isLoadingImage = true;

        String imageUrl = getNextImageUrl();
        loadImageWithGlide(imageUrl);
    }

    /**
     * 获取下一个图片URL（轮流使用不同源）
     */
    private String getNextImageUrl() {
        // 先使用 picsum，如果失败会切换到 bing
        if (currentUrlIndex < IMAGE_URLS.length) {
            String url = IMAGE_URLS[currentUrlIndex];
            // 添加时间戳避免缓存
            return url + "?t=" + System.currentTimeMillis();
        } else {
            // 使用必应壁纸
            int bingIndex = currentUrlIndex - IMAGE_URLS.length;
            if (bingIndex < BING_URLS.length) {
                return BING_URLS[bingIndex] + "?t=" + System.currentTimeMillis();
            }
        }
        return IMAGE_URLS[0] + "?t=" + System.currentTimeMillis();
    }

    /**
     * 使用 Glide 加载图片
     */
    private void loadImageWithGlide(String url) {
        Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用缓存，每次都获取新图片
                .skipMemoryCache(true)
                .timeout(15000)  // 15秒超时
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
                        // 当前URL加载失败，尝试下一个
                        currentUrlIndex++;
                        if (currentUrlIndex < IMAGE_URLS.length + BING_URLS.length) {
                            // 还有备选URL，继续尝试
                            String nextUrl = getNextImageUrl();
                            loadImageWithGlide(nextUrl);
                        } else {
                            // 所有在线图片都失败，使用渐变背景
                            isLoadingImage = false;
                            setGradientBackground();
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // 清理时的处理
                    }
                });
    }

    /**
     * 设置渐变背景（保底方案）
     */
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

    /**
     * 手动刷新背景
     */
    private void refreshBackground() {
        // 重置索引，重新开始尝试所有图片源
        currentUrlIndex = 0;
        isLoadingImage = false;
        loadOnlineBackground();
        Toast.makeText(this, "更换背景中...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置透明状态栏
     */
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

    /**
     * 应用字体大小设置
     */
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

    @Override
    protected void onResume() {
        super.onResume();
        showData();
    }

    private void initViews() {
        buttonNewRecord = findViewById(R.id.button_new_record);
        buttonRecords = findViewById(R.id.button_records);
        textIncome = findViewById(R.id.text_income);
        textPay = findViewById(R.id.text_pay);
        textTotal = findViewById(R.id.text_total);
        buttonAboutMe = findViewById(R.id.button_main_about_me);
        buttonPlsRcd = findViewById(R.id.button_main_pls);
    }

    private void setListeners() {
        Button btnIncomeQuick = findViewById(R.id.btn_income_quick);
        if (btnIncomeQuick != null) {
            btnIncomeQuick.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, QuickRecordActivity.class);
                intent.putExtra("type", "income");
                startActivity(intent);
            });
        }

        Button btnExpenseQuick = findViewById(R.id.btn_expense_quick);
        if (btnExpenseQuick != null) {
            btnExpenseQuick.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, QuickRecordActivity.class);
                intent.putExtra("type", "expense");
                startActivity(intent);
            });
        }

        buttonAboutMe.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutMeActivity.class);
            startActivity(intent);
        });

        buttonNewRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewRecordActivity.class);
            startActivity(intent);
        });

        buttonRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
        });

        buttonPlsRcd.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "感谢支持！❤️", Toast.LENGTH_SHORT).show();
            refreshBackground();
        });

        Button btnSettings = findViewById(R.id.button_home_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                refreshBackground();
            });
        }

        Button btnShare = findViewById(R.id.button_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "推荐使用小金库记账应用！");
                startActivity(Intent.createChooser(shareIntent, "分享"));
            });
        }
    }

    private void initDatabase() {
        iDBHelper = new iDBHelper(MainActivity.this, "MyAccount.db", null, 1);
    }

    @SuppressLint("SetTextI18n")
    private void showData() {
        SQLiteDatabase sqLiteDatabase = iDBHelper.getWritableDatabase();
        String sqlIncome = "select sum(amount) from record where amount > 0";
        String sqlPay = "select sum(amount) from record where amount < 0";

        Cursor cursorIncome = sqLiteDatabase.rawQuery(sqlIncome, null);
        Cursor cursorPay = sqLiteDatabase.rawQuery(sqlPay, null);
        double income = 0.0;

        if (cursorIncome.moveToFirst()) {
            income = cursorIncome.getDouble(0);
            textIncome.setText(new BigDecimal(income)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString());
        }
        cursorIncome.close();

        double pay = 0.0;
        if (cursorPay.moveToFirst()) {
            pay = cursorPay.getDouble(0);
            textPay.setText(BigDecimal.ZERO.subtract(new BigDecimal(pay))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString());
        }
        cursorPay.close();

        double total = income + pay;
        textTotal.setText(new BigDecimal(total)
                .setScale(2, RoundingMode.HALF_UP)
                .toString());
    }
}