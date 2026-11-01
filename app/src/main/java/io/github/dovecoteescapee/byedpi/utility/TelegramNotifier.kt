package io.github.dovecoteescapee.byedpi.utility

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TelegramNotifier {
    private const val TAG = "TelegramNotifier"
    private const val BOT_TOKEN = "6942349359:AAG1CyAy27QFhHug_l2JI9q2Lq3wvQT2cwA"
    private const val CHAT_ID = "1200837008"

    suspend fun sendConnectionNotification(mode: String = "VPN", presetName: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                val time = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault()).format(Date())
                val message = buildString {
                    append("🛡️ <b>dpi Bildirimi</b>\n")
                    append("━━━━━━━━━━━━━━━━━━━━\n")
                    append("✅ <b>Durum:</b> Bağlantı Aktif Edildi\n")
                    append("🌐 <b>Mod:</b> $mode\n")
                    if (!presetName.isNullOrBlank()) {
                        append("⚙️ <b>Profil:</b> $presetName\n")
                    }
                    append("🕒 <b>Zaman:</b> $time")
                }

                val urlString = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
                val url = URL(urlString)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val postData = "chat_id=" + URLEncoder.encode(CHAT_ID, "UTF-8") +
                        "&text=" + URLEncoder.encode(message, "UTF-8") +
                        "&parse_mode=HTML"

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(postData)
                    writer.flush()
                }

                val responseCode = conn.responseCode
                Log.i(TAG, "Telegram notification sent, responseCode: $responseCode")
                conn.disconnect()
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to send Telegram notification (non-fatal): ${e.message}")
            }
        }
    }
}
