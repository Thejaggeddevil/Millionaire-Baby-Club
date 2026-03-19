package com.example.babyparenting.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyparenting.ui.screens.AdminPanelScreen
import com.example.babyparenting.ui.screens.AdviceScreen
import com.example.babyparenting.ui.screens.BabyJourneyScreen
import com.example.babyparenting.ui.screens.LoginScreen
import com.example.babyparenting.ui.screens.OnboardingScreen
import com.example.babyparenting.ui.screens.SettingsScreen
import com.example.babyparenting.viewmodel.AdminViewModel
import com.example.babyparenting.viewmodel.JourneyViewModel

object Routes {
    const val LOGIN      = "login"
    const val ONBOARDING = "onboarding"
    const val JOURNEY    = "journey"
    const val ADVICE     = "advice"
    const val SETTINGS   = "settings"
    const val ADMIN      = "admin"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    val journeyVm: JourneyViewModel = viewModel()
    val adminVm:   AdminViewModel   = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Login — always the entry point ────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onParentLogin = {
                    // Always allow entry — Firebase auth will be added later
                    // For now: if name set → go to journey, else onboarding
                    val hasProfile = journeyVm.getChildName().isNotBlank()
                    if (hasProfile) {
                        navController.navigate(Routes.JOURNEY) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onAdminLogin = { password ->
                    val ok = adminVm.loginFromStart(password)
                    if (ok) {
                        navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                    ok
                }
            )
        }

        // ── Onboarding — first-time parents only ──────────────────────────────
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

        // ── Journey map ───────────────────────────────────────────────────────
        composable(Routes.JOURNEY) {
            BabyJourneyScreen(
                viewModel        = journeyVm,
                onMilestoneTapped = { milestone ->
                    journeyVm.onMilestoneTapped(milestone)
                    navController.navigate(Routes.ADVICE)
                },
                onSettingsTapped = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        // ── Advice detail ─────────────────────────────────────────────────────
        composable(Routes.ADVICE) {
            AdviceScreen(
                viewModel = journeyVm,
                onBack    = {
                    journeyVm.resetAdvice()
                    navController.popBackStack()
                }
            )
        }

        // ── Settings / profile ────────────────────────────────────────────────
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = journeyVm,
                onBack    = { navController.popBackStack() },
                onLogout  = {
                    // Clear and go to login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Admin panel ───────────────────────────────────────────────────────
        composable(Routes.ADMIN) {
            AdminPanelScreen(
                viewModel = adminVm,
                onBack    = {
                    journeyVm.refreshAfterAdminEdit()
                    // Admin goes back to login (they don't see parent journey)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}