package com.reas.redditdownloaderkotlin.gallery

import com.google.android.material.textfield.TextInputLayout

/**
 * Regex tested on https://regex101.com/r/RBnzKr/1
 */
class UrlInputValidator(private val layout: TextInputLayout?) {
    private var valid = false

    fun validate(text: String) {
        togglePrefix(text)

        if (matchesRegex(text)) {
            validationSuccess()
        } else {
            validationFailed()
        }
    }

    fun isValid() = valid

    fun triggerValidationFailed() {
        validationFailed()
    }

    private fun matchesRegex(text: String): Boolean {
        val regex = """^(https?://)?([a-z.\-]*)?(reddit\.com|redd\.it|instagram\.com|instagr\.am)([/][a-z0-9.\-_~!$&'()*+,;=:@/]+)+([?][a-z0-9=&%_]*)?""".toRegex(RegexOption.IGNORE_CASE)

        val result = regex.matchEntire(text)

        return !result?.value.isNullOrEmpty()
    }

    private fun validationFailed() {
        valid = false
        layout?.error = "Enter a valid URL"
    }

    private fun validationSuccess() {
        valid = true
        layout?.error = null
    }

    private fun togglePrefix(text: String) {
        val regex = """^(\bhttps?://\b)""".toRegex(RegexOption.IGNORE_CASE)
        layout?.prefixText = if (regex.containsMatchIn(text)) "" else "https://"
    }
}