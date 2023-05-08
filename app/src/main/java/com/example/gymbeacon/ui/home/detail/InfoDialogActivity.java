package com.example.gymbeacon.ui.home.detail;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.gymbeacon.R;

public class InfoDialogActivity extends Dialog {
    private TextView dialog_text;
    private Button dialog_ok_btn;
    private TextView dialog_title;

    private ImageView dialog_gif;

    public InfoDialogActivity(@NonNull Context context, String title, String contents) {
        super(context);
        setContentView(R.layout.activity_info_dialog);

        dialog_title = findViewById(R.id.dialog_title);
        dialog_title.setText(title);

        dialog_text = findViewById(R.id.dialog_text);
        dialog_text.setText(contents);

        dialog_ok_btn = findViewById(R.id.dialog_ok_btn);
        dialog_ok_btn.setOnClickListener(v -> dismiss());

        dialog_gif = (ImageView)findViewById(R.id.dialog_gif); //GIF ImageView연결
        Glide.with(dialog_gif.getContext()).load(R.raw.squat_dialog).into(dialog_gif); //R.raw.loading GIF파일 load

    }
}
