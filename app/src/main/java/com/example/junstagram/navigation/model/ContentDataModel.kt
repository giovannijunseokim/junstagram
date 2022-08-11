package com.example.junstagram.navigation.model

data class ContentDataModel (var explain : String? = null,
                             var imageUri : String? = null,
                             var uid : String? = null,
                             var userId : String? = null,
                             var timeStamp : Long?=null,
                             var favoriteCount : Int = 0,
                             var favorites : Map<String, Boolean> = HashMap()) {
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment: String? = null,
                       var timeStamp: Long? = null)
}
