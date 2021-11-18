package com.programmer74.vkmusic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.codec.digest.DigestUtils

class VkApi(
  val token: String,
  val secret: String
) {
  private val USER_AGENT = "VKAndroidApp/4.38-849 (Android 6.0; SDK 23; x86; Google Nexus 5X; ru)"
  private val API_PATH = "https://api.vk.com"

  val restApi = RestApi(USER_AGENT)

  private fun doMethod(method: String, methodParams: Map<String, String>): String {
    val params = HashMap<String, String>()
    params["v"] = "5.64"
    params["lang"] = "ru"
    params["https"] = "1"
    params.putAll(methodParams)
    var rq = "/method/$method?"
      params.forEach {
      rq += it.key + "=" + it.value + "&"
    }
    rq += "access_token=$token"

    val sig = DigestUtils.md5Hex(rq + secret)

    val url = "$API_PATH$rq&sig=$sig"
    return restApi.sendGet(url)
  }

  fun getAudios(ownerId: Int): List<VkAudio> {
    val ans = doMethod("audio.get", mapOf("owner_id" to ownerId.toString()))
    val mapper = ObjectMapper().registerModule(KotlinModule())
    val audios: VkApiResponseWrapper<VkAudio> = mapper.readValue(ans)
    return audios.response!!.items!!
  }
}