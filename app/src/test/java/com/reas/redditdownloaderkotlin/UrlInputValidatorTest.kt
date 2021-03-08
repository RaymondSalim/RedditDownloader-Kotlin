package com.reas.redditdownloaderkotlin
import com.google.common.truth.Truth.assertThat
import com.reas.redditdownloaderkotlin.gallery.UrlInputValidator
import org.junit.Test


class UrlInputValidatorTest {

    @Test
    fun testRegex() {
        val validator = UrlInputValidator(null)

        validator.validate("https://www.google.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.instagrom.com/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.redd.ot/92dd8")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.goog.it/92dd8")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_reddit_noProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("www.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("www.redd.it/92dd8")
        assertThat(validator.isValid()).isTrue()

        validator.validate("reddit.com/r/wallstreetbets/comments/lycp91/wsb_rules_please_read_before_posting/?utm_source=share&utm_medium=web2x&context=3")
        assertThat(validator.isValid()).isTrue()

        validator.validate("np.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("np-dk.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("www.reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("www.reddit.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("redd.it")
        assertThat(validator.isValid()).isFalse()

        validator.validate("redd.it/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("reddit.com/")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_reddit_withProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("http://www.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://www.redd.it/92dd8")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://reddit.com/r/wallstreetbets/comments/lycp91/wsb_rules_please_read_before_posting/?utm_source=share&utm_medium=web2x&context=3")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://np.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://np-dk.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://www.reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://www.reddit.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://redd.it")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://redd.it/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://reddit.com/")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_reddit_withSecureProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("https://www.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://www.redd.it/92dd8")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://reddit.com/r/wallstreetbets/comments/lycp91/wsb_rules_please_read_before_posting/?utm_source=share&utm_medium=web2x&context=3")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://np.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://np-dk.reddit.com/r/pics/comments/92dd8/test_post_please_ignore/")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://www.reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.reddit.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://redd.it")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://redd.it/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://reddit.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://reddit.com/")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_instagram_noProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("www.instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("www.instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("www.instagram.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("www.instagram.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("www.instagr.am")
        assertThat(validator.isValid()).isFalse()

        validator.validate("www.instagr.am/")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_instagram_withProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("http://www.instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://www.instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("http://www.instagram.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://www.instagram.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://www.instagr.am")
        assertThat(validator.isValid()).isFalse()

        validator.validate("http://www.instagr.am/")
        assertThat(validator.isValid()).isFalse()
    }

    @Test
    fun urlInputValidator_validationSuccess_instagram_withSecureProtocol() {
        val validator = UrlInputValidator(null)

        validator.validate("https://www.instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://instagram.com/p/CMIybsdhf3s/?utm_source=ig_web_copy_link")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://www.instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://instagr.am/p/CMIybsdhf3s/?utm_source=ig_web_button_share_sheet")
        assertThat(validator.isValid()).isTrue()

        validator.validate("https://www.instagram.com")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.instagram.com/")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.instagr.am")
        assertThat(validator.isValid()).isFalse()

        validator.validate("https://www.instagr.am/")
        assertThat(validator.isValid()).isFalse()
    }
}