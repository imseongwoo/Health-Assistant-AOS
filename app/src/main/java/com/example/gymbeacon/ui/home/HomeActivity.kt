package com.example.gymbeacon.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityHomeBinding
import com.example.gymbeacon.ui.home.fragment.NaviCalendarFragment
import com.example.gymbeacon.ui.home.fragment.NaviHomeFragment
import com.example.gymbeacon.ui.home.fragment.NaviMyPageFragment
import com.example.gymbeacon.ui.home.fragment.NaviSettingFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val naviFragmentHome by lazy { NaviHomeFragment.newInstance() }
    private val naviFragmentSetting by lazy { NaviSettingFragment.newInstance() }
    private val naviFragmentMyPage by lazy { NaviMyPageFragment.newInstance() }
    private val naviCalendarFragment by lazy { NaviCalendarFragment.newInstance() }
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)
        binding.lifecycleOwner = this
        auth = FirebaseAuth.getInstance()
        Log.e("info", auth!!.currentUser?.uid.toString())
        initNavigationBar()
    }

    private fun initNavigationBar() {
        binding.navigationView.run {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.homeItem -> {
                        changeFragment(naviFragmentHome)
                    }
                    R.id.myPageItem -> {
                        changeFragment(naviFragmentMyPage)
                    }
                    R.id.calendarItem -> {
                        changeFragment(naviCalendarFragment)
                    }
                    R.id.settingItem -> {
                        changeFragment(naviFragmentSetting)
                    }
                }
                true
            }
            selectedItemId = R.id.homeItem
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.framgent_container_view, fragment)
            .commit()
    }
}