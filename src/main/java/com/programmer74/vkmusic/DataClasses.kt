package com.programmer74.vkmusic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import feign.RequestLine

interface VkApi {
  @RequestLine("POST /method/audio.get")
  fun getAudios(body: String): VkApiResponseWrapper<VkApiResponseList<VkAudio>>

  @RequestLine("POST /method/audio.getLyrics")
  fun getLyrics(body: String): VkApiResponseWrapper<VkLyrics>
}

data class VkApiResponseList<T>(
  var count: Int? = null,
  var items: List<T>? = null
)

data class VkApiResponseWrapper<T>(
  var response: T? = null,
  var error: Map<Any, Any>? = null
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkLyrics(
  var lyrics_id: Long? = null,
  var text: String? = null
)