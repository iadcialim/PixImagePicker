package io.ak1.pixsample.samples

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.ak1.pix.helpers.setupScreen
import io.ak1.pix.helpers.showStatusBar
import io.ak1.pixsample.IMAGE_VIDEOS_URI
import io.ak1.pixsample.R
import io.ak1.pixsample.commons.Adapter
import io.ak1.pixsample.custom.fragmentBody2
import java.util.ArrayList

class PixActivitySample : AppCompatActivity() {

    private val resultsFragment = PixActivityResultsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_sample)
        setupScreen()
        supportActionBar?.hide()
        val uriList = intent.getParcelableArrayListExtra<Uri>(IMAGE_VIDEOS_URI)
        uriList?.apply {
            showResultsFragment(this)
        } ?: finish()
    }

    private fun showResultsFragment(imageUriList: ArrayList<Uri>) {
        showStatusBar()
        val bundle = Bundle()
        bundle.putParcelableArrayList(IMAGE_VIDEOS_URI, imageUriList)
        resultsFragment.arguments = bundle
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, resultsFragment).commit()
    }
}

class PixActivityResultsFragment : Fragment() {
    private val customAdapter = Adapter()

    private fun setList(list: List<Uri>) {
        customAdapter.apply {
            this.list.clear()
            this.list.addAll(list)
            notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = fragmentBody2(requireActivity(), customAdapter)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUriList = arguments?.getParcelableArrayList<Uri>(IMAGE_VIDEOS_URI)
        imageUriList?.apply {
            setList(this)
        }
    }
}