package com.example.repository

sealed class RepositoryException(message: String) : RuntimeException(message)

class NotFoundException(message: String) : RepositoryException(message)

class ValidationException(message: String) : RepositoryException(message)

class ConflictException(message: String) : RepositoryException(message)
