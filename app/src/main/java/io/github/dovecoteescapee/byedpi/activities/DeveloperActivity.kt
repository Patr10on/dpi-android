package io.github.dovecoteescapee.byedpi.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.databinding.ActivityDeveloperBinding
import io.github.dovecoteescapee.byedpi.utility.getPreferences

class DeveloperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeveloperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.developer_title)
        }

        binding.developerNameText.text = "patr10on"
        binding.instagramHandleText.text = "@sgepv"
        binding.githubHandleText.text = "patr10on"

        val prefs = getPreferences()
        val instagramUrl = prefs.getString("developer_instagram_url", null)
            ?: getString(R.string.developer_instagram_url)
        val githubUrl = prefs.getString("developer_github_url", null)
            ?: getString(R.string.developer_github_url)

        val openInstagram = {
            openInstagramProfile(instagramUrl)
        }

        val openGithub = {
            openUrl(githubUrl)
        }

        binding.cardInstagram.setOnClickListener { openInstagram() }
        binding.btnOpenInstagram.setOnClickListener { openInstagram() }

        binding.cardGithub.setOnClickListener { openGithub() }
        binding.btnOpenGithub.setOnClickListener { openGithub() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openInstagramProfile(url: String) {
        val uri = Uri.parse(url)
        val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
        }
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            openUrl(url)
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Bağlantı açılamadı: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
