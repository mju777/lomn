package me.zackyu.yubook;


/**
 *
 *
 Android Dialog导入说明
 import android.content.ClipboardManager;
 详细注解
 这是一个非常详细的 ClipboardManager 类注解。我将从包路径、作用、获取方式、核心方法、权限、数据存储、使用示例以及注意事项等多个维度进行深度解析。

 1. 包路径与官方定义
 java
 import android.content.ClipboardManager;
 所属包： android.content

 API 级别： 引入于 API 级别 11 (Android 3.0)，替代了旧版的 android.text.ClipboardManager。

 核心作用： 它是一个系统服务，用于管理设备上的剪贴板。它允许应用程序向剪贴板写入数据（复制），以及从剪贴板读取数据（粘贴）。

 2. 核心概念：ClipData vs ClipData.Item
 ClipboardManager 不直接操作纯文本字符串，而是操作 ClipData 对象。这使得剪贴板可以支持复杂数据（如 HTML、URI、Intent）。

 类	描述
 ClipData	剪贴板内容的容器。它可以包含 1 个或多个 Item（条目）。
 ClipData.Item	容器内的单个数据项。可以包含文本、URI、Intent 或自定义 MIME 类型的数据。
 3. 如何获取 ClipboardManager 实例
 ClipboardManager 是一个系统服务，需要通过 Context.getSystemService() 获取：

 java
 // 在 Activity 或 Service 中
 ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

 // 或者使用 ApplicationContext 获取（推荐，避免内存泄漏）
 ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
 4. 核心方法详解
 4.1 写入剪贴板 (复制)
 java
 // 1. 创建描述信息 (Label 通常用于 UI 显示，仅用于帮助用户理解)
 // 2. 创建 ClipData 对象
 // 3. 设置到剪贴板
 public void setPrimaryClip(ClipData clip);
 典型代码示例：复制普通文本

 java
 // 创建文本 ClipData
 ClipData clip = ClipData.newPlainText("label", "你要复制的文本内容");
 clipboard.setPrimaryClip(clip);
 4.2 读取剪贴板 (粘贴)
 java
 // 获取当前剪贴板中的 ClipData 对象
 public ClipData getPrimaryClip();
 典型代码示例：读取文本

 java
 if (clipboard.hasPrimaryClip()) { // 检查是否有内容
 ClipData clip = clipboard.getPrimaryClip();
 if (clip != null && clip.getItemCount() > 0) {
 // 获取第一个 Item 的文本
 CharSequence text = clip.getItemAt(0).getText();
 if (text != null) {
 String pastedText = text.toString();
 }
 }
 }
 4.3 其他重要方法
 方法	说明
 getPrimaryClipDescription()	获取当前剪贴板内容的描述信息（如 MIME 类型、标签），不包含实际数据。用于判断是否可粘贴。
 hasPrimaryClip()	判断剪贴板中是否有内容。
 clearPrimaryClip()	清除剪贴板内容。
 addPrimaryClipChangedListener(listener)	监听剪贴板内容变化（全局监听，需注意 Android 10+ 限制）。
 removePrimaryClipChangedListener(listener)	移除监听器。
 5. 常用 ClipData 工厂方法
 java
 // 纯文本
 ClipData.newPlainText(CharSequence label, CharSequence text);

 // RTF / HTML 文本
 ClipData.newHtmlText(CharSequence label, CharSequence text, String htmlText);

 // URI (如复制图片 Uri)
 ClipData.newUri(ContentResolver resolver, CharSequence label, Uri uri);

 // Intent (用于延迟加载或复制快捷方式)
 ClipData.newIntent(CharSequence label, Intent intent);
 6. 权限与安全 (Android 10+ / API 29+)
 Android 对剪贴板的访问权限越来越严格，特别是后台读取的限制。

 Android 版本	限制说明
 Android 9 (API 28) 及以下	任何应用随时可以读取剪贴板。
 Android 10 (API 29) 及以上	默认限制： 只有输入法编辑器 (IME) 或前台应用（或拥有焦点的应用）可以读取剪贴板。后台应用调用 getPrimaryClip() 通常会返回一个 ClipDescription 为空的 ClipData 或抛出异常。
 Android 12 (API 31) 及以上	增加了吐司提示：当应用调用 getPrimaryClip() 读取数据时，系统会自动弹出提示框“App 粘贴自 [来源应用]”。
 关键注意事项：

 不要在后台服务或广播接收器中频繁调用 getPrimaryClip()。

 不要试图绕过此机制（如注册无障碍服务），否则会被 Google Play 下架。

 如果确实需要后台访问（例如剪贴板管理 App），必须将 App 设为默认输入法 (IME) 或默认主屏幕应用。

 7. 完整的实用代码示例 (Kotlin/Java)
 场景：Button 复制 TextView 内容，并显示 Toast

 java
 public class MainActivity extends AppCompatActivity {

 private ClipboardManager clipboard;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_main);

 clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 TextView textView = findViewById(R.id.my_text);
 Button copyBtn = findViewById(R.id.copy_btn);
 Button pasteBtn = findViewById(R.id.paste_btn);

 copyBtn.setOnClickListener(v -> {
 String dataToCopy = textView.getText().toString();
 ClipData clip = ClipData.newPlainText("Copied Text", dataToCopy);
 clipboard.setPrimaryClip(clip);
 Toast.makeText(this, "已复制: " + dataToCopy, Toast.LENGTH_SHORT).show();
 });

 pasteBtn.setOnClickListener(v -> {
 if (clipboard.hasPrimaryClip()) {
 ClipData clip = clipboard.getPrimaryClip();
 if (clip != null && clip.getItemCount() > 0) {
 CharSequence pasted = clip.getItemAt(0).getText();
 if (pasted != null) {
 textView.setText(pasted);
 Toast.makeText(this, "粘贴成功", Toast.LENGTH_SHORT).show();
 }
 }
 } else {
 Toast.makeText(this, "剪贴板为空", Toast.LENGTH_SHORT).show();
 }
 });
 }
 }
 8. 高级用法：支持多种 MIME 类型
 在粘贴时，不要假设一定是文本，应检查 ClipDescription：

 java
 ClipboardManager clipboard = ...;
 if (clipboard.hasPrimaryClip()) {
 ClipDescription description = clipboard.getPrimaryClipDescription();
 // 检查是否包含文本
 if (description.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
 // 安全地读取文本
 CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
 }
 // 检查是否包含 URI (例如来自图库的图片路径)
 else if (description.hasMimeType(MIMETYPE_TEXT_URILIST)) {
 Uri uri = clipboard.getPrimaryClip().getItemAt(0).getUri();
 }
 }
 9. 常见误区与最佳实践
 误区 / 错误做法	正确做法
 在 Application 类或单例中持有 ClipboardManager 监听器（可能导致内存泄漏）	在 Activity onCreate 获取，onDestroy 移除监听器。
 假设剪贴板总是包含文本，直接调用 getText() 导致崩溃	先判断 hasPrimaryClip() 和 MIME 类型。
 复制大文件（如 100MB 视频）的 URI 到剪贴板	可以复制 URI，没问题。但如果尝试复制文件二进制数据到 ClipData.Item，会导致内存溢出 (OOM)。
 Android 12+ 上静默读取剪贴板	系统会弹 Toast，用户体验差。只在用户主动点击“粘贴”按钮时读取。
 总结
 ClipboardManager 是 Android 中实现复制/粘贴功能的核心 API。它采用 ClipData 作为数据载体，支持文本、HTML、URI 等多种格式。从 Android 10 开始，系统对后台读取进行了严格限制，开发者需要确保只在应用处于前台且用户触发的情况下读取剪贴板，以保证兼容性和用户体验。



 */

