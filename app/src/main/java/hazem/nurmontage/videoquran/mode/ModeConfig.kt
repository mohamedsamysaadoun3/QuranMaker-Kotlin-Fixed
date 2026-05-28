package hazem.nurmontage.videoquran.mode

/**
 * Configuration for each mode: which fragments, tools, and settings are available.
 */
class ModeConfig private constructor(
    val modeName: String,
    private val enabledFragments: Set<String>,
    private val enabledTools: Set<String>,
    private val enabledSettings: Set<String>,
    private val enabledFeatures: Set<String>
) {

    fun isFragmentEnabled(fragmentId: String): Boolean = enabledFragments.contains(fragmentId)

    fun isToolEnabled(toolId: String): Boolean = enabledTools.contains(toolId)

    fun isSettingEnabled(settingId: String): Boolean = enabledSettings.contains(settingId)

    fun isFeatureEnabled(featureId: String): Boolean = enabledFeatures.contains(featureId)

    fun getEnabledFragments(): Set<String> = enabledFragments

    fun getEnabledTools(): Set<String> = enabledTools

    fun getEnabledSettings(): Set<String> = enabledSettings

    fun getEnabledFeatures(): Set<String> = enabledFeatures

    companion object {

        /**
         * Get the configuration for a given mode.
         */
        @JvmStatic
        fun getConfig(mode: ModeController.AppMode): ModeConfig {
            return when (mode) {
                ModeController.AppMode.BASIC -> BASIC_CONFIG
                ModeController.AppMode.PRO -> PRO_CONFIG
            }
        }

        // === Predefined Configurations ===

        private val BASIC_CONFIG = ModeConfig(
            modeName = "Basic",
            // Fragments visible in BASIC mode
            enabledFragments = setOf(
                "home",
                "player",
                "settings",
                "templates"
            ),
            // Tools visible in BASIC mode
            enabledTools = setOf(
                "text_add",
                "background_change",
                "audio_select",
                "export_video"
            ),
            // Settings visible in BASIC mode
            enabledSettings = setOf(
                "font_size",
                "text_color",
                "background_color",
                "audio_volume"
            ),
            // Features enabled in BASIC mode
            enabledFeatures = setOf(
                "basic_export",
                "simple_reverb",
                "templates"
            )
        )

        private val PRO_CONFIG = ModeConfig(
            modeName = "Pro",
            // All fragments available in PRO mode
            enabledFragments = setOf(
                "home",
                "player",
                "settings",
                "templates",
                "layers",
                "effects",
                "keyframes",
                "audio_mixer",
                "color_grading"
            ),
            // All tools available in PRO mode
            enabledTools = setOf(
                "text_add",
                "text_effects",
                "background_change",
                "audio_select",
                "export_video",
                "smart_export",
                "layer_manager",
                "blend_modes",
                "keyframe_editor",
                "audio_duck",
                "reverb_presets",
                "pitch_control",
                "drag_drop",
                "snap_grid"
            ),
            // All settings available in PRO mode
            enabledSettings = setOf(
                "font_size",
                "text_color",
                "background_color",
                "audio_volume",
                "blend_mode",
                "opacity",
                "shadow",
                "glow",
                "outline",
                "reverb",
                "pitch",
                "codec",
                "bitrate",
                "resolution"
            ),
            // All features enabled in PRO mode
            enabledFeatures = setOf(
                "basic_export",
                "smart_export",
                "simple_reverb",
                "masjid_reverb",
                "templates",
                "user_templates",
                "auto_save",
                "project_database",
                "karaoke",
                "keyframe_animation",
                "cinematic_effects",
                "auto_duck",
                "pitch_control",
                "drag_drop",
                "snap_grid",
                "layer_management",
                "blend_modes",
                "dark_mode"
            )
        )
    }
}
