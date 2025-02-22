package com.example.labyrinthmarblegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MarbleGameDatabase.getDatabase(applicationContext)
        val repository = MarbleGameRepository(database.marbleGameDao())

        val viewModel = ViewModelProvider(
            this,
            MarbleGameViewModelFactory(repository)
        )[MarbleGameViewModel::class.java]

        // Insert test scores when app launches
        // Comment out if you don't need
        insertTestScores(viewModel)

        setContent {
            AppNavigation(viewModel)
        }
    }

    private fun insertTestScores(viewModel: MarbleGameViewModel) {
        lifecycleScope.launch {
            viewModel.insertScore(MarbleGameScore(level = 1, completionTime = 60, playerName = "Yee Lei"))
            viewModel.insertScore(MarbleGameScore(level = 1, completionTime = 25, playerName = "William"))
            viewModel.insertScore(MarbleGameScore(level = 1, completionTime = 40, playerName = "Charlie"))
            viewModel.insertScore(MarbleGameScore(level = 1, completionTime = 20, playerName = "David"))
            viewModel.insertScore(MarbleGameScore(level = 1, completionTime = 35, playerName = "Eve"))
        }
    }
}


@Composable
fun AppNavigation(viewModel: MarbleGameViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("game") { GameScreen() }
        composable("highscores") { HighscoreScreen(viewModel = viewModel) }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with actual image
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Text
            Text(
                text = "Labyrinth",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Play Button
            Button(
                onClick = { navController.navigate("game") },
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Play", fontSize = 20.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Highscores Button
            Button(
                onClick = { navController.navigate("highscores") },
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Highscores", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun GameScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Game Screen (Coming Soon)",
            fontSize = 30.sp,
            color = Color.White
        )
    }
}

@Composable
fun HighscoreScreen(viewModel: MarbleGameViewModel) {
    val scores by viewModel.highScores.collectAsState(initial = emptyList()) // Observe scores
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp) // Add padding to avoid overlap
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Highscores",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Display scores in a LazyColumn sorted in descending order
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(scores.sortedByDescending { it.completionTime }) { score ->
                    ScoreItem(score)
                }
            }
        }

        // Floating Action Button (FAB) to clear scores
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    viewModel.clearScores()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Red
        ) {
            Text("Clear", color = Color.White)
        }
    }
}

@Composable
fun ScoreItem(score: MarbleGameScore) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = score.playerName, fontSize = 18.sp, color = Color.White)
        Text(text = "${score.completionTime}s", fontSize = 18.sp, color = Color.Yellow)
    }
}
