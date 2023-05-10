package com.example.gymbeacon.ui.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import com.example.gymbeacon.R

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        videoView = findViewById(R.id.video_view)

        // MediaController 생성
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // 동영상 파일 경로 가져오기
        val videoPath = intent.getStringExtra("video_path")

        // VideoView에 동영상 파일 지정
        videoView.setVideoPath(videoPath)

        // VideoView 준비 완료 후 재생
        videoView.setOnPreparedListener {
            videoView.start()
        }

        // VideoView 재생 종료 시 액티비티 종료
        videoView.setOnCompletionListener {
            finish()
        }

        // VideoView 에러 발생 시 액티비티 종료
        videoView.setOnErrorListener { _, _, _ ->
            finish()
            true
        }
    }
}
