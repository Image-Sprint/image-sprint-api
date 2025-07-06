package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.output.webhook.WebhookUrlValidator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class TestWebhookUrlServiceTest {
    private lateinit var validator: WebhookUrlValidator
    private lateinit var testWebhookUrlService: TestWebhookUrlService

    @BeforeEach
    fun setUp() {
        validator = mockk()
        testWebhookUrlService = TestWebhookUrlService(validator)
    }

    @Test
    fun `단위 - 올바른 URL일 경우 예외 없이 성공한다`() {
        // given
        val type = WebhookType.SLACK
        val url = "https://hooks.slack.com/services/test"
        every { validator.validate(type, url) } returns Unit

        // when
        testWebhookUrlService.test(type, url)

        // then
        verify(exactly = 1) { validator.validate(type, url) }
    }

    @Test
    fun `단위 - 유효하지 않은 URL이면 예외가 발생한다`() {
        // given
        val type = WebhookType.DISCORD
        val url = "invalid-url"
        every { validator.validate(type, url) } throws CustomException(ErrorCode.WEBHOOK_NOT_FOUND)

        // when & then
        assertThatThrownBy {
            testWebhookUrlService.test(type, url)
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WEBHOOK_NOT_FOUND)

        verify(exactly = 1) { validator.validate(type, url) }
    }
}
