package me.zackyu.yubook;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import me.zackyu.yubook.db.iDBHelper;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // 控件
    private Button btnBack;
    private Switch switchNightMode, switchNotification;
    private TextView tvFontSize, tvCacheSize, tvVersion;
    private TextView tvWallpaperStatus;
    private LinearLayout layoutBackup, layoutRestore, layoutClearCache;
    private LinearLayout layoutFontSize;
    private LinearLayout layoutWallpaper;
    private LinearLayout layoutIncomeSources, layoutExpenseSources, layoutAccounts;
    private View rootView;

    // 数据库帮助类
    private iDBHelper dbHelper;

    // 文件选择器 - 必须在 onCreate 中尽早注册
    private ActivityResultLauncher<String[]> restoreFileLauncher;
    private ActivityResultLauncher<Intent> backupFolderLauncher;

    // 壁纸相关
    private final String[] WALLPAPER_URLS = {
            "https://picsum.photos/1080/2400",
            "https://picsum.photos/1080/2400?random=1",
            "https://picsum.photos/1080/2400?random=2",
            "https://picsum.photos/1080/2400?random=3"
    };

    private final int[][] GRADIENT_COLORS = {
            {0xFF667eea, 0xFF764ba2},
            {0xFFf093fb, 0xFFf5576c},
            {0xFF4facfe, 0xFF00f2fe},
            {0xFF43e97b, 0xFF38f9d7}
    };

    private Handler mainHandler;
    private boolean isLoadingImage = false;
    private int currentUrlIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. 先读取设置
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        applyTheme();

        // 2. 注册文件选择器 - 必须在 super.onCreate 之前或之后立即执行，不能在后面
        registerFilePickers();

        super.onCreate(savedInstanceState);

        setupTransparentWindow();
        setContentView(R.layout.activity_settings);

        rootView = findViewById(android.R.id.content);
        mainHandler = new Handler(Looper.getMainLooper());
        dbHelper = new iDBHelper(this, "MyAccount.db", null, 1);

        // 应用保存的壁纸设置
        applyWallpaperSetting();

        initViews();
        loadSettings();
        setupListeners();
        calculateCacheSize();
    }

    /**
     * 注册文件选择器 - 必须在 onCreate 中尽早调用
     */
    private void registerFilePickers() {
        // 恢复文件选择器 - 使用 RegisterForActivityResult 的正确方式
        restoreFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        confirmRestore(uri);
                    }
                }
        );

        // 备份文件夹选择器
        backupFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            // 持久化权限
                            getContentResolver().takePersistableUriPermission(treeUri,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            performBackupToUri(treeUri);
                        }
                    }
                }
        );
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

    private void applyWallpaperSetting() {
        String wallpaperMode = sharedPreferences.getString("wallpaper_mode", "gradient");

        if ("online".equals(wallpaperMode)) {
            loadOnlineWallpaper();
        } else if ("gradient".equals(wallpaperMode)) {
            setGradientWallpaper();
        }
    }

    private void loadOnlineWallpaper() {
        if (isLoadingImage) return;
        isLoadingImage = true;

        String imageUrl = WALLPAPER_URLS[currentUrlIndex] + "?t=" + System.currentTimeMillis();

        Glide.with(this)
                .load(imageUrl)
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
                        if (currentUrlIndex < WALLPAPER_URLS.length) {
                            loadOnlineWallpaper();
                        } else {
                            isLoadingImage = false;
                            setGradientWallpaper();
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {}
                });
    }

    private void setGradientWallpaper() {
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

    private void refreshWallpaper() {
        String wallpaperMode = sharedPreferences.getString("wallpaper_mode", "gradient");
        if ("online".equals(wallpaperMode)) {
            currentUrlIndex = 0;
            isLoadingImage = false;
            loadOnlineWallpaper();
        } else {
            setGradientWallpaper();
        }
        Toast.makeText(this, "壁纸已更换", Toast.LENGTH_SHORT).show();
    }

    private void applyTheme() {
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        String fontSize = sharedPreferences.getString("font_size", "medium");
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
        tvWallpaperStatus = findViewById(R.id.tv_wallpaper_status);

        layoutFontSize = findViewById(R.id.layout_font_size);
        layoutBackup = findViewById(R.id.layout_backup);
        layoutRestore = findViewById(R.id.layout_restore);
        layoutClearCache = findViewById(R.id.layout_clear_cache);
        layoutWallpaper = findViewById(R.id.layout_wallpaper);

        layoutIncomeSources = findViewById(R.id.layout_income_sources);
        layoutExpenseSources = findViewById(R.id.layout_expense_sources);
        layoutAccounts = findViewById(R.id.layout_accounts);

        String wallpaperMode = sharedPreferences.getString("wallpaper_mode", "gradient");
        tvWallpaperStatus.setText("online".equals(wallpaperMode) ? "在线图片" : "渐变背景");

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText(versionName);
        } catch (Exception e) {
            tvVersion.setText("1.0.0");
        }
    }

    private void loadSettings() {
        switchNightMode.setChecked(sharedPreferences.getBoolean("night_mode", false));
        switchNotification.setChecked(sharedPreferences.getBoolean("notification", true));

        String fontSize = sharedPreferences.getString("font_size", "medium");
        tvFontSize.setText(getFontSizeText(fontSize));
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
        btnBack.setOnClickListener(v -> finish());

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();
            Toast.makeText(this, isChecked ? "夜间模式已开启，重启应用后生效" : "夜间模式已关闭，重启应用后生效", Toast.LENGTH_LONG).show();
            recreate();
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            editor.putBoolean("notification", isChecked);
            editor.apply();
            Toast.makeText(this, isChecked ? "已开启记账提醒" : "已关闭记账提醒", Toast.LENGTH_SHORT).show();
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationsPermission();
            }
        });

        layoutFontSize.setOnClickListener(v -> showFontSizeDialog());
        layoutBackup.setOnClickListener(v -> selectBackupFolder());
        layoutRestore.setOnClickListener(v -> selectRestoreFile());
        layoutClearCache.setOnClickListener(v -> showClearCacheDialog());
        layoutWallpaper.setOnClickListener(v -> showWallpaperDialog());

        layoutIncomeSources.setOnClickListener(v -> manageIncomeSources());
        layoutExpenseSources.setOnClickListener(v -> manageExpenseSources());
        layoutAccounts.setOnClickListener(v -> manageAccounts());
    }

    /**
     * 选择备份文件夹
     */
    private void selectBackupFolder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            backupFolderLauncher.launch(intent);
        } else {
            Toast.makeText(this, "请选择备份文件夹", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择恢复文件
     */
    private void selectRestoreFile() {
        restoreFileLauncher.launch(new String[]{"application/json"});
    }

    /**
     * 执行备份到指定URI
     */
    private void performBackupToUri(android.net.Uri folderUri) {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("正在备份")
                .setMessage("请稍候...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        new Thread(() -> {
            try {
                DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
                if (folder == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "无法访问所选文件夹", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 创建备份文件名
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String backupFileName = "yubook_backup_" + timestamp + ".json";

                DocumentFile backupFile = folder.createFile("application/json", backupFileName);
                if (backupFile == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "无法创建备份文件", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 创建JSON对象存储所有数据
                JSONObject backupData = new JSONObject();

                // 1. 备份数据库
                File dbFile = getDatabasePath("MyAccount.db");
                if (dbFile != null && dbFile.exists()) {
                    String dbBase64 = encodeFileToBase64(dbFile);
                    backupData.put("database", dbBase64);
                    backupData.put("database_name", "MyAccount.db");
                }

                // 2. 导出所有记录到JSON
                JSONObject recordsData = exportAllRecords();
                backupData.put("records", recordsData);

                // 3. 备份SharedPreferences设置
                JSONObject settingsData = new JSONObject();
                settingsData.put("night_mode", sharedPreferences.getBoolean("night_mode", false));
                settingsData.put("notification", sharedPreferences.getBoolean("notification", true));
                settingsData.put("font_size", sharedPreferences.getString("font_size", "medium"));
                settingsData.put("wallpaper_mode", sharedPreferences.getString("wallpaper_mode", "gradient"));
                backupData.put("settings", settingsData);

                // 4. 备份时间戳
                backupData.put("backup_time", timestamp);
                backupData.put("app_version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

                // 写入文件
                OutputStream os = getContentResolver().openOutputStream(backupFile.getUri());
                os.write(backupData.toString().getBytes());
                os.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "备份成功！\n文件：" + backupFileName + "\n位置：" + folder.getName(), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                Log.e("Settings", "Backup error", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "备份失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 确认恢复
     */
    private void confirmRestore(android.net.Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("确认恢复")
                .setMessage("恢复数据将覆盖当前所有数据，此操作不可撤销！\n确定要继续吗？")
                .setPositiveButton("确定", (d, w) -> performRestoreFromUri(uri))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 执行恢复
     */
    private void performRestoreFromUri(android.net.Uri uri) {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("正在恢复")
                .setMessage("请稍候...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        new Thread(() -> {
            try {
                // 读取备份文件
                InputStream is = getContentResolver().openInputStream(uri);
                StringBuilder content = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                is.close();

                JSONObject backupData = new JSONObject(content.toString());

                // 1. 恢复数据库
                if (backupData.has("database")) {
                    String dbBase64 = backupData.getString("database");
                    byte[] dbBytes = android.util.Base64.decode(dbBase64, android.util.Base64.DEFAULT);

                    // 关闭现有数据库连接
                    dbHelper.close();

                    // 删除旧数据库
                    File dbFile = getDatabasePath("MyAccount.db");
                    if (dbFile.exists()) {
                        dbFile.delete();
                    }

                    // 写入新数据库
                    FileOutputStream fos = new FileOutputStream(dbFile);
                    fos.write(dbBytes);
                    fos.close();
                }

                // 2. 恢复SharedPreferences设置
                if (backupData.has("settings")) {
                    JSONObject settings = backupData.getJSONObject("settings");
                    editor = sharedPreferences.edit();
                    if (settings.has("night_mode")) editor.putBoolean("night_mode", settings.getBoolean("night_mode"));
                    if (settings.has("notification")) editor.putBoolean("notification", settings.getBoolean("notification"));
                    if (settings.has("font_size")) editor.putString("font_size", settings.getString("font_size"));
                    if (settings.has("wallpaper_mode")) editor.putString("wallpaper_mode", settings.getString("wallpaper_mode"));
                    editor.apply();
                }

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "数据恢复成功！应用将重启", Toast.LENGTH_LONG).show();

                    // 延迟重启应用
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        System.exit(0);
                    }, 1500);
                });

            } catch (Exception e) {
                Log.e("Settings", "Restore error", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "恢复失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 导出所有记录到JSON
     */
    private JSONObject exportAllRecords() throws Exception {
        JSONObject recordsData = new JSONObject();
        JSONArray recordsArray = new JSONArray();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM record", null);

        while (cursor.moveToNext()) {
            JSONObject record = new JSONObject();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                String columnName = cursor.getColumnName(i);
                String value = cursor.getString(i);
                record.put(columnName, value);
            }
            recordsArray.put(record);
        }
        cursor.close();
        db.close();

        recordsData.put("records", recordsArray);
        recordsData.put("record_count", recordsArray.length());

        return recordsData;
    }

    /**
     * 显示壁纸设置对话框
     */
    private void showWallpaperDialog() {
        String[] items = {"渐变背景", "在线随机图片"};
        int current = "online".equals(sharedPreferences.getString("wallpaper_mode", "gradient")) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("壁纸设置")
                .setSingleChoiceItems(items, current, (dialog, which) -> {
                    String mode = which == 0 ? "gradient" : "online";
                    editor = sharedPreferences.edit();
                    editor.putString("wallpaper_mode", mode);
                    editor.apply();

                    tvWallpaperStatus.setText(items[which]);

                    if (which == 0) {
                        setGradientWallpaper();
                    } else {
                        currentUrlIndex = 0;
                        isLoadingImage = false;
                        loadOnlineWallpaper();
                    }

                    dialog.dismiss();
                    Toast.makeText(this, "壁纸已切换为：" + items[which], Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("刷新壁纸", (dialog, which) -> refreshWallpaper())
                .show();
    }

    /**
     * 将文件转换为Base64字符串
     */
    private String encodeFileToBase64(File file) throws java.io.IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        fis.read(buffer);
        fis.close();
        return android.util.Base64.encodeToString(buffer, android.util.Base64.DEFAULT);
    }

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

                    applyFontSize(sizeValue);

                    dialog.dismiss();
                    Toast.makeText(this, "字体大小已设置为：" + sizeText, Toast.LENGTH_SHORT).show();
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

    private void clearCache() {
        try {
            deleteDir(getCacheDir());
            deleteDir(getCodeCacheDir());

            // 清除Glide缓存
            Glide.get(this).clearDiskCache();
            Glide.get(this).clearMemory();

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

    private void requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void manageIncomeSources() {
        Intent intent = new Intent(this, IncomeSourceActivity.class);
        startActivity(intent);
    }

    private void manageExpenseSources() {
        Intent intent = new Intent(this, ExpenseSourceActivity.class);
        startActivity(intent);
    }

    private void manageAccounts() {
        Intent intent = new Intent(this, AccountManageActivity.class);
        startActivity(intent);
    }
}