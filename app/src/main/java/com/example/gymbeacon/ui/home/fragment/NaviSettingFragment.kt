package com.example.gymbeacon.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R

class NaviSettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navi_setting,container,false)
    }

    companion object {
        fun newInstance(): NaviSettingFragment {
            val args = Bundle().apply {
            }
            val fragment = NaviSettingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}