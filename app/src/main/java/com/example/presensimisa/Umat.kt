package com.example.presensimisa

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Umat(
    var id: Int? = 0,
    var nama: String? = "",
    var umur: Int? = 0,
    var wilayah: String? = "",
)