package io.ak1.pix.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.ak1.pix.R
import io.ak1.pix.databinding.DialogImagePreviewBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.ui.image_pager.ImagePagerAdapter
import io.ak1.pix.utility.ARG_PARAM_PIX

/**
 *
 * Created by Pritam Dasgupta on 17th December, 2021
 *
 */

class ImagePreviewFragment : PixBaseFragment() {

    private var dialogImageBinding: DialogImagePreviewBinding? = null
    private var uriList: PixEventCallback.Results? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        uriList = arguments?.getParcelable(ARG_PARAM_PIX) ?: PixEventCallback.Results()
        dialogImageBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_image_preview, container, false)
        return dialogImageBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogImageBinding?.cancelBtn?.setOnClickListener {
            (activity as? PixActivity)?.navController?.navigateUp()
        }

        dialogImageBinding?.doneBtn?.setOnClickListener {
            uriList?.apply {
                PixBus.returnObjects(
                    event = PixEventCallback.Results(
                        this.data,
                        PixEventCallback.Status.SUCCESS
                    )
                )
            }
        }
        dialogImageBinding?.imagePager?.adapter = ImagePagerAdapter(requireContext(),
            uriList?.data ?: arrayListOf())
    }
}
