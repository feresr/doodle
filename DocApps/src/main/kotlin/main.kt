@file:Suppress("unused")

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.impl.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.DragDropModule
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.CalculatorApp
import io.nacular.doodle.examples.DataStore
import io.nacular.doodle.examples.FilterButtonProvider
import io.nacular.doodle.examples.LocalStorePersistence
import io.nacular.doodle.examples.NumberFormatterImpl
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.PhotosApp
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.TodoApp
import io.nacular.doodle.examples.contacts.AppConfig
import io.nacular.doodle.examples.contacts.AppConfigImpl
import io.nacular.doodle.examples.contacts.Contact
import io.nacular.doodle.examples.contacts.ContactList
import io.nacular.doodle.examples.contacts.ContactView
import io.nacular.doodle.examples.contacts.ContactsApp
import io.nacular.doodle.examples.contacts.CreateContactView
import io.nacular.doodle.examples.contacts.EditContactView
import io.nacular.doodle.examples.contacts.EmbeddedRouter
import io.nacular.doodle.examples.contacts.Header
import io.nacular.doodle.examples.contacts.NoOpContacts
import io.nacular.doodle.examples.contacts.NoOpPersistence
import io.nacular.doodle.examples.contacts.SimpleContactsModel
import io.nacular.doodle.examples.contacts.appModule
import io.nacular.doodle.examples.contacts.showcase
import io.nacular.doodle.examples.contacts.showcaseModules
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicCircularProgressIndicatorBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicMutableSpinnerBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI.Module
import org.kodein.di.bindFactory
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance

@JsName("calculator")
fun calculator(element: HTMLElement) {
    application(root = element, modules = listOf(FontModule, PointerModule)) {
        // load app
        CalculatorApp(instance(), instance(), instance(), NumberFormatterImpl())
    }
}

@JsName("todo")
fun todo(element: HTMLElement) {
    class EmbeddedFilterButtonProvider(private val dataStore: DataStore): FilterButtonProvider {
        override fun invoke(text: String, filter: DataStore.Filter?, behavior: Behavior<Button>) = PushButton(text).apply {
            this.behavior               = behavior
            this.acceptsThemes          = false
            fired += { dataStore.filter = filter }

            dataStore.filterChanged += { rerender() }
        }
    }

    application(root = element, modules = listOf(FontModule, ImageModule, PointerModule, KeyboardModule, basicLabelBehavior(),
            nativeTextFieldBehavior(), nativeHyperLinkBehavior(), nativeScrollPanelBehavior(smoothScrolling = true),
            Module(name = "AppModule") {
                bindSingleton<PersistentStore>      { LocalStorePersistence                   (          ) }
                bindSingleton                       { DataStore                               (instance()) }
                bindSingleton<Router>               { io.nacular.doodle.examples.TrivialRouter(window    ) }
                bindSingleton<FilterButtonProvider> { EmbeddedFilterButtonProvider            (instance()) }

            }
    )) {
        // load app
        TodoApp(instance(), Dispatchers.UI, instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())
    }
}

@JsName("photos")
fun photos(element: HTMLElement) {
    application(root = element, modules = listOf(
            FocusModule,
            ImageModule,
            KeyboardModule,
            DragDropModule,
            basicLabelBehavior(),
            nativeTextFieldBehavior(spellCheck = false),
            basicMutableSpinnerBehavior(),
            basicCircularProgressIndicatorBehavior(thickness = 18.0),
            Module(name = "AppModule") {
                bindSingleton<Animator> { AnimatorImpl   (instance(), instance()) }
            }
    )) {
        // load app
        PhotosApp(instance(), instance(), instance(), instance(), instance(), instance())
    }
}

@JsName("contacts")
fun contacts(element: HTMLElement) {
    val contacts = SimpleContactsModel(io.nacular.doodle.examples.contacts.LocalStorePersistence())
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = listOf(
        FontModule,
        ImageModule,
        FocusModule,
        PointerModule,
        KeyboardModule,
        basicLabelBehavior       (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeHyperLinkBehavior  (),
        nativeScrollPanelBehavior(),
        appModule(appScope = appScope, contacts = contacts, uiDispatcher = Dispatchers.UI),
        Module   (name = "PlatformModule") {
            // Platform-specific bindings
            bindSingleton<PathMetrics>                               { PathMetricsImpl(instance()) }
            bindInstance<io.nacular.doodle.examples.contacts.Router> { EmbeddedRouter (          ) }
        }
    )) {
        // load app
        ContactsApp(
            theme             = instance(),
            assets            = { AppConfigImpl(instance(), instance()) },
            router            = instance(),
            Header            = factory(),
            display           = instance(),
            contacts          = contacts,
            appScope          = appScope,
            navigator         = instance(),
            ContactList       = factory(),
            uiDispatcher      = Dispatchers.UI,
            ContactView       = { assets, contact -> factory<Pair<AppConfig, Contact>, ContactView>()(assets to contact) },
            CreateButton      = factory(),
            themeManager      = instance(),
            EditContactView   = { assets, contact -> factory<Pair<AppConfig, Contact>, EditContactView>()(assets to contact) },
            CreateContactView = factory(),
        )
    }
}

