package com.example.labyrinthmarblegame

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var gameLogic: MarbleGameLogic
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // Lock orientation?

        val database = MarbleGameDatabase.getDatabase(applicationContext)
        val repository = MarbleGameRepository(database.marbleGameDao())

        val viewModel = ViewModelProvider(
            this,
            MarbleGameViewModelFactory(repository)
        )[MarbleGameViewModel::class.java]

        // Initialize the game
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gameLogic = MarbleGameLogic(this, viewModel)

        // Initialize sounds
        initBGM(this)
        initSounds(this)
        bgmPlayer.start()

        // Insert test scores when app launches
        //insertTestScores(viewModel)

        setContent {
            AppNavigation(viewModel, gameLogic)
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

    override fun onPause() {
        super.onPause()
        if (bgmPlayer.isPlaying) {
            bgmPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bgmPlayer.isPlaying) {
            bgmPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bgmPlayer.release()
        soundPool.release()
    }
}


@Composable
fun AppNavigation(viewModel: MarbleGameViewModel, gameLogic: MarbleGameLogic) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("game") { GameScreen(navController, gameLogic, viewModel) }
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
fun GameScreen(navController: NavController, gameLogic: MarbleGameLogic, viewModel: MarbleGameViewModel) {
    AndroidView(
        factory = { context ->
            MarbleGameView(context, viewModel).apply {
                // Set the navigation callback
                setNavigationCallback {
                    navController.navigate("highscores") {
                        // Pop up to the home destination
                        popUpTo("home") {
                            inclusive = false
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun HighscoreScreen(viewModel: MarbleGameViewModel) {
    val scores by viewModel.highScores.collectAsState(initial = emptyList())
    val currentPlayerName by viewModel.playerName.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Local state for updating the player's name.
    var playerName by remember { mutableStateOf(currentPlayerName) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp)
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

            // Headers row includes ranking
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp)
                )
                Text(
                    text = "Player",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Time (sec)",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn for all scores, sorted by fastest time
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val sortedScores = scores.sortedBy { it.completionTime }
                itemsIndexed(sortedScores) { index, score ->
                    // Use the current player name from the ViewModel to check if this is the player's score
                    val isPlayerScore = score.playerName == currentPlayerName
                    val backgroundColor = if (isPlayerScore) Color(0xFF1E3F66) else Color.Transparent
                    val textColor = if (isPlayerScore) Color.Yellow else Color.White

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(backgroundColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ranking number column
                        Text(
                            text = "${index + 1}",
                            fontSize = 18.sp,
                            color = textColor,
                            modifier = Modifier.width(30.dp)
                        )
                        // Player name column
                        Text(
                            text = score.playerName,
                            fontSize = 18.sp,
                            color = textColor,
                            fontWeight = if (isPlayerScore) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        // Completion time column
                        Text(
                            text = "${score.completionTime}s",
                            fontSize = 18.sp,
                            color = textColor,
                            fontWeight = if (isPlayerScore) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Row at the bottom for clear button and player name change
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Change player name
            Column {
                Text("Player Name", color = Color.White)
                TextField(
                    value = playerName,
                    onValueChange = {
                        playerName = it
                        coroutineScope.launch {
                            viewModel.updatePlayerName(it) // Update name instantly
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    modifier = Modifier.width(200.dp)
                )
            }

            // Floating Action Button (FAB) to clear scores
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.clearScores()
                    }
                },
                containerColor = Color.Red
            ) {
                Text("Clear", color = Color.White)
            }
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
