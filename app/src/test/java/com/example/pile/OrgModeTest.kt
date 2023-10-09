package com.example.pile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrgModeTest {
    @Test
    fun testParseId_ValidId() {
        val input = """
            :PROPERTIES:
            :ID:       d6a6af7e-5f68-4cec-b0d7-36f0ad55125e
            :END:
            #+TITLE: Homeostatic Newspaper
            """.trimIndent()
        val expected = "d6a6af7e-5f68-4cec-b0d7-36f0ad55125e"
        val actual = parseId(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testParseId_NoId() {
        val input = """
            :AnotherField: Value
            """.trimIndent()
        val actual = parseId(input)
        assertNull(actual)
    }

    @Test
    fun testParseTitle_ValidTitle() {
        val input = """
            :PROPERTIES:
            :ID:       d6a6af7e-5f68-4cec-b0d7-36f0ad55125e
            :END:
            #+TITLE: Homeostatic Newspaper
            """.trimIndent()
        val expected = "Homeostatic Newspaper"
        val actual = parseTitle(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testParseTitle_NoTitle() {
        val input = """
            :AnotherField: Value
            """.trimIndent()
        assertEquals(parseTitle(input), "<NA>")
    }

    @Test
    fun testParseTitle_NoText() {
        val input = ""
        assertEquals(parseTitle(input), "<NA>")
    }

    @Test
    fun testParseOrgRef_URLRef() {
        val input = """
            :PROPERTIES:
            :ID:       465b0a81-b472-4889-bcf6-5368ce97f08a
            :ROAM_REFS: http://arxiv.org/abs/2212.09689
            :END:
            #+TITLE: Unnatural Instructions: Tuning Language Models with (Almost) No Human Labor
            """.trimIndent()

        val expected = "http://arxiv.org/abs/2212.09689"
        assertEquals(expected, parseOrgRef(input))
    }
    @Test
    fun testBreakBlocks() {
        val input = """
            domain and human-labeled training sets."
            4. Multiple agents. "The case of two or more cooperating agents also includes
               cognitive assistants and prostheses".

            #+begin_quote
            there is always a tradeoff between reaction time and the quality of the decision
            #+end_quote
            
            #+begin_src test :var value
            lorem ipsum
            #+end_src

            #+begin_quote
            The Alberta Plan characterizes the problem of AI as the online maximization of
            reward via continual sensing and acting, with limited computation, and
            potentially in the presence of other agents. This characterization might seem
            natural, even obvious, but it is also contrary to current practice, which is

            often focused on offline learning, prepared training sets, human assistance, and
            unlimited computation. The Alberta Plan research vision is both classical and
            contrarian, and radical in the sense of going to the root.
            #+end_quote
            
            post blocks
            """.trimIndent()
        val expected = listOf(
            """
            domain and human-labeled training sets."
            4. Multiple agents. "The case of two or more cooperating agents also includes
               cognitive assistants and prostheses".
            """.trimIndent(),
            """
            #+begin_quote
            there is always a tradeoff between reaction time and the quality of the decision
            #+end_quote
            """.trimIndent(),
            """
            #+begin_src test :var value
            lorem ipsum
            #+end_src
            """.trimIndent(),
            """
            #+begin_quote
            The Alberta Plan characterizes the problem of AI as the online maximization of
            reward via continual sensing and acting, with limited computation, and
            potentially in the presence of other agents. This characterization might seem
            natural, even obvious, but it is also contrary to current practice, which is

            often focused on offline learning, prepared training sets, human assistance, and
            unlimited computation. The Alberta Plan research vision is both classical and
            contrarian, and radical in the sense of going to the root.
            #+end_quote
            """.trimIndent(),
            """
            post blocks
            """.trimIndent()
        )
        assertEquals(expected, breakBlocks(input))
    }

    @Test
    fun testBreakHeadingContent_Quote() {
        val input = """
            domain and human-labeled training sets."
            4. Multiple agents. "The case of two or more cooperating agents also includes
               cognitive assistants and prostheses".

            #+begin_quote
            there is always a tradeoff between reaction time and the quality of the decision
            #+end_quote

            #+begin_quote
            The Alberta Plan characterizes the problem of AI as the online maximization of
            reward via continual sensing and acting, with limited computation, and
            potentially in the presence of other agents. This characterization might seem
            natural, even obvious, but it is also contrary to current practice, which is

            often focused on offline learning, prepared training sets, human assistance, and
            unlimited computation. The Alberta Plan research vision is both classical and
            contrarian, and radical in the sense of going to the root.
            #+end_quote
            """.trimIndent()
        val expected = listOf(
            """
            domain and human-labeled training sets."
            """.trimIndent(),
            """
            4. Multiple agents. "The case of two or more cooperating agents also includes
               cognitive assistants and prostheses".
            """.trimIndent(),
            """
            #+begin_quote
            there is always a tradeoff between reaction time and the quality of the decision
            #+end_quote
            """.trimIndent(),
            """
            #+begin_quote
            The Alberta Plan characterizes the problem of AI as the online maximization of
            reward via continual sensing and acting, with limited computation, and
            potentially in the presence of other agents. This characterization might seem
            natural, even obvious, but it is also contrary to current practice, which is

            often focused on offline learning, prepared training sets, human assistance, and
            unlimited computation. The Alberta Plan research vision is both classical and
            contrarian, and radical in the sense of going to the root.
            #+end_quote
            """.trimIndent()
        )
        assertEquals(expected, breakHeadingContent(input))
    }
}