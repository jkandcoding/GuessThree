package com.example.android.guessthree

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.TypedArray
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.transition.ChangeBounds
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.example.android.guessthree.databinding.ActivityNativeBinding
import com.example.android.guessthree.helper.Helper
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlin.random.Random

class NativeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNativeBinding
    private var randomInt: Int = 0
    private val noOfColorVariations = 3
    private val usedColors =
        ArrayList<Int>(3 * noOfColorVariations)  // color variations * no of each
    private val correctColorName: String = "ic_btn_yellow"
    private lateinit var clLayout: ConstraintLayout
    private lateinit var iBtn5: ImageButton
    private lateinit var tv: TextView
    private lateinit var adLayout: FrameLayout
    //--------------------------------
    private var currentNativeAd: UnifiedNativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Native ad"
        supportActionBar?.setHomeButtonEnabled(true)

        MobileAds.initialize(this) {}

        //Adding test device -> REMOVE CODE WHEN RELEASING THE APP
        val testDeviceIds = listOf("B115ED9C35FB45C3EBA320C9D527AA0A")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        clLayout = findViewById(R.id.cl_layout)
        iBtn5 = findViewById(R.id.ibtn_5)
        tv = findViewById(R.id.tv)
        adLayout = findViewById(R.id.fl_frameLayout)

        setImageToImgBtn()
        setListeners()

        useNativeAd()
    }

    private fun useNativeAd() {
        val builder =
            AdLoader.Builder(this, "ca-app-pub-6164776318860899/9585174388").forUnifiedNativeAd {
                // Show the ad.
                val adView = layoutInflater.inflate(R.layout.ad_native, null) as UnifiedNativeAdView
                populateUnifiedNativeAdView(it, adView)
                binding.flFrameLayout.removeAllViews()
                binding.flFrameLayout.addView(adView)
                // If this callback occurs after the activity is destroyed, you
                // must call destroy and return or you may get a memory leak.
                // Note `isDestroyed` is a method on Activity.
                if (isDestroyed) {
                    it.destroy()
                    return@forUnifiedNativeAd
                }
            }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdLoaded() {
                rotateBtn()
                super.onAdLoaded()
            }

            override fun onAdClicked() {
            }
        }).build()

        //NOTE: The loadAds() - up to 5 - method currently works only with AdMob ads.
        // For mediated ads, use loadAd() instead.
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun rotateBtn() {
        val btnRotate = AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_btn
        )
        iBtn5.startAnimation(btnRotate)
    }

    /**
     * Populates a [UnifiedNativeAdView] object with data from a given
     * [UnifiedNativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
    private fun populateUnifiedNativeAdView(
        nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView
    ) {
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.
        currentNativeAd?.destroy()
        currentNativeAd = nativeAd
        //Set the media view
        adView.mediaView = adView.findViewById(R.id.ad_media)
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        val vc = nativeAd.videoController
        if (vc.hasVideoContent()) {
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    useNativeAd()
                    super.onVideoEnd()
                }
            }
        }
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
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
        if (sum == countImageButtons()) {

            appearDissappearCongrats()
            useNativeAd()
            Handler().postDelayed({
                clearGameButtons()
            }, 2000)
        }
    }

    private fun clearGameButtons() {
        clLayout.forEach {
            if (it is ImageButton) {
                it.tag = null
            }
        }
        Helper.slowlyDisappearCongratsTv(tv)
        appearDissappearCongrats()
        usedColors.clear()
        setImageToImgBtn()
    }

    private fun appearDissappearCongrats() {
        val transContainer : ViewGroup = findViewById(R.id.transitions_container)

        TransitionManager.beginDelayedTransition(transContainer)

        if (tv.visibility == View.GONE) {
            tv.visibility = View.VISIBLE
        } else {
            tv.visibility = View.GONE
        }

    }

    private fun countImageButtons(): Int {
        var imageBtnCount = 0
        clLayout.forEach { if (it is ImageButton) imageBtnCount++ }
        return imageBtnCount
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_native, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.ad_interstitial) {
            val interstitialActivity = Intent(this, InterstitialActivity::class.java).apply { }
            startActivity(interstitialActivity)
            return true;
        }

        if (id == R.id.ad_banner) {
            val bannerActivity = Intent(this, BannerActivity::class.java).apply { }
            startActivity(bannerActivity)
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