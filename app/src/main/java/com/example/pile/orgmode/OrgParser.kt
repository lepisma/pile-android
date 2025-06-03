package com.example.pile.orgmode

class OrgParser {
    fun parse(tokens: List<Token>): OrgDocument? {
        val document = OrgDocument(
            title = OrgLine(
                listOf(OrgInlineElem.Text("hello", range = Pair(0, 0))),
                range = Pair(0, 0)
            ),
            preface = OrgPreface(body = emptyList()),
            content = emptyList(),
            range = Pair(0, 0)
        )
        return document
    }
}