@JsName("contactList")
fun contactList(element: HTMLElement) {
    val contacts = SimpleContactsModel(NoOpPersistence)
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = showcaseModules + Module(name = "PlatformModule") {
        bindFactory<AppConfig, ContactList> {
            ContactList(
                assets       = it,
                modals       = instance(),
                appScope     = appScope,
                contacts     = contacts,
                navigator    = instance(),
                textMetrics  = instance(),
                pathMetrics  = instance(),
                uiDispatcher = Dispatchers.UI
            )
        }
    }) {
        showcase(
            theme        = instance(),
            assets       = { AppConfigImpl(instance(), instance()) },
            display      = instance(),
            appScope     = appScope,
            themeManager = instance()
        ) {
            factory<AppConfig, ContactList>()(it)
        }
    }
}

@JsName("contactCreation")
fun contactCreation(element: HTMLElement) {
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = showcaseModules + Module(name = "PlatformModule") {
        bindFactory<AppConfig, CreateContactView> {
            CreateContactView(
                assets          = it,
                buttons         = instance(),
                contacts        = NoOpContacts,
                navigator       = instance(),
                pathMetrics     = instance(),
                textMetrics     = instance(),
                textFieldStyler = instance(),
            )
        }
    }) {
        showcase(
            theme        = instance(),
            assets       = { AppConfigImpl(instance(), instance()) },
            display      = instance(),
            appScope     = appScope,
            themeManager = instance()
        ) {
            factory<AppConfig, CreateContactView>()(it)
        }
    }
}

@JsName("contactEditing")
fun contactEditing(element: HTMLElement) {
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = showcaseModules + Module(name = "PlatformModule") {
        bindFactory<Pair<AppConfig, Contact>, EditContactView> { (assets, contact) ->
            EditContactView(
                assets          = assets,
                modals          = instance(),
                contact         = contact,
                contacts        = NoOpContacts,
                buttons         = instance(),
                appScope        = appScope,
                navigator       = instance(),
                pathMetrics     = instance(),
                textMetrics     = instance(),
                uiDispatcher    = Dispatchers.UI,
                textFieldStyler = instance(),
            )
        }
    }) {
        showcase(
            theme        = instance(),
            assets       = { AppConfigImpl(instance(), instance()) },
            display      = instance(),
            appScope     = appScope,
            themeManager = instance()
        ) {
            factory<Pair<AppConfig, Contact>, EditContactView>()(it to Contact("Santa Clause", "123456789"))
        }
    }
}

@JsName("contactView")
fun contactView(element: HTMLElement) {
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = showcaseModules + Module(name = "PlatformModule") {
        bindFactory<Pair<AppConfig, Contact>, ContactView> { (assets, contact) ->
            ContactView(
                assets       = assets,
                modals       = instance(),
                buttons      = instance(),
                contact      = contact,
                contacts     = NoOpContacts,
                appScope     = appScope,
                navigator    = instance(),
                linkStyler   = instance(),
                pathMetrics  = instance(),
                textMetrics  = instance(),
                uiDispatcher = Dispatchers.UI
            )
        }
    }) {
        showcase(
            theme        = instance(),
            assets       = { AppConfigImpl(instance(), instance()) },
            display      = instance(),
            appScope     = appScope,
            themeManager = instance()
        ) {
            factory<Pair<AppConfig, Contact>, ContactView>()(it to Contact("Jack Frost", "123456789"))
        }
    }
}

@JsName("contactsHeader")
fun contactsHeader(element: HTMLElement) {
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (root = element, modules = showcaseModules + Module(name = "PlatformModule") {
        bindFactory<AppConfig, Header> {
            Header(
                assets       = it,
                animate      = instance(),
                contacts     = NoOpContacts,
                navigator    = instance(),
                textMetrics  = instance(),
                pathMetrics  = instance(),
                focusManager = instance(),
            )
        }
    }) {
        showcase(
            theme        = instance(),
            assets       = { AppConfigImpl(instance(), instance()) },
            display      = instance(),
            appScope     = appScope,
            themeManager = instance()
        ) {
            factory<AppConfig, Header>()(it)
        }
    }
}