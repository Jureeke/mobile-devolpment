package edu.ap.project

import UserViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import edu.ap.project.screens.AddScreen
import edu.ap.project.screens.DetailScreen
import edu.ap.project.screens.HomeScreen
import edu.ap.project.screens.ListScreen
import edu.ap.project.screens.MapScreen
import edu.ap.project.screens.RentScreen
import edu.ap.project.theme.ProjectTheme

class MainActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("list") { ListScreen(navController = navController) }
                        composable("add") { AddScreen() }
                        composable("map") { MapScreen(navController = navController) }
                        composable("profile") { ProfileScreen()}
                        composable(
                            "detail/{itemId}",
                            arguments = listOf(navArgument("itemId") { nullable = false })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                            DetailScreen(itemId = itemId, navController = navController)
                        }
                        composable(
                            "rent/{itemId}",
                            arguments = listOf(navArgument("itemId") { nullable = false })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                            RentScreen(itemId = itemId)
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "List") },
            label = { Text("List") },
            selected = currentRoute == "List",
            onClick = {
                navController.navigate("list") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
            label = { Text("Toevoegen") },
            selected = currentRoute == "Add",
            onClick = {
                navController.navigate("add") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentRoute == "Map",
            onClick = {
                navController.navigate("map") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreens() {
    ProjectTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Home Screen Preview")
        }
    }
}
