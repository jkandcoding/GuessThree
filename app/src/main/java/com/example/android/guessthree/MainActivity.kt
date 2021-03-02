package com.example.android.guessthree

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.example.android.guessthree.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PRIVACY_POLICY_URL: String = ""     //todo fill this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnBtnsClicklListeners()
    }

    private fun setOnBtnsClicklListeners() {
        binding.clLayout.forEach { it ->
            if (it is Button) {
                it.setOnClickListener { goToAdActivity(it as Button) }
            }
        }
    }

    private fun goToAdActivity(it: Button) {
        if (it.id == binding.btnInterstit.id) {
            val interstActivity = Intent(this, InterstitialActivity::class.java).apply { }
            startActivity(interstActivity)
        }
        if (it.id == binding.btnBanner.id) {
            val bannerActivity = Intent(this, BannerActivity::class.java).apply { }
            startActivity(bannerActivity)
        }
        if (it.id == binding.btnNative.id) {
            val nativeActivity = Intent(this, NativeActivity::class.java).apply { }
            startActivity(nativeActivity)
        }
        if (it.id == binding.btnReward.id) {
            val rewardedActivity = Intent(this, RewardedActivity::class.java).apply { }
            startActivity(rewardedActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.legal) {
            val uri: Uri = Uri.parse(PRIVACY_POLICY_URL)
            val privacyPolicyPage = Intent(Intent.ACTION_VIEW, uri).apply { }
            startActivity(privacyPolicyPage)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}