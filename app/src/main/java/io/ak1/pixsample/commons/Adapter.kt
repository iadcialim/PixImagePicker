package io.ak1.pixsample.commons

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.getMimeType
import io.ak1.pix.utility.WIDTH
import io.ak1.pixsample.R
import io.ak1.pixsample.databinding.ItemImageVideoBinding

/**
 * Created By Akshay Sharma on 20,June,2021
 * https://ak1.io
 */

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    val list = ArrayList<Uri>()
    private val options: RequestOptions =
        RequestOptions().override(350).transform(CenterCrop(), RoundedCorners(40))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_video, parent, false)
        view?.apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 10, 10, 10)
                val size = (WIDTH / 3) - 20
                height = size
                width = size
            }
        }
        return ViewHolder(
            DataBindingUtil.bind(view) ?: DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_image_video, parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = list[position]
        holder.binding.imageVideoThumbnail.apply {
            Glide.with(holder.binding.imageVideoThumbnail.context).asBitmap()
                .load(uri)
                .apply(options)
                .into(this)
        }
        val mimeType = holder.binding.imageVideoThumbnail.context?.getMimeType(uri)
        if (mimeType == PixEventCallback.CameraMode.PICTURE) {
            holder.binding.indicator.setImageResource(R.drawable.ic_camera)
        } else {
            holder.binding.indicator.setImageResource(R.drawable.ic_video)
        }
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(val binding: ItemImageVideoBinding) :
        RecyclerView.ViewHolder(binding.root)
}