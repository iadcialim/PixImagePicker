package io.ak1.pix.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import io.ak1.pix.models.Options
import io.ak1.pix.ui.PixActivity

/**
 *
 * Created by iad.guno on 06 December, 2021
 * Copyright © 2020 Quantum Inventions. All rights reserved.
 */

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
