package me.zackyu.yubook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import me.zackyu.yubook.db.iDBHelper;

public class MainActivity extends AppCompatActivity {
    private Button buttonNewRecord;
    private Button buttonRecords;
    private TextView textIncome;
    private TextView textPay;
    private TextView textTotal;
    private TextView titleApp;
    private TextClock textClock;
    private TextView labelTotal, labelIncome, labelPay;
    private TextView sectionQuick, sectionQuickRecord, sectionMore;
    private iDBHelper iDBHelper;
    private Button buttonAboutMe;
    private Button buttonPlsRcd;

    // 存储需要自适应颜色的View
    private View[] textViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTransparentWindow();
        applyAppSettings();
        setContentView(R.layout.activity_main);

        initViews();
        setupAdaptiveTextColors();  // 设置自适应颜色
        setListeners();
        initDatabase();
        showData();

        Button button_main_back = findViewById(R.id.button_main_back);
        button_main_back.setOnClickListener(v -> finish());
    }

    private void setupAdaptiveTextColors() {
        // 收集所有需要自适应颜色的TextView
        textViews = new View[]{
                findViewById(R.id.title_app),
                findViewById(R.id.textClock),
                findViewById(R.id.label_total),
                findViewById(R.id.text_total),
                findViewById(R.id.label_income),
                findViewById(R.id.text_income),
                findViewById(R.id.label_pay),
                findViewById(R.id.text_pay),
                findViewById(R.id.section_quick),
                findViewById(R.id.section_quick_record),
                findViewById(R.id.section_more)
        };

        // 获取背景颜色并应用自适应文字颜色
        int backgroundColor = getBackgroundColor();
        applyAdaptiveTextColors(backgroundColor);
    }

    private int getBackgroundColor() {
        Drawable background = getWindow().getDecorView().getRootView().getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable gradient = (GradientDrawable) background;
            // 对于渐变，取平均色
            return Color.parseColor("#667eea"); // 默认颜色
        }
        return Color.WHITE;
    }

    private void applyAdaptiveTextColors(int backgroundColor) {
        // 计算背景亮度
        double brightness = calculateBrightness(backgroundColor);

        // 根据亮度选择文字颜色
        int textColor;
        int secondaryTextColor;
        int labelColor;

        if (brightness > 0.5) {
            // 背景亮色，使用深色文字
            textColor = Color.parseColor("#1C1C1E");
            secondaryTextColor = Color.parseColor("#3A3A3C");
            labelColor = Color.parseColor("#8E8E93");
        } else {
            // 背景暗色，使用浅色文字
            textColor = Color.parseColor("#FFFFFF");
            secondaryTextColor = Color.parseColor("#E5E5EA");
            labelColor = Color.parseColor("#C6C6C8");
        }

        // 应用颜色到各个TextView
        for (View view : textViews) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (view.getId() == R.id.label_total ||
                        view.getId() == R.id.label_income ||
                        view.getId() == R.id.label_pay) {
                    textView.setTextColor(labelColor);
                } else if (view.getId() == R.id.section_quick ||
                        view.getId() == R.id.section_quick_record ||
                        view.getId() == R.id.section_more) {
                    textView.setTextColor(secondaryTextColor);
                } else {
                    textView.setTextColor(textColor);
                }
            }
        }

        // 设置卡片背景颜色（半透明，根据亮度调整）
        CardView cardView = findViewById(R.id.card_asset);
        if (cardView != null) {
            if (brightness > 0.5) {
                cardView.setCardBackgroundColor(Color.parseColor("#E6FFFFFF"));
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#B31C1C1E"));
            }
        }
    }

    private double calculateBrightness(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        // 使用感知亮度公式
        return (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
    }

    // 可选：动态获取壁纸主色调
    private int getWallpaperDominantColor() {
        try {
            Drawable wallpaper = getWallpaper();
            if (wallpaper instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) wallpaper).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    // 采样降低计算量
                    Bitmap sampled = Bitmap.createScaledBitmap(bitmap, 10, 10, true);
                    int pixel = sampled.getPixel(0, 0);
                    sampled.recycle();
                    return pixel;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Color.parseColor("#667eea");
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

    @Override
    protected void onResume() {
        super.onResume();
        showData();
        // 重新应用自适应颜色（防止设置页改变主题）
        setupAdaptiveTextColors();
    }

    private void initViews() {
        buttonNewRecord = findViewById(R.id.button_new_record);
        buttonRecords = findViewById(R.id.button_records);
        textIncome = findViewById(R.id.text_income);
        textPay = findViewById(R.id.text_pay);
        textTotal = findViewById(R.id.text_total);
        buttonAboutMe = findViewById(R.id.button_main_about_me);
        buttonPlsRcd = findViewById(R.id.button_main_pls);
        titleApp = findViewById(R.id.title_app);
        textClock = findViewById(R.id.textClock);
        labelTotal = findViewById(R.id.label_total);
        labelIncome = findViewById(R.id.label_income);
        labelPay = findViewById(R.id.label_pay);
        sectionQuick = findViewById(R.id.section_quick);
        sectionQuickRecord = findViewById(R.id.section_quick_record);
        sectionMore = findViewById(R.id.section_more);
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
        });

        Button btnSettings = findViewById(R.id.button_home_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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