import android.content.ClipboardManager;

/**
 * 这是一个非常核心的Android编程类。下面为你提供 `import android.content.Context;` 的详细注解，从基础概念到深入理解。
 *
 * ### 一句话概括
 *
 * **`Context` 是 Android 应用程序环境的全局信息接口，它提供了访问系统资源、启动组件、获取系统服务等能力的“句柄”。几乎所有应用组件（Activity、Service等）都直接或间接继承自它。**
 *
 * ---
 *
 * ### 1. 核心概念：Context 是什么？
 *
 * 你可以把 `Context` 理解为**Android 应用的“上帝对象”**或**环境管家**。
 *
 * -   **字面意思**：上下文、环境、背景。
 * -   **技术本质**：一个抽象类，它的具体实现类（如 `ContextImpl`）由 Android 系统在运行时创建。它持有应用所有资源的引用。
 * -   **比喻理解**：
 *     -   **商场管理员**：你知道商场里所有店铺（资源）、水电（系统服务）、洗手间（文件目录）、电梯（启动新 Activity）的位置。任何“顾客”（你的代码组件）想做事，都需要先问你。
 *     -   **电源插座**：你的设备（App）本身不产生电（功能），但插上插座（获得 Context）后，就能接通系统提供的各种电力（资源、服务）。
 *
 * ### 2. 为什么需要 Context？主要作用（6大核心能力）
 *
 * | 能力类别 | 具体方法示例 | 说明 |
 * | :--- | :--- | :--- |
 * | **1. 访问应用资源** | `getResources()`<br>`getString()`<br>`getColor()` | 获取 `res/` 目录下的字符串、图片、颜色、布局等。 |
 * | **2. 启动组件** | `startActivity()`<br>`startService()`<br>`sendBroadcast()` | 启动 Activity、Service，发送广播（需要 Context 知道正确的包名和权限）。 |
 * | **3. 获取系统服务** | `getSystemService(Context.LAYOUT_INFLATER_SERVICE)` | 获取 `LayoutInflater`、`ActivityManager`、`NotificationManager` 等系统级服务。 |
 * | **4. 访问应用文件目录** | `getFilesDir()`<br>`getCacheDir()`<br>`openFileOutput()` | 读写应用私有的内部存储空间。 |
 * | **5. 访问数据库/SharedPreferences** | `getSharedPreferences(name, mode)`<br>`openOrCreateDatabase()` | 操作应用私有数据和设置。 |
 * | **6. 处理应用级信息** | `getPackageName()`<br>`getApplicationInfo()`<br>`getPackageManager()` | 获取应用包名、版本信息、安装的包列表等。 |
 *
 * ### 3. Context 的继承体系（谁实现了它？）
 *
 * 虽然你 `import android.content.Context;`，但实际使用的都是它的子类。
 *
 * ```text
 * Context (抽象类)
 *     ↑
 * ContextWrapper (包装器类，内部持有真正的 ContextImpl)
 *     ↑
 *     ├── Application        // 整个应用的全局 Context，单例
 *     ├── Activity           // 每个 Activity 都是一个 Context
 *     └── Service            // 每个 Service 也是一个 Context
 * ```
 *
 * -   **真正干活的是 `ContextImpl`**：所有 `Context` 抽象方法的实现都在 `ContextImpl` 类中。
 * -   **`ContextWrapper` 是代理**：Activity、Service 等通过 `ContextWrapper` 包装 `ContextImpl`，实现“外观模式”，让你调用起来感觉统一。
 *
 * ### 4. 实际应用场景与代码示例
 *
 * #### 场景1：在非 Activity 类中使用 Context（最常见的问题）
 *
 * ```java
 * // 例如：你写了一个工具类，需要加载资源
 * public class DisplayUtils {
 *     // 通过构造函数或方法参数传入 Context
 *     public static int dpToPx(Context context, float dp) {
 *         // 使用传入的 context 获取资源
 *         float density = context.getResources().getDisplayMetrics().density;
 *         return Math.round(dp * density);
 *     }
 * }
 *
 * // 在 Activity 中调用
 * int pixels = DisplayUtils.dpToPx(this, 16); // this 就是 Activity Context
 * ```
 *
 * #### 场景2：启动一个新的 Activity
 *
 * ```java
 * // 需要 Context 来告诉系统“从哪里启动”以及“启动谁”
 * Intent intent = new Intent(context, SecondActivity.class);
 * context.startActivity(intent);  // 如果 context 不是 Activity 类型，需要加 FLAG_ACTIVITY_NEW_TASK
 * ```
 *
 * #### 场景3：获取系统服务（如布局加载器）
 *
 * ```java
 * // 在 Activity 内部
 * LayoutInflater inflater = LayoutInflater.from(this); // this 是 Context
 * // 等价于
 * LayoutInflater inflater2 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 * ```
 *
 * ### 5. 深入理解：不同 Context 的区别与生命周期
 *
 * | 类型 | 实例个数 | 生命周期 | 能做的事 | 不能做的事（易错点） |
 * | :--- | :--- | :--- | :--- | :--- |
 * | **Application Context** | 1个（全局单例） | 从 App 启动到进程结束 | 单例模式、工具类、需要长生命周期的操作 | **不能**启动新的 Activity（需要新任务栈，除非加 `FLAG_ACTIVITY_NEW_TASK`）<br>**不能**创建 Dialog（需要 UI 主题） |
 * | **Activity Context** | 每个 Activity 一个 | Activity 创建到销毁 | 几乎所有 UI 相关操作、启动 Activity、显示 Dialog | **不能**在非 UI 线程或生命周期结束后使用（可能导致内存泄漏） |
 * | **Service Context** | 每个 Service 一个 | Service 创建到销毁 | 后台任务、不涉及 UI 的操作 | **不能**直接启动 Activity（需要特殊 Flag）<br>**不能**创建 Dialog |
 *
 * ### 6. 常见陷阱与最佳实践
 *
 * #### ❌ 陷阱1：内存泄漏（最严重的问题）
 *
 * ```java
 * // 错误：静态变量持有 Activity Context
 * public class MyManager {
 *     private static Context sContext;  // 静态引用
 *     public static void init(Context ctx) {
 *         sContext = ctx;  // 如果传入 Activity，Activity 无法被 GC，导致内存泄漏
 *     }
 * }
 * // ✅ 正确：应该使用 Application Context
 * MyManager.init(context.getApplicationContext());
 * ```
 *
 * #### ❌ 陷阱2：在非 UI 线程中操作 Context
 *
 * ```java
 * // 错误：在子线程中直接更新 UI（虽然与 Context 无直接关系，但容易混淆）
 * new Thread(() -> {
 *     textView.setText("hello");  // 崩溃！不能在子线程操作 View
 * }).start();
 * ```
 *
 * #### ✅ 最佳实践总结
 *
 * 1.  **优先使用 Application Context** 进行单例、工具类、长生命周期操作。
 * 2.  **谨慎使用 Activity Context** 作为内部类或匿名类的引用，容易造成泄漏。
 * 3.  **区分 `this`、`getBaseContext()`、`getApplicationContext()`**：
 *     -   `this`：当前 Activity/Service 的 Context。
 *     -   `getApplicationContext()`：全局唯一的 Application Context。
 *     -   `getBaseContext()`：几乎用不到，是 `ContextWrapper` 内部代理的底层 Context。
 * 4.  **在需要 UI 主题的地方（如 Dialog、Layout Inflation）必须使用 Activity Context**。
 *
 * ### 总结一句
 *
 * > **`Context` 是 Android 应用的“生命线”和“万能钥匙”，它让任何代码组件都能与系统环境、资源、服务进行交互。理解不同类型的 Context 及其生命周期，是写出健壮、无内存泄漏的 Android 代码的关键。**
 *
 * 希望这份注解对你有帮助！如果有具体的代码场景需要分析，欢迎继续提问。
 */
