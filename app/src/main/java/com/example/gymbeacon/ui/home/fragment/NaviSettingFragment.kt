package com.example.gymbeacon.ui.home.fragment

import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviSettingBinding
import com.example.gymbeacon.ui.dialog.LogoutCustomDialog
import com.google.firebase.auth.FirebaseAuth

class NaviSettingFragment : Fragment() {
    lateinit var binding: FragmentNaviSettingBinding
    val logoutDialog = LogoutCustomDialog()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navi_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textViewLogOut.setOnClickListener {
                logoutDialog.show(childFragmentManager,"LogoutDialog")
            }
        }
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