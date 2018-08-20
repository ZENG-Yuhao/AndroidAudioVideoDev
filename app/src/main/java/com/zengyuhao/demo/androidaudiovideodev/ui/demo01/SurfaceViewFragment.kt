package com.zengyuhao.demo.androidaudiovideodev.ui.demo01


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zengyuhao.demo.androidaudiovideodev.R
import kotlinx.android.synthetic.main.demo01_fragment.*

class SurfaceViewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.demo01_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txtVersion.text = "Kotlin"
    }
}
