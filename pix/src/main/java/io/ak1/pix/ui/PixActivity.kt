package io.ak1.pix.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.get
import io.ak1.pix.R
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
internal class PixActivity : AppCompatActivity() {

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
        options = getOptionsParams()

        lifecycleScope.launch {
            hideStatusBar()
            delay(200) // without the delay, the camera does not start
            showCameraFragment()
        }
    }

    private fun showCameraFragment() {
        val bundle = bundleOf(ARG_PARAM_PIX to options)
        navController = findNavController(R.id.nav_host_fragment)
        navController?.setGraph(R.navigation.pix_navigation, bundle)

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
        if (navController?.currentDestination == navController?.graph?.get(R.id.navigation_pix)) {
            PixBus.onBackPressedEvent()
        } else {
            super.onBackPressed()
        }
    }

    fun navigate(navId: Int, destinationArgs: Bundle? = null) {
        navController?.navigate(navId, destinationArgs)
    }
}
