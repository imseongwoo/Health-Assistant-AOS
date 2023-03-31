package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding
import com.example.gymbeacon.ui.home.LowerBodyCategoryActivity
import com.example.gymbeacon.ui.home.UpperBodyCategoryActivity

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
            textViewUpperBody.setOnClickListener {
                goToUpperBodyCategoryActivity()
            }
            textViewLowerBody.setOnClickListener {
                goToLowerBodyCategoryActivity()
            }
        }

    }

    fun goToUpperBodyCategoryActivity() {
        Intent(activity, UpperBodyCategoryActivity::class.java).also { startActivity(it) }
    }

    fun goToLowerBodyCategoryActivity() {
        Intent(activity, LowerBodyCategoryActivity::class.java).also { startActivity(it) }
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