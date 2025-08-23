package com.guogua.ur

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guogua.ur.ui.theme.TheRoyalGameOfURTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheRoyalGameOfURTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    All(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class GameStep {
    //游戏阶段
    GAME_START, DICE_ROLLED, WAITING_MOVE, REPAINTING
}

data class GameState(
    val score_r: Int = 0,              // 红方比分
    val score_b: Int = 0,              // 蓝方比分
    val currentPlayer: Int = 1,         // 当前玩家（1号/2号）
    val pieces_r: List<Boolean> = List(15) { if (it == 0) true else false }, // 红棋子位置数组
    val pieces_b: List<Boolean> = List(15) { if (it == 0) true else false }, // 蓝棋子位置数组
    val dicenum: Int = 0,
    val cellsize: Float = 0f,
    val step: GameStep = GameStep.GAME_START,
    val r_pieces_left: Int = 7,
    val b_pieces_left: Int = 7
)

class GameViewModel : ViewModel() {
    // 整个游戏状态，用 mutableStateOf 包裹
    var state by mutableStateOf(GameState())
        private set

    // 修改比分
    fun addScore(points: Int, player: Int) {
        if (player == 1) {
            state = state.copy(score_r = state.score_r + points)
        } else {
            state = state.copy(score_b = state.score_b + points)
        }
    }

    // 切换玩家
    fun nextPlayer() {
        state = state.copy(currentPlayer = if (state.currentPlayer == 1) 2 else 1)
    }

    // 更新棋子位置
    fun movePiece(newPositions_r: List<Boolean>, newPositions_b: List<Boolean>) {
        state = state.copy(pieces_r = newPositions_r, pieces_b = newPositions_b)
    }

    //更新色子点数
    fun rollDice(n: Int) {
        state = state.copy(dicenum = n)
    }

    //设置格子大小
    fun setCellSize(cs: Float) {
        state = state.copy(cellsize = cs)
    }

    //设置游戏阶段
    fun setGamestep(gs: GameStep) {
        state = state.copy(step = gs)
    }

    //设置剩余棋子数
    fun setPiecesleft(r: Int, b: Int) {
        state = state.copy(r_pieces_left = r, b_pieces_left = b)
    }

    fun resetGame() {
        state = GameState()
    }

}

@Composable
fun All(modifier: Modifier = Modifier, gameViewModel: GameViewModel = viewModel()) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Dice(modifier = Modifier.weight(0.2f, fill = true), gameViewModel = gameViewModel)
            Box(
                modifier = Modifier.weight(0.8f, fill = true), contentAlignment = Alignment.Center
            ) {
                GameBoard(gameViewModel = gameViewModel)
                Pieces(gameViewModel = gameViewModel)

            }
        }

    }
}

