package xyz.lepisma.pile

import junit.framework.TestCase.assertEquals
import org.junit.Test
import xyz.lepisma.pile.data.generateInitialContent

class DatabaseKtTest {

    @Test
    fun testGenerateInitialContent_Plain() {
        val id = "id"
        val title = "Test Title"
        val output = """
            :PROPERTIES:
            :ID:      $id
            :END:
            #+TITLE: $title
            
        """.trimIndent()

        assertEquals(output, generateInitialContent(title, id, null, null))
    }

    @Test
    fun testGenerateInitialContent_Ref() {
        val id = "id"
        val title = "Test Title"
        val refLink = "ref_link_here"
        val output = """
            :PROPERTIES:
            :ID:      $id
            :ROAM_REFS: $refLink
            :END:
            #+TITLE: $title
            
        """.trimIndent()

        assertEquals(output, generateInitialContent(title, id, refLink, null))
    }
    @Test
    fun testGenerateInitialContent_Tags() {
        val id = "id"
        val title = "Test Title"
        val tags = listOf("hi", "hello")
        val output = """
            :PROPERTIES:
            :ID:      $id
            :END:
            #+TAGS: ${tags.joinToString(", ")}
            #+TITLE: $title
            
        """.trimIndent()

        assertEquals(output, generateInitialContent(title, id, null, tags))
    }

    @Test
    fun testGenerateInitialContent_All() {
        val id = "id"
        val title = "Test Title"
        val refLink = "ref_link_here"
        val tags = listOf("hi", "hello")
        val output = """
            :PROPERTIES:
            :ID:      $id
            :ROAM_REFS: $refLink
            :END:
            #+TAGS: ${tags.joinToString(", ")}
            #+TITLE: $title
            
        """.trimIndent()

        assertEquals(output, generateInitialContent(title, id, refLink, tags))
    }

}