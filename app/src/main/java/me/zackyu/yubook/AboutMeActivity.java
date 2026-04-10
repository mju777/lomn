package me.zackyu.yubook;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutMeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        TextView textWechatAccount = findViewById(R.id.text_wechat_account);
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