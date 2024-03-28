package com.stop.domain.model.route

import com.stop.domain.model.route.tmap.custom.MoveType

enum class TransportMoveType {
    BUS, SUBWAY;

    companion object {
        fun from(moveType: MoveType): TransportMoveType? {
            return when (moveType) {
                MoveType.BUS -> BUS
                MoveType.SUBWAY -> SUBWAY
                else -> null
            }
        }
    }
}