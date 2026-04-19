package me.zackyu.yubook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import me.zackyu.yubook.db.Record;

public class RecordAdapter extends ArrayAdapter<Record> {

    private final int resourceId;
    private final SimpleDateFormat dateTimeFormat;
    private List<Record> dataList;

    public RecordAdapter(@NonNull Context context, int resource, @NonNull List<Record> objs) {
        super(context, resource, objs);
        this.resourceId = resource;
        this.dataList = objs;
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Nullable
    @Override
    public Record getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Record record = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }

        TextView textAmount = convertView.findViewById(R.id.text_amount);
        TextView textTime = convertView.findViewById(R.id.text_time);
        TextView textSource = convertView.findViewById(R.id.text_source);
        TextView textType = convertView.findViewById(R.id.text_type);
        TextView textAccount = convertView.findViewById(R.id.text_account);

        if (record != null) {
            double amount = record.getAmount();

            // 设置金额，收入和支出用不同颜色
            String amountText = String.format(Locale.getDefault(), "¥ %.2f", Math.abs(amount));

            if (amount > 0) {
                // 收入 - 绿色，带加号
                textAmount.setText("+ " + amountText);
                textAmount.setTextColor(0xFF4CAF50);
            } else {
                // 支出 - 红色，带减号
                textAmount.setText("- " + amountText);
                textAmount.setTextColor(0xFFFF6B6B);
            }

            // 设置时间
            if (record.getCrttime() != null) {
                textTime.setText(dateTimeFormat.format(record.getCrttime()));
            } else {
                textTime.setText("时间未知");
            }

            // 设置来源
            String sourceText = record.getSource() != null && !record.getSource().isEmpty()
                    ? "来源: " + record.getSource()
                    : "来源: 未知";
            textSource.setText(sourceText);

            // 设置去向/类型
            String typeText = record.getType() != null && !record.getType().isEmpty()
                    ? "去向: " + record.getType()
                    : "去向: 未知";
            textType.setText(typeText);

            // 设置账户
            String accountText = record.getAccount() != null && !record.getAccount().isEmpty()
                    ? "账户: " + record.getAccount()
                    : "账户: 未知";
            textAccount.setText(accountText);
        }

        return convertView;
    }

    /**
     * 更新数据（替代clear + addAll）
     */
    public void updateData(List<Record> newData) {
        this.dataList = newData;
        notifyDataSetChanged();
    }
}