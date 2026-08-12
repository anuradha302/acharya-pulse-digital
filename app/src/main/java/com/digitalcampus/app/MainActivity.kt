package com.digitalcampus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import canteen.CanteenScreen
import orders.OrdersScreen
import sanitary.SanitaryScreen

import com.digitalcampus.app.campusmart.CampusMartScreen
import com.digitalcampus.app.stationery.StationeryScreen

import com.digitalcampus.app.ui.dashboard.RiderDashboard
import com.digitalcampus.app.ui.dashboard.ShopkeeperDashboard
import com.digitalcampus.app.ui.dashboard.StudentDashboard

import com.digitalcampus.app.ui.login.LoginScreen
import com.digitalcampus.app.ui.login.RoleSelectionScreen
import com.digitalcampus.app.ui.login.UserRole

import com.digitalcampus.app.ui.theme.DigitalCampusTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitalCampusTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "role_selection",
            modifier = Modifier.padding(innerPadding)
        ) {

            // ROLE SELECTION
            composable("role_selection") {

                RoleSelectionScreen(
                    onRoleSelected = { role ->

                        navController.navigate(
                            "login/${role.name}"
                        )
                    }
                )
            }

            // LOGIN
            composable(
                route = "login/{role}",
                arguments = listOf(
                    navArgument("role") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val roleName =
                    backStackEntry.arguments
                        ?.getString("role")

                val role =
                    roleName?.let {
                        UserRole.valueOf(it)
                    } ?: UserRole.STUDENT

                LoginScreen(

                    role = role,

                    onBack = {
                        navController.popBackStack()
                    },

                    onLoginSuccess = { successfulRole ->

                        when (successfulRole) {

                            UserRole.STUDENT -> {

                                navController.navigate(
                                    "student_dashboard"
                                )
                            }

                            UserRole.SHOPKEEPER -> {

                                navController.navigate(
                                    "shopkeeper_dashboard"
                                )
                            }

                            UserRole.RIDER -> {

                                navController.navigate(
                                    "rider_dashboard"
                                )
                            }
                        }
                    }
                )
            }

            // STUDENT DASHBOARD
            composable("student_dashboard") {

                StudentDashboard(

                    onLogout = {

                        navController.navigate(
                            "role_selection"
                        ) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },

                    onCanteenClick = {
                        navController.navigate("canteen")
                    },

                    onStationeryClick = {
                        navController.navigate("stationery")
                    },

                    onMartClick = {
                        navController.navigate("campus_mart")
                    },

                    onSanitaryClick = {
                        navController.navigate("sanitary")
                    },

                    onOrdersClick = {
                        navController.navigate("orders")
                    }
                )
            }

            // CANTEEN
            composable("canteen") {

                CanteenScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // STATIONERY
            composable("stationery") {

                StationeryScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // CAMPUS MART
            composable("campus_mart") {

                CampusMartScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // PINK PACKAGING
            composable("sanitary") {

                SanitaryScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // MY ORDERS
            composable("orders") {

                OrdersScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // SHOPKEEPER DASHBOARD
            composable("shopkeeper_dashboard") {

                ShopkeeperDashboard(

                    onLogout = {

                        navController.navigate(
                            "role_selection"
                        ) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // RIDER DASHBOARD
            composable("rider_dashboard") {

                RiderDashboard(

                    onLogout = {

                        navController.navigate(
                            "role_selection"
                        ) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