@Composable
fun GameBoard(modifier: Modifier = Modifier, gameViewModel: GameViewModel) {
    val game = gameViewModel.state
    Canvas(
        modifier = modifier.aspectRatio(8f / 3f)  // 固定宽高比 8:3
    ) {
        val cellSize = size.width / 8f  // 每格宽度
        val boardHeight = cellSize * 3f   // 总高度
        gameViewModel.setCellSize(cellSize)//写入游戏记录
        // 绘制格子
        for (row in 0 until 3) {
            for (col in 0 until 8) {
                val color = when {
                    row == 0 && col == 2 -> Color.Transparent
                    row == 0 && col == 3 -> Color.Transparent
                    row == 2 && col == 2 -> Color.Transparent
                    row == 2 && col == 3 -> Color.Transparent
                    row == 0 && col == 1 -> Color.Gray
                    row == 2 && col == 1 -> Color.Gray
                    row == 0 && col == 7 -> Color.Gray
                    row == 2 && col == 7 -> Color.Gray
                    row == 1 && col == 4 -> Color.Gray
                    else -> Color.LightGray
                }
                drawRect(
                    color = color,
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }
        }

        // 绘制网格线
        // 水平线
        for (r in 1..2) {
            drawLine(
                color = Color.Black,
                start = Offset(0f, r * cellSize),
                end = Offset(size.width, r * cellSize),
                strokeWidth = 1f.dp.toPx()
            )
        }
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(2 * cellSize, 0f),
            strokeWidth = 1f.dp.toPx()
        )
        drawLine(
            color = Color.Black,
            start = Offset(4 * cellSize, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1f.dp.toPx()
        )
        drawLine(
            color = Color.Black,
            start = Offset(0f, boardHeight),
            end = Offset(2 * cellSize, boardHeight),
            strokeWidth = 1f.dp.toPx()
        )
        drawLine(
            color = Color.Black,
            start = Offset(4 * cellSize, boardHeight),
            end = Offset(size.width, boardHeight),
            strokeWidth = 1f.dp.toPx()
        )


        // 垂直线 (0-8列)
        for (c in (0..2) + (4..8)) {
            drawLine(
                color = Color.Black,
                start = Offset(c * cellSize, 0f),
                end = Offset(c * cellSize, boardHeight),
                strokeWidth = 1f.dp.toPx()
            )
        }
        drawLine(
            color = Color.Black,
            start = Offset(3 * cellSize, cellSize),
            end = Offset(3 * cellSize, 2f * cellSize),
            strokeWidth = 1f.dp.toPx()
        )


    }
}

@Composable
fun Dice(modifier: Modifier = Modifier, gameViewModel: GameViewModel) {
    Box(modifier = modifier
        .background(Color.LightGray, RoundedCornerShape(8.dp))
        .padding(16.dp)
        .aspectRatio(2f / 3f)
        .graphicsLayer {
            rotationZ = when (gameViewModel.state.currentPlayer) {
                1 -> 0f
                2 -> 180f
                else -> 0f
            }
        }) {
        Column {
            //比分板
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Red, fontSize = 20.sp)) {
                    append("${gameViewModel.state.score_r}")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontSize = 20.sp)) {
                    append(":")
                }
                withStyle(style = SpanStyle(color = Color.Blue, fontSize = 20.sp)) {
                    append("${gameViewModel.state.score_b}")
                }
            })
            //摇色子的显示框
            Text(
                text = gameViewModel.state.dicenum.toString(),
                fontSize = 48.sp,
                color = when (gameViewModel.state.currentPlayer) {
                    1 -> Color.Red
                    2 -> Color.Blue
                    else -> Color.LightGray
                }
            )
            //按钮
            Button(
                onClick = {
                    if (gameViewModel.state.step == GameStep.GAME_START) {
                        var rn: Int = 0
                        for (i in 0..3) {
                            val r = Random.nextInt(2)
                            rn += r
                        }
                        gameViewModel.rollDice(rn)
                        gameViewModel.setGamestep(GameStep.DICE_ROLLED)
                    }


                }, colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        gameViewModel.state.step == GameStep.DICE_ROLLED -> Color.Gray
                        gameViewModel.state.step == GameStep.GAME_START && gameViewModel.state.currentPlayer == 1 -> Color.Red
                        gameViewModel.state.step == GameStep.GAME_START && gameViewModel.state.currentPlayer == 2 -> Color.Blue
                        else -> Color.Gray
                    }

                )
            ) { Text("Roll") }
        }
    }
}


