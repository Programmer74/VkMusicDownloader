package com.programmer74.vkmusic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Files

class RestApiTest {

  @Test
  @Disabled("manual use only")
  fun `can perform get rq`() {
    //given
    val ra = RestApi()
    //when
    val html = ra.sendGet("https://github.com/nikita-tomilov")
    //then
    assertThat(html).contains("Nikita Tomilov")
  }

  @Test
  @Disabled("manual use only")
  fun `can download file`() {
    //given
    val url = "https://raw.githubusercontent.com/nikita-tomilov/hawk/master/Makefile"
    val file = Files.createTempFile("test", "").toFile()
    //when
    RestApi().downloadFile(url, file)
    //then
    val content = Files.readAllLines(file.toPath()).joinToString(" ")
    assertThat(content).contains("lex.yy.c")
  }

  @Test
  @Disabled("manual use only")
  fun `can download file with support for redirects`() {
    //given
    val url = "http://coverartarchive.org/release/f3af0dc4-2959-438e-8b6a-73bb0139e06d/30937134031.jpg"
    //when
    val bytes = RestApi().downloadFile(url)
    //then
    assertThat(bytes).hasSize(161457)
  }
}