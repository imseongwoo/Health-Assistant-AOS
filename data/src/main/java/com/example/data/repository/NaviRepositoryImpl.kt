package com.example.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.datasource.remote.UserRemoteDataSource
import com.example.domain.auth.CommonUtil
import com.example.domain.base.ErrorType
import com.example.domain.base.RespResult
import com.example.domain.model.HealthEntity
import com.example.domain.model.NaviHomeEntity
import com.example.domain.repository.NaviRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class NaviRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) : NaviRepository{
    private val exerciseCountMap = mutableMapOf<String, Int>()

    override fun getDatabaseData(nowTimeStamp: String) : LiveData<MutableList<HealthEntity>> {

        val mutableData = MutableLiveData<MutableList<HealthEntity>>()
        val exerciseCountMap = mutableMapOf<String, Int>()

        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid).
        addValueEventListener(object : ValueEventListener {
            val listData: MutableList<HealthEntity> = mutableListOf<HealthEntity>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val getData = userSnapshot.getValue(HealthEntity::class.java)
                        if (getData?.timestamp == nowTimeStamp) {
                            val exercise = getData.exercise.toString()
                            val count = getData.count?.toInt() ?: 0
                            val currentCount = exerciseCountMap[exercise] ?: 0
                            exerciseCountMap[exercise] = currentCount + count
                            listData.add(getData)
                        }
                        mutableData.value = listData
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        setExerciseCountMap(exerciseCountMap)
        return mutableData
    }

    override fun setExerciseCountMap(map: MutableMap<String, Int>) {
        for ((exercise,count) in map) {
            exerciseCountMap[exercise] = count
        }
    }

    override fun getExerciseCountMap(): MutableMap<String, Int> {
        return exerciseCountMap
    }

    override suspend fun getHomeWeightData(onChanged: (NaviHomeEntity) -> Unit) {
        return userRemoteDataSource.getTrainingData() { naviHomeEntity ->
            if (naviHomeEntity.isSuccess){
                onChanged(naviHomeEntity)
                //RespResult.Success(naviHomeEntity.isSuccess)
            }else {
            }
        }
    }
}