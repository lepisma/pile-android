package com.example.pile

import org.junit.Test

class DataKtTest {
    @Test
    fun testIsLiterature_FileStringParsing() {
        val litPath = "content://com.android.externalstorage.documents/tree/primary%3ADocuments%2Fnotes/document/primary%3ADocuments%2Fnotes%2Fliterature%2F20230607233110-lima_less_is_more_for_alignment.org"
        val nonPath = "content://com.android.externalstorage.documents/tree/primary%3ADocuments%2Fnotes/document/primary%3ADocuments%2Fnotes%2F20231105151019-interviews.org"

        assert(isLiteratureNodePath(litPath))
        assert(!isLiteratureNodePath(nonPath))
    }
}