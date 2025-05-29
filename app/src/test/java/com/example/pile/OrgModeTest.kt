package com.example.pile

import com.example.pile.orgmode.OrgListType
import com.example.pile.orgmode.OrgParagraph
import com.example.pile.orgmode.PileOptions
import com.example.pile.orgmode.breakBlocks
import com.example.pile.orgmode.breakHeadingContent
import com.example.pile.orgmode.dropPreamble
import com.example.pile.orgmode.parseId
import com.example.pile.orgmode.parseNodeLinks
import com.example.pile.orgmode.parseOrgList
import com.example.pile.orgmode.parseOrgRef
import com.example.pile.orgmode.parsePileOptions
import com.example.pile.orgmode.parseTitle
import com.example.pile.orgmode.unfillText
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
    fun testUnfill() {
        val input = """
            The Alberta Plan characterizes the problem of AI as the online maximization of
            reward via continual sensing and acting, with limited computation, and
            potentially in the presence of other agents. This characterization might seem
            natural, even obvious, but it is also contrary to current practice, which is

            often focused on offline learning, prepared training sets, human assistance, and
            unlimited computation. The Alberta Plan research vision is both classical and
            contrarian, and radical in the sense of going to the root.
        """.trimIndent()
        val expected = "The Alberta Plan characterizes the problem of AI as the online maximization of " +
                "reward via continual sensing and acting, with limited computation, and " +
                "potentially in the presence of other agents. This characterization might seem " +
                "natural, even obvious, but it is also contrary to current practice, which is" +
                "\n\n" +
                "often focused on offline learning, prepared training sets, human assistance, and " +
                "unlimited computation. The Alberta Plan research vision is both classical and " +
                "contrarian, and radical in the sense of going to the root."
        assertEquals(unfillText(input), expected)
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

    @Test
    fun testParseOrgList_Unordered() {
        val input = """
            - I think I have identified problems with my writing in a formal way and can
              write simple functions to let me know things like:
            - There can be other assists that go with Emacs, like a cached thesaurus.
            """.trimIndent()
        val expected = OrgParagraph.OrgList(input, OrgListType.UNORDERED, listOf(
            OrgParagraph.OrgListItem(
                "I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:",
                listOf(OrgParagraph.OrgPlainParagraph("I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:"))
            ),
            OrgParagraph.OrgListItem(
                "There can be other assists that go with Emacs, like a cached thesaurus.",
                listOf(OrgParagraph.OrgPlainParagraph("There can be other assists that go with Emacs, like a cached thesaurus."))
            )
        ))
        assertEquals(expected, parseOrgList(input))
    }

    @Test
    fun testParseOrgList_Ordered() {
        val input = """
            1. I think I have identified problems with my writing in a formal way and can
               write simple functions to let me know things like:
            2. There can be other assists that go with Emacs, like a cached thesaurus.
            """.trimIndent()
        val expected = OrgParagraph.OrgList(input, OrgListType.ORDERED, listOf(
            OrgParagraph.OrgListItem(
                "I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:",
                listOf(OrgParagraph.OrgPlainParagraph("I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:"))
            ),
            OrgParagraph.OrgListItem(
                "There can be other assists that go with Emacs, like a cached thesaurus.",
                listOf(OrgParagraph.OrgPlainParagraph("There can be other assists that go with Emacs, like a cached thesaurus."))
            )
        ))
        assertEquals(expected, parseOrgList(input))
    }

    @Test
    fun testParseOrgList_Multiline() {
        val input = """
            1. I think I have identified problems with my writing in a formal way and can
               write simple functions to let me know things like:
               
               Hello world.
            2. There can be other assists that go with Emacs, like a cached thesaurus.
            """.trimIndent()
        val expected = OrgParagraph.OrgList(input, OrgListType.ORDERED, listOf(
            OrgParagraph.OrgListItem(
                "I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:\n\nHello world.",
                listOf(
                    OrgParagraph.OrgPlainParagraph("I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:"),
                    OrgParagraph.OrgPlainParagraph("Hello world.")
                )
            ),
            OrgParagraph.OrgListItem(
                "There can be other assists that go with Emacs, like a cached thesaurus.",
                listOf(OrgParagraph.OrgPlainParagraph("There can be other assists that go with Emacs, like a cached thesaurus."))
            )
        ))
        assertEquals(expected, parseOrgList(input))
    }

    @Test
    fun testParseOrgList_Nested() {
        val input = """
            1. I think I have identified problems with my writing in a formal way and can
               write simple functions to let me know things like:
               + Hello world.
               + hi
            2. There can be other assists that go with Emacs, like a cached thesaurus.
            """.trimIndent()
        val expected = OrgParagraph.OrgList(input, OrgListType.ORDERED, listOf(
            OrgParagraph.OrgListItem(
                "I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:\n+ Hello world.\n+ hi",
                listOf(
                    OrgParagraph.OrgPlainParagraph("I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:"),
                    OrgParagraph.OrgList("+ Hello world.\n+ hi", OrgListType.UNORDERED, listOf(
                        OrgParagraph.OrgListItem(
                            "Hello world.",
                            listOf(OrgParagraph.OrgPlainParagraph("Hello world."))
                        ),
                        OrgParagraph.OrgListItem(
                            "hi",
                            listOf(OrgParagraph.OrgPlainParagraph("hi"))
                        )
                    ))
                )
            ),
            OrgParagraph.OrgListItem(
                "There can be other assists that go with Emacs, like a cached thesaurus.",
                listOf(OrgParagraph.OrgPlainParagraph("There can be other assists that go with Emacs, like a cached thesaurus."))
            )
        ))
        assertEquals(expected, parseOrgList(input))
    }

    @Test
    fun testParseOrgList_Linebreaks() {
        val input = """
            1. I think I have identified problems with my writing in a formal way and can
               write simple functions to let me know things like:

            2. There can be other assists that go with Emacs, like a cached thesaurus.
            3. Hello
            """.trimIndent()
        val expected = OrgParagraph.OrgList(input, OrgListType.ORDERED, listOf(
            OrgParagraph.OrgListItem(
                "I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:",
                listOf(
                    OrgParagraph.OrgPlainParagraph("I think I have identified problems with my writing in a formal way and can\nwrite simple functions to let me know things like:"),
                )
            ),
            OrgParagraph.OrgListItem(
                "There can be other assists that go with Emacs, like a cached thesaurus.",
                listOf(OrgParagraph.OrgPlainParagraph("There can be other assists that go with Emacs, like a cached thesaurus."))
            ),
            OrgParagraph.OrgListItem(
                "Hello",
                listOf(OrgParagraph.OrgPlainParagraph("Hello"))
            )
        ))
        assertEquals(expected, parseOrgList(input))
    }

    @Test
    fun testDropPreamble() {
        val input = """
            :PROPERTIES:
            :ID:       e1be8ebc-a4d4-4c00-82c8-a2bf21315bc7
            :ROAM_REFS: http://arxiv.org/abs/2010.11982
            :END:

            #+TITLE: The Turking Test
            This is a relatively old paper from 2020.
        """.trimIndent()

        val expected = "This is a relatively old paper from 2020."
        assertEquals(expected, dropPreamble(input))
    }

    @Test
    fun testDropPreamble_TrailingBlock() {
        val input = """
            :PROPERTIES:
            :ID:       e1be8ebc-a4d4-4c00-82c8-a2bf21315bc7
            :ROAM_REFS: http://arxiv.org/abs/2010.11982
            :END:

            #+TITLE: The Turking Test
            
            #+begin_quote
            This is a relatively old paper from 2020
            #+end_quote
        """.trimIndent()

        val expected = """
            #+begin_quote
            This is a relatively old paper from 2020
            #+end_quote
        """.trimIndent()
        assertEquals(expected, dropPreamble(input))
    }

    @Test
    fun testParseNodeLinks() {
        val input = """
            Hello hi, [[id:e1be8ebc-a4d4-4c00-82c8-a2bf21315bc7][this]] is a link,
            This is another [[id:eab]] without link title
        """.trimIndent()

        val expected = listOf("e1be8ebc-a4d4-4c00-82c8-a2bf21315bc7", "eab")
        assertEquals(expected, parseNodeLinks(input))
    }
}