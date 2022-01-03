package io.ak1.pix

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.get
import io.ak1.pix.helpers.*
import io.ak1.pix.models.Options
import io.ak1.pix.utility.ARG_PARAM_PIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * Created by iad.guno on 01 December, 2021
 *
 */

/**
 * Activity that uses camera
 *
 * @constructor Create empty Camera activity
 */
class PixActivity : AppCompatActivity() {

    /**
     * This is like the ID or source of the request
     */
    private var id = ""
    var navController: NavController? = null
    private var options: Options? = null

    companion object {
        const val OPTIONS = "options"
        const val REQUEST_ID = "requestId"
        const val IMAGE_URI_LIST = "imageUriList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        setupScreen()
        supportActionBar?.hide()
        id = intent?.getStringExtra(REQUEST_ID) ?: ""
        options = intent?.getParcelableExtra(OPTIONS) as? Options
        lifecycleScope.launch {
            hideStatusBar()
            delay(200) // without the delay, the camera does not start
            showCameraFragment()
        }
    }

    private fun showCameraFragment() {
        val bundle = bundleOf(ARG_PARAM_PIX to options)
        navController = findNavController(R.id.nav_host_fragment)
        navController?.setGraph(R.navigation.pix_activity_navigation, bundle)

        //receiving the result send from either PixFragment/ImagePreviewFragment and passing the result to the
        //backstack activity where the receiver is registered if success.
        PixBus.results(coroutineScope = CoroutineScope(Dispatchers.Main)) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    val intent = Intent().apply {
                        putExtra(REQUEST_ID, id)
                        putStringArrayListExtra(IMAGE_URI_LIST, ArrayList(it.data.map { uri ->
                            uri.toString()
                        }))
                    }
                    setResult(Activity.RESULT_OK, intent)
                    // showStatusBar()
                    finish()
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    onBackPressed()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (navController?.currentDestination == navController?.graph?.get(R.id.pix_fragment)) {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun navigate(navId: Int, destinationArgs: Bundle? = null) {
        navController?.navigate(navId, destinationArgs)
    }
}

/**
 * PixActivity Result Contract
 *
 * From https://proandroiddev.com/is-onactivityresult-deprecated-in-activity-results-api-lets-deep-dive-into-it-302d5cf6edd
 */
class PixActivityContract :
    ActivityResultContract<PixActivityContract.PixActivityInput, PixActivityContract.PixActivityResult?>() {

    /**
     * Wrapper class for the input of this launcher
     *
     * @param requestId - pass the request id (similar to REQUEST_CODE in the old API)
     * @param options - see [Options]
     *
     * */
    data class PixActivityInput(
        val requestId: String,
        val options: Options,
    )

    /**
     * Wrapper class for the result of this launcher
     *
     * @param requestId - pass the request id (similar to REQUEST_CODE in the old API)
     * @param imageUriList - return the image(s) or the video URIs
     *
     * */
    data class PixActivityResult(val requestId: String, val imageUriList: List<Uri>?)

    override fun createIntent(
        context: Context,
        input: PixActivityInput
    ): Intent {
        return Intent(context, PixActivity::class.java).apply {
            putExtra(PixActivity.REQUEST_ID, input.requestId)
            putExtra(PixActivity.OPTIONS, input.options)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): PixActivityResult? {
        val id = intent?.getStringExtra(PixActivity.REQUEST_ID) ?: ""
        val data = intent?.getStringArrayListExtra(PixActivity.IMAGE_URI_LIST)
        return if (resultCode == Activity.RESULT_OK && !id.isNullOrBlank() && data != null) {
            PixActivityResult(id, data.toList().map {
                Uri.parse(it)
            })
        } else {
            null
        }
    }
}
