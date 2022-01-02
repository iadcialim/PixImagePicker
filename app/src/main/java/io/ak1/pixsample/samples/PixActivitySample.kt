package io.ak1.pixsample.samples

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ak1.pix.helpers.getOptionsParams
import io.ak1.pix.helpers.registerPixActivity
import io.ak1.pix.helpers.setupScreen
import io.ak1.pix.helpers.showStatusBar
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import io.ak1.pix.ui.camera.PixActivityContract
import io.ak1.pixsample.R

class PixActivitySample : AppCompatActivity() {

    private val defaultOptions = Options().apply {
        ratio = Ratio.RATIO_AUTO
        count = 3
        spanCount = 4
        path = "Camera"
        isFrontFacing = false
        mode = Mode.All
        flash = Flash.Auto
        preSelectedUrls = ArrayList()
        showGallery = true
        showPreview = true
    }

    private val resultsFragment = ResultsFragment {
        // show the camera
        pixActivityResultLauncher.launch(
            PixActivityContract.PixActivityInput(
                "anyIdWillDoForNow",
                getOptionsParams() ?: defaultOptions
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_sample)
        setupScreen()
        supportActionBar?.hide()
        showResultsFragment()
    }

    private var pixActivityResultLauncher = registerPixActivity { result ->
        if (result.imageUriList?.isNotEmpty() == true) {
            updateResults(result.imageUriList!!)
        }
    }

    private fun updateResults(imageUriList: List<Uri>) {
        showResultsFragment()
        resultsFragment.setList(imageUriList)
    }

    private fun showResultsFragment() {
        showStatusBar()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, resultsFragment).commit()
    }
}
