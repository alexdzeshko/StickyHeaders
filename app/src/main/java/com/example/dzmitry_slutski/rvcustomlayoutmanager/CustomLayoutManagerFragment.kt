package com.example.dzmitry_slutski.rvcustomlayoutmanager

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.dzmitry_slutski.rvcustomlayoutmanager.data.ItemsGenerator
import kotlinx.android.synthetic.main.fragment_custom_layout_manager.*


class CustomLayoutManagerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = inflater?.inflate(R.layout.fragment_custom_layout_manager, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.layoutManager = CustomLayoutManager(resources.getDimensionPixelSize(R.dimen.episode_shift))

        val adapter = RecyclerAdapter(context, ItemsGenerator(context).initList())
        recycler.adapter = adapter
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)

                outRect.bottom = 48
            }
        })

        removeButton.setOnClickListener({
            val indexToRemove = Integer.parseInt(number.getText().toString())
            adapter.removeItem(indexToRemove)
        })

        requestLayout.setOnClickListener({ recycler.requestLayout() })

        scrollDown.setOnClickListener({ recycler.scrollBy(0, -10) })
    }


    override fun onResume() {
        super.onResume()

        number.post({ hideKeyboard(number) })
    }

    fun hideKeyboard(view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        currentFocus?.windowToken?.let {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}

