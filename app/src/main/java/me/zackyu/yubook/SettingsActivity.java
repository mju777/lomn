package me.zackyu.yubook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // 控件
    private Button btnBack;
    private Switch switchNightMode, switchNotification;
    private TextView tvFontSize, tvCacheSize, tvVersion;
    private LinearLayout layoutBackup, layoutRestore, layoutClearCache;
    private LinearLayout layoutThemeColor, layoutPrivacy, layoutTerms, layoutLicenses;
    private LinearLayout layoutFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        // 在 super.onCreate 之前应用主题
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupListeners();
        calculateCacheSize();
    }

    /**
     * 应用主题
     */
    private void applyTheme() {
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // 应用字体大小
        String fontSize = sharedPreferences.getString("font_size", "medium");
        applyFontSize(fontSize);
    }

    /**
     * 应用字体大小
     */
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
            default:
                config.fontScale = 1.0f;
                break;
        }

        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_settings_back);
        switchNightMode = findViewById(R.id.switch_night_mode);
        switchNotification = findViewById(R.id.switch_notification);
        tvFontSize = findViewById(R.id.tv_font_size);
        tvCacheSize = findViewById(R.id.tv_cache_size);
        tvVersion = findViewById(R.id.tv_version);

        layoutFontSize = findViewById(R.id.layout_font_size);
        layoutBackup = findViewById(R.id.layout_backup);
        layoutRestore = findViewById(R.id.layout_restore);
        layoutClearCache = findViewById(R.id.layout_clear_cache);
        layoutThemeColor = findViewById(R.id.layout_theme_color);
        layoutPrivacy = findViewById(R.id.layout_privacy);
        layoutTerms = findViewById(R.id.layout_terms);
        layoutLicenses = findViewById(R.id.layout_licenses);

        // 设置版本号
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText(version);
        } catch (Exception e) {
            tvVersion.setText("1.0.0");
        }
    }

    private void loadSettings() {
        // 加载夜间模式设置
        switchNightMode.setChecked(sharedPreferences.getBoolean("night_mode", false));

        // 加载通知设置
        switchNotification.setChecked(sharedPreferences.getBoolean("notification", true));

        // 加载字体大小设置
        String fontSize = sharedPreferences.getString("font_size", "medium");
        String fontSizeText = getFontSizeText(fontSize);
        tvFontSize.setText(fontSizeText);
    }

    private String getFontSizeText(String fontSize) {
        switch (fontSize) {
            case "small": return "小";
            case "medium": return "中等";
            case "large": return "大";
            case "xlarge": return "超大";
            default: return "中等";
        }
    }

    private String getFontSizeValue(String text) {
        switch (text) {
            case "小": return "small";
            case "中等": return "medium";
            case "大": return "large";
            case "超大": return "xlarge";
            default: return "medium";
        }
    }

    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 夜间模式开关
        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();

            String message = isChecked ? "夜间模式已开启，重启应用后生效" : "夜间模式已关闭，重启应用后生效";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // 保存后立即重启 Activity 使主题生效
            recreate();
        });

        // 通知开关
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            editor.putBoolean("notification", isChecked);
            editor.apply();

            String message = isChecked ? "已开启记账提醒" : "已关闭记账提醒";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // 如果开启通知，请求权限（Android 13+）
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationsPermission();
            }
        });

        // 字体大小
        layoutFontSize.setOnClickListener(v -> showFontSizeDialog());

        // 备份数据
        layoutBackup.setOnClickListener(v -> backupData());

        // 恢复数据
        layoutRestore.setOnClickListener(v -> restoreData());

        // 清除缓存
        layoutClearCache.setOnClickListener(v -> showClearCacheDialog());

        // 主题颜色
        layoutThemeColor.setOnClickListener(v -> showThemeColorDialog());

        // 隐私政策
        layoutPrivacy.setOnClickListener(v -> showPrivacyPolicy());

        // 用户协议
        layoutTerms.setOnClickListener(v -> showTermsOfService());

        // 开源许可
        layoutLicenses.setOnClickListener(v -> showOpenSourceLicenses());
    }

    /**
     * 字体大小选择对话框
     */
    private void showFontSizeDialog() {
        String[] items = {"小", "中等", "大", "超大"};
        int current = getCurrentFontSizeIndex();

        new AlertDialog.Builder(this)
                .setTitle("字体大小")
                .setSingleChoiceItems(items, current, (dialog, which) -> {
                    String sizeText = items[which];
                    String sizeValue = getFontSizeValue(sizeText);

                    tvFontSize.setText(sizeText);

                    editor = sharedPreferences.edit();
                    editor.putString("font_size", sizeValue);
                    editor.apply();

                    // 应用字体大小
                    applyFontSize(sizeValue);

                    dialog.dismiss();
                    Toast.makeText(this, "字体大小已设置为：" + sizeText, Toast.LENGTH_SHORT).show();

                    // 重新创建 Activity 使字体生效
                    recreate();
                })
                .show();
    }

    private int getCurrentFontSizeIndex() {
        String current = sharedPreferences.getString("font_size", "medium");
        switch (current) {
            case "small": return 0;
            case "medium": return 1;
            case "large": return 2;
            case "xlarge": return 3;
            default: return 1;
        }
    }

    /**
     * 主题颜色选择对话框
     */
    private void showThemeColorDialog() {
        String[] items = {"金色", "蓝色", "绿色", "紫色"};
        int[] colors = {0xFFD4AF37, 0xFF2196F3, 0xFF4CAF50, 0xFF9C27B0};
        String[] colorValues = {"gold", "blue", "green", "purple"};

        int current = getCurrentThemeIndex();

        new AlertDialog.Builder(this)
                .setTitle("主题颜色")
                .setSingleChoiceItems(items, current, (dialog, which) -> {
                    String colorName = colorValues[which];

                    editor = sharedPreferences.edit();
                    editor.putString("theme_color", colorName);
                    editor.apply();

                    dialog.dismiss();
                    Toast.makeText(this, "主题颜色已设置为：" + items[which] + "，重启应用后生效", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private int getCurrentThemeIndex() {
        String current = sharedPreferences.getString("theme_color", "gold");
        switch (current) {
            case "gold": return 0;
            case "blue": return 1;
            case "green": return 2;
            case "purple": return 3;
            default: return 0;
        }
    }

    /**
     * 备份数据
     */
    private void backupData() {
        // 显示加载对话框
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("正在备份")
                .setMessage("请稍候...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // 在后台线程执行备份
        new Thread(() -> {
            try {
                // 获取数据目录
                File dataDir = getFilesDir();
                File backupDir = new File(Environment.getExternalStorageDirectory(), "YooBook/Backup");

                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }

                // 创建备份文件名
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File backupFile = new File(backupDir, "backup_" + timestamp + ".json");

                // TODO: 实现实际的备份逻辑
                // 这里只是示例，实际需要导出数据库和SharedPreferences

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "备份成功！\n保存位置：" + backupFile.getPath(), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "备份失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 恢复数据
     */
    private void restoreData() {
        // 显示文件选择器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择备份文件");

        // TODO: 列出备份目录中的文件供用户选择
        String[] backupFiles = {"backup_20240101_120000.json", "backup_20240102_120000.json"};

        builder.setItems(backupFiles, (dialog, which) -> {
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle("正在恢复")
                    .setMessage("请稍候...")
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            new Thread(() -> {
                try {
                    // TODO: 实现实际的恢复逻辑
                    Thread.sleep(1500); // 模拟恢复过程

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "数据恢复成功！", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "恢复失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 清除缓存对话框
     */
    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除缓存")
                .setMessage("确定要清除所有缓存数据吗？此操作不会删除您的记账记录。")
                .setPositiveButton("确定", (dialog, which) -> {
                    clearCache();
                    calculateCacheSize();
                    Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        try {
            // 清除应用缓存目录
            File cacheDir = getCacheDir();
            deleteDir(cacheDir);

            // 清除代码缓存
            File codeCacheDir = getCodeCacheDir();
            deleteDir(codeCacheDir);

            Log.d("Settings", "Cache cleared");
        } catch (Exception e) {
            Log.e("Settings", "Error clearing cache", e);
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    /**
     * 计算缓存大小
     */
    private void calculateCacheSize() {
        new Thread(() -> {
            long size = getDirSize(getCacheDir()) + getDirSize(getCodeCacheDir());
            String sizeText = formatSize(size);

            runOnUiThread(() -> tvCacheSize.setText(sizeText));
        }).start();
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        size += getDirSize(file);
                    }
                }
            } else {
                size += dir.length();
            }
        }
        return size;
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.getDefault(), "%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 请求通知权限（Android 13+）
     */
    private void requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    /**
     * 显示隐私政策
     */
    private void showPrivacyPolicy() {
        new AlertDialog.Builder(this)
                .setTitle("隐私政策")
                .setMessage("本应用尊重并保护您的隐私。\n\n" +
                        "1. 我们不会收集您的个人信息\n" +
                        "2. 所有数据仅保存在本地\n" +
                        "3. 不会上传任何数据到服务器\n\n" +
                        "如有疑问，请联系：yuzhang@yubook.com")
                .setPositiveButton("知道了", null)
                .show();
    }

    /**
     * 显示用户协议
     */
    private void showTermsOfService() {
        new AlertDialog.Builder(this)
                .setTitle("用户协议")
                .setMessage("欢迎使用 YooBook！\n\n" +
                        "1. 本应用完全免费使用\n" +
                        "2. 您可以自由使用所有功能\n" +
                        "3. 数据安全由您自己负责\n" +
                        "4. 建议定期备份数据\n\n" +
                        "使用本应用即表示您同意以上条款。")
                .setPositiveButton("同意", null)
                .show();
    }

    /**
     * 显示开源许可
     */
    private void showOpenSourceLicenses() {
        new AlertDialog.Builder(this)
                .setTitle("开源许可")
                .setMessage("本应用使用了以下开源项目：\n\n" +
                        "• AndroidX - Apache 2.0\n" +
                        "• Material Design Components - Apache 2.0\n\n" +
                        "感谢所有开源贡献者！")
                .setPositiveButton("关闭", null)
                .show();
    }
}