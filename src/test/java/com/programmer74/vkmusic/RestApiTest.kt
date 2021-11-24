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
    val ra = RestApi("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36")
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
    RestApi("").downloadFile(url, file)
    //then
    val content = Files.readAllLines(file.toPath()).joinToString(" ")
    assertThat(content).contains("lex.yy.c")
  }
}