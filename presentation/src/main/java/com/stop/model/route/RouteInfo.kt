package com.stop.model.route

import androidx.annotation.DrawableRes
import com.stop.domain.model.route.tmap.custom.MoveType

data class RouteInfo(
    val mode: MoveType, // 이동 방법
    val sectionTime: Double, // 소요된 시간(초 단위)
    val proportionOfSectionTime: Float, // 전체 소요 시간 중 현재 경로가 차지하는 비율
    val typeName: String, // 해당 구간 타입 정보 ex) 지하철은 호선, 버스는 노선 번호, 도보는 하차
    val stationName: String, // 역 또는 정류소 이름
    val symbolColor: Int, // 대중교통은 해당 노선 고유 색상, 도보는 회색으로 처리
    @DrawableRes val symbolDrawableId: Int, // 이동 수단을 나타내는 리소스 아이디
)
