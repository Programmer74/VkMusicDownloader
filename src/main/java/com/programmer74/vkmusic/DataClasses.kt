package com.programmer74.vkmusic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class VkApiResponseWrapper<T>(
  var response: VkApiResponse<T>? = null,
  var error: Map<Any, Any>? = null
)

data class VkApiResponse<T>(
  var count: Int? = null,
  var items: List<T>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkAudio(
  var id: Int? = null,
  var owner_id: Int? = null,
  var artist: String? = null,
  var title: String? = null,
  var duration: String? = null,
  var url: String? = null,
  var lyrics_id: Int? = null,
  var genre_id: Int? = null
)