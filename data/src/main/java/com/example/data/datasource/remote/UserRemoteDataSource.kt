package com.example.data.datasource.remote

import com.example.data.datasource.UserDataSource
import com.example.domain.auth.CommonUtil
import com.example.domain.auth.FirebaseAuthManager
import com.example.domain.base.RespResult
import com.example.domain.model.HealthEntity
import com.example.domain.model.LoginResult
import com.example.domain.model.NaviHomeEntity
import com.example.domain.model.SignUpResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserRemoteDataSource : UserDataSource {
    override suspend fun postLogin(
        account: String,
        password: String,
        onResult: (LoginResult) -> Unit,
    ) {
        FirebaseAuthManager.signinEmail(account, password) { result ->
            if (result.isSuccess) {
                onResult(LoginResult(isSuccess = true))
            } else {
                onResult(LoginResult(errorMessage = "로그인 실패", isSuccess = false))
            }

        }
    }

    override suspend fun signUp(
        account: String,
        password: String,
        onResult: (SignUpResult) -> Unit,
    ) {
        FirebaseAuthManager.signUp(account, password) { result ->
            if (result.isSuccess) {
                onResult(SignUpResult(isSuccess = true))
            } else {
                onResult(SignUpResult(errorMessage = result.errorMessage, isSuccess = false))
            }

        }
    }

    override suspend fun getTrainingData(onResult: (NaviHomeEntity) -> Unit) {
        var countsLower = 0
        var countsChest = 0
        var countsBack = 0
        val entityArrayList = ArrayList<HealthEntity>()

        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (shot in snapshot.children) {
                        val uid = shot.child("uid").getValue(String::class.java)
                        val date = shot.child("timestamp").getValue(String::class.java)
                        val count = shot.child("count").getValue(String::class.java)
                        val exercise = shot.child("exercise").getValue(String::class.java)
                        entityArrayList.add(HealthEntity(uid, date, count, exercise))

                        when (exercise) {
                            "스쿼트", "레그 익스텐션", "데드리프트" -> countsLower += count?.toInt() ?: 0
                            "벤치프레스", "인클라인 벤치프레스" -> countsChest += count?.toInt() ?: 0
                            "랫 풀 다운" -> countsBack += count?.toInt() ?: 0
                        }
                        onResult(NaviHomeEntity(countsLower,
                            countsChest,
                            countsBack,
                            entityArrayList,
                            isSuccess = true))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    entityArrayList.add(HealthEntity("uid", "timestamp", "count", "exercise"))
                    onResult(NaviHomeEntity(entityArrayList = entityArrayList, isSuccess = false))
                }
            })
    }

}