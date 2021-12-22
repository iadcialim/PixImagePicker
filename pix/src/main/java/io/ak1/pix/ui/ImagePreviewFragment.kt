package io.ak1.pix.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import io.ak1.pix.R
import io.ak1.pix.databinding.FragmentImagePreviewBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.deleteImage
import io.ak1.pix.models.Mode
import io.ak1.pix.ui.image_pager.ImagePagerAdapter
import io.ak1.pix.utility.ARG_PARAM_PIX
import io.ak1.pix.utility.IMG_PICKER

/**
 *
 * Created by Pritam Dasgupta on 17th December, 2021
 *
 * show fragment with image(s) if showPreview from option model is passed as true
 */

class ImagePreviewFragment : PixBaseFragment() {

    private var fragmentImageBinding: FragmentImagePreviewBinding? = null

    //model to fetch the image(s) from arguments
    private var uriList: PixEventCallback.Results? = null

    //observer to check the image resource callback status
    private var loaderLD: MutableLiveData<Boolean> = MutableLiveData(false)
    private var imagePickerOption = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uriList = arguments?.getParcelable(ARG_PARAM_PIX) ?: PixEventCallback.Results()
        imagePickerOption = arguments?.getInt(IMG_PICKER) ?: 2
        fragmentImageBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_image_preview, container, false)
        return fragmentImageBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentImageBinding?.cancelBtn?.setOnClickListener {
            //if images are not required/accepted, deleting images from internal as well as external storage.
            if (imagePickerOption == 1) {
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
            (activity as? PixActivity)?.navController?.navigateUp()
        }

        fragmentImageBinding?.doneBtn?.setOnClickListener {
            //sending the model to the PixActivity to collect the data.
            uriList?.apply {
                PixBus.returnObjects(
                    event = PixEventCallback.Results(
                        this.data,
                        PixEventCallback.Status.SUCCESS,
                        Mode.Picture
                    )
                )
            }
        }
        loaderLD.observe(viewLifecycleOwner, {
            it?.apply {
                fragmentImageBinding?.cancelBtn?.visibility = if (this) View.VISIBLE else View.GONE
                fragmentImageBinding?.doneBtn?.visibility = if (this) View.VISIBLE else View.GONE
            }
        })
        fragmentImageBinding?.imagePager?.adapter = ImagePagerAdapter(
            requireContext(),
            uriList?.data ?: arrayListOf(), loaderLD
        )
    }
}
