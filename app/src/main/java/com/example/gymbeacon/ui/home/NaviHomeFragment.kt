package com.example.gymbeacon.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding

class NaviHomeFragment : Fragment() {
    lateinit var binding: FragmentNaviHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_navi_home,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            constraintNewGym.setOnClickListener {
                goToNewGymDetailActivity()
            }
            constraintLayoutYeasolGym.setOnClickListener {
                goToYeasolGymDetailActivity()
            }
            constraintLayoutDamheonGym.setOnClickListener {
                goToDamheonGymDetailActivity()
            }
        }

    }

    fun goToNewGymDetailActivity() {
        Intent(activity,NewGymDetailActivity::class.java).also { startActivity(it) }
    }

    fun goToYeasolGymDetailActivity() {
        Intent(activity,YeasolGymDetailActivity::class.java).also { startActivity(it) }
    }

    fun goToDamheonGymDetailActivity() {
        Intent(activity,DamheonGymDetailActivity::class.java).also { startActivity(it) }
    }

    companion object {
        fun newInstance(): NaviHomeFragment {
            val args = Bundle().apply {
            }
            val fragment = NaviHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}