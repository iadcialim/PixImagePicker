@file:Suppress("MemberVisibilityCanBePrivate")

package io.ak1.pixsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.ak1.pix.helpers.launchPixCamera
import io.ak1.pix.helpers.registerPixCamera
import io.ak1.pix.helpers.showStatusBar
import io.ak1.pix.models.*
import io.ak1.pixsample.databinding.ActivityMainBinding
import io.ak1.pixsample.samples.FragmentSample
import io.ak1.pixsample.samples.NavControllerSample
import io.ak1.pixsample.samples.PixActivitySample
import io.ak1.pixsample.samples.ViewPager2Sample
import io.ak1.pixsample.samples.settings.SettingsActivity


/**
 * Created By Akshay Sharma on 18,June,2021
 * https://ak1.io
 */
internal const val TAG = "Pix logs"

var options = Options()
const val IMAGE_VIDEOS_URI = "image_videos_uri"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var startResultActivity: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        options = getOptionsByPreference(this)
        startResultActivity = registerPixCamera(options) { result ->
            if (result.imageUriList?.isNotEmpty() == true) {
                pixActivitySampleClick(result.imageUriList!! as ArrayList<Uri>)
            }
        }
    }

    private fun getOptionsByPreference(mainActivity: MainActivity): Options {
        val sp = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        return Options().apply {
            isFrontFacing = sp.getBoolean("frontFacing", false)
            ratio = when (sp.getString("ratio", "0")) {
                "1" -> Ratio.RATIO_4_3
                "2" -> Ratio.RATIO_16_9
                else -> Ratio.RATIO_AUTO
            }
            flash = when (sp.getString("flash", "0")) {
                "1" -> Flash.Disabled
                "2" -> Flash.On
                "3" -> Flash.Off
                else -> Flash.Auto
            }
            mode = when (sp.getString("mode", "0")) {
                "1" -> Mode.Picture
                "2" -> Mode.Video
                else -> Mode.All
            }
            videoOptions = VideoOptions().apply {
                videoDurationLimitInSeconds = try {
                    sp.getString("videoDuration", "30")?.toInt() ?: 30
                } catch (e: Exception) {
                    sp.apply {
                        edit().putString("videoDuration", "30").commit()
                    }
                    30
                }
            }
            count = try {
                sp.getString("count", "1")?.toInt() ?: 1
            } catch (e: Exception) {
                sp.apply {
                    edit().putString("count", "1").commit()
                }
                1
            }
            spanCount = sp.getString("spanCount", "4")?.toInt() ?: 4
            showGallery = sp.getBoolean("showGallery", false)
            showPreview = sp.getBoolean("showPreview", true)
        }
    }

    private fun pixActivitySampleClick(imageUriList: ArrayList<Uri>) {
        startActivity(Intent(this, PixActivitySample::class.java).also {
            it.putParcelableArrayListExtra(IMAGE_VIDEOS_URI, imageUriList)
        })
    }

    fun fragmentSampleClick(view: View) =
        startActivity(Intent(this, FragmentSample::class.java))

    fun navControllerSampleClick(view: View) =
        startActivity(Intent(this, NavControllerSample::class.java))

    fun viewPager2SampleClick(view: View) =
        startActivity(Intent(this, ViewPager2Sample::class.java))

    fun pixActivityImplClick(view: View) = startResultActivity?.launchPixCamera("1")

    fun openSettings(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}