package io.github.dovecoteescapee.byedpi.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.core.TProxyService
import io.github.dovecoteescapee.byedpi.data.*
import io.github.dovecoteescapee.byedpi.fragments.MainSettingsFragment
import io.github.dovecoteescapee.byedpi.databinding.ActivityMainBinding
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.*
import kotlinx.coroutines.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var statsJob: Job? = null

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName

        private fun collectLogs(): String? =
            try {
                Runtime.getRuntime()
                    .exec("logcat *:D -d")
                    .inputStream.bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to collect logs", e)
                null
            }

        private fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }

        private fun formatSpeed(bytesPerSec: Long): String =
            "${formatBytes(bytesPerSec)}/s"
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
                updateStatus()
            }
        }

    private val logsRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val logs = collectLogs()

                if (logs == null) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.logs_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val uri = it.data?.data ?: run {
                        Log.e(TAG, "No data in result")
                        return@launch
                    }
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        try {
                            stream.write(logs.toByteArray())
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to save logs", e)
                        }
                    } ?: run {
                        Log.e(TAG, "Failed to open output stream")
                    }
                }
            }
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received intent: ${intent?.action}")

            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }

            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)
            if (sender == null) {
                Log.w(TAG, "Received intent with unknown sender: $senderOrd")
                return
            }

            when (val action = intent.action) {
                STARTED_BROADCAST,
                STOPPED_BROADCAST -> updateStatus()

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start, sender.name),
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateStatus()
                }

                else -> Log.w(TAG, "Unknown action: $action")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        val toggleAction = {
            val (status, _) = appStatus
            when (status) {
                AppStatus.Halted -> start()
                AppStatus.Running -> stop()
            }
        }

        binding.buttonInnerCard.setOnClickListener { toggleAction() }
        binding.statusButton.setOnClickListener { toggleAction() }

        binding.rowPreset.setOnClickListener {
            showPresetPickerDialog()
        }

        binding.cardDeveloperBanner.setOnClickListener {
            startActivity(Intent(this, DeveloperActivity::class.java))
        }

        binding.quickInstagram.setOnClickListener {
            val instagramUrl = getPreferences().getString("developer_instagram_url", null)
                ?: getString(R.string.developer_instagram_url)
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
                setPackage("com.instagram.android")
            }
            try {
                startActivity(appIntent)
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)))
            }
        }

        binding.quickGithub.setOnClickListener {
            val githubUrl = getPreferences().getString("developer_github_url", null)
                ?: getString(R.string.developer_github_url)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl)))
            } catch (e: Exception) {
                Toast.makeText(this, "Bağlantı açılamadı", Toast.LENGTH_SHORT).show()
            }
        }

        val theme = getPreferences().getString("app_theme", null)
        MainSettingsFragment.setTheme(theme ?: "system")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onPause() {
        super.onPause()
        stopStatsWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStatsWatcher()
        unregisterReceiver(receiver)
    }

    private fun showPresetPickerDialog() {
        val presets = arrayOf(
            DpiPreset.BALANCED,
            DpiPreset.DISCORD_VOICE,
            DpiPreset.SUPERONLINE,
            DpiPreset.TURK_TELEKOM,
            DpiPreset.LIGHT
        )
        val titles = presets.map { "${it.title}\n(${it.cmdArgs})" }.toTypedArray()
        val prefs = getPreferences()
        val currentPresetId = prefs.getString("active_dpi_preset", DpiPreset.BALANCED.id)
        val selectedIndex = presets.indexOfFirst { it.id == currentPresetId }.let {
            if (it >= 0) it else 0
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.choose_preset_title)
            .setSingleChoiceItems(titles, selectedIndex) { dialog, which ->
                val chosen = presets[which]
                DpiPreset.applyPreset(prefs, chosen)
                binding.presetValueText.text = chosen.title
                Toast.makeText(
                    this,
                    getString(R.string.preset_applied, chosen.title),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startStatsWatcher() {
        statsJob?.cancel()
        statsJob = lifecycleScope.launch(Dispatchers.IO) {
            var prevTx = 0L
            var prevRx = 0L
            while (isActive) {
                val stats = try {
                    TProxyService.TProxyGetStats()
                } catch (e: Throwable) {
                    null
                }
                if (stats != null && stats.size >= 4) {
                    val txBytes = stats[1]
                    val rxBytes = stats[3]
                    val speedTx = if (prevTx > 0 && txBytes >= prevTx) txBytes - prevTx else 0L
                    val speedRx = if (prevRx > 0 && rxBytes >= prevRx) rxBytes - prevRx else 0L
                    prevTx = txBytes
                    prevRx = rxBytes

                    val rxFormatted = formatBytes(rxBytes)
                    val txFormatted = formatBytes(txBytes)
                    val speedDown = formatSpeed(speedRx)
                    val speedUp = formatSpeed(speedTx)

                    withContext(Dispatchers.Main) {
                        binding.trafficStatsText.text = "↓ $speedDown ($rxFormatted) • ↑ $speedUp ($txFormatted)"
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopStatsWatcher() {
        statsJob?.cancel()
        statsJob = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val (status, _) = appStatus

        return when (item.itemId) {
            R.id.action_developer -> {
                startActivity(Intent(this, DeveloperActivity::class.java))
                true
            }

            R.id.action_settings -> {
                if (status == AppStatus.Halted) {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            R.id.action_save_logs -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "xenci.log")
                }

                logsRegister.launch(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun start() {
        when (getPreferences().mode()) {
            Mode.VPN -> {
                val intentPrepare = VpnService.prepare(this)
                if (intentPrepare != null) {
                    vpnRegister.launch(intentPrepare)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop(this)
    }

    private fun updateStatus() {
        val (status, mode) = appStatus

        Log.i(TAG, "Updating status: $status, $mode")

        val preferences = getPreferences()
        val proxyIp = preferences.getStringNotNull("byedpi_proxy_ip", "127.0.0.1")
        val proxyPort = preferences.getStringNotNull("byedpi_proxy_port", "1080")
        val dnsIp = preferences.getStringNotNull("dns_ip", "1.1.1.1")
        val currentMode = preferences.mode()
        val presetId = preferences.getString("active_dpi_preset", DpiPreset.BALANCED.id) ?: DpiPreset.BALANCED.id
        val preset = DpiPreset.fromId(presetId)

        binding.proxyAddress.text = getString(R.string.proxy_address, proxyIp, proxyPort)
        binding.dnsValueText.text = dnsIp
        binding.modeValueText.text = when (currentMode) {
            Mode.VPN -> "VPN"
            Mode.Proxy -> "Proxy"
        }
        binding.presetValueText.text = preset.title

        val colorConnected = ContextCompat.getColor(this, R.color.status_connected)
        val colorConnectedGlow = ContextCompat.getColor(this, R.color.status_connected_glow)
        val colorDisconnected = ContextCompat.getColor(this, R.color.status_disconnected)
        val colorDisconnectedGlow = ContextCompat.getColor(this, R.color.status_disconnected_glow)
        val colorBrand = ContextCompat.getColor(this, R.color.brand_primary)

        when (status) {
            AppStatus.Halted -> {
                stopStatsWatcher()
                binding.trafficStatsText.setText(R.string.traffic_idle)

                binding.buttonInnerCard.setCardBackgroundColor(colorBrand)
                binding.buttonOuterRing.setCardBackgroundColor(colorDisconnectedGlow)
                binding.badgeStatusContainer.setCardBackgroundColor(colorDisconnectedGlow)
                binding.badgeStatusContainer.strokeColor = colorDisconnected
                binding.statusIndicatorDot.backgroundTintList = ColorStateList.valueOf(colorDisconnected)
                binding.badgeStatusText.setTextColor(colorDisconnected)

                when (currentMode) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_disconnected)
                        binding.badgeStatusText.setText(R.string.vpn_disconnected)
                        binding.statusButton.setText(R.string.vpn_connect)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_down)
                        binding.badgeStatusText.setText(R.string.proxy_down)
                        binding.statusButton.setText(R.string.proxy_start)
                    }
                }
                binding.statusDesc.setText(R.string.status_ready_desc)
                binding.statusButton.isEnabled = true
                binding.buttonInnerCard.isEnabled = true
            }

            AppStatus.Running -> {
                startStatsWatcher()

                binding.buttonInnerCard.setCardBackgroundColor(colorConnected)
                binding.buttonOuterRing.setCardBackgroundColor(colorConnectedGlow)
                binding.badgeStatusContainer.setCardBackgroundColor(colorConnectedGlow)
                binding.badgeStatusContainer.strokeColor = colorConnected
                binding.statusIndicatorDot.backgroundTintList = ColorStateList.valueOf(colorConnected)
                binding.badgeStatusText.setTextColor(colorConnected)

                when (mode) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_connected)
                        binding.badgeStatusText.setText(R.string.vpn_connected)
                        binding.statusButton.setText(R.string.vpn_disconnect)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_up)
                        binding.badgeStatusText.setText(R.string.proxy_up)
                        binding.statusButton.setText(R.string.proxy_stop)
                    }
                }
                binding.statusDesc.setText(R.string.status_connected_desc)
                binding.statusButton.isEnabled = true
                binding.buttonInnerCard.isEnabled = true
            }
        }
    }
}
