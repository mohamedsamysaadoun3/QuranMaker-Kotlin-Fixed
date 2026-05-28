package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.content.SharedPreferences

object BillingPreferences {

    private const val KEY_IS_SUBSCRIBED = "isSubscribed"
    private const val PREF_NAME = "BillingPrefs"

    @JvmStatic
    fun saveSubscriptionStatus(context: Context, isSubscribed: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
            .apply()
    }

    @JvmStatic
    fun isSubscribed(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_SUBSCRIBED, false)
    }

    @JvmStatic
    fun saveSubscribeAllItemValueTofalse(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_SUBSCRIBED, false)
            .apply()
    }
}