//棋子（包含移动逻辑）
@Composable
fun Pieces(modifier: Modifier = Modifier, gameViewModel: GameViewModel) {
    val red: MutableList<Boolean> = gameViewModel.state.pieces_r.toMutableList()
    val blue: MutableList<Boolean> = gameViewModel.state.pieces_b.toMutableList()
    val highlight: MutableList<Boolean> = List(15) { false }.toMutableList()
    var clickindex by remember {
        mutableIntStateOf(-1)
    }
    var np: Int by remember {
        mutableIntStateOf(-1)
    }
    var showDialog by remember { mutableStateOf(false) }
    var winner by remember {
        mutableIntStateOf(1)
    }
    //运算高亮部分
    if (gameViewModel.state.step == GameStep.DICE_ROLLED) {
        var i: Int = 0
        if (gameViewModel.state.currentPlayer == 1) {
            for ((index, value) in red.withIndex()) {
                if (value) {
                    if (index + gameViewModel.state.dicenum > 14) {
                        if (index + gameViewModel.state.dicenum == 15) {
                            highlight[index] = true
                            i++
                        }

                        continue
                    }
                    if (!red[index + gameViewModel.state.dicenum]) {
                        if (!(index + gameViewModel.state.dicenum == 8 && blue[8])) {
                            highlight[index] = true
                            i++
                        }

                    }
                }

            }
        }
        if (gameViewModel.state.currentPlayer == 2) {
            for ((index, value) in blue.withIndex()) {
                if (value) {
                    if (index + gameViewModel.state.dicenum > 14) {
                        if (index + gameViewModel.state.dicenum == 15) {
                            highlight[index] = true
                            i++
                        }
                        continue
                    }
                    if (!blue[index + gameViewModel.state.dicenum]) {
                        if (!(index + gameViewModel.state.dicenum == 8 && red[8])) {
                            highlight[index] = true
                            i++
                        }
                    }
                }

            }
        }
        Log.d("GUOGUA", "$i")
        if (i == 0 || gameViewModel.state.dicenum == 0) {
            gameViewModel.nextPlayer()
            gameViewModel.setGamestep(GameStep.GAME_START)
            return
        }
    }
    Canvas(modifier = modifier
        .aspectRatio(8f / 3f)
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                if (gameViewModel.state.step != GameStep.DICE_ROLLED) {
                    return@detectTapGestures
                }
                val col = (offset.x / gameViewModel.state.cellsize).toInt()
                val row = (offset.y / gameViewModel.state.cellsize).toInt()
                //转换坐标
                if (gameViewModel.state.currentPlayer == 1 && row != 0) {
                    if (row == 1) {
                        clickindex = 12 - col
                    }
                    if (row == 2) {
                        if (col >= 3) {
                            clickindex = col - 3
                        }

                        if (col <= 1) {
                            clickindex = col + 13
                        }
                    }
                }
                if (gameViewModel.state.currentPlayer == 2 && row != 2) {

                    if (row == 1) {
                        clickindex = 12 - col
                    }
                    if (row == 0) {
                        if (col >= 3) {
                            clickindex = col - 3
                        }
                        if (col <= 1) {
                            clickindex = col + 13
                        }
                    }
                }
                np = clickindex + gameViewModel.state.dicenum
                if (np > 15) {
                    return@detectTapGestures
                }
                if (np == 15 && ((gameViewModel.state.currentPlayer == 1 && red[clickindex]) || (gameViewModel.state.currentPlayer == 2 && blue[clickindex]))) {
                    if (gameViewModel.state.currentPlayer == 1) red[clickindex] =
                        false else blue[clickindex] = false
                    gameViewModel.movePiece(red, blue)
                    gameViewModel.setGamestep(GameStep.REPAINTING)
                }
                //计算是否可以移动
                else if (gameViewModel.state.currentPlayer == 1 && red[clickindex] && !red[clickindex + gameViewModel.state.dicenum]) {
                    if (!(clickindex + gameViewModel.state.dicenum == 8 && blue[8]) && clickindex + gameViewModel.state.dicenum in 0..15) {
                        red[clickindex] = false
                        if (np in 1..14) {
                            red[np] = true
                            if (clickindex == 0) {
                                if (gameViewModel.state.r_pieces_left == 1) {
                                    red[0] = false
                                    gameViewModel.setPiecesleft(
                                        0, gameViewModel.state.b_pieces_left
                                    )
                                } else {
                                    red[0] = true
                                }
                            }
                            if (np in 5..12 && blue[np]) {
                                blue[np] = false
                                if (!blue[0]) {
                                    blue[0] = true
                                }
                                gameViewModel.setPiecesleft(
                                    gameViewModel.state.r_pieces_left,
                                    gameViewModel.state.b_pieces_left + 1
                                )
                            }
                        }
                        gameViewModel.movePiece(red, blue)
                        gameViewModel.setGamestep(GameStep.REPAINTING)
                    }
                } else if (gameViewModel.state.currentPlayer == 2 && blue[clickindex] && !blue[clickindex + gameViewModel.state.dicenum]) {
                    if (!(clickindex + gameViewModel.state.dicenum == 8 && red[8])) {
                        blue[clickindex] = false
                        if (np in 1..14) {
                            blue[np] = true
                            if (clickindex == 0) {
                                if (gameViewModel.state.b_pieces_left == 1) {
                                    blue[0] = false
                                    gameViewModel.setPiecesleft(
                                        gameViewModel.state.r_pieces_left, 0
                                    )
                                } else {
                                    blue[0] = true
                                }
                            }
                            if (np in 5..12 && red[np]) {
                                red[np] = false
                                if (!red[0]) {
                                    red[0] = true
                                }
                                gameViewModel.setPiecesleft(
                                    gameViewModel.state.r_pieces_left + 1,
                                    gameViewModel.state.b_pieces_left
                                )
                            }
                        }

                        gameViewModel.movePiece(red, blue)
                        gameViewModel.setGamestep(GameStep.REPAINTING)

                    }
                }


            }
        }

    ) {
        for ((index, value) in gameViewModel.state.pieces_r.withIndex()) {
            if (value)//如果有棋子
            {
                if (index in 0..4) {
                    drawCircle(
                        color = when {
                            (gameViewModel.state.currentPlayer == 2 || !highlight[index]) -> Color.Red
                            else -> Color.Yellow
                        }, center = Offset(
                            (3.5f + index) * gameViewModel.state.cellsize,
                            2.5f * gameViewModel.state.cellsize
                        ), radius = 0.4f * gameViewModel.state.cellsize
                    )
                } else if (index == 13 || index == 14) {
                    drawCircle(
                        color = when {
                            (gameViewModel.state.currentPlayer == 2 || !highlight[index]) -> Color.Red
                            else -> Color.Yellow
                        }, center = Offset(
                            (index - 12.5f) * gameViewModel.state.cellsize,
                            2.5f * gameViewModel.state.cellsize
                        ), radius = 0.4f * gameViewModel.state.cellsize
                    )
                } else {
                    drawCircle(
                        color = when {
                            (gameViewModel.state.currentPlayer == 2 || !highlight[index]) -> Color.Red
                            else -> Color.Yellow
                        }, center = Offset(
                            (12.5f - index) * gameViewModel.state.cellsize,
                            1.5f * gameViewModel.state.cellsize
                        ), radius = 0.4f * gameViewModel.state.cellsize
                    )
                }
            }

            for ((index, value) in gameViewModel.state.pieces_b.withIndex()) {
                if (value)//如果有棋子
                {
                    if (index in 0..4) {
                        drawCircle(
                            color = when {
                                (gameViewModel.state.currentPlayer == 1 || !highlight[index]) -> Color.Blue
                                else -> Color.Yellow
                            }, center = Offset(
                                (3.5f + index) * gameViewModel.state.cellsize,
                                0.5f * gameViewModel.state.cellsize
                            ), radius = 0.4f * gameViewModel.state.cellsize
                        )
                    } else if (index == 13 || index == 14) {
                        drawCircle(
                            color = when {
                                (gameViewModel.state.currentPlayer == 1 || !highlight[index]) -> Color.Blue
                                else -> Color.Yellow
                            }, center = Offset(
                                (index - 12.5f) * gameViewModel.state.cellsize,
                                0.5f * gameViewModel.state.cellsize
                            ), radius = 0.4f * gameViewModel.state.cellsize
                        )
                    } else {
                        drawCircle(
                            color = when {
                                (gameViewModel.state.currentPlayer == 1 || !highlight[index]) -> Color.Blue
                                else -> Color.Yellow
                            }, center = Offset(
                                (12.5f - index) * gameViewModel.state.cellsize,
                                1.5f * gameViewModel.state.cellsize
                            ), radius = 0.4f * gameViewModel.state.cellsize
                        )
                    }
                }
            }
        }
    }
    if (gameViewModel.state.step == GameStep.REPAINTING) {
        if (clickindex == 0) {
            if (gameViewModel.state.currentPlayer == 1 && gameViewModel.state.r_pieces_left != 0) {
                gameViewModel.setPiecesleft(
                    gameViewModel.state.r_pieces_left - 1, gameViewModel.state.b_pieces_left
                )
            }
            if (gameViewModel.state.currentPlayer == 2 && gameViewModel.state.b_pieces_left != 0) {
                gameViewModel.setPiecesleft(
                    gameViewModel.state.r_pieces_left, gameViewModel.state.b_pieces_left - 1
                )
            }

        }
        if (np == 15) {
            gameViewModel.addScore(1, gameViewModel.state.currentPlayer)
        }
        if (np != 4 && np != 8 && np != 14) {
            gameViewModel.nextPlayer()
        }
        if (gameViewModel.state.score_r == 7) {
            winner = 1
            showDialog = true
        }
        if (gameViewModel.state.score_b == 7) {
            winner = 2
            showDialog = true
        }
        np = -1
        gameViewModel.rollDice(0)
        gameViewModel.setGamestep(GameStep.GAME_START)
    }
    if (showDialog) {
        Log.d("GUOGUA", "$showDialog")
        AlertDialog(
            title = { Text("Congratulations") },
            text = { Text(if (winner == 1) "Red wins!!" else "Blue wins!!") },
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        gameViewModel.resetGame()
                        showDialog = false
                    }
                )
                {
                    Text("OK")
                }
            })
    }
}