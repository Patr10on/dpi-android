package io.github.dovecoteescapee.byedpi.data

import android.content.SharedPreferences

enum class DpiPreset(
    val id: String,
    val title: String,
    val description: String,
    val cmdArgs: String
) {
    BALANCED(
        "balanced",
        "Dengeli (Varsayılan)",
        "Çoğu servis sağlayıcı için standart paket bölme ve OOB modu",
        "-s1 -q1 -Y"
    ),
    DISCORD_VOICE(
        "discord_voice",
        "Discord & Ses İletişimi",
        "Discord ses kanalları ve UDP trafiği için sahte paket koruması",
        "-s1 -q1 -a1"
    ),
    SUPERONLINE(
        "superonline",
        "Katı Filtreleme / Superonline",
        "SNI sahteleme ve TLS kayıt bölme ile gelişmiş filtre aşma",
        "-s1 -d1 -f -1 -r 1+s"
    ),
    TURK_TELEKOM(
        "turk_telekom",
        "Türk Telekom / Hızlı",
        "Hafif gecikmeli Out-of-band desenkronizasyonu",
        "-s1 -o1 -Y"
    ),
    LIGHT(
        "light",
        "Hafif (Sadece Split)",
        "Minimum gecikme, temel DPI aşma yöntemi",
        "-s1"
    ),
    CUSTOM(
        "custom",
        "Özel Yapılandırma",
        "Kullanıcının ayarladığı özel parametreler",
        ""
    );

    companion object {
        fun fromId(id: String): DpiPreset =
            entries.firstOrNull { it.id == id } ?: BALANCED

        fun applyPreset(prefs: SharedPreferences, preset: DpiPreset) {
            if (preset == CUSTOM) return
            prefs.edit()
                .putString("active_dpi_preset", preset.id)
                .putBoolean("byedpi_enable_cmd_settings", true)
                .putString("byedpi_cmd_args", preset.cmdArgs)
                .apply()
        }
    }
}
