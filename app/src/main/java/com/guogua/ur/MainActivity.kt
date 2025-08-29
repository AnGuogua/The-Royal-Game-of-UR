package com.guogua.ur

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
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
    GAME_START, DICE_ROLLED, REPAINTING, NO_MOVE
    //分别是游戏开始（还没摇色子）、摇完色子、正在重绘（没有结算）、无法移动（显示NO MOVE）
}

data class GameState(
    val redScore: Int = 0,              // 红方比分
    val blueScore: Int = 0,              // 蓝方比分
    val currentPlayer: Int = 1,         // 当前玩家（1号/2号）
    val redPieces: List<Boolean> = List(15) { it == 0 }, // 红棋子位置数组
    val bluePieces: List<Boolean> = List(15) { it == 0 }, // 蓝棋子位置数组
    val diceNum: Int = 0,
    val cellSize: Float = 0f,
    val step: GameStep = GameStep.GAME_START,
    val redPiecesLeft: Int = 7,
    val bluePiecesLeft: Int = 7
)

class GameViewModel : ViewModel() {
    // 整个游戏状态，用 mutableStateOf 包裹
    var state by mutableStateOf(GameState())
        private set

    // 修改比分
    fun addScore(points: Int, player: Int) {
        state = if (player == 1) {
            state.copy(redScore = state.redScore + points)
        } else {
            state.copy(blueScore = state.blueScore + points)
        }
    }

    // 切换玩家
    fun nextPlayer() {
        state = state.copy(currentPlayer = if (state.currentPlayer == 1) 2 else 1)
    }

    // 更新棋子位置
    fun movePiece(redNewPositions: List<Boolean>, blueNewPositions: List<Boolean>) {
        state = state.copy(redPieces = redNewPositions, bluePieces = blueNewPositions)
    }

    //更新色子点数
    fun rollDice(n: Int) {
        state = state.copy(diceNum = n)
    }

    //设置格子大小
    fun setCellSize(cs: Float) {
        state = state.copy(cellSize = cs)
    }

    //设置游戏阶段
    fun setGameStep(gs: GameStep) {
        state = state.copy(step = gs)
    }

    //设置剩余棋子数
    fun setPiecesLeft(r: Int, b: Int) {
        state = state.copy(redPiecesLeft = r, bluePiecesLeft = b)
    }

    //重置游戏
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            //比分板
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Red, fontSize = 20.sp)) {
                    append("${gameViewModel.state.redScore}")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontSize = 20.sp)) {
                    append(":")
                }
                withStyle(style = SpanStyle(color = Color.Blue, fontSize = 20.sp)) {
                    append("${gameViewModel.state.blueScore}")
                }

            })
            //摇色子的显示框
            Text(
                text = gameViewModel.state.diceNum.toString(),
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
                        var rn = 0
                        for (i in 0..3) {
                            val r = Random.nextInt(2)
                            rn += r
                        }
                        gameViewModel.rollDice(rn)
                        gameViewModel.setGameStep(GameStep.DICE_ROLLED)
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
            LaunchedEffect(gameViewModel.state.step) {
                if (gameViewModel.state.step == GameStep.NO_MOVE) {
                    delay(1500)
                    gameViewModel.nextPlayer()
                    gameViewModel.rollDice(0)
                    gameViewModel.setGameStep(GameStep.GAME_START)
                }
            }
            //NO MOVE 文本框
            if (gameViewModel.state.step == GameStep.NO_MOVE) {
                Text(
                    text = "No Move",
                    fontSize = 24.sp,
                    color = when (gameViewModel.state.currentPlayer) {
                        1 -> Color.Red
                        2 -> Color.Blue
                        else -> Color.LightGray
                    }
                )
            }


        }
    }
}


