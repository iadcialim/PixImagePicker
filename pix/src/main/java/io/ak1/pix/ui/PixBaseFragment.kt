package io.ak1.pix.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.ak1.pix.helpers.setupScreen

open class PixBaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().let {
            it.setupScreen()
            it.actionBar?.hide()
        }
    }
}