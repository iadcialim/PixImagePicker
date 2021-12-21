package io.ak1.pix.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import io.ak1.pix.R
import io.ak1.pix.databinding.DialogImagePreviewBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.deleteImage
import io.ak1.pix.ui.image_pager.ImagePagerAdapter
import io.ak1.pix.utility.ARG_PARAM_PIX

/**
 *
 * Created by Pritam Dasgupta on 17th December, 2021
 *
 * show fragment with image(s) if showImagePreview from option model is passed as true
 */

class ImagePreviewFragment : PixBaseFragment() {

    private var dialogImageBinding: DialogImagePreviewBinding? = null
    //model to fetch the image(s) from arguments
    private var uriList: PixEventCallback.Results? = null
    //observer to check the image resource callback status
    private var loaderLD: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uriList = arguments?.getParcelable(ARG_PARAM_PIX) ?: PixEventCallback.Results()
        dialogImageBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_image_preview, container, false)
        return dialogImageBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogImageBinding?.cancelBtn?.setOnClickListener {
            //if images are not required/accepted, deleting images from internal as well as external storage.
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

        dialogImageBinding?.doneBtn?.setOnClickListener {
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
                dialogImageBinding?.cancelBtn?.visibility = if (this) View.VISIBLE else View.GONE
                dialogImageBinding?.doneBtn?.visibility = if (this) View.VISIBLE else View.GONE
            }
        })
        dialogImageBinding?.imagePager?.adapter = ImagePagerAdapter(
            requireContext(),
            uriList?.data ?: arrayListOf(), loaderLD
        )
    }
}
