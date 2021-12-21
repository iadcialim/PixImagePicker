package io.ak1.pix.ui.image_pager

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

internal class ImagePagerAdapter(
    private val context: Context,
    private val images: List<Uri>,
    private val loaderLD: MutableLiveData<Boolean>
) : PagerAdapter() {

    // Layout Inflater
    private var mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        // return the number of images
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView: View = mLayoutInflater.inflate(R.layout.item_photo_viewer, container, false)
        val previewImg: PhotoView = itemView.findViewById(R.id.previewImg) as PhotoView
        val drawable = CircularProgressDrawable(context)
        drawable.colorFilter = PorterDuffColorFilter(0xff3CE330.toInt(), PorterDuff.Mode.SRC_IN)
        drawable.centerRadius = 100f
        drawable.strokeWidth = 25f
        drawable.start()
        // setting the image in the imageView
        Glide.with(context)
            .asBitmap().load(images[position])
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
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}