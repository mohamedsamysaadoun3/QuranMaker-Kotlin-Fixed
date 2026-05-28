package hazem.nurmontage.videoquran.model

import java.io.File

/**
 * Represents a file-system folder entry displayed in the project/file explorer.
 *
 * Each [ExploreItem] corresponds to a directory on the device's storage that
 * may contain video, image, or audio files. It stores:
 *   - [folder]: The [File] object pointing to the directory
 *   - [path]: The absolute path string of the folder
 *   - [size]: A human-readable string representing the folder size (e.g., "12 MB")
 *   - [name]: The display name of the folder
 *   - [firstFilePath]: The path of the first media file inside the folder,
 *     used as a thumbnail preview in the explorer grid
 *
 * This model is used by the gallery/explore adapters to display browsable
 * folders to the user when selecting media from local storage.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from ExploreItem.java (40 lines).
 */
data class ExploreItem(
    val folder: File,
    val path: String,
    val size: String,
    val name: String,
    val firstFilePath: String
)