import android.content.Context;
/**
 *
 Android Dialog导入说明
 import android.os.Bundle;
 详细注解
 这是一个非常核心的 Android 开发包。以下是针对 import android.os.Bundle; 的详细注解：

 1. 包路径解析
 android：Android 应用层框架的根包。

 os：操作系统包，包含与底层系统交互、跨进程通信、以及数据持久化的核心类。

 Bundle：一种用于在不同组件（如 Activity、Service、BroadcastReceiver）之间传递数据的键值对容器。

 2. Bundle 的核心本质
 Bundle 在内部通常被视为Android 版的 HashMap<String, Object>，但具有以下特殊属性：

 序列化优化：实现了 Parcelable 接口（而非 Java 的 Serializable），专为 Android 的 Binder 跨进程通信机制优化，速度快、开销小。

 类型安全：内置大量 putXxx() 和 getXxx() 方法（putInt, getString, putParcelable 等），避免强制转换错误。

 数据量限制：默认约 1MB 上限（因版本和设备略有差异），超过会抛出 TransactionTooLargeException。

 3. 常见使用场景与代码示例
 场景1：Activity 间跳转传值
 java
 // 发送方 (MainActivity)
 Intent intent = new Intent(this, DetailActivity.class);
 Bundle bundle = new Bundle();
 bundle.putString("user_name", "张三");
 bundle.putInt("user_age", 25);
 bundle.putBoolean("is_vip", true);
 intent.putExtras(bundle);
 startActivity(intent);

 // 接收方 (DetailActivity)
 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 // 注意：getIntent().getExtras() 可能为 null
 Bundle extras = getIntent().getExtras();
 if (extras != null) {
 String name = extras.getString("user_name", "默认值");
 int age = extras.getInt("user_age");
 boolean isVip = extras.getBoolean("is_vip", false);
 }
 }
 场景2：Activity 状态保存与恢复（最经典）
 java
 // 保存状态 (系统在销毁前自动调用)
 @Override
 protected void onSaveInstanceState(Bundle outState) {
 super.onSaveInstanceState(outState);
 outState.putString("edit_content", editText.getText().toString());
 outState.putInt("scroll_position", scrollView.getScrollY());
 }

 // 恢复状态 (在 onCreate 或 onRestoreInstanceState 中)
 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 if (savedInstanceState != null) {
 String content = savedInstanceState.getString("edit_content");
 int position = savedInstanceState.getInt("scroll_position");
 // 恢复 UI 状态...
 }
 }
 场景3：Fragment 之间通信
 java
 // 创建 Fragment 时设置参数 (推荐方式)
 MyFragment fragment = new MyFragment();
 Bundle args = new Bundle();
 args.putString("param_key", "重要数据");
 fragment.setArguments(args); // 注意：必须在 Fragment 附加到 Activity 前调用

 // 在 Fragment 内部获取参数
 @Override
 public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 Bundle arguments = getArguments();
 if (arguments != null) {
 String data = arguments.getString("param_key");
 }
 }
 4. 关键技术细节
 4.1 支持的数据类型
 基础类型：int, long, float, double, boolean, String, char, byte, CharSequence

 数组类型：int[], String[], Parcelable[] 等

 集合类型：ArrayList<Integer>, ArrayList<String>

 可序列化对象：Serializable 对象（性能较差，不推荐）、Parcelable 对象（推荐）

 Bundle 嵌套：Bundle 自身也可以作为值存入另一个 Bundle

 4.2 与 Parcelable 的配合
 java
 // 定义 Parcelable 数据类
 public class User implements Parcelable {
 // ... 实现 writeToParcel, describeContents, CREATOR
 }

 // 存入 Bundle
 Bundle bundle = new Bundle();
 bundle.putParcelable("user_key", new User("李四", 30));

 // 取出 Bundle
 User user = bundle.getParcelable("user_key");
 4.3 性能注意事项
 java
 // ❌ 不推荐：存入大量数据或大对象
 Bitmap largeBitmap = ...;
 bundle.putParcelable("image", largeBitmap); // 可能超过 1MB 限制

 // ✅ 推荐：传递 URI 或文件路径
 bundle.putString("image_uri", "content://...");
 5. 常见误区澄清
 误区	正确理解
 Bundle 可以在不同 App 间传递	只能在同一 App 内或系统组件间（如启动系统相机），跨 App 需用 ContentProvider 或 Intent.EXTRA_STREAM
 Bundle 可以无限存储数据	有约 1MB 大小限制，大对象建议用数据库或文件
 onSaveInstanceState 保证被调用	用户主动按返回键时不会调用，仅系统因配置变更（旋转屏幕）或内存回收时调用
 6. 核心方法速查表
 方法分类	示例方法
 存入数据	putString(key, value), putInt(key, value), putParcelable(key, parcelable)
 取出数据	getString(key), getInt(key, defaultValue), getParcelable(key)
 检查键	containsKey(key), keySet(), isEmpty()
 删除数据	remove(key), clear()
 合并	putAll(bundle)
 总结
 Bundle 是 Android 组件通信的轻量级数据护照，理解它的特性（特别是序列化机制和大小限制）对于编写健壮的 Android 应用至关重要。几乎所有涉及组件间数据传递的场景（Intent、Fragment、状态恢复）都离不开它。



 */
import android.os.Bundle;

