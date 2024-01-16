package ru.rutoken.tech.repository.document

interface DocumentRepository {
    suspend fun addDocument(document: Document)
}