package hazem.nurmontage.videoquran.core.common

import android.graphics.RectF

/**
 * Immutable snapshot of an [Entity][hazem.nurmontage.videoquran.entity_timeline.Entity]'s
 * geometric and timing state, used by the undo/redo stack in the timeline editor.
 *
 * Each instance captures the entity's bounding rect, offsets, start/end times,
 * and left/right edge positions at the moment the snapshot was taken.
 *
 * Originally: `hazem.nurmontage.videoquran.common.StackEntity`
 */
class StackEntity(
    val rectF: RectF,
    val offset: Float,
    val end: Float,
    val start: Float,
    val left: Float,
    val right: Float,
    val max: Float,
    val offset_right: Float,
    val offset_left: Float
) {
    var index_start_thumbnail: Int = 0
        protected set
    var index_end_thumbnail: Int = 0
        protected set
}
