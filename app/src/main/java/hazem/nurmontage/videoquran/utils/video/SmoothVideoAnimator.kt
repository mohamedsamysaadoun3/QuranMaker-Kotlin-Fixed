package hazem.nurmontage.videoquran.utils.video

import android.view.Choreographer
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.views.TrackEntityView
import java.io.File
import java.util.Locale

/**
 * Smooth frame-by-frame video playback using [Choreographer] for vsync-aligned updates.
 *
 * Reads pre-extracted JPEG frames from the template's `VideoFrame/` directory
 * and delivers frame paths to a [FrameUpdateListener] at a consistent FPS rate.
 *
 * The animation is **self-regulating**: each [doFrame] callback checks whether
 * enough nanoseconds have elapsed since the last frame before advancing.
 * If the frame index reaches [maxFrameIndex], it wraps around (loop playback).
 *
 * Usage:
 * ```
 * val animator = SmoothVideoAnimator(trackView, template, 30) { path ->
 *     imageView.setImageBitmap(BitmapFactory.decodeFile(path))
 * }
 * animator.start()
 * ```
 *
 * Converted from SmoothVideoAnimator.java — frame timing and looping logic
 * preserved exactly.
 */
class SmoothVideoAnimator(
    private val trackViewEntity: TrackEntityView,
    private val mTemplate: Template,
    private val fps: Int,
    private val listener: FrameUpdateListener
) : Choreographer.FrameCallback {

    /**
     * Listener for frame update events during video animation.
     */
    interface FrameUpdateListener {
        /** Called with the absolute file path of the current frame. */
        fun onFrameUpdate(framePath: String)
        /** Called when the animation completes or is stopped. */
        fun onAnimationEnd()
    }

    private val frameIntervalNanos: Long = (1_000_000_000.0f / fps).toLong()
    private var lastFrameTimeNanos: Long = 0L
    private var currentFrameIndex: Int = 0
    private var maxFrameIndex: Int = 0
    private var mIsPlaying: Boolean = false

    /**
     * Start the animation from the current cursor position.
     *
     * Calculates the starting frame index from the timeline cursor position
     * and the maximum frame index from the template's video duration × FPS.
     */
    fun start() {
        mIsPlaying = true
        currentFrameIndex = Math.max(
            1,
            Math.round((trackViewEntity.current_cursur_position / 1000.0f) * fps)
        )
        maxFrameIndex = mTemplate.duration_video_media * fps
        lastFrameTimeNanos = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    /**
     * Stop the animation and notify the listener.
     */
    fun stop() {
        mIsPlaying = false
        Choreographer.getInstance().removeFrameCallback(this)
        listener.onAnimationEnd()
    }

    /**
     * Vsync-aligned frame callback.
     *
     * Advances the frame index at the target FPS rate, wraps around at the end
     * (looping), and delivers the frame path to the listener.
     *
     * The frame file is expected at:
     * `<template_folder>/VideoFrame/frame_XXXX.jpg`
     *
     * where `XXXX` is a zero-padded 4-digit frame number.
     */
    override fun doFrame(frameTimeNanos: Long) {
        if (!mIsPlaying || maxFrameIndex == 0) return

        if (lastFrameTimeNanos == 0L) {
            lastFrameTimeNanos = frameTimeNanos
        }

        if (frameTimeNanos - lastFrameTimeNanos >= frameIntervalNanos) {
            lastFrameTimeNanos = frameTimeNanos

            val framePath = File(
                mTemplate.folder_template + "/VideoFrame",
                buildFrameFilePath(currentFrameIndex)
            ).absolutePath

            listener.onFrameUpdate(framePath)

            // Advance and wrap around (1-based indexing)
            val prev = currentFrameIndex
            currentFrameIndex = (prev % maxFrameIndex) + 1
        }

        Choreographer.getInstance().postFrameCallback(this)
    }

    /**
     * Build the frame file name with zero-padded index.
     * Example: frame index 42 → "frame_0042.jpg"
     */
    private fun buildFrameFilePath(index: Int): String {
        return String.format(Locale.US, "frame_%04d.jpg", index)
    }
}
