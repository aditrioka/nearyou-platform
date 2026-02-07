package id.nearyou.app.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import id.nearyou.app.ui.main.HomeScreen
import id.nearyou.app.ui.profile.EditProfileScreen
import id.nearyou.app.ui.profile.ProfileScreen

fun NavGraphBuilder.mainNavGraph(
    navController: NavController,
    onLogout: () -> Unit,
) {
    navigation<MainGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Profile) },
                onLogout = onLogout,
            )
        }

        composable<Profile> {
            ProfileScreen(
                onEditProfile = { navController.navigate(EditProfile) },
                onLogout = onLogout,
            )
        }

        composable<EditProfile> {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
