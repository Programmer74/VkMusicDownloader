package com.programmer74.vkmusic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LyricsRetrieverTest {

  @Test
  @Disabled("manual use only")
  fun `can retrieve lyrics`() {
    //given
    val lr =
        LyricsRetriever(RestApi("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36"))
    //when
    val ans = lr.getLyrics("Linkin Park", "In The End")
    //then
    assertThat(ans).contains("It starts with one")
  }
}