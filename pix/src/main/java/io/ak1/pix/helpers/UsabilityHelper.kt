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
import io.ak1.pix.models.*
import io.ak1.pix.PixFragment
import io.ak1.pix.ui.camera.CameraActivityContract
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
        var responseStatus: Status = Status.SUCCESS,
        var mode: Mode = Mode.Picture
    ) : Parcelable {
        @IgnoredOnParcel
        val data: List<Uri> = results

        @IgnoredOnParcel
        val status: Status = responseStatus

        @IgnoredOnParcel
        val cameraMode: Mode = mode
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

/**
 * Call this method in on create for Activity and onCreateView/onViewCreated in Fragment
 * This method registers the launcher in the activity to receive the result.
 * */

fun AppCompatActivity.registerPixCamera(
    options: Options?,
    resultCallback: ((CameraActivityContract.CameraActivityResult) -> Unit)? = null
)
        : ActivityResultLauncher<String> {
    setOptionsParam(options)
    return registerForActivityResult(CameraActivityContract()) { result ->
        result?.let {
            resultCallback?.invoke(it)
        }
    }
}

private var options: Options? = null
fun setOptionsParam(customOptions: Options?) {
    options = customOptions
}
fun getOptionsParams() = options

/**
 * Launch the ActivityResultLauncher registered in Activity.
 * */
fun ActivityResultLauncher<String>.launchPixCamera(input: String) {
    this.launch(input)
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
// TODO: 18/06/21 more usability methods to be added
// TODO: 18/06/21 add documentation for usability methods