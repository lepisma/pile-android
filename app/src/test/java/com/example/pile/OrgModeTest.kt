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
"""
        val expected = "d6a6af7e-5f68-4cec-b0d7-36f0ad55125e"
        val actual = parseId(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testParseId_NoId() {
        val input = """
            :AnotherField: Value
        """
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
"""
        val expected = "Homeostatic Newspaper"
        val actual = parseTitle(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testParseTitle_NoTitle() {
        val input = """
            :AnotherField: Value
        """
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
        """

        val expected = "http://arxiv.org/abs/2212.09689"
        assertEquals(expected, parseOrgRef(input))
    }
}