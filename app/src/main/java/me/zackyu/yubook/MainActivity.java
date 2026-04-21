package me.zackyu.yubook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
    private iDBHelper iDBHelper;
    private Button buttonAboutMe;
    private Button buttonPlsRcd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置透明状态栏和导航栏
        setupTransparentWindow();

        applyAppSettings();
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
        initDatabase();
        showData();

        // 注意：不再使用 RenderEffect 模糊，改用半透明背景实现玻璃效果

        Button button_main_back = findViewById(R.id.button_main_back);
        button_main_back.setOnClickListener(v -> finish());
    }

    private void setupTransparentWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            // 设置状态栏文字为浅色
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void applyAppSettings() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        String fontSize = prefs.getString("font_size", "medium");
        applyFontSize(fontSize);

        String themeColor = prefs.getString("theme_color", "gold");
        applyThemeColor(themeColor);
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

    private void applyThemeColor(String themeColor) {
        // 主题颜色应用逻辑（可选实现）
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