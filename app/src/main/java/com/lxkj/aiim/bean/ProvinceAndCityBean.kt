package com.lxkj.aiim.bean


class ProvinceAndCityBean {
    var code = 0
    var msg: String? = null
    var data: ArrayList<ProvinceBean>? = null

    class ProvinceBean {
        var id = 0
        var parentId = 0
        var regionName: String? = null
        var children: ArrayList<CityBean>? = null

        class CityBean {
            var id = 0
            var parentId = 0
            var regionName: String? = null
            var children: ArrayList<AreaBean>? = null

            class AreaBean {
                var id = 0
                var parentId = 0
                var regionName: String? = null
                var adcode: String? = null
            }
        }
    }
}