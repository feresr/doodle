import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.AppAssets
import io.nacular.doodle.examples.AppAssetsImpl
import io.nacular.doodle.examples.Contact
import io.nacular.doodle.examples.ContactView
import io.nacular.doodle.examples.ContactsApp
import io.nacular.doodle.examples.EditContactView
import io.nacular.doodle.examples.LocalStorePersistence
import io.nacular.doodle.examples.PersistentStore
import io.nacular.doodle.examples.Router
import io.nacular.doodle.examples.SimpleContactsModel
import io.nacular.doodle.examples.TrivialRouter
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance

suspend fun main() {
    val contacts = SimpleContactsModel(LocalStorePersistence())
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (modules = listOf(
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
            bindInstance<Router>                   { TrivialRouter        (window    ) }
            bindSingleton<PathMetrics>             { PathMetricsImpl      (instance()) }
            bindSingleton<PersistentStore<Contact>>{ LocalStorePersistence(          ) }
        }
    )) {
        // load app
        ContactsApp(
            theme             = instance(),
            assets            = { AppAssetsImpl(instance(), instance()) },
            router            = instance(),
            Header            = factory(),
            display           = instance(),
            contacts          = contacts,
            appScope          = appScope,
            navigator         = instance(),
            ContactList       = factory(),
            uiDispatcher      = Dispatchers.UI,
            ContactView       = { assets, contact -> factory<Pair<AppAssets, Contact>, ContactView>()(assets to contact) },
            CreateButton      = factory(),
            themeManager      = instance(),
            EditContactView   = { assets, contact -> factory<Pair<AppAssets, Contact>, EditContactView>()(assets to contact) },
            CreateContactView = factory(),
        )
    }
}