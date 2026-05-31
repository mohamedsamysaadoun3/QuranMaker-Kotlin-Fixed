package hazem.nurmontage.videoquran.ui.engine

import android.net.Uri

// ==========================================================================
// EditorDialogs.kt
// Extension functions for EngineActivity dialogs.
// Extracted from EngineUIHelper.kt for maintainability.
// Premium dialogs (dialogPremium, dialogPremiumIpad, dialogWatermark) have
// been removed — all users are pro.
// ==========================================================================

/**
 * Show the copyright notice dialog (reciter usage rights warning).
 */
fun EngineActivity.dialogCopyRight() {
    // TODO: Move from EngineUIHelper
}

/**
 * Show "no internet" dialog with retry for a single audio URI.
 */
fun EngineActivity.dialogNoInternet(uri: Uri) {
    // TODO: Move from EngineUIHelper
}

/**
 * Show "no internet" dialog with retry for a list of reciter URLs.
 */
fun EngineActivity.dialogNoInternetList(list: List<String>) {
    // TODO: Move from EngineUIHelper
}

/**
 * Show confirmation dialog before deleting all selected entities.
 */
fun EngineActivity.dialogDeleteSelected() {
    // TODO: Move from EngineUIHelper
}

/**
 * Show exit confirmation dialog (leave vs. continue editing).
 */
fun EngineActivity.showExitDialog() {
    // TODO: Move from EngineUIHelper
}

/**
 * Dismiss the general-purpose dialog and clear the reference.
 */
fun EngineActivity.cancelDialog() {
    // TODO: Move from EngineUIHelper
}

/**
 * Dismiss the internet-retry dialog and clear the reference.
 */
fun EngineActivity.cancelDialogInternet() {
    // TODO: Move from EngineUIHelper
}
