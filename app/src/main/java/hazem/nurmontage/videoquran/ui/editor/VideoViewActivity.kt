package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityVideoViewBinding

class VideoViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val videoPath = intent.getStringExtra("video_path")
        if (videoPath != null) {
            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.videoView)
            // videoView is ImageView in layout, not VideoView
            binding.videoView.setImageURI(Uri.parse(videoPath))
            // start() not available on ImageView
        }
        binding.btnOnBack?.setOnClickListener { finish() }
    }
}
