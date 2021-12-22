package io.ak1.pix.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.ak1.pix.R
import io.ak1.pix.databinding.FragmentVideoPreviewBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.deleteImage
import io.ak1.pix.models.Mode
import io.ak1.pix.utility.ARG_PARAM_PIX

/**
 *
 * Created by Pritam Dasgupta on 22nd December, 2021
 *
 * show fragment with video if showPreview from option model is passed as true
 */

class VideoPreviewFragment : PixBaseFragment() {

    private var fragmentVideoBinding: FragmentVideoPreviewBinding? = null
    //model to fetch the video from arguments
    private var uriList: PixEventCallback.Results? = null
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uriList = arguments?.getParcelable(ARG_PARAM_PIX) ?: PixEventCallback.Results()
        fragmentVideoBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_video_preview, container, false)
        return fragmentVideoBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentVideoBinding?.cancelBtn?.setOnClickListener {
            exoPlayer?.stop()
            val data = uriList?.data
            data?.apply {
                if (this.size == 1) {
                    (activity as? PixActivity)?.deleteImage(this[0])
                } else {
                    this.forEach {
                        requireActivity().deleteImage(it)
                    }
                }
            }
            (activity as? PixActivity)?.navController?.navigateUp()
        }

        fragmentVideoBinding?.doneBtn?.setOnClickListener {
            //sending the model to the PixActivity to collect the data.
            uriList?.apply {
                PixBus.returnObjects(
                    event = PixEventCallback.Results(
                        this.data,
                        PixEventCallback.Status.SUCCESS,
                        Mode.Video
                    )
                )
            }
        }
        if (uriList?.data?.isNotEmpty() == true) {
            initializePlayer()
        } else {
            (activity as? PixActivity)?.navController?.navigateUp()
        }
    }

    private fun initializePlayer() {
        val mediaDataSourceFactory = DefaultDataSourceFactory(requireContext(), Util.getUserAgent(requireContext(),
            "pixMediaPlayer"))

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            MediaItem.fromUri(uriList!!.data[0]))

        val trackSelector = DefaultTrackSelector(requireContext())
        val loadControl = DefaultLoadControl()

        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)
        exoPlayer = SimpleExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
        exoPlayer?.addMediaSource(mediaSource)
        exoPlayer?.playWhenReady = true
        fragmentVideoBinding?.playerView?.player = exoPlayer
        fragmentVideoBinding?.playerView?.requestFocus()
    }

    private fun releasePlayer() {
        exoPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) releasePlayer()
    }

}