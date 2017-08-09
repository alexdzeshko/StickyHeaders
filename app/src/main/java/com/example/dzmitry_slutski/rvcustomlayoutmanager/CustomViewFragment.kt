package com.example.dzmitry_slutski.rvcustomlayoutmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dzmitry_slutski.rvcustomlayoutmanager.view.HeadersAdapter
import kotlinx.android.synthetic.main.fragment_frame_plus_recycler.*

class CustomViewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = inflater?.inflate(R.layout.fragment_frame_plus_recycler, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.setAdapter(HeadersAdapter())
    }

}