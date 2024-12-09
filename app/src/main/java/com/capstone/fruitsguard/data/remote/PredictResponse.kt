package com.capstone.fruitsguard.data.remote

import com.google.gson.annotations.SerializedName

data class PredictResponse(
	@field:SerializedName("hasil")
	val hasil: String? = null
)
