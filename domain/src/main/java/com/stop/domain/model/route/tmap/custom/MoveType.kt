package com.stop.domain.model.route.tmap.custom

enum class MoveType {
    WALK, BUS, SUBWAY, EXPRESSBUS, TRAIN, AIRPLANE, FERRY, TRANSFER;

    companion object {
        private const val NO_MATCHING_VALUE = "해당 변수 값이 없습니다."

        fun getMoveTypeByName(name: String): MoveType {
            return entries.firstOrNull {
                    it.name == name
                } ?: throw IllegalArgumentException(NO_MATCHING_VALUE)
        }
    }
}