package com.example.gymbeacon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.model.HealthEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NaviMyPageRepository {
    private val exerciseCountMap = mutableMapOf<String, Int>()

    fun getDatabaseData(nowTimeStamp: String) : LiveData<MutableList<HealthEntity>> {

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

    private fun setExerciseCountMap(map: MutableMap<String, Int>) {
        for ((exercise,count) in map) {
            exerciseCountMap[exercise] = count
        }
    }

    fun getExerciseCountMap(): MutableMap<String, Int> {
        return exerciseCountMap
    }
}