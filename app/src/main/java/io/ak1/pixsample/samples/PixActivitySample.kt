package io.ak1.pixsample.samples

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ak1.pix.PixActivityContract
import io.ak1.pix.helpers.setupScreen
import io.ak1.pix.helpers.showStatusBar
import io.ak1.pixsample.R
import io.ak1.pixsample.options

class PixActivitySample : AppCompatActivity() {

    private val resultsFragment = ResultsFragment {
        pixActivityResultLauncher.launch(
            PixActivityContract.PixActivityInput(
                "anyIdWillDoForNow",
                options
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

    private var pixActivityResultLauncher =
        registerForActivityResult(PixActivityContract()) { result ->
            result?.let {
                if (it.imageUriList?.isNotEmpty() == true) {
                    updateResults(it.imageUriList!!)
                }
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
