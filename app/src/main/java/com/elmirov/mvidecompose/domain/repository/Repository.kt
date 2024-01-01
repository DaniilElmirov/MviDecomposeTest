package com.elmirov.mvidecompose.domain.repository

import com.elmirov.mvidecompose.domain.entity.Contact
import kotlinx.coroutines.flow.Flow

interface Repository {

    val contacts: Flow<List<Contact>>

    fun saveContact(contact: Contact)
}