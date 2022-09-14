package io.ak1.pix.preview.pager

import android.view.ViewGroup
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import io.ak1.pix.R
import java.util.*
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.getMimeType

internal class ImageVideoPagerAdapter(
    private val context: Context,
    private val imagesVideosList: List<Uri>,
    private val loaderLD: MutableLiveData<Boolean>
) : PagerAdapter() {

    private var exoPlayer: SimpleExoPlayer? = null

    // Layout Inflater
    private var mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        // return the number of images
        return imagesVideosList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mimeType = context.getMimeType(imagesVideosList[position])
        val itemView: View
        if (mimeType == PixEventCallback.CameraMode.PICTURE) {
            itemView = mLayoutInflater.inflate(R.layout.item_photo_viewer, container, false)
            loadImageItemView(itemView, container, position)
        } else {
            itemView = mLayoutInflater.inflate(R.layout.item_video_viewer, container, false)
            loadVideoItemView(itemView, container, position)
        }
        return itemView
    }

    private fun loadVideoItemView(itemView: View, container: ViewGroup, position: Int) {
        exoPlayer = initializePlayer(position)
        val playerView: StyledPlayerView = itemView.findViewById(R.id.playerView)
        playerView.player = exoPlayer
        playerView.requestFocus()
        loaderLD.postValue(true)
        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
    }

    private fun initializePlayer(position: Int): SimpleExoPlayer {
        val mediaDataSourceFactory = DefaultDataSourceFactory(
            context, Util.getUserAgent(
                context,
                "pixMediaPlayer"
            )
        )

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            MediaItem.fromUri(imagesVideosList[position])
        )

        val trackSelector = DefaultTrackSelector(context)
        val loadControl = DefaultLoadControl()

        val mediaSourceFactory: MediaSourceFactory =
            DefaultMediaSourceFactory(mediaDataSourceFactory)
        val exoPlayer = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
        exoPlayer.addMediaSource(mediaSource)
        exoPlayer.playWhenReady = true
        return exoPlayer
    }

    private fun loadImageItemView(itemView: View, container: ViewGroup, position: Int) {
        val previewImg: PhotoView = itemView.findViewById(R.id.previewImg) as PhotoView
        val drawable = CircularProgressDrawable(context)
        drawable.colorFilter = PorterDuffColorFilter(0xff3CE330.toInt(), PorterDuff.Mode.SRC_IN)
        drawable.centerRadius = 100f
        drawable.strokeWidth = 25f
        drawable.start()
        // setting the image in the imageView
        Glide.with(context)
            .asBitmap().load(imagesVideosList[position])
            .placeholder(drawable)
            .error(R.drawable.ic_error)
            .into(object :
                CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    loaderLD.postValue(false)
                    previewImg.setImageDrawable(placeholder)
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    loaderLD.postValue(false)
                    previewImg.setImageDrawable(placeholder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    loaderLD.postValue(true)
                    previewImg.setImageDrawable(errorDrawable)
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    loaderLD.postValue(true)
                    previewImg.setImageBitmap(resource)
                }
            })
        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
        exoPlayer?.release()
    }
}