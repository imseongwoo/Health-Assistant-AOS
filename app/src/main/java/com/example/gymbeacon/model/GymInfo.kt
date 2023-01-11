package com.example.gymbeacon.model

import com.google.gson.annotations.SerializedName

data class GymInfo(
    @SerializedName("gym_name")val gymName: String,
    @SerializedName("current_user")val currentUser: String
)
