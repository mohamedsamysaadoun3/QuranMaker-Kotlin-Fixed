package hazem.nurmontage.videoquran.layers

import android.graphics.PorterDuff
import android.graphics.Xfermode

/**
 * Blend modes for layer compositing.
 * Includes PorterDuff modes and custom shader modes with GLSL code and FFmpeg filter names.
 */
enum class BlendMode(
    val porterDuffMode: PorterDuff.Mode?,
    val ffmpegFilterName: String,
    val glslName: String
) {
    // Standard PorterDuff modes
    NORMAL(PorterDuff.Mode.SRC_OVER, "normal", "srcOver"),
    MULTIPLY(PorterDuff.Mode.MULTIPLY, "multiply", "multiply"),
    SCREEN(PorterDuff.Mode.SCREEN, "screen", "screen"),
    OVERLAY(PorterDuff.Mode.OVERLAY, "overlay", "overlay"),
    DARKEN(PorterDuff.Mode.DARKEN, "darken", "darken"),
    LIGHTEN(PorterDuff.Mode.LIGHTEN, "lighten", "lighten"),
    COLOR_DODGE(null, "colordodge", "colorDodge"),
    COLOR_BURN(null, "colorburn", "colorBurn"),
    HARD_LIGHT(null, "hardlight", "hardLight"),
    SOFT_LIGHT(null, "softlight", "softLight"),
    DIFFERENCE(null, "difference", "difference"),
    EXCLUSION(PorterDuff.Mode.XOR, "exclusion", "exclusion"),
    ADD(PorterDuff.Mode.ADD, "addition", "linearDodge"),
    LINEAR_DODGE(null, "addition", "linearDodge"),
    LINEAR_BURN(null, "linearburn", "linearBurn");

    /**
     * Get the PorterDuff mode if available, null for shader-only modes.
     */
    fun getPorterDuffMode(): PorterDuff.Mode? = porterDuffMode

    /**
     * Get the Xfermode for this blend mode.
     */
    fun getXfermode(): Xfermode? {
        return porterDuffMode?.let { PorterDuffXfermode(it) }
    }

    /**
     * Get the FFmpeg blend filter name for this mode.
     */
    fun getFFmpegFilterName(): String = ffmpegFilterName

    /**
     * Get the GLSL function name for this blend mode.
     */
    fun getGLSLName(): String = glslName

    /**
     * Get GLSL shader code for this blend mode.
     */
    fun getGLSLCode(): String = when (this) {
        MULTIPLY -> "vec3 blendMultiply(vec3 base, vec3 blend) { return base * blend; }\n"
        SCREEN -> "vec3 blendScreen(vec3 base, vec3 blend) { return 1.0 - (1.0 - base) * (1.0 - blend); }\n"
        OVERLAY -> "vec3 blendOverlay(vec3 base, vec3 blend) {\n" +
                "  vec3 result;\n" +
                "  result.r = base.r < 0.5 ? 2.0 * base.r * blend.r : 1.0 - 2.0 * (1.0 - base.r) * (1.0 - blend.r);\n" +
                "  result.g = base.g < 0.5 ? 2.0 * base.g * blend.g : 1.0 - 2.0 * (1.0 - base.g) * (1.0 - blend.g);\n" +
                "  result.b = base.b < 0.5 ? 2.0 * base.b * blend.b : 1.0 - 2.0 * (1.0 - base.b) * (1.0 - blend.b);\n" +
                "  return result;\n" +
                "}\n"
        SOFT_LIGHT -> "vec3 blendSoftLight(vec3 base, vec3 blend) {\n" +
                "  return 2.0 * base * blend + base * base - 2.0 * base * base * blend;\n" +
                "}\n"
        HARD_LIGHT -> "vec3 blendHardLight(vec3 base, vec3 blend) {\n" +
                "  vec3 result;\n" +
                "  result.r = blend.r < 0.5 ? 2.0 * base.r * blend.r : 1.0 - 2.0 * (1.0 - base.r) * (1.0 - blend.r);\n" +
                "  result.g = blend.g < 0.5 ? 2.0 * base.g * blend.g : 1.0 - 2.0 * (1.0 - base.g) * (1.0 - blend.g);\n" +
                "  result.b = blend.b < 0.5 ? 2.0 * base.b * blend.b : 1.0 - 2.0 * (1.0 - base.b) * (1.0 - blend.b);\n" +
                "  return result;\n" +
                "}\n"
        COLOR_DODGE -> "vec3 blendColorDodge(vec3 base, vec3 blend) {\n" +
                "  return blend == vec3(1.0) ? blend : min(base / (1.0 - blend), vec3(1.0));\n" +
                "}\n"
        COLOR_BURN -> "vec3 blendColorBurn(vec3 base, vec3 blend) {\n" +
                "  return blend == vec3(0.0) ? blend : max(1.0 - (1.0 - base) / blend, vec3(0.0));\n" +
                "}\n"
        ADD, LINEAR_DODGE -> "vec3 blendAdd(vec3 base, vec3 blend) { return min(base + blend, vec3(1.0)); }\n"
        LINEAR_BURN -> "vec3 blendLinearBurn(vec3 base, vec3 blend) { return max(base + blend - vec3(1.0), vec3(0.0)); }\n"
        DIFFERENCE -> "vec3 blendDifference(vec3 base, vec3 blend) { return abs(base - blend); }\n"
        EXCLUSION -> "vec3 blendExclusion(vec3 base, vec3 blend) { return base + blend - 2.0 * base * blend; }\n"
        else -> "vec3 blendNormal(vec3 base, vec3 blend) { return blend; }\n"
    }

    /**
     * Get FFmpeg blend filter string for this mode.
     */
    fun getFFmpegBlendFilter(): String {
        if (this == NORMAL) return ""
        return "blend=$ffmpegFilterName"
    }

    /**
     * Custom Xfermode wrapper for PorterDuff modes.
     */
    private class PorterDuffXfermode(val mode: PorterDuff.Mode) : Xfermode()
}
