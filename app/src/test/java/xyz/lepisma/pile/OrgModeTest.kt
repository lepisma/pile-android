package xyz.lepisma.pile

import xyz.lepisma.pile.orgmode.PileOptions
import xyz.lepisma.pile.orgmode.parsePileOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class OrgModeTest {
    @Test
    fun testParsePileOptions_Pinned() {
        val input = """
            :PROPERTIES:
            :ID:      21e2c8f6-8dbb-4002-bcf5-a15203516114
            :END:
            #+TITLE: Org Test
            #+PILE: pinned:t
            #+TOC: headlines 2

            #+BEGIN_page-intro
            This is an introductory paragraph. Kind of like an abstract. Let's fill this a
            little. Pellentesque dapibus suscipit ligula. Donec posuere augue in quam. Etiam
            vel tortor sodales tellus ultricies commodo. Suspendisse potenti. Aenean in sem
            ac leo mollis blandit. Donec neque quam, dignissim in, mollis nec, sagittis eu,
            wisi. Phasellus lacus. Etiam laoreet quam sed arcu. Phasellus at dui in ligula
            mollis ultricies. Integer placerat tristique nisl.
            #+END_page-intro
            """.trimIndent()

        val expected = PileOptions(pinned = true)
        assertEquals(expected, parsePileOptions(input))
    }
}