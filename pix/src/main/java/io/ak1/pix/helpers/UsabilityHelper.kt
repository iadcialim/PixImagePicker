package io.ak1.pix.helpers


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import io.ak1.pix.PixActivityContract
import io.ak1.pix.models.*
import io.ak1.pix.PixFragment
import io.ak1.pix.utility.ARG_PARAM_PIX
import io.ak1.pix.utility.ARG_PARAM_PIX_KEY
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.*
import kotlin.collections.ArrayList


/**
 * Created By Akshay Sharma on 17,June,2021
 * https://ak1.io
 */

open class PixEventCallback {

    enum class Status {
        SUCCESS, BACK_PRESSED
    }

    enum class CameraMode {
        PICTURE, VIDEO
    }

    @SuppressLint("ParcelCreator")
    @Parcelize
    class Results(
        var results: List<Uri> = ArrayList(),
        var responseStatus: Status = Status.SUCCESS
    ) : Parcelable {
        @IgnoredOnParcel
        val data: List<Uri> = results

        @IgnoredOnParcel
        val status: Status = responseStatus
    }

    private val backPressedEvents = MutableSharedFlow<Any>()
    private val outputEvents = MutableSharedFlow<Results>()

    fun onBackPressedEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            backPressedEvents.emit(Any())
        }

    }


    suspend fun on(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        handler: suspend (Any) -> Unit
    ) = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
        backPressedEvents.asSharedFlow().collect {
            handler(it)
        }
    }

    fun returnObjects(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        event: Results
    ) = coroutineScope.launch {
        outputEvents.emit(event)
    }


    fun results(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        handler: suspend (Results) -> Unit
    ) = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
        outputEvents.asSharedFlow().collect { handler(it) }
    }
}

object PixBus : PixEventCallback()


fun AppCompatActivity.addPixToActivity(
    containerId: Int,
    options: Options?,
    resultCallback: ((PixEventCallback.Results) -> Unit)? = null
) {
    supportFragmentManager.beginTransaction()
        .replace(containerId, PixFragment(resultCallback).apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM_PIX, options)
            }
        }).commit()
}

fun pixFragment(
    options: Options,
    resultCallback: ((PixEventCallback.Results) -> Unit)? = null
): PixFragment {

    return PixFragment(resultCallback).apply {
        arguments = Bundle().apply {
            putParcelable(ARG_PARAM_PIX, options)
        }
    }
}

fun FragmentManager.resetMedia(preSelectedUrls: ArrayList<Uri> = ArrayList()) {
    setFragmentResult(
        ARG_PARAM_PIX_KEY,
        bundleOf(ARG_PARAM_PIX to if (preSelectedUrls.isEmpty()) null else Options().apply {
            this.preSelectedUrls.apply {
                clear()
                addAll(preSelectedUrls)
            }
        })
    )

}

/**
 * delete images from internal as well as external storage
 * */
fun Context.deleteImage(
    deletedImageUri: Uri,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) = coroutineScope.launch {
    deleteFile(deletedImageUri.pathSegments.last())
    contentResolver.delete(deletedImageUri, null, null)
}

/**
 * Check the mimetype of the data from uri
 * Below are the valid type for images.
 * */
private val imageMimeTypes = arrayOf(
    "image/jpeg",
    "image/bmp",
    "image/gif",
    "image/jpg",
    "image/png"
)

fun Context.getMimeType(uri: Uri): PixEventCallback.CameraMode {
    return if (imageMimeTypes.contains(contentResolver.getType(uri)))
        PixEventCallback.CameraMode.PICTURE
    else
        PixEventCallback.CameraMode.VIDEO
}

// TODO: 18/06/21 more usability methods to be added
// TODO: 18/06/21 add documentation for usability methods