package hazem.nurmontage.videoquran.template

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Save, load, and delete user custom templates.
 * Uses SharedPreferences + org.json for JSON serialization (no Gson dependency needed).
 */
class UserTemplateManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Represents a user-created template.
     */
    class UserTemplate {
        var id: String? = null
        var name: String? = null
        var category: String? = null
        var createdAt: Long = System.currentTimeMillis()
        var updatedAt: Long = createdAt
        var width: Int = 0
        var height: Int = 0
        var backgroundColor: String? = null
        var textColor: String? = null
        var fontName: String? = null
        var fontSize: Int = 0
        var effectPreset: String? = null
        var reverbPreset: Float = 0f
        var backgroundPath: String? = null
        var isFavorite: Boolean = false

        @Throws(JSONException::class)
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("id", id)
                put("name", name)
                put("category", category)
                put("createdAt", createdAt)
                put("updatedAt", updatedAt)
                put("width", width)
                put("height", height)
                put("backgroundColor", backgroundColor)
                put("textColor", textColor)
                put("fontName", fontName)
                put("fontSize", fontSize)
                put("effectPreset", effectPreset)
                put("reverbPreset", reverbPreset.toDouble())
                put("backgroundPath", backgroundPath)
                put("isFavorite", isFavorite)
            }
        }

        companion object {
            @Throws(JSONException::class)
            fun fromJson(o: JSONObject): UserTemplate {
                return UserTemplate().apply {
                    id = o.optString("id", null)
                    name = o.optString("name", null)
                    category = o.optString("category", null)
                    createdAt = o.optLong("createdAt", 0)
                    updatedAt = o.optLong("updatedAt", 0)
                    width = o.optInt("width", 1080)
                    height = o.optInt("height", 1920)
                    backgroundColor = o.optString("backgroundColor", "#000000")
                    textColor = o.optString("textColor", "#FFFFFF")
                    fontName = o.optString("fontName", null)
                    fontSize = o.optInt("fontSize", 24)
                    effectPreset = o.optString("effectPreset", null)
                    reverbPreset = o.optDouble("reverbPreset", 0.0).toFloat()
                    backgroundPath = o.optString("backgroundPath", null)
                    isFavorite = o.optBoolean("isFavorite", false)
                }
            }
        }
    }

    fun saveTemplate(template: UserTemplate?): Boolean {
        if (template == null || template.name.isNullOrEmpty()) return false
        val templates = loadAllTemplates()
        if (template.id.isNullOrEmpty()) {
            template.id = "tmpl_${System.currentTimeMillis()}"
        }
        var existingIndex = -1
        for (i in templates.indices) {
            if (template.id == templates[i].id) {
                existingIndex = i
                break
            }
        }
        template.updatedAt = System.currentTimeMillis()
        if (existingIndex >= 0) {
            templates[existingIndex] = template
        } else {
            templates.add(template)
        }
        return saveTemplatesList(templates)
    }

    fun loadTemplate(templateId: String): UserTemplate? {
        for (t in loadAllTemplates()) {
            if (templateId == t.id) return t
        }
        return null
    }

    fun loadAllTemplates(): List<UserTemplate> {
        val json = prefs.getString(KEY_TEMPLATES, "[]")
        val templates = mutableListOf<UserTemplate>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                templates.add(UserTemplate.fromJson(arr.getJSONObject(i)))
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error loading templates", e)
        }
        return templates
    }

    fun deleteTemplate(templateId: String): Boolean {
        val templates = loadAllTemplates()
        var i = templates.size - 1
        while (i >= 0) {
            if (templateId == templates[i].id) {
                templates.removeAt(i)
                return saveTemplatesList(templates)
            }
            i--
        }
        return false
    }

    fun getTemplatesByCategory(category: String): List<UserTemplate> {
        return loadAllTemplates().filter { category == it.category }
    }

    fun getFavoriteTemplates(): List<UserTemplate> {
        return loadAllTemplates().filter { it.isFavorite }
    }

    fun toggleFavorite(templateId: String): Boolean {
        val templates = loadAllTemplates()
        for (t in templates) {
            if (templateId == t.id) {
                t.isFavorite = !t.isFavorite
                t.updatedAt = System.currentTimeMillis()
                return saveTemplatesList(templates)
            }
        }
        return false
    }

    private fun saveTemplatesList(templates: List<UserTemplate>): Boolean {
        try {
            val arr = JSONArray()
            for (t in templates) {
                arr.put(t.toJson())
            }
            return prefs.edit().putString(KEY_TEMPLATES, arr.toString()).commit()
        } catch (e: JSONException) {
            Log.e(TAG, "Error saving templates", e)
            return false
        }
    }

    companion object {
        private const val TAG = "UserTemplateManager"
        private const val PREFS_NAME = "user_templates"
        private const val KEY_TEMPLATES = "templates_list"
    }
}
