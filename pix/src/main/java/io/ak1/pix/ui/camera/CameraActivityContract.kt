package io.ak1.pix.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import io.ak1.pix.models.Options
import io.ak1.pix.ui.PixActivity

/**
 * tracksynq-android
 *
 * Created by iad.guno on 06 December, 2021
 * Copyright © 2020 Quantum Inventions. All rights reserved.
 */

/**
 * Camera Activity Result Contract
 *
 * From https://proandroiddev.com/is-onactivityresult-deprecated-in-activity-results-api-lets-deep-dive-into-it-302d5cf6edd
 */
class CameraActivityContract :
    ActivityResultContract<String, CameraActivityContract.CameraActivityResult?>() {

    /**
     * @param id - pass the id by which it was launched.
     * @param cameraMode - pass 1 for picture and 2 for video
     * @param imageUriList - pass the image(s) or the video uri
    * */
    data class CameraActivityResult(val id: String, val cameraMode: Int, val imageUriList: List<Uri>?)

    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, PixActivity::class.java).apply {
            putExtra(PixActivity.ID, input)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): CameraActivityResult? {
        val id = intent?.getStringExtra(PixActivity.ID)
        val mode = intent?.getIntExtra(PixActivity.MODE, 1) ?: 1
        val data = intent?.getStringArrayListExtra(PixActivity.IMAGE_URI_LIST)
        return if (resultCode == Activity.RESULT_OK && !id.isNullOrBlank() && data != null) {
            CameraActivityResult(id,mode, data.toList().map {
                Uri.parse(it)
            })
        } else {
            null
        }
    }
}