import android.os.PersistableBundle;
/**
 * `import android.view.View;` 是 Android 开发中非常核心的一条导入语句。下面为你提供详细的注解。
 *
 * ### 1. 语句结构解析
 *
 * -   **`import`**：Java/Kotlin 的关键字，用于引入一个类或整个包，使得在当前文件中可以直接使用类的简写名称（如 `View`），而无需每次写完整的包路径（如 `android.view.View`）。
 * -   **`android.view`**：这是 Android SDK 中的一个核心包名。
 *     -   `android`：Android 框架的根命名空间。
 *     -   `view`：表示“视图”或“界面组件”的子包。所有用户界面相关的核心类都位于此包或它的子包中。
 * -   **`View`**：这是包中的核心类名。
 *
 * ### 2. `View` 类的核心作用
 *
 * `View` 类是 Android 所有**用户界面（UI）组件**的**基类**。
 *
 * -   **基本概念**：一个 `View` 对象代表屏幕上的一个**矩形区域**，负责处理该区域的**绘制**和**事件处理**。
 * -   **类比理解**：可以把 `View` 想象成一块“画布”或一个“零件”。按钮、文本框、图片、复选框等所有你能看到的控件，都是这块“零件”的具体化。
 *
 * ### 3. `View` 的直接子类（常见的UI组件）
 *
 * 由于 `View` 是基类，它有许多具体的子类，你见过的几乎所有UI控件都直接或间接继承自 `View`。例如：
 *
 * -   `TextView`：显示文本。
 * -   `Button`：按钮，用户可以点击。
 * -   `EditText`：文本输入框。
 * -   `ImageView`：显示图片。
 * -   `ProgressBar`：进度条。
 * -   `CheckBox`：复选框。
 *
 * ### 4. 为什么需要 `import android.view.View;`？
 *
 * 当你需要直接使用 `View` 类本身，而不是它的子类时，这个导入语句就是必须的。常见场景如下：
 *
 * #### 场景 1：在代码中动态创建 View
 * ```java
 * import android.view.View;
 * // ... 在 Activity 或 Fragment 中
 * View myView = new View(this);  // 创建一个最基础的 View 对象
 * ```
 *
 * #### 场景 2：设置点击监听器
 * 这是最常用的场景之一。`View.OnClickListener` 是一个定义在 `View` 类内部的接口。
 * ```java
 * import android.view.View;
 * // ...
 *
 * Button myButton = findViewById(R.id.button);
 * myButton.setOnClickListener(new View.OnClickListener() {  // 注意这里使用了 View.
 *     @Override
 *     public void onClick(View v) {  // 这里的参数 v 也是一个 View 对象
 *         // 处理点击事件
 *     }
 * });
 * ```
 *
 * #### 场景 3：查找并操作任何类型的 View
 * `findViewById()` 方法返回的类型就是 `View`。虽然你通常将它转型为具体的子类（如 `TextView`），但有时需要通用处理。
 * ```java
 * import android.view.View;
 * // ...
 *
 * View someView = findViewById(R.id.any_view_id);
 * // 对所有 View 都有效的方法
 * someView.setVisibility(View.GONE);  // 隐藏视图
 * someView.setEnabled(false);         // 禁用视图
 * ```
 *
 * #### 场景 4：自定义 View
 * 当你创建自己的 UI 组件时，必须继承自 `View` 或其子类。
 * ```java
 * import android.view.View;
 * // ...
 *
 * public class MyCustomView extends View {
 *     public MyCustomView(Context context) {
 *         super(context);
 *         // 初始化代码
 *     }
 *     // 重写 onDraw, onMeasure 等关键方法
 * }
 * ```
 *
 * ### 5. `View` 类中的关键常量与静态方法（重要）
 *
 * 导入 `View` 后，你可以直接使用它定义的众多常量，这能让代码更简洁。
 *
 * -   **可见性常量**：
 *     -   `View.VISIBLE` (0)：可见
 *     -   `View.INVISIBLE` (4)：不可见但仍占位
 *     -   `View.GONE` (8)：完全消失，不占位
 *     ```java
 *     myView.setVisibility(View.GONE);
 *     ```
 *
 * -   **布局方向/对齐常量**：
 *     -   `View.FOCUSABLE`, `View.NO_ID` 等。
 *
 * ### 总结
 *
 * | 方面 | 说明 |
 * | :--- | :--- |
 * | **包路径** | `android.view.View` |
 * | **类类型** | 抽象基类（实际可实例化，但通常使用其子类） |
 * | **核心作用** | 所有 UI 组件的“根”。负责测量、布局、绘制和事件处理。 |
 * | **何时需要 import** | 当你的代码中**直接使用**到 `View` 这个类名时（例如：设置点击监听器、处理通用视图、自定义视图）。<br>如果只用 `TextView`、`Button` 等子类，**不需要**导入 `View`，但导入也无妨。 |
 * | **常用导入方式** | `import android.view.View;` |
 *
 * **一句话总结**：这条语句让你能直接操控 Android 屏幕上所有 UI 组件的“始祖”——`View` 类，从而执行设置可见性、添加点击事件、自定义绘制等通用操作。
 */
import android.view.View;
/**
 * `import android.widget.TextView;` 是 Android 开发中非常常见的一条导入语句。下面为你提供详细的注解，从基础概念到实际使用场景。
 *
 * ### 1. 核心概念：这是什么？
 *
 * -   **`import`**：Java 或 Kotlin 中的一个关键字，用于导入一个类或整个包，使得在当前文件中可以直接使用该类的简写名称，而不需要写完整的包名路径。
 * -   **`android.widget`**：这是 Android SDK 中的一个标准包（Package）。它包含了 Android 提供的各种**小部件（Widget）** 类。这些小部件是用户界面（UI）的基本构建块，比如按钮、文本框、列表等。
 * -   **`TextView`**：这是一个具体的类。从名字可以看出，“Text”代表文本，“View”代表视图，因此 `TextView` 就是**用于在屏幕上显示文本**的视图组件。
 *
 * **一句话总结：**
 * 这条语句让你在当前代码文件中，可以使用 `TextView` 这个类来创建或操作一个用于**显示文字**的界面元素。
 *
 * ### 2. 如果没有这行 `import` 会怎样？
 *
 * 如果你不使用 `import`，在代码中每次用到 `TextView` 时，都必须写出它的完整包名：
 *
 * ```java
 * // 没有 import，需要写完整路径
 * android.widget.TextView myTextView = new android.widget.TextView(context);
 * ```
 *
 * 这会让代码变得非常冗长和难以阅读。有了 `import`，代码就变得简洁清晰：
 *
 * ```java
 * // 有了 import，直接写类名即可
 * TextView myTextView = new TextView(context);
 * ```
 *
 * ### 3. `TextView` 是什么？—— 一个具体的例子
 *
 * `TextView` 是 Android 中最常用、最基本的 UI 组件之一。它就像一个“标签”，主要功能是：
 *
 * -   **显示静态文本**：例如一个页面的标题、一段说明文字。
 * -   **显示动态文本**：例如从网络获取的用户昵称、消息内容。
 * -   **作为其他复杂组件的基类**：比如 `Button`（按钮）、`EditText`（输入框）其实都是 `TextView` 的子类，它们在显示文本的基础上增加了交互功能。
 *
 * **形象比喻：**
 * > `TextView` 就像一张贴在墙上的**便利贴**，你可以在上面写任何文字，但它本身不支持你直接用手去修改上面的字（那是 `EditText` 的功能）。
 *
 * ### 4. 典型使用场景（在代码中）
 *
 * 假设你有一个 Activity（一个手机屏幕），你想在屏幕上显示一句 “Hello World!”。通常有两个步骤：
 *
 * **步骤1：在布局文件 (`activity_main.xml`) 中声明**
 *
 * ```xml
 * <!-- 这行代码在 XML 中创建了一个 TextView -->
 * <TextView
 *     android:id="@+id/my_text_view"   <!-- 给这个TextView起个名字，以便在代码中找到它 -->
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="Hello World!" />    <!-- 设置显示的文本 -->
 * ```
 *
 * **步骤2：在 Activity 的代码文件 (`MainActivity.java` 或 `.kt`) 中使用 `import`**
 *
 * ```java
 * // 导入 TextView 类
 * import android.widget.TextView;
 * import android.os.Bundle;
 * import androidx.appcompat.app.AppCompatActivity;
 *
 * public class MainActivity extends AppCompatActivity {
 *
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_main); // 加载上面的布局文件
 *
 *         // 1. 通过 findViewById 方法找到 XML 中定义的 TextView
 *         TextView myTextView = findViewById(R.id.my_text_view);
 *
 *         // 2. 动态修改 TextView 显示的文本
 *         myTextView.setText("欢迎使用我的App！");
 *
 *         // 3. 还可以修改文字颜色、大小等
 *         myTextView.setTextColor(android.graphics.Color.BLUE);
 *         myTextView.setTextSize(20);
 *     }
 * }
 * ```
 *
 * ### 5. 常见问题与延伸
 *
 * -   **`TextView` 和 `EditText` 的区别？**
 *     -   `TextView`：只读，用户无法修改其内容。
 *     -   `EditText`：可编辑，用户可以在里面输入文字。`EditText` 继承自 `TextView`。
 *
 * -   **`android.widget` 包下还有哪些常用类？**
 *     -   `Button`：按钮。
 *     -   `ImageView`：显示图片。
 *     -   `EditText`：文本输入框。
 *     -   `ListView` / `RecyclerView`：显示列表。
 *     -   `CheckBox`、`RadioButton`、`Switch` 等。
 *
 * -   **Android Studio 会自动帮你导入**
 *     当你输入 `TextView` 并回车后，Android Studio 会自动在文件顶部添加这行 `import` 语句（如果之前没有的话）。你通常不需要手动输入它。
 *
 * ### 总结
 *
 * | 组成部分 | 含义 |
 * | :--- | :--- |
 * | `import` | Java/Kotlin 关键字，用于引入外部类 |
 * | `android.widget` | Android 系统的小部件包，包含所有基础 UI 组件 |
 * | `TextView` | 一个用于**显示文本**的 UI 组件类 |
 *
 * **最终效果：** 有了这行代码，你就可以在你的 Android 程序中创建和控制一个可以显示文字的区域了。这是几乎所有 Android 应用的起点。
 */
