package com.stop.domain.model.route.seoul.subway

/**
 *
 * @param type T MAP 대중교통 API에서 사용하는 이동수단 노선 코드
 * @param lineName 공공데이터포털에서 사용하는 호선명
 */
enum class StationType(val type: Int, val lineName: String) {
    ONE(1, "01호선"),
    TWO(2, "02호선"),
    THREE(3, "03호선"),
    FOUR(4, "04호선"),
    FIVE(5, "05호선"),
    SIX(6, "06호선"),
    SEVEN(7, "07호선"),
    EIGHT(8, "08호선"),
    NINE(9, "09호선"),
    GYEONG_GANG(112, "경강선"),
    GYEONGUI_JUNGANG(112, "경의선"), // 경의중앙선
    SHINBUNDANG(109, "신분당선");

    companion object {
        // 22년 11월 기준, 공공데이터 포털에서 1 ~ 8호선에 속한 지하철 역의 막차 시간만 제공합니다.
        val allowedStationType = hashSetOf(ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT)

        fun from(type: Int): StationType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}