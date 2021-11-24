package com.programmer74.vkmusic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.jackson.JacksonDecoder
import org.apache.commons.codec.digest.DigestUtils

class VkApiGateway(
  private val token: String,
  private val secret: String
) {

  private val om = ObjectMapper().registerKotlinModule()

  private val api = Feign.builder()
      .decoder(JacksonDecoder(om))
      .requestInterceptor {
        it.header("Accept", "application/json")
        it.header("Content-Type", "application/x-www-form-urlencoded")
        it.header("User-Agent", USER_AGENT)
      }
      .target(VkApi::class.java, API_PATH)

  private fun doMethod(method: String, methodParams: Map<String, String>): VkApiResponseWrapper<*> {
    var rq = "v=5.82&lang=ru&https=1"
    methodParams.forEach { rq += "&${it.key}=${it.value}" }
    rq += "&access_token=$token"
    val sig = DigestUtils.md5Hex("/method/$method?${rq}$secret")
    rq += "&sig=$sig"
    return when (method) {
      "audio.get" -> api.getAudios(rq)
      else -> error("unknown method $method")
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun getAudios(ownerId: Int): List<VkAudio> {
    val ans = doMethod("audio.get", mapOf("owner_id" to ownerId.toString(), "count" to "5000"))
    if (ans.error != null) {
      error(ans.error!!)
    }
    return (ans.response?.items as List<VkAudio>?) ?: emptyList()
  }

  companion object {
    private const val USER_AGENT =
        "VKAndroidApp/4.13.1-1206 (Android 11; SDK 30; armeabi-v7a; realme RMX1921; ru)"
    private const val API_PATH = "https://api.vk.com"
  }
}