package com.example.android.guessthree

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import com.example.android.guessthree.databinding.ActivityInterstitialBinding
import com.example.android.guessthree.helper.Helper
import com.google.android.gms.ads.*
import kotlin.random.Random

class InterstitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterstitialBinding

    private var randomInt: Int = 0
    private val noOfColorVariations = 3
    private val usedColors =
        ArrayList<Int>(3 * noOfColorVariations)  // color variations * no of each
    private val correctColorName: String = "ic_btn_yellow"
    private lateinit var clLayout: ConstraintLayout
    private lateinit var iBtn5: ImageButton
    private lateinit var tv: TextView
    private val INTERSTITIAL_AD_UNIT_AD: String = "ca-app-pub-6164776318860899/9071196739"

    //--------------------------------
    private lateinit var mInterstitialAd: InterstitialAd


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterstitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Interstitial ad"
        supportActionBar?.setHomeButtonEnabled(true)

        MobileAds.initialize(this) {}

        //Adding test device -> REMOVE CODE WHEN RELEASING THE APP
        val testDeviceIds = listOf("B115ED9C35FB45C3EBA320C9D527AA0A")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        clLayout = findViewById(R.id.cl_layout)
        iBtn5 = findViewById(R.id.ibtn_5)
        tv = findViewById(R.id.tv)

        setImageToImgBtn()
        setListeners()

        useInterstitialAd()
    }

    private fun useInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = INTERSTITIAL_AD_UNIT_AD
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                tv.visibility = View.GONE
                setImageToImgBtn()
            }

            override fun onAdLeftApplication() {
                tv.visibility = View.GONE
            }

            override fun onAdClicked() {
                tv.visibility = View.GONE
            }

            override fun onAdLoaded() {
                tv.visibility = View.GONE
                rotateBtn()
            }

            override fun onAdFailedToLoad(p0: Int) {
            }

            override fun onAdFailedToLoad(p0: LoadAdError?) {
            }
        }
    }

    private fun rotateBtn() {
        val btnRotate = AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_btn
        )
        iBtn5.startAnimation(btnRotate)
    }

    private fun setImageToImgBtn() {
        clLayout.forEach {
            if (it is ImageButton) {
                it.isActivated = false
                val colorImageResName: String = resources.getResourceEntryName(pickColor())
                it.setImageResource(
                    resources.getIdentifier(
                        colorImageResName, "drawable", packageName
                    )
                )
                setTagToCorrectColor(it, colorImageResName)
            }
        }
    }

    @SuppressLint("Recycle")           //for obtainTypedArray method
    private fun pickColor(): Int {
        val imgs: TypedArray = resources.obtainTypedArray(R.array.randomColors)
        do {
            val rand = Random
            randomInt = rand.nextInt(imgs.length())        // 0 || 1 || 2
            usedColors.add(randomInt)
            val evenColor = usedColors.count { it == randomInt }
        } while (evenColor > noOfColorVariations)

        return imgs.getResourceId(randomInt, 0)          //e.g. 2131230829
    }

    private fun setTagToCorrectColor(view: ImageButton, colorImageResName: String) {
        if (colorImageResName == correctColorName) {
            view.tag = "tag"
        }
    }

    private fun setListeners() {
        clLayout.forEach { it ->
            if (it is ImageButton) {
                it.setOnClickListener { btnClickListener(it as ImageButton) }
            }
        }
    }


    private fun btnClickListener(v: ImageButton) {
        v.isActivated = v.isActivated == false
        checkIfComplete()
    }

    private fun checkIfComplete() {
        var sum = 0
        clLayout.forEach {
            val tag: String = java.lang.String.valueOf(it.tag)
            if (it is ImageButton) {
                if ((tag == "tag" && it.isActivated) || (tag != "tag" && !it.isActivated)) {
                    sum++
                }
            }
        }
        if (sum == countImageButtons()) showAdResetBtns()
    }

    private fun showAdResetBtns() {
        Helper.slowlyAppearCongratsTv(tv)
        clearGameButtons()

        if (mInterstitialAd.isLoaded) {
            Handler().postDelayed({
                mInterstitialAd.show()
            }, 1000)
        } else {
            Handler().postDelayed({
                Helper.slowlyDisappearCongratsTv(tv)
                setImageToImgBtn()
            }, 2000)
        }
    }

    private fun clearGameButtons() {
        clLayout.forEach {
            if (it is ImageButton) {
                it.tag = null
            }
        }
        usedColors.clear()

    }

    private fun countImageButtons(): Int {
        var imageBtnCount = 0
        clLayout.forEach { if (it is ImageButton) imageBtnCount++ }
        return imageBtnCount
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_interst, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.ad_banner) {
            val bannerActivity = Intent(this, BannerActivity::class.java).apply { }
            startActivity(bannerActivity)
            return true;
        }

        if (id == R.id.ad_native) {
            val nativeActivity = Intent(this, NativeActivity::class.java).apply { }
            startActivity(nativeActivity)
            return true;
        }

        if (id == R.id.ad_rewarded) {
            val rewardedActivity = Intent(this, RewardedActivity::class.java).apply { }
            startActivity(rewardedActivity)
            return true;
        }

        return super.onOptionsItemSelected(item)
    }
}