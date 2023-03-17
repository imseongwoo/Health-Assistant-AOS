package com.example.gymbeacon.tts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityTextToSpeechBinding
import java.util.*

class TextToSpeechActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var binding: ActivityTextToSpeechBinding
    lateinit var tts : TextToSpeech

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
            tts = TextToSpeech(this,this)
        } else {
            val installIntent: Intent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_text_to_speech)
        binding.lifecycleOwner = this


        binding.buttonStartTts.setOnClickListener {
            val text = binding.buttonStartTts.text.toString()
            val intent: Intent = Intent()
            intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            activityResult.launch(intent)
        }

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus: Int = tts.setLanguage(Locale.KOREAN)

            if(languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                    languageStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this,"언어를 지원할 수 없습니다.",Toast.LENGTH_SHORT).show()
            } else {
                val data: String = binding.buttonStartTts.text.toString()
                var speechStatus: Int = 0

                speechStatus = tts.speak(data,TextToSpeech.QUEUE_FLUSH,null,null)

                if(speechStatus == TextToSpeech.ERROR) {
                    Toast.makeText(this,"음성전환 에러입니다.",Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.let {
            it.stop()
            it.shutdown()
        }
    }




}