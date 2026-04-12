package me.zackyu.yubook.util;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 *
 */
public class NeutralDialogFragment extends DialogFragment {

    private DialogInterface.OnClickListener neutralCallback;

    private String title;

    private String message;

    private String hint;

    public void show(String title, String message, String hint, DialogInterface.OnClickListener neutralCallback,
                     FragmentManager fragmentManager) {
        this.title = title;
        this.message = message;
        this.hint = hint;
        this.neutralCallback = neutralCallback;
        super.show(fragmentManager, "NeutralDialogFragment");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(hint, neutralCallback);
        return builder.create();
    }
}