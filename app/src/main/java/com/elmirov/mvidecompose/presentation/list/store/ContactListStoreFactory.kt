package com.elmirov.mvidecompose.presentation.list.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.elmirov.mvidecompose.data.RepositoryImpl
import com.elmirov.mvidecompose.domain.entity.Contact
import com.elmirov.mvidecompose.domain.usecase.GetContactsUseCase
import kotlinx.coroutines.launch

class ContactListStoreFactory {

    private val storeFactory = DefaultStoreFactory()
    private val repository = RepositoryImpl
    private val getContactsUseCase = GetContactsUseCase(repository)

    fun create(): ContactListStore = object : ContactListStore,
        Store<ContactListStore.Intent, ContactListStore.State, ContactListStore.Label> by storeFactory.create(
            name = "ContactListStore",
            initialState = ContactListStore.State(listOf()),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data class ContactsLoaded(val contacts: List<Contact>) : Action
    }

    private sealed interface Msg {
        data class ContactsLoaded(val contacts: List<Contact>) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                getContactsUseCase().collect {
                    dispatch(Action.ContactsLoaded(contacts = it))
                }
            }
        }
    }

    private inner class ExecutorImpl() :
        CoroutineExecutor<ContactListStore.Intent, Action, ContactListStore.State, Msg, ContactListStore.Label>() {
        override fun executeAction(action: Action, getState: () -> ContactListStore.State) {
            when (action) {
                is Action.ContactsLoaded -> dispatch(Msg.ContactsLoaded(action.contacts))
            }
        }

        override fun executeIntent(
            intent: ContactListStore.Intent,
            getState: () -> ContactListStore.State,
        ) {
            when (intent) {
                ContactListStore.Intent.AddContact -> publish(ContactListStore.Label.AddContact)

                is ContactListStore.Intent.SelectContact -> publish(
                    ContactListStore.Label.EditContact(
                        intent.contact
                    )
                )
            }
        }
    }

    private object ReducerImpl : Reducer<ContactListStore.State, Msg> {
        override fun ContactListStore.State.reduce(msg: Msg): ContactListStore.State = when (msg) {
            is Msg.ContactsLoaded -> copy(contacts = msg.contacts)
        }
    }
}