import android.widget.TextView;
/**
 *
 Android Dialog导入说明
 import android.widget.Toast;
 详细注解
 好的，我们来详细注解 import android.widget.Toast; 这一行代码。

 一、整体解释
 java
 import android.widget.Toast;
 这行代码是 Java 的导入语句，它的作用是：
 告诉当前文件，要使用 android.widget 包下的 Toast 类。

 如果不写这行，在代码中直接写 Toast.makeText(...) 就会报编译错误，因为编译器不知道 Toast 是什么。

 二、逐词拆解
 1. import
 关键字

 含义：导入 / 引入

 作用：让当前文件可以使用其他包（package）中的类或接口

 类似于 C/C++ 中的 #include，但更严格（只能导入类/接口，不能导入任意代码）

 2. android.widget
 包名（package）

 含义：Android 框架提供的一个标准组件包

 作用：存放与“界面控件（Widget）”相关的类

 常见的 android.widget 中的类有：

 Button

 TextView

 EditText

 ImageView

 ProgressBar

 Toast

 📦 包名结构：
 android → 顶级 Android 包
 widget → 界面控件子包

 3. .（点）
 含义：层级分隔符

 表示“包中的包”或“包中的类”

 4. Toast
 类名

 含义：一个用于显示简短提示信息的 UI 组件

 特点：

 不会获取焦点

 自动显示、自动消失

 不影响用户当前操作

 常用于操作反馈（如“保存成功”“网络错误”）

 三、完整代码含义
 将 android.widget 包中的 Toast 类导入到当前文件中。

 导入后，可以直接写：

 java
 Toast.makeText(context, "消息内容", Toast.LENGTH_SHORT).show();
 如果没有导入，就必须写完整类名（完全限定名）：

 java
 android.widget.Toast.makeText(context, "消息内容", Toast.LENGTH_SHORT).show();
 显然，使用 import 可以让代码更简洁。

 四、常见写法对比
 写法	说明
 import android.widget.Toast;	只导入 Toast 类（推荐）
 import android.widget.*;	导入 widget 包下所有类（不推荐，但语法正确）
 不写 import	必须用 android.widget.Toast 全名
 ✅ 实际开发中，Android Studio 会自动帮你添加 import。

 五、一个完整的简单示例
 java
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;   // 👈 我们注解的这一行

 public class MainActivity extends AppCompatActivity {
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);

Button btn = findViewById(R.id.button);
btn.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
// 使用 Toast
Toast.makeText(
MainActivity.this,
"按钮被点击了",
Toast.LENGTH_SHORT
).show();
}
});
}
}
 六、Toast 的简单补充（帮助理解为什么要导入它）
 方法	作用
 makeText(Context, CharSequence, int)	创建 Toast 对象
 setDuration(int)	设置显示时长
 show()	显示 Toast
 cancel()	取消当前显示的 Toast
 常量：

 Toast.LENGTH_SHORT → 约 2 秒

 Toast.LENGTH_LONG → 约 3.5 秒

 七、总结一句话
 import android.widget.Toast; 是为了在代码中方便地使用 Android 系统的短时提示工具，无需每次写冗长的完整包名。

 如果你愿意，我可以继续帮你详细注解 Toast.makeText(...).show(); 这一整行语句。


 */
import android.widget.Toast;
/**
 *
 Android Dialog导入说明
 import androidx.annotation.Nullable;
 详细注解
 这是一个 Android Support Library / AndroidX 中的注解，用于标识某个参数、字段或方法的返回值可以为 null。

 作用
 告诉编译器、IDE 和代码检查工具：这个值不保证一定非空，调用方需要做好 null 检查。

 常见使用场景
 java
 public class UserManager {
 // 返回值可能为 null（比如用户不存在）
 public @Nullable User findUser(String userId) {
 if (userId == null || userId.isEmpty()) {
 return null;
 }
 return new User(userId);
 }
 }

 // 使用方需要判断
 User user = userManager.findUser("123");
 if (user != null) {
 String name = user.getName();
 }
 与 @NonNull 对比
 注解	含义	使用时
 @Nullable	可以为 null	必须判空
 @NonNull	不为 null	可直接使用
 为什么重要
 避免 NullPointerException：IDE 会智能提示需要判空

 提高代码可读性：明确表明方法可能返回 null

 配合静态检查工具（如 Android Lint、SpotBugs）在编译期发现问题

 注意事项
 在 Kotlin 中会被识别为可空类型（User?）

 需要确保 import 路径正确：import androidx.annotation.Nullable（而非 android.support.annotation.Nullable）

 需要我详细说明如何配合 Android Studio 的 Nullness 检查使用吗？



 */
