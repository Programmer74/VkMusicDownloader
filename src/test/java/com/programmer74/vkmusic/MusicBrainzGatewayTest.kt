package com.programmer74.vkmusic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MusicBrainzGatewayTest {
  @Test
  @Disabled("manual use only")
  fun `can retrieve info about track`() {
    //given
    val te = MusicBrainzGateway(RestApi())
    //when
    val ans = te.tryGetAlbumInfo("Rammstein", "Du Hast")!!
    //then
    assertThat(ans.track).isEqualTo("5")
    assertThat(ans.album).isEqualTo("Reise, Reise")
    assertThat(ans.cover).isNotEmpty
  }
}