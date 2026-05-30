package hazem.nurmontage.videoquran.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.databinding.ActivityThanksYouBinding
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

/**
 * Thank you / credits screen.
 *
 * Displays a celebratory message with confetti animation.
 * This is an informational screen — no result is returned.
 */
class ThanksYouActivity : BaseActivity() {

    private lateinit var binding: ActivityThanksYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanksYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()

        // Back button
        binding.btnOnBack.setOnClickListener {
            finish()
        }

        // Set donation text
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            binding.tvPriceDonate.text = getString(R.string.donate_hint, pi.versionName)
        } catch (_: Exception) {
            binding.tvPriceDonate.text = getString(R.string.donate_hint, "")
        }

        binding.tvThnksDonate.text = getString(R.string.thanks_hint)

        // Launch confetti animation after layout is complete
        binding.konfettiView.post {
            startConfetti()
        }
    }

    /**
     * Start the confetti celebration animation using konfetti 2.x API.
     */
    private fun startConfetti() {
        try {
            val party1 = PartyFactory(
                Emitter(2000L, TimeUnit.MILLISECONDS).max(100)
            )
                .angle(Angle.Companion.TOP)
                .spread(Spread.Companion.SMALL)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .shapes(listOf(Shape.Square, Shape.Circle))
                .sizes(listOf(Size.SMALL, Size.MEDIUM, Size.LARGE))
                .rotation(Rotation())
                .colors(listOf(
                    android.graphics.Color.YELLOW,
                    android.graphics.Color.GREEN,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.RED,
                    android.graphics.Color.CYAN
                ))
                .position(Position.Relative(0.5, 0.5))
                .build()

            binding.konfettiView.start(listOf(party1))

            // Second burst after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val party2 = PartyFactory(
                        Emitter(2500L, TimeUnit.MILLISECONDS).max(80)
                    )
                        .angle(Angle.Companion.TOP)
                        .spread(Spread.Companion.SMALL)
                        .setSpeedBetween(2f, 6f)
                        .timeToLive(2500L)
                        .shapes(listOf(Shape.Circle, Shape.Square))
                        .sizes(listOf(Size.SMALL, Size.MEDIUM, Size.LARGE))
                        .rotation(Rotation())
                        .colors(listOf(
                            android.graphics.Color.BLUE,
                            android.graphics.Color.GREEN,
                            android.graphics.Color.RED,
                            android.graphics.Color.YELLOW
                        ))
                        .position(Position.Relative(0.3, 0.5))
                        .build()

                    binding.konfettiView.start(listOf(party2))
                } catch (_: Exception) {
                    // Konfetti view may be detached
                }
            }, 500)
        } catch (_: Exception) {
            // Konfetti library may not be properly initialized
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // KonfettiView handles its own cleanup
    }
}
