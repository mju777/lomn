package me.zackyu.yubook;
/**
 *
 Android Dialog导入说明
 import androidx.appcompat.app.AppCompatActivity;
 详细注解
 这是一个非常经典的 Android 导入语句。下面是对 import androidx.appcompat.app.AppCompatActivity; 的详细注解：

 📦 包结构解析
 java
 import androidx.appcompat.app.AppCompatActivity;
 //      │       │        │   │
 //      │       │        │   └── 类名
 //      │       │        └────── 子包名 (app)
 //      │       └─────────────── 父包名 (appcompat)
 //      └─────────────────────── 根包名 (androidx)
 🎯 作用说明
 这个语句用于导入 AndroidX 库中的 AppCompatActivity 类。它是 Android 应用开发中最基础的 Activity 类之一。

 📱 AppCompatActivity 的特点
 1. 向下兼容性
 java
 public class MainActivity extends AppCompatActivity {
 // 支持 Android 2.1 (API 7+) 到最新版本
 // 自动处理不同版本的差异
 }
 2. 提供的主要功能
 ✅ ActionBar/Toolbar 支持 - 低版本也能使用高版本的 ActionBar 特性

 ✅ Fragment 支持 - 更好的 Fragment 管理

 ✅ Material Design 主题 - 支持 Material 主题向后兼容

 ✅ 生命周期管理 - 完整的 Activity 生命周期方法

 🔄 与其他 Activity 的对比
 Activity 类型	最低 API	特性	使用场景
 AppCompatActivity	API 7+	完整兼容性 + Material Design	✅ 最推荐（绝大多数情况）
 Activity	API 1+	基础功能，无兼容包	简单应用或特殊需求
 FragmentActivity	API 11+	Fragment 支持	需要 Fragment 但不需要 AppCompat
 ComponentActivity	API 14+	Lifecycle 组件	使用 Jetpack 组件
 💻 实际使用示例
 java
 import androidx.appcompat.app.AppCompatActivity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;

 public class MyActivity extends AppCompatActivity {

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);

// 启用 ActionBar 返回按钮
if (getSupportActionBar() != null) {
getSupportActionBar().setDisplayHomeAsUpEnabled(true);
}

setContentView(R.layout.activity_main);
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
// 加载菜单资源
getMenuInflater().inflate(R.menu.main_menu, menu);
return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
// 处理菜单点击
if (item.getItemId() == android.R.id.home) {
finish();
return true;
}
return super.onOptionsItemSelected(item);
}
}
 ⚙️ Gradle 依赖配置
 在 build.gradle 文件中需要添加：

 gradle
 dependencies {
 implementation 'androidx.appcompat:appcompat:1.6.1'
 // 或更高版本
 }
 📌 重要方法
 AppCompatActivity 提供了一些特有的方法：

 java
 // 获取支持 ActionBar 的实例（替代 getActionBar()）
 getSupportActionBar()

 // 启动 Activity 并返回结果
 startActivityForResult()

 // Fragment 管理器
 getSupportFragmentManager()

 // 加载器管理器（已废弃，推荐使用 ViewModel）
 getSupportLoaderManager()
 ⚠️ 注意事项
 不要混淆：不要同时混用 android.app.Activity 和 androidx.appcompat.app.AppCompatActivity

 主题要求：使用 AppCompatActivity 的 Activity 必须在 Manifest 或代码中设置 AppCompat 主题

 资源命名：使用 AppCompat 的控件需要用对应的 AppCompat 版本（如 AppCompatButton）

 📖 历史背景
 旧版：android.support.v7.app.AppCompatActivity（Support Library）

 新版：androidx.appcompat.app.AppCompatActivity（AndroidX）

 AndroidX 是 Google 推出的新库体系，替代旧的 Support Library

 🎓 总结
 AppCompatActivity 是 Android 开发中最基础、最常用的 Activity 基类，它为应用提供了：

 最好的向下兼容性

 Material Design 支持

 现代化的 UI 组件

 与 Jetpack 组件的良好集成

 建议：所有新的 Android 项目都应该使用 AppCompatActivity 作为 Activity 的基类。


 */

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

import me.zackyu.yubook.db.iDBHelper;

public class MainActivity extends AppCompatActivity {
    private Button buttonNewRecord;
    private Button buttonRecords;
    private TextView textIncome;
    private TextView textPay;
    private TextView textTotal;
    private TextView textviewAboutMe;
    private iDBHelper iDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
        initDatabase();

        String html = "<u>about</u>";
        textviewAboutMe.setText(Html.fromHtml(html));
        showData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showData();
    }

    private void initViews() {
        buttonNewRecord = findViewById(R.id.button_new_record);
        buttonRecords = findViewById(R.id.button_records);
        textviewAboutMe = findViewById(R.id.textview_about_me);
        textIncome = findViewById(R.id.text_income);
        textPay = findViewById(R.id.text_pay);
        textTotal = findViewById(R.id.text_total);
    }

    private void setListeners() {
        textviewAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutMeActivity.class);
                startActivity(intent);
            }
        });

        buttonNewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewRecordActivity.class);
                startActivity(intent);
            }
        });

        buttonRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
                startActivity(intent);
            }
        });
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
        Double income = 0.0;

        if (cursorIncome.moveToFirst()) {
            income = cursorIncome.getDouble(0);
            textIncome.setText(new BigDecimal(income)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString());
        }

        double pay = 0.0;
        if (cursorPay.moveToFirst()) {
            pay = cursorPay.getDouble(0);
            textPay.setText(BigDecimal.ZERO.subtract(new BigDecimal(pay))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString());
        }

        double total = 0.0;
        total = income + pay;
        textTotal.setText(new BigDecimal(total)
                .setScale(2, RoundingMode.HALF_UP)
                .toString());
    }
}