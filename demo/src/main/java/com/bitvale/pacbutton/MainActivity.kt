package com.bitvale.pacbutton

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pac_button.setSelectAction {
            if (it) icon.setImageResource(R.drawable.ic_video_cam)
            else icon.setImageResource(R.drawable.ic_photo_cam)
        }
        pac_button.setAnimationUpdateListener { progress ->
            icon.alpha = 1 - progress
        }
    }
}