import androidx.annotation.Nullable;
/**
 *
 Android Dialog导入说明
 import androidx.appcompat.app.AppCompatActivity;
 详细注解
 这是一个非常核心的 Android 导入语句。下面为你提供一份超详细的注解，涵盖其作用、所属库、历史背景以及现代 Android 开发中的现状。

 1. 核心作用
 这行代码的作用是：将 AppCompatActivity 这个类导入到你的 Java/Kotlin 文件中。

 为什么要导入？ AppCompatActivity 并不在 java.lang 包下（该包是默认导入的），所以必须显式导入，编译器才能识别它。之后你才能在代码中直接使用 AppCompatActivity 这个简写类名，而不必每次写完整的包名 androidx.appcompat.app.AppCompatActivity。

 2. 所属库与包结构解析
 java
 androidx.appcompat.app.AppCompatActivity
 │        │       │   │
 │        │       │   └── 类名：AppCompatActivity
 │        │       └────── 子包名：app（存放 Activity, Fragment 相关类）
 │        └────────────── 库名/主包名：appcompat（兼容库）
 └────────────────────── 根包名：androidx（现代 Android 扩展库）
 关键点：androidx vs 旧版 android.support
 androidx：这是 Android Jetpack 库的命名空间，从 Android 9.0 （API 28） 开始推出，是 Google 对旧支持库的彻底重构和现代化。

 旧版写法（已废弃）：在 androidx 出现之前，同样的类来自 android.support.v7.app.AppCompatActivity。现在所有新项目必须使用 androidx。

 3. 为什么要用 AppCompatActivity？
 简单说：为了向下兼容。

 Android 系统版本碎片化严重（从 API 21 到 API 34+）。高版本的 Android （如 12， 13） 有漂亮的 Material Design 特性（如 ActionBar、Toolbar、Material You 主题），但低版本（如 API 15 或更早）的系统本身并不支持这些新特性。

 AppCompatActivity 的作用就是作为一座“桥梁”：

 如果你继承的是...	在 Android 5.0 （API 21） 以下设备	在 Android 5.0+ 设备
 Activity （原生）	界面老旧，没有 Toolbar 等新控件，且容易出现主题错误。	可以使用部分新特性，但兼容性需要手动处理。
 AppCompatActivity	自动模拟出高版本的效果（通过兼容库实现）。例如自带 ActionBar 或支持 Toolbar、支持 Vector Drawable、支持 AppCompat 主题。	自动使用设备原生或兼容库提供的最新 Material 样式。
 核心优势：

 提供 ActionBar：即使你在 Android 2.3 上运行，也能拥有一个现代化的标题栏。

 支持 Toolbar：可以完全自定义标题栏。

 支持 Vector Drawable：允许在 API 21 以下使用矢量图。

 统一主题：Theme.AppCompat.* 系列主题在各种版本上看起来一致。

 支持 Material Design：如 AlertDialog、Button 的涟漪效果等。

 4. 典型用法示例
 java
 // 1. 导入语句
 import androidx.appcompat.app.AppCompatActivity;
 import android.os.Bundle;

 // 2. 让你的 Activity 继承 AppCompatActivity
 public class MainActivity extends AppCompatActivity {

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);

// 3. 使用 AppCompat 提供的主题布局
setContentView(R.layout.activity_main);

// 4. 可以安全地使用 getSupportActionBar() 而不是原生的 getActionBar()
if (getSupportActionBar() != null) {
getSupportActionBar().setTitle("我的兼容性标题");
}
}
}
 5. 现代 Android 开发中的现状
 绝大多数 Activity 都应该继承 AppCompatActivity，除非你有非常特殊的需求（比如继承 FragmentActivity 用于 ViewPager，但 AppCompatActivity 本身也是 FragmentActivity 的子类，所以功能更强）。

 Google 官方模板：当你在 Android Studio 中创建一个新的 “Empty Activity” 时，生成的代码默认就是继承 AppCompatActivity。

 未来替代者：随着 Compose UI 的兴起，如果你使用 ComponentActivity（来自 androidx.activity.ComponentActivity），它提供了更基础的 Jetpack Compose 支持。但 AppCompatActivity 对于基于 XML 视图的传统开发仍然是标准首选。

 6. 常见疑问：如果不导入会怎样？
 java
 // 错误写法
 public class MainActivity extends AppCompatActivity {  // 编译错误！
 编译器会报错：error： cannot find symbol class AppCompatActivity。你必须手动添加导入（在 Android Studio 中按 Alt + Enter 自动修复）。

 总结
 项目	说明
 代码行	import androidx.appcompat.app.AppCompatActivity;
 本质	导入一个支持向下兼容的 Activity 基类。
 为什么需要它	为了在不同 Android 版本上获得一致的新特性（ActionBar、Material 主题等）。
 必须使用	所有基于传统 View 系统的 Android App 都应该使用它作为 Activity 的基类。
 依赖配置	需要在 build.gradle （Module： app） 中添加：
 implementation ‘androidx.appcompat：appcompat：1.6.1’ （版本号可更新）

 */
import androidx.appcompat.app.AppCompatActivity;

/**
 * /**
 *  * AboutMeActivity 类继承自 AppCompatActivity
 *  * 这是一个用于展示“关于我”信息的界面（Activity）
 *  * AppCompatActivity 是 Android Support Library 中的基类，
 *  * 用于提供向后兼容的 Action Bar 和 Material Design 特性
 *  */

