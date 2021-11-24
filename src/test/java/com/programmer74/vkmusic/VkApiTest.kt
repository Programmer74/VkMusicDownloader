package com.programmer74.vkmusic

import mu.KLogging

class VkApiTest {
  companion object : KLogging() {
    @JvmStatic
    fun main(args: Array<String>) {
      val token = args[0]
      val secret = args[1]
      val userId = args[2].toInt()
      val gw = VkApiGateway(token, secret)
      val audios = gw.getAudios(userId)
      logger.warn { "Got ${audios.size} tracks, cool!" }
    }
  }
}