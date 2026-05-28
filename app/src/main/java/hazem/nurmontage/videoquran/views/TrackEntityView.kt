package hazem.nurmontage.videoquran.views

/**
 * Callback interface for audio trim line animations.
 *
 * Implemented by the timeline track view to receive fade-in/fade-out
 * delta values during [android.animation.ObjectAnimator] playback.
 *
 * The [EntityAudio] class drives these callbacks via property animation:
 * - `FadeInDelta` (0→1): gradually increases audio volume from silence
 * - `FadeOutDelta` (1→0): gradually decreases audio volume to silence
 */
interface TrackEntityView {

    interface ITrimLineCallback {
        fun fadeInAudio(delta: Float)
        fun fadeOutAudio(delta: Float)
    }
}