public class AboutMeActivity extends AppCompatActivity {
    /**
     *
     Android Dialog导入说明
     @Override
     详细注解
     下面是对 @Override 注解的详细解释，适合面试、学习或代码审查时深入理解。

     一、基本概念
     @Override 是一个标记注解（marker annotation，没有成员变量），用于告诉编译器：下面的方法意图重写（override）父类中的方法，或实现（implement）接口中的抽象方法。

     java
     @Override
     public String toString() {
     return "自定义字符串";
     }
     二、主要作用
     作用	说明
     编译期检查	帮助开发者避免重写失败的错误（如方法签名写错、父类没有该方法等）
     提高可读性	明确告诉阅读代码的人：这个方法不是新增的，而是来自上层定义
     避免误写	例如想重写 equals 但写成了 public boolean equal(Object obj)，编译器会报错
     三、使用规则
     ✅ 可以使用的地方
     重写父类的非 final 方法

     实现接口中的抽象方法（从 Java 6 开始允许）

     重写父类中的 abstract 方法

     ❌ 不能使用的地方
     静态方法（static method 不能被重写）

     父类中的 final 方法

     父类中不存在的方法

     四、常见错误示例
     错误 1：拼写错误
     java
     @Override
     public void tostring() {  // 父类是 toString，少写一个大写 S
     }
     // 编译错误：Method does not override method from its superclass
     错误 2：参数类型错误
     java
     @Override
     public boolean equals(MyClass obj) {  // 应该是 Object obj
     }
     错误 3：返回值类型不兼容（协变返回除外）
     java
     // 父类方法：public Object clone()
     @Override
     public String clone() {  // JDK 5+ 允许，这叫协变返回类型
     }
     五、与接口的关系（Java 版本差异）
     版本	实现接口方法时能否加 @Override
     Java 5	❌ 不允许
     Java 6+	✅ 允许（强烈推荐加）
     java
     public class MyList implements List<String> {
     @Override  // Java 6+ 合法
     public int size() {
     return 0;
     }
     }
     六、底层原理
     @Override 只在编译期起作用，编译后的 .class 文件中不会保留这个注解的信息（RetentionPolicy.SOURCE）

     编译器在语法分析阶段会检查被 @Override 标记的方法：

     在父类或接口中查找相同名称、参数列表的方法

     如果找不到 → 编译报错

     七、最佳实践
     ✅ 强烈建议：任何时候想要重写父类或实现接口的方法，都加上 @Override

     java
     @Override
     protected void finalize() throws Throwable {  // 避免误写为 finalise
     super.finalize();
     }
     ✅ 结合 IDE 功能：用 IDE（如 IntelliJ IDEA、Eclipse）生成 equals/hashCode/toString 时，会自动加上 @Override

     八、面试常见追问
     Q：不加 @Override 行不行？
     A：语法上可以，但容易导致隐藏的 bug，例如想重写但实际写成了重载（overload）。

     Q：@Override 和重载（overload）的关系？
     A：如果方法被 @Override 标记，一定不是重载（必须是父类/接口中已有的方法签名）。

     Q：可以同时 @Override 和 @SuppressWarnings 吗？
     A：可以，注解可以共存。

     java
     @Override
     @SuppressWarnings("unchecked")
     public Object clone() {
     return super.clone();
     }
     如果需要我给出一个完整的代码示例（包含父子类、接口、常见陷阱演示），我也可以为你写出来。


      * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
/**
 * 活动（Activity）创建时的系统回调方法。
 * 这是Activity生命周期的第一个方法，当Activity被系统首次创建时会调用此方法。
 * 该方法执行完毕后，会进入 onStart() -> onResume() 流程。
 *
 * 注意：该方法通常与 onDestroy() 成对出现。
 *
 * @param savedInstanceState 保存的实例状态Bundle对象。
 *                           用于Activity被系统销毁后重建时，恢复之前的临时数据。
 *                           如果Activity是正常首次启动，该值为 null。
 *                           如果是因配置变更（如旋转屏幕）或被系统回收后重启，则包含之前 onSaveInstanceState() 保存的数据。
 */
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        /**
         * 这段代码是Android开发中`Activity`生命周期方法`onCreate`里的标准写法。下面给你一个**超详细注解**：
         *
         * ## 代码作用
         * ```java
         * super.onCreate(savedInstanceState);
         * ```
         * **调用父类（`Activity`）的`onCreate`方法**，完成系统级的初始化工作。
         *
         * ## 详细注解
         *
         * ### 1. **为什么要调用`super.onCreate`？**
         * - `Activity`类内部已经实现了一套完整的生命周期管理逻辑
         * - 如果不调用，系统会抛出`SuperNotCalledException`异常
         * - 必须**在`onCreate`方法的最开头**调用（第一行）
         *
         * ### 2. **`savedInstanceState`参数详解**
         * - **作用**：保存Activity被销毁前的状态数据
         * - **类型**：`Bundle`对象（键值对容器）
         * - **何时有值**：
         *   - ✅ Activity被系统回收后重建（如屏幕旋转）
         *   - ✅ 内存不足时系统杀死Activity后恢复
         *   - ❌ 正常首次启动时（为`null`）
         *
         * ### 3. **父类`onCreate`内部做了哪些事？**
         * ```java
         * // 伪代码示意
         * protected void onCreate(Bundle savedInstanceState) {
         *     // 1. 初始化窗口管理器
         *     // 2. 绑定ContextThemeWrapper
         *     // 3. 初始化ActionBar/ToolBar
         *     // 4. 恢复savedInstanceState中的状态
         *     // 5. 准备Fragment管理器
         *     // 6. 初始化组件回调等
         * }
         * ```
         *
         * ### 4. **常见使用场景**
         *
         * #### 场景1：正常启动
         * ```java
         * @Override
         * protected void onCreate(Bundle savedInstanceState) {
         *     super.onCreate(savedInstanceState); // savedInstanceState == null
         *     setContentView(R.layout.activity_main);
         * }
         * ```
         *
         * #### 场景2：恢复状态
         * ```java
         * @Override
         * protected void onCreate(Bundle savedInstanceState) {
         *     super.onCreate(savedInstanceState);
         *     setContentView(R.layout.activity_main);
         *
         *     if (savedInstanceState != null) {
         *         // 恢复数据
         *         String savedText = savedInstanceState.getString("key_text");
         *         int savedScore = savedInstanceState.getInt("key_score");
         *     }
         * }
         *
         * // 保存状态
         * @Override
         * protected void onSaveInstanceState(Bundle outState) {
         *     super.onSaveInstanceState(outState);
         *     outState.putString("key_text", "需要保存的文本");
         *     outState.putInt("key_score", 100);
         * }
         * ```
         *
         * ### 5. **常见错误示范**
         *
         * ❌ **错误1：不调用**
         * ```java
         * @Override
         * protected void onCreate(Bundle savedInstanceState) {
         *     // 忘记调用 super.onCreate()
         *     setContentView(R.layout.activity_main);
         *     // 会抛出 SuperNotCalledException
         * }
         * ```
         *
         * ❌ **错误2：位置不对**
         * ```java
         * @Override
         * protected void onCreate(Bundle savedInstanceState) {
         *     setContentView(R.layout.activity_main); // 先写自己的逻辑
         *     super.onCreate(savedInstanceState); // 晚调用可能导致异常
         * }
         * ```
         *
         * ✅ **正确写法**
         * ```java
         * @Override
         * protected void onCreate(Bundle savedInstanceState) {
         *     super.onCreate(savedInstanceState); // 必须第一行
         *     setContentView(R.layout.activity_main);
         *     // 其他初始化代码...
         * }
         * ```
         *
         * ### 6. **在Fragment中的对应**
         * Fragment中也有类似的调用：
         * ```java
         * @Override
         * public void onCreate(Bundle savedInstanceState) {
         *     super.onCreate(savedInstanceState); // 调用父类Fragment的onCreate
         *     // Fragment特有初始化
         * }
         * ```
         *
         * ## 总结记忆口诀
         * > **"第一行，调父类，不调用，会崩溃"**
         *
         * - **必须调用**
         * - **必须第一行**
         * - **参数传下去**
         *
         * 这样理解后，你就能明白为什么每个Activity的`onCreate`都要写这一行了。这是Android框架强制要求的生命周期规范。
         */
        super.onCreate(savedInstanceState);
        /**
         *
         Android Dialog导入说明
         setContentView(R.layout.activity_about_me);
         详细注解
         这行代码是 Android 开发中非常核心的一个操作。以下是针对 setContentView(R.layout.activity_about_me); 的详细注解：

         1. 基本含义
         这行代码的作用是为当前的 Activity 设置一个用户界面。

         setContentView：是一个方法，字面意思是“设置内容视图”。

         R.layout.activity_about_me：是一个整型常量（资源 ID），指向 res/layout/ 目录下的一个名为 activity_about_me.xml 的布局文件。

         2. 逐词深度解析
         setContentView
         所属类：android.app.Activity 类（或其子类，如 AppCompatActivity）的方法。

         调用时机：通常在 Activity 的 onCreate(Bundle savedInstanceState) 生命周期方法中调用。

         核心作用：告诉 Android 系统：“请解析 activity_about_me.xml 文件，根据文件中的 XML 标签（如 <TextView>, <Button>, <LinearLayout> 等）创建相应的 View 对象，并将这些 View 对象组成的视图树设置为这个屏幕（Activity）显示的内容。”

         R.layout.activity_about_me
         这是一个资源 ID，其组成可以分解为三部分：

         组成部分	含义	示例值
         R	由 Android 编译工具（aapt2）自动生成的 Java 类（资源索引类）。	public final class R {}
         layout	R 类中的一个静态内部类，代表布局类型的资源。	public static final class layout {}
         activity_about_me	具体的资源名称，对应文件名 activity_about_me.xml（自动转换为合法变量名）。	public static int activity_about_me = 0x7F01001C;
         注意：R.layout.activity_about_me 实际上是一个 int 类型的数值（内存地址指针），而不是一个文件对象或 View 对象。setContentView 方法接收这个 int 值，在底层通过 LayoutInflater 将其“膨胀”成真正的 View 对象。

         3. 执行流程（底层原理）
         当执行这行代码时，系统内部大致会发生以下步骤：

         获取资源：系统通过 R.layout.activity_about_me 这个 ID，在资源表中找到对应的 XML 文件路径。

         解析 XML：使用 XML 解析器读取 activity_about_me.xml 文件。

         膨胀布局：调用 LayoutInflater 根据 XML 中的标签名（如 Button），使用反射创建对应的 View 类的实例。

         应用属性：将 XML 中定义的属性（如 android:layout_width， android:text）设置到创建的 View 对象上。

         建立视图树：根据 XML 的层级结构（父子关系），将 View 对象组织成一棵树。

         附加到窗口：将根 View 设置为 Activity 窗口（Window）的内容视图。

         4. 代码示例（完整上下文）
         java
         public class AboutMeActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 必须先调用，完成 Activity 初始化

        // 为这个 Activity 设置布局文件
        setContentView(R.layout.activity_about_me);

        // ---------- setContentView 调用之后 ----------
        // 此时，布局中的 View 对象已经创建完毕，可以查找并操作它们了
        TextView nameTextView = findViewById(R.id.name_text_view);
        Button backButton = findViewById(R.id.back_button);

        // 设置点击事件等逻辑...
        }
        }
         5. 重要变体（重载方法）
         setContentView 除了接收布局资源 ID，还有其他重载形式：

         方法签名	说明	使用场景
         setContentView(int layoutResID)	最常用，直接传入布局文件 ID。	绝大多数情况，静态定义好的布局。
         setContentView(View view)	直接传入一个已经创建好的 View 对象。	动态创建整个界面，或只显示一个简单的图形（如仅一个 TextView）。
         setContentView(View view, ViewGroup.LayoutParams params)	传入 View 并指定其布局参数。	需要精确控制根视图的宽高、边距等。
         6. 常见误区与注意事项
         误区/注意点	正确理解
         每个 Activity 只能调用一次？	可以多次调用，但第二次及之后的调用会替换整个视图树。注意：如果在调用 setContentView 之前调用了 findViewById，会返回 null。
         R.layout.activity_about_me 是一个文件？	不是文件，是 int 类型的 ID。真正的文件在编译时会被处理并打包。
         必须在 onCreate 中调用吗？	通常是的。但也可以在 onStart 或 onResume 中调用（不推荐，会导致界面延迟显示）。最晚需要在 onResume 之前调用，否则看不到界面。
         不调用会怎样？	Activity 会显示一个空白黑屏（只有标题栏，如果存在的话）。
         7. 补充知识：DataBinding 和 ViewBinding
         在现代 Android 开发中，setContentView(R.layout.activity_about_me); 经常被更安全的替代方案所取代：

         使用 ViewBinding：

         java
         // 自动生成的绑定类
         ActivityAboutMeBinding binding = ActivityAboutMeBinding.inflate(getLayoutInflater());
         setContentView(binding.getRoot()); // 传入的是 View，不是 ID
         使用 DataBinding：

         java
         ActivityAboutMeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_about_me);
         // 这一行内部已经调用了 setContentView
         优点：避免 findViewById 的类型转换错误和空指针异常。

         总结
         方面	总结
         一句话概括	将 XML 布局文件“画”到手机屏幕上。
         核心本质	通过资源 ID 找到 XML，解析并创建 View 对象树，挂载到 Activity 的窗口上。
         调用位置	onCreate 方法中，super.onCreate 之后，任何 findViewById 之前。
         现代替代	ViewBinding / DataBinding 提供的 inflate 或 setContentView 重载。


         */
        setContentView(R.layout.activity_about_me);
/**
 *
 Android Dialog导入说明
 TextView textWechatAccount = findViewById(R.id.text_wechat_account);
 详细注解
 下面是对这行 Android 代码的详细注解：

 java
 TextView textWechatAccount = findViewById(R.id.text_wechat_account);
 1. 整体作用
 这行代码的作用是从当前 Activity（或 Fragment）的布局文件中，查找 ID 为 text_wechat_account 的 TextView 控件，并将其引用赋值给一个名为 textWechatAccount 的 TextView 类型变量。之后可以通过这个变量来操作该控件（如设置文本、颜色、点击事件等）。

 2. 分解说明
 (1) TextView
 类型：Android 中的类，用于显示文本。

 作用：声明变量的类型为 TextView，表示 textWechatAccount 这个变量只能引用 TextView 或其子类的对象。

 (2) textWechatAccount
 变量名：自定义的引用名称，用于后续操作该控件。

 命名规范：通常采用小驼峰命名法（如 textWechatAccount），便于识别这是一个 TextView 控件，内容与微信账号相关。

 (3) =
 赋值操作符：将右边表达式的结果赋值给左边的变量。

 (4) findViewById(R.id.text_wechat_account)
 方法作用：在 Activity 或 Fragment 的当前布局树中，根据提供的资源 ID 查找对应的 View 对象。

 参数 R.id.text_wechat_account：

 R：Android 自动生成的资源类，包含所有资源的 ID。

 id：R 类中的内部类，存放所有布局控件的 ID。

 text_wechat_account：在 res/layout/xxx.xml 布局文件中为 TextView 设置的 android:id="@+id/text_wechat_account"。

 返回值：返回找到的 View 对象，由于 TextView 是 View 的子类，因此可以向上转型后赋值给 TextView 变量。

 3. 注意事项
 必须确保布局中确实存在 @+id/text_wechat_account 的 TextView，否则 findViewById 会返回 null，后续操作可能导致 NullPointerException。

 在 Activity 中直接调用 findViewById 即可；在 Fragment 中需要通过 getView().findViewById(...) 或 view.findViewById(...) 调用。

 Android 推荐替代方案：

 ViewBinding（推荐）：

 java
 ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
 TextView textWechatAccount = binding.textWechatAccount;
 Kotlin 合成属性（已废弃）或 DataBinding。

 使用 findViewById 在 API 26+ 已不需强制类型转换（Kotlin 类型推导可省略）。

 4. 后续常见操作示例
 java
 // 设置文本
 textWechatAccount.setText("微信号: example");

 // 设置点击事件
 textWechatAccount.setOnClickListener(v -> {
 // 点击后复制微信号等操作
 });

 // 修改文字颜色
 textWechatAccount.setTextColor(Color.BLUE);
 5. 对应的 XML 布局示例
 xml
 <TextView
 android:id="@+id/text_wechat_account"
 android:layout_width="wrap_content"
 android:layout_height="wrap_content"
 android:text="微信账号"
 android:textSize="16sp" />
 如果需要更深入（如 View 树的查找原理、R 文件的生成机制等），可以继续补充。


 */
        TextView textWechatAccount = findViewById(R.id.text_wechat_account);
        /**
         *
         Android Dialog导入说明
         setUpCopyClickListener(textWechatAccount, "WeChat");
         详细注解
         下面是对这行代码的详细注解：

         javascript
         setUpCopyClickListener(textWechatAccount, "WeChat");
         函数作用
         调用一个名为 setUpCopyClickListener 的函数，用于为一个显示微信账号的文本元素绑定“点击复制”功能。

         参数详解
         1. textWechatAccount
         类型： 通常是一个 DOM 元素（例如 <span>、<div>、<input>）或选择器字符串，也可能是包含微信账号文本的变量。

         含义： 表示页面上显示微信账号的那个元素。

         常见场景： 该元素里包含类似 "wxid_abc123" 或 "example123" 这样的微信号文本。

         作用： 当用户点击这个元素或其关联的按钮时，程序会从这个元素中提取出微信号内容，然后复制到剪贴板。

         2. "WeChat"
         类型： 字符串常量

         含义： 标识当前复制的数据类型或来源，这里是“微信账号”。

         可能用途：

         用于日志记录或调试（例如在控制台输出 “已复制 WeChat 账号：xxx”）。

         用于显示提示信息（如弹窗提示 “WeChat 账号已复制”）。

         用于区分不同复制功能（例如还有复制 QQ、邮箱等）。

         函数内部可能的实现逻辑
         javascript
         function setUpCopyClickListener(element, type) {
         element.addEventListener('click', function() {
         // 获取要复制的文本
         let textToCopy = element.innerText;  // 或者 element.value / 自定义属性等

         // 执行复制操作
         navigator.clipboard.writeText(textToCopy).then(() => {
         console.log(`已复制 ${type} 账号：${textToCopy}`);
         alert(`${type} 账号已复制到剪贴板`);
         }).catch(err => {
         console.error('复制失败：', err);
         });
         });
         }
         一句话总结
         为指定的页面元素绑定点击事件，点击后自动复制该元素中的微信账号到剪贴板，并标注复制内容的类型为“WeChat”。


         */
        setUpCopyClickListener(textWechatAccount, "WeChat");

        TextView textQqAccount = findViewById(R.id.text_qq_account);
        setUpCopyClickListener(textQqAccount, "QQ");
    }

    private void setUpCopyClickListener(TextView textView, String appName) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(textView.getText());
                Toast.makeText(AboutMeActivity.this, "copy！open" + appName + "to paste", Toast.LENGTH_LONG).show();
            }
        });
    }
}