package com.stop.domain.model.route.tmap.custom

sealed interface Route {
    val distance: Double
    val end: Place
    val mode: MoveType
    val sectionTime: Double
    val proportionOfSectionTime: Float
    val start: Place
}