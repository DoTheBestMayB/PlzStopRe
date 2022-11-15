package com.stop.remote.model.nearplace

import com.squareup.moshi.JsonClass
import com.stop.model.nearplace.Place
import com.stop.model.nearplace.RoadAddress

@JsonClass(generateAdapter = true)
data class Poi(
    val bizName: String,
    val collectionType: String,
    val dataKind: String,
    val desc: String,
    val detailAddrName: String,
    val detailBizName: String,
    val detailInfoFlag: String,
    val evChargers: EvChargers,
    val firstBuildNo: String,
    val firstNo: String,
    val frontLat: String,
    val frontLon: String,
    val id: String,
    val lowerAddrName: String,
    val lowerBizName: String,
    val middleAddrName: String,
    val middleBizName: String,
    val mlClass: String,
    val name: String,
    val navSeq: String,
    val newAddressList: NewAddressList,
    val noorLat: String,
    val noorLon: String,
    val parkFlag: String,
    val pkey: String,
    val radius: String,
    val roadName: String,
    val rpFlag: String,
    val secondBuildNo: String,
    val secondNo: String,
    val telNo: String,
    val upperAddrName: String,
    val upperBizName: String,
    val zipCode: String
) {

    fun toRepositoryModel(): Place {
        val roadAddressList = newAddressList.newAddress.map {
            RoadAddress(
                it.bldNo1,
                it.bldNo2,
                it.centerLat.toFloat(),
                it.centerLon.toFloat(),
                it.frontLat.toFloat(),
                it.frontLon.toFloat(),
                it.fullAddressRoad,
                it.roadId,
                it.roadName
            )
        }

        return Place(name, roadAddressList)
    }

}