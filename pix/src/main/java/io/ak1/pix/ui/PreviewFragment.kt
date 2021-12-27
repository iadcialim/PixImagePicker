package io.ak1.pix.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import io.ak1.pix.R
import io.ak1.pix.databinding.FragmentPreviewBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.deleteImage
import io.ak1.pix.ui.image_video_pager.ImageVideoPagerAdapter
import io.ak1.pix.utility.ARG_PARAM_PIX
import io.ak1.pix.utility.IMG_PICKER

/**
 * Created by Pritam Dasgupta on 27th December, 2021
 * */

class PreviewFragment: PixBaseFragment() {
    private var fragmentPreviewBinding: FragmentPreviewBinding? = null
    //model to fetch the image(s) from arguments
    private var uriList: PixEventCallback.Results? = null
    private var isCameraClicked = false

    //observer to check the image resource callback status
    private var loaderLD: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uriList = arguments?.getParcelable(ARG_PARAM_PIX) ?: PixEventCallback.Results()
        isCameraClicked = arguments?.getInt(IMG_PICKER) == 1 || arguments?.getInt(IMG_PICKER) == 3
        fragmentPreviewBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_preview, container, false)
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isCameraClicked) {
                        deleteImagesVideos()
                    }
                    (activity as? PixActivity)?.navController?.navigateUp()
                }
            })
        return fragmentPreviewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentPreviewBinding?.cancelBtn?.setOnClickListener {
            //if images are not required/accepted, deleting images from internal as well as external storage.
            if (isCameraClicked) {
                deleteImagesVideos()
            }
            (activity as? PixActivity)?.navController?.navigateUp()
        }

        fragmentPreviewBinding?.doneBtn?.setOnClickListener {
            //sending the model to the PixActivity to collect the data.
            uriList?.apply {
                PixBus.returnObjects(
                    event = PixEventCallback.Results(
                        this.data,
                        PixEventCallback.Status.SUCCESS
                    )
                )
            }
        }
        loaderLD.observe(viewLifecycleOwner, {
            it?.apply {
                fragmentPreviewBinding?.cancelBtn?.visibility = if (this) View.VISIBLE else View.GONE
                fragmentPreviewBinding?.doneBtn?.visibility = if (this) View.VISIBLE else View.GONE
            }
        })
        fragmentPreviewBinding?.imageVideoPager?.adapter = ImageVideoPagerAdapter(
            requireContext(),
            uriList?.data ?: arrayListOf(), loaderLD
        )
    }

    private fun deleteImagesVideos() {
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
    }
}