//棋子（包含移动逻辑）
@Composable
fun Pieces(modifier: Modifier = Modifier, gameViewModel: GameViewModel) {
    var highlight: MutableList<Boolean> = MutableList(15) { false }
    var clickindex by remember {
        mutableIntStateOf(-1)
    }
    var newPosition: Int by remember {
        mutableIntStateOf(-1)
    }
    var showDialog by remember { mutableStateOf(false) }
    var winner by remember {
        mutableIntStateOf(1)
    }
    //运算高亮部分
    if (gameViewModel.state.step == GameStep.DICE_ROLLED) {
        highlight = highlightPieces(gameViewModel)

        //如果摇到0或者没法移动，显示NO MOVE
        if (highlight.all { !it } || gameViewModel.state.diceNum == 0) {
            newPosition = -1
            gameViewModel.setGameStep(GameStep.NO_MOVE)
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
                val col = (offset.x / gameViewModel.state.cellSize).toInt()
                val row = (offset.y / gameViewModel.state.cellSize).toInt()
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

                //计算移动
                newPosition = clickindex + gameViewModel.state.diceNum
                if (newPosition > 15) {
                    return@detectTapGestures
                } else {
                    movePiece(clickindex, newPosition, gameViewModel)
                }

            }
        }

    ) {
        //绘制红色的
        for ((index, value) in gameViewModel.state.redPieces.withIndex()) {
            var piecePosition: Offset
            if (value)//如果有棋子
            {
                piecePosition = when (index) {
                    in 0..4 -> Offset(
                        (3.5f + index) * gameViewModel.state.cellSize,
                        2.5f * gameViewModel.state.cellSize
                    )

                    13, 14 -> Offset(
                        (index - 12.5f) * gameViewModel.state.cellSize,
                        2.5f * gameViewModel.state.cellSize
                    )

                    else -> Offset(
                        (12.5f - index) * gameViewModel.state.cellSize,
                        1.5f * gameViewModel.state.cellSize
                    )

                }

                drawCircle(
                    color = when {
                        (gameViewModel.state.currentPlayer == 2 || !highlight[index]) -> Color.Red
                        else -> Color.Yellow
                    }, center = piecePosition, radius = 0.4f * gameViewModel.state.cellSize
                )
            }
        }

        //绘制蓝色的
        for ((index, value) in gameViewModel.state.bluePieces.withIndex()) {
            var piecePosition: Offset
            if (value)//如果有棋子
            {
                piecePosition = when (index) {
                    in 0..4 -> Offset(
                            (3.5f + index) * gameViewModel.state.cellSize,
                            0.5f * gameViewModel.state.cellSize
                        )

                    13, 14 -> Offset(
                        (index - 12.5f) * gameViewModel.state.cellSize,
                        0.5f * gameViewModel.state.cellSize
                    )


                    else -> Offset(
                        (12.5f - index) * gameViewModel.state.cellSize,
                        1.5f * gameViewModel.state.cellSize
                    )
                }

                drawCircle(
                    color = when {
                        (gameViewModel.state.currentPlayer == 1 || !highlight[index]) -> Color.Blue
                        else -> Color.Yellow
                    }, center = piecePosition, radius = 0.4f * gameViewModel.state.cellSize
                )
            }
        }
    }
    if (gameViewModel.state.step == GameStep.REPAINTING) {
        //棋子出格
        if (newPosition == 15) {
            gameViewModel.addScore(1, gameViewModel.state.currentPlayer)
        }
        //不是保护格则切换
        if (newPosition != 4 && newPosition != 8 && newPosition != 14) {
            gameViewModel.nextPlayer()
        }
        if (gameViewModel.state.redScore == 7) {
            winner = 1
            showDialog = true
        }
        if (gameViewModel.state.blueScore == 7) {
            winner = 2
            showDialog = true
        }
        newPosition = -1
        gameViewModel.rollDice(0)
        gameViewModel.setGameStep(GameStep.GAME_START)
    }
    //若一方到达7分（胜利）
    if (showDialog) {
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

//高亮（判断是否可以走）
fun highlightPieces(gameViewModel: GameViewModel): MutableList<Boolean> {
    val highlight: MutableList<Boolean> = MutableList(15) { false }
    //避免重复写，先把两个数组存起来
    val currentPlayerPieces: List<Boolean> =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.redPieces else gameViewModel.state.bluePieces
    val opponentPieces: List<Boolean> =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.bluePieces else gameViewModel.state.redPieces
    //遍历数组
    for ((index, value) in currentPlayerPieces.withIndex()) {
        //如果该格有棋子
        if (value) {
            // 出格（避免数组越界）
            if (index + gameViewModel.state.diceNum > 15) {
                //如果不是正好到15格，跳过
                continue
            } else if (index + gameViewModel.state.diceNum == 15) {
                highlight[index] = true
            } else if (!currentPlayerPieces[index + gameViewModel.state.diceNum]) {
                //保护格特殊判定
                if (!(index + gameViewModel.state.diceNum == 8 && opponentPieces[8])) {
                    highlight[index] = true
                }

            }
        }
    }
    return highlight
}

//移动逻辑
fun movePiece(clickIndex: Int, newPosition: Int, gameViewModel: GameViewModel) {
    val currentPlayerPieces: MutableList<Boolean> =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.redPieces.toMutableList() else gameViewModel.state.bluePieces.toMutableList()
    val opponentPieces: MutableList<Boolean> =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.bluePieces.toMutableList() else gameViewModel.state.redPieces.toMutableList()
    var currentPlayerPiecesLeft: Int =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.redPiecesLeft else gameViewModel.state.bluePiecesLeft
    var opponentPiecesLeft: Int =
        if (gameViewModel.state.currentPlayer == 1) gameViewModel.state.bluePiecesLeft else gameViewModel.state.redPiecesLeft
    //判断是否可以移出棋盘（15格）
    if (newPosition == 15 && currentPlayerPieces[clickIndex]) {
        currentPlayerPieces[clickIndex] = false
    }
    // 这里无需担心越界，因为前方已经排除newPosition>=15的情况
    // 计算是否可以移动
    else if (currentPlayerPieces[clickIndex] && !currentPlayerPieces[newPosition]) {
        //保护格特殊判定
        if (!(newPosition == 8 && opponentPieces[8])) {
            currentPlayerPieces[clickIndex] = false
            currentPlayerPieces[newPosition] = true
            if (clickIndex == 0) {
                if (currentPlayerPiecesLeft != 1) {
                    currentPlayerPieces[0] = true
                }
                currentPlayerPiecesLeft -= 1
            }
            //踢人
            if (newPosition in 5..12 && opponentPieces[newPosition]) {
                opponentPieces[newPosition] = false
                opponentPiecesLeft += 1
                if (!opponentPieces[0]) {
                    opponentPieces[0] = true
                }
            }

        } else {
            return
        }
    } else {
        return
    }
    //设置新的状态（根据当前玩家来判断）
    if (gameViewModel.state.currentPlayer == 1) {
        gameViewModel.movePiece(currentPlayerPieces, opponentPieces)
        gameViewModel.setPiecesLeft(currentPlayerPiecesLeft, opponentPiecesLeft)
    } else {
        gameViewModel.movePiece(opponentPieces, currentPlayerPieces)
        gameViewModel.setPiecesLeft(opponentPiecesLeft, currentPlayerPiecesLeft)
    }
    gameViewModel.setGameStep(GameStep.REPAINTING)
}