package com.example.dzmitry_slutski.rvcustomlayoutmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShowCustomLayout.setOnClickListener {
            showFragment(1)
        }

        btnShowCustomView.setOnClickListener {
            showFragment(2)
        }

    }

    private fun showFragment(id: Int) {
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag("$id")
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment == null) {
            when (id) {
                1 -> fragment = CustomLayoutManagerFragment()
                2 -> fragment = CustomViewFragment()
            }
            transaction.add(R.id.content_container, fragment, "$id")
        } else {
            transaction.show(fragment)
        }
        supportFragmentManager.fragments.forEach {
            if (it.tag != "$id") transaction.hide(it)
        }
        transaction.commit()
    }

}
