package hazem.nurmontage.videoquran.ui.ads

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.EdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.Utils
import hazem.nurmontage.videoquran.views.WaveformView

/**
 * Activity that showcases the "Tuffah" audio app by playing "before" and
 * "after" audio samples with waveform visualisation.  The user can switch
 * between the two samples, seek on the waveform, and either open the
 * Tuffah app (if installed) or be directed to the Play Store to download
 * it.
 */
class AdsTuffahActivity : BaseActivity() {

    // ──────────────────────────────────────────────
    //  Views
    // ──────────────────────────────────────────────

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPlayPauseAfter: ImageButton
    private lateinit var currentBtn: ImageButton
    private lateinit var currentWave: WaveformView
    private lateinit var waveformViewAfter: WaveformView
    private lateinit var waveformViewBefore: WaveformView

    // ──────────────────────────────────────────────
    //  Media state
    // ──────────────────────────────────────────────

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var currentResId: Int = R.raw.before

    private val handler = Handler(Looper.getMainLooper())

    /** Periodically updates the waveform progress while audio is playing. */
    private val updateProgressTask = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: return
            if (!mp.isPlaying) return
            currentWave.setProgress(mp.currentPosition.toFloat() / mp.duration.toFloat())
            handler.postDelayed(this, 50L)
        }
    }

    /** Handles the back-press by simply finishing the activity. */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // ──────────────────────────────────────────────
    //  Lifecycle
    // ──────────────────────────────────────────────

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_ads_tuufah)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Dark system bars
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setString()

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener { finish() }

        // Wire up views
        btnPlayPause = findViewById(R.id.btnPlayPause)
        waveformViewBefore = findViewById(R.id.waveformView)
        waveformViewAfter = findViewById(R.id.waveformView_after)
        btnPlayPauseAfter = findViewById(R.id.btnPlayPause_after)
        currentWave = waveformViewBefore
        currentBtn = btnPlayPause

        setupMediaPlayer(currentResId)

        // Before play/pause
        btnPlayPause.setOnClickListener {
            switchAudio(R.raw.before, btnPlayPause, waveformViewBefore)
        }

        // After play/pause
        btnPlayPauseAfter.setOnClickListener {
            switchAudio(R.raw.after, btnPlayPauseAfter, waveformViewAfter)
        }

        // After waveform seek
        waveformViewAfter.setOnWaveformClickListener { progress ->
            mediaPlayer?.let { mp ->
                mp.seekTo((mp.duration * progress).toInt())
                if (!mp.isPlaying) {
                    waveformViewAfter.setProgress(progress)
                }
            }
        }

        // Before waveform seek
        waveformViewBefore.setOnWaveformClickListener { progress ->
            mediaPlayer?.let { mp ->
                mp.seekTo((mp.duration * progress).toInt())
                if (!mp.isPlaying) {
                    waveformViewBefore.setProgress(progress)
                }
            }
        }

        // Tuffah CTA button
        findViewById<View>(R.id.btn_tuffah).setOnClickListener {
            if (Utils.isAppInstalled(this, TUFFAH_PACKAGE)) {
                val launchIntent = packageManager.getLaunchIntentForPackage(TUFFAH_PACKAGE)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                }
            } else {
                installTuffah()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            togglePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ──────────────────────────────────────────────
    //  UI helpers
    // ──────────────────────────────────────────────

    private fun setString() {
        val tvBefore: TextView = findViewById(R.id.tv_before)
        val tvAfter: TextView = findViewById(R.id.tv_after)
        val tvDownload: TextView = findViewById(R.id.tv_download)

        tvBefore.text = resources.getString(R.string.before)
        tvAfter.text = resources.getString(R.string.after)

        if (!Utils.isAppInstalled(this, TUFFAH_PACKAGE)) {
            tvDownload.text = resources.getString(R.string.download)
        } else {
            tvDownload.text = resources.getString(R.string.openTuffah)
        }

        if (LocaleHelper.getLanguage(this) == "ar") {
            findViewById<View>(R.id.iv_en).visibility = View.GONE
            findViewById<View>(R.id.iv_ar).visibility = View.VISIBLE
        }
    }

    // ──────────────────────────────────────────────
    //  Audio playback
    // ──────────────────────────────────────────────

    private fun setupMediaPlayer(resId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            setOnCompletionListener {
                isPlaying = false
                btnPlayPauseAfter.setImageResource(R.drawable.play_btn)
                btnPlayPause.setImageResource(R.drawable.play_btn)
                currentWave.setProgress(0.0f)
                handler.removeCallbacks(updateProgressTask)
            }
        }
    }

    private fun switchAudio(resId: Int, btn: ImageButton, wave: WaveformView) {
        btnPlayPauseAfter.setImageResource(R.drawable.play_btn)
        btnPlayPause.setImageResource(R.drawable.play_btn)
        currentWave = wave
        currentBtn = btn

        if (currentResId == resId) {
            togglePlayback()
            return
        }

        currentResId = resId
        handler.removeCallbacks(updateProgressTask)
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
        }
        setupMediaPlayer(resId)
        isPlaying = false
        currentWave.setProgress(0.0f)
        currentBtn.setImageResource(R.drawable.play_btn)
        togglePlayback()
    }

    private fun togglePlayback() {
        val mp = mediaPlayer ?: return
        if (isPlaying) {
            mp.pause()
            currentBtn.setImageResource(R.drawable.play_btn)
            handler.removeCallbacks(updateProgressTask)
        } else {
            mp.start()
            currentBtn.setImageResource(R.drawable.pause_24px)
            handler.post(updateProgressTask)
        }
        isPlaying = !isPlaying
    }

    // ──────────────────────────────────────────────
    //  Tuffah installation
    // ──────────────────────────────────────────────

    /**
     * Opens the Play Store listing for the Tuffah app.  Falls back to
     * a browser URL when the Play Store app is not available.
     */
    private fun installTuffah() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$TUFFAH_PACKAGE")
        ).apply {
            setPackage("com.android.vending")
            addFlags(0x58000000.toInt()) // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$TUFFAH_PACKAGE")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Unable to open app store or browser.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TUFFAH_PACKAGE = "hazem.tuffah.quranaudio"
    }
}
