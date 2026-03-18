package com.example.babyparenting.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyparenting.ui.screens.AdviceScreen
import com.example.babyparenting.ui.screens.AdminPanelScreen
import com.example.babyparenting.ui.screens.BabyJourneyScreen
import com.example.babyparenting.ui.screens.OnboardingScreen
import com.example.babyparenting.viewmodel.AdminViewModel
import com.example.babyparenting.viewmodel.JourneyViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val JOURNEY    = "journey"
    const val ADVICE     = "advice"
    const val ADMIN      = "admin"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.ONBOARDING
) {
    val journeyVm: JourneyViewModel = viewModel()
    val adminVm:   AdminViewModel   = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = { name, ageMonths ->
                    journeyVm.setChildName(name)
                    journeyVm.setChildAge(ageMonths)
                    navController.navigate(Routes.JOURNEY) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.JOURNEY) {
            BabyJourneyScreen(
                viewModel         = journeyVm,
                onMilestoneTapped = { milestone ->
                    journeyVm.onMilestoneTapped(milestone)
                    navController.navigate(Routes.ADVICE)
                },
                onAdminTapped = { navController.navigate(Routes.ADMIN) }
            )
        }

        composable(Routes.ADVICE) {
            AdviceScreen(
                viewModel = journeyVm,
                onBack    = {
                    journeyVm.resetAdvice()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN) {
            AdminPanelScreen(
                viewModel = adminVm,
                onBack    = {
                    journeyVm.refreshAfterAdminEdit()
                    navController.popBackStack()
                }
            )
        }
    }
}