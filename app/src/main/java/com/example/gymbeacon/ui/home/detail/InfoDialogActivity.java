package com.example.gymbeacon.ui.home.detail;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.gymbeacon.R;

public class InfoDialogActivity extends Dialog {
    private TextView dialog_text;       // 다이어로그 내용
    private Button dialog_ok_btn;       // 확인 버튼
    private TextView dialog_title;      // 다이어로그 제목
    private ImageView dialog_gif;       // 다이어로그에 들어가는 이미지

    public InfoDialogActivity(@NonNull Context context, String title, String contents) {
        super(context);
        setContentView(R.layout.activity_info_dialog);

        dialog_title = findViewById(R.id.dialog_title);         // 다이어로그 제목 설정
        dialog_title.setText(title);

        dialog_text = findViewById(R.id.dialog_text);           // 다이어로그 내용 설정
        dialog_text.setText(contents);                          

        dialog_ok_btn = findViewById(R.id.dialog_ok_btn);
        dialog_ok_btn.setOnClickListener(v -> dismiss());       // 확인 버튼을 누르면 사라지기

        dialog_gif = (ImageView)findViewById(R.id.dialog_gif);  //GIF ImageView와 연결

        Log.d("title 제목: ", title);

        if (title.equals("벤치프레스")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.bench_press_dialog).into(dialog_gif);
        }
        else if (title.equals("랫 풀 다운")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.lat_pull_down_dialog).into(dialog_gif);
        }
        else if (title.equals("인클라인 벤치프레스")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.incline_dialog).into(dialog_gif);
        }
        else if (title.equals("스쿼트")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.squat_dialog).into(dialog_gif);
        }
        else if (title.equals("데드리프트")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.deadlift_dialog).into(dialog_gif);
        }
        else if (title.equals("레그 익스텐션")) {
            Glide.with(dialog_gif.getContext()).load(R.raw.legex_dialog).into(dialog_gif);
        }
        // Glide.with(dialog_gif.getContext()).load(R.raw.squat_dialog).into(dialog_gif);  //R.raw에 저장된 GIF파일 불러오기
    }
}
