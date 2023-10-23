package com.example.pile.ui.components

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class OrgTextTest {

    @Test
    fun testSplitCheckList() {
        val input = AnnotatedString("\uD83D\uDFE9 Hi my name is abhinav.")
        val (checkbox, content) = splitCheckList(input)

        assertEquals(AnnotatedString("\uD83D\uDFE9 "), checkbox)
        assertEquals(AnnotatedString("Hi my name is abhinav."), content)
    }
}