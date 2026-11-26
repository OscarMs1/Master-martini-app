package cl.duoc.mastermartini.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mastermartini.MasterMartiniApp
import cl.duoc.mastermartini.ui.screens.account.AccountViewModel
import cl.duoc.mastermartini.ui.screens.account.AddressesViewModel
import cl.duoc.mastermartini.ui.screens.account.EditProfileViewModel
import cl.duoc.mastermartini.ui.screens.auth.LoginViewModel
import cl.duoc.mastermartini.ui.screens.auth.RegisterViewModel
import cl.duoc.mastermartini.ui.screens.catalog.CatalogViewModel
import cl.duoc.mastermartini.ui.screens.catalog.ProductReviewsViewModel
import cl.duoc.mastermartini.ui.screens.catalog.QuoteFormViewModel
import cl.duoc.mastermartini.ui.screens.home.HomeViewModel
import cl.duoc.mastermartini.ui.screens.learn.LearnDetailViewModel
import cl.duoc.mastermartini.ui.screens.learn.LearnViewModel
import cl.duoc.mastermartini.ui.screens.services.BookingViewModel
import cl.duoc.mastermartini.ui.screens.services.ServicesViewModel

/** Fábrica única de ViewModels: les entrega los repositorios del AppContainer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { MainViewModel(container().settings) }
        initializer { RegisterViewModel(container().users, container().regions) }
        initializer { LoginViewModel(container().users) }
        initializer { HomeViewModel(container().users, container().content, container().classes, container().reservations, container().settings) }
        initializer { LearnViewModel(createSavedStateHandle(), container().content, container().settings) }
        initializer { LearnDetailViewModel(createSavedStateHandle(), container().content, container().settings) }
        initializer { CatalogViewModel(container().products, container().quotes, container().favorites) }
        initializer { QuoteFormViewModel(
                container().users, container().quotes, container().products, container().regions, container().notifications,
                container().classes, container().reservations, container().addresses,
            ) }
        initializer { ServicesViewModel(container().content, container().classes, container().reservations, container().notifications, container().settings) }
        initializer {
            BookingViewModel(createSavedStateHandle(), container().content, container().users, container().reservations, container().notifications)
        }
        initializer {
            AccountViewModel(
                container().users, container().content, container().quotes,
                container().reservations, container().settings,
                container().addresses, container().favorites, container().products,
            )
        }
        initializer { EditProfileViewModel(container().users) }
        initializer { ProductReviewsViewModel(createSavedStateHandle(), container().products, container().users, container().reviews) }
        initializer { AddressesViewModel(container().users, container().addresses, container().regions) }
    }
}

private fun CreationExtras.container() = (this[APPLICATION_KEY] as MasterMartiniApp).container
