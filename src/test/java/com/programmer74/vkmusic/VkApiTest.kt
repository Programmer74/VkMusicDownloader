package com.programmer74.vkmusic

class VkApiTest {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val token = args[0]
      val secret = args[1]
      val userId = args[2].toInt()
      val gw = VkApiGateway(token, secret)
      val audios = gw.getAudios(userId)
      val i = 1
    }
  }
}