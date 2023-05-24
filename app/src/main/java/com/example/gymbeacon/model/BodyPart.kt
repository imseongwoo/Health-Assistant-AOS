package com.example.gymbeacon.model

enum class BodyPart(val position: Int) {
                                // y x 신뢰도 순
    NOSE(0),            // 0 1 2
    LEFT_EYE(1),        // 3 4 5
    RIGHT_EYE(2),       // 6 7 8
    LEFT_EAR(3),        // 9 10 11
    RIGHT_EAR(4),       // 12 13 14
    LEFT_SHOULDER(5),   // 15 16 17
    RIGHT_SHOULDER(6),  // 18 19 20
    LEFT_ELBOW(7),      // 21 22 23
    RIGHT_ELBOW(8),     // 24 25 26
    LEFT_WRIST(9),      // 27 28 29
    RIGHT_WRIST(10),    // 30 31 32
    LEFT_HIP(11),       // 33 34 35
    RIGHT_HIP(12),      // 36 37 38
    LEFT_KNEE(13),      // 39 40 41
    RIGHT_KNEE(14),     // 42 43 44
    LEFT_ANKLE(15),     // 45 46 47
    RIGHT_ANKLE(16);    // 48 49 50
    companion object{
        private val map = values().associateBy(BodyPart::position)
        fun fromInt(position: Int): BodyPart = map.getValue(position)
    }
}