package com.programmer74.vkmusic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LyricsRetrieverTest {

  @Test
  @Disabled("manual use only")
  fun `can retrieve lyrics`() {
    //given
    val lr = LyricsRetriever(RestApi())
    //when
    val ans = lr.getLyrics("Linkin Park", "In The End")
    //then
    assertThat(ans).contains("It starts with one")
  }
}