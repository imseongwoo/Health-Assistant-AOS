package com.example.gymbeacon.ui.common

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat

object CommonUtil {

    const val CHAT_DB_URL = "https://health-assistant-39e16-default-rtdb.asia-southeast1.firebasedatabase.app/"
    lateinit var CHAT_REF: DatabaseReference

    val mAuth = FirebaseAuth.getInstance()
    var database = Firebase.database
    val myRef = database.getReference("health/momentum")

    var userName = ""  // 로그인 사용자 이름

    fun getTime(timeStamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(timeStamp)
    }

    fun getUid(): String? {
        val user = mAuth.currentUser
        val uid = user?.uid
        return uid
    }

}