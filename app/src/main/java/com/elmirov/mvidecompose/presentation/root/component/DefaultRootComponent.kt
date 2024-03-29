package com.elmirov.mvidecompose.presentation.root.component

import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.elmirov.mvidecompose.domain.entity.Contact
import com.elmirov.mvidecompose.presentation.add.component.DefaultAddContactComponent
import com.elmirov.mvidecompose.presentation.edit.component.DefaultEditContactComponent
import com.elmirov.mvidecompose.presentation.list.component.DefaultContactListComponent
import com.elmirov.mvidecompose.presentation.root.component.DefaultRootComponent.Config.AddContact
import com.elmirov.mvidecompose.presentation.root.component.DefaultRootComponent.Config.ContactList
import com.elmirov.mvidecompose.presentation.root.component.DefaultRootComponent.Config.EditContact
import com.elmirov.mvidecompose.presentation.root.component.RootComponent.Child
import kotlinx.parcelize.Parcelize

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        initialConfiguration = ContactList,
        handleBackButton = true,
        childFactory = ::child
    )

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): Child =
        when (config) {
            AddContact -> {
                val component = DefaultAddContactComponent(
                    componentContext = componentContext,
                    onContactSaved = {
                        navigation.pop()
                    },
                )
                Child.AddContact(component = component)
            }

            ContactList -> {
                val component = DefaultContactListComponent(
                    componentContext = componentContext,
                    onAddContactRequest = {
                        navigation.push(AddContact)
                    },
                    onEditingContactRequest = {
                        navigation.push(EditContact(contact = it))
                    },
                )
                Child.ContactList(component = component)
            }

            is EditContact -> {
                val component = DefaultEditContactComponent(
                    componentContext = componentContext,
                    contact = config.contact,
                    onContactSaved = {
                        navigation.pop()
                    }
                )
                Child.EditContact(component = component)
            }
        }

    private sealed interface Config : Parcelable {
        @Parcelize
        data object ContactList : Config

        @Parcelize
        data object AddContact : Config

        @Parcelize
        data class EditContact(val contact: Contact) : Config
    }
}