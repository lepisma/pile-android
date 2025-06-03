package com.example.pile.orgmode

class OrgParser {
    fun parse(tokens: List<Token>): OrgDocument? {
        val document = OrgDocument(
            title = OrgLine(listOf(OrgInlineElem.Text("hello world"))),
            preface = OrgPreface(body = emptyList()),
            content = emptyList(),
        )
        return document
    }
}