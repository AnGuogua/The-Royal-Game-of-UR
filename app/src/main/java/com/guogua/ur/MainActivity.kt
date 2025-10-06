package com.guogua.ur

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guogua.ur.ui.theme.TheRoyalGameOfURTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawBehind

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
    val redPieces: List<Boolean> = List(15) { it == 0 }, // 红棋子位置数组 注意：下标从1开始
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
    var columnSize:IntSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    Column(
        modifier = Modifier
            .aspectRatio(8f / 3f)
            .onGloballyPositioned { layoutCoordinates ->
                columnSize = layoutCoordinates.size
            }
    ){
        val cellSize = columnSize.width / 8f  // 每格宽度
        val boardHeight = cellSize * 3f   // 总高度
        gameViewModel.setCellSize(cellSize)//写入游戏记录

        val board = listOf(
            listOf(R.drawable.gameboard_13,R.drawable.gameboard_14,null,null,R.drawable.gameboard_1,R.drawable.gameboard_2,R.drawable.gameboard_3,R.drawable.gameboard_4),
            listOf(R.drawable.gameboard_12,R.drawable.gameboard_11,R.drawable.gameboard_10,R.drawable.gameboard_9,R.drawable.gameboard_8,R.drawable.gameboard_7,R.drawable.gameboard_6,R.drawable.gameboard_5),
            listOf(R.drawable.gameboard_13,R.drawable.gameboard_14,null,null,R.drawable.gameboard_1,R.drawable.gameboard_2,R.drawable.gameboard_3,R.drawable.gameboard_4),
        )
        board.forEachIndexed{rowID,row->
            Row(Modifier.weight(1f))
            {
                row.forEachIndexed{colID,resID->


                    Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(if (resID == null) Color.Transparent else Color(245, 199, 127))
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val color = Color.Black

                            // 上边
                            //如果是0行2、3列不画
                            if (!(rowID == 0 && (colID == 2 || colID == 3))) {
                                drawLine(
                                    color = color,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                            }
                            // 左边
                            //0、2行3列不画
                            if (!(colID == 3 && (rowID == 0 || rowID == 2))) {
                                drawLine(
                                    color = color,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                            //下边
                            //第2行画，排除2，3
                            if (rowID == 2 && colID != 2 && colID != 3) {
                                drawLine(
                                    color = color,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                            //右边
                            if (colID == 7 && !(rowID == 3 && (colID == 0 || colID == 2))) {
                                drawLine(
                                    color = color,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                        }
                    )
                    {
                        if(resID!=null)//有东西
                        {
                            Image(
                                painter = painterResource(id = resID),
                                modifier = Modifier
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Fit,//适合大小
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
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
            }
            )
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
                        var randomNumber = 0
                        for (i in 0..3) {
                            val r = Random.nextInt(2)
                            randomNumber += r
                        }
                        gameViewModel.rollDice(randomNumber)
                        Log.d("GAMETEST","[ROLL]currentPlayer:${gameViewModel.state.currentPlayer} diceNum:$randomNumber")
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
    var newPosition: Int by remember {
        mutableIntStateOf(-1)
    }
    var showDialog by remember { mutableStateOf(false) }
    var winner by remember {
        mutableIntStateOf(1)
    }
    var columnSize:IntSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    //运算高亮部分
    if (gameViewModel.state.step == GameStep.DICE_ROLLED) {
        highlight = highlightPieces(gameViewModel)

        //如果摇到0或者没法移动，显示NO MOVE
        if (highlight.all { !it } || gameViewModel.state.diceNum == 0) {
            newPosition = -1
            gameViewModel.setGameStep(GameStep.NO_MOVE)
        }

    }

    Column(
        modifier = Modifier
            .aspectRatio(8f / 3f)
            .onGloballyPositioned { layoutCoordinates ->
                columnSize = layoutCoordinates.size
            }
    ) {
        val piece: List<List<Int>> = listOf(
            listOf(13, 14, -1, 0, 1, 2, 3, 4),
            listOf(12, 11, 10, 9, 8, 7, 6, 5),
            listOf(13, 14, -1, 0, 1, 2, 3, 4)
        )
        piece.forEachIndexed {rowID, row ->
            Row(Modifier.weight(1f))
            {
                row.forEach { boardID ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(Color.Transparent)
                            .clickable (
                                indication = null, //  关闭水波纹
                                interactionSource = remember { MutableInteractionSource() }// 必须传，否则会报错
                            )
                            {
                                //满足的条件：可以被移动+是我方棋子
                                if(highlight[boardID]&&((gameViewModel.state.currentPlayer==1&&rowID in 1..2)||(gameViewModel.state.currentPlayer==2&&rowID in 0..1)))
                                {
                                    newPosition = boardID+gameViewModel.state.diceNum
                                    movePiece(boardID,gameViewModel)
                                }
                            }
                    )
                    {
                        if(boardID == -1)
                        {
                            return@Box
                        }
                        var resID = when{
                            rowID in 1..2 && gameViewModel.state.redPieces[boardID] -> R.drawable.piece_red
                            rowID in 0..1 && gameViewModel.state.bluePieces[boardID] ->R.drawable.piece_blue
                            else->null
                        }
                        if(resID!=null)
                        {
                            resID = when
                            {
                                gameViewModel.state.currentPlayer == 1&&resID == R.drawable.piece_red&&highlight[boardID]->R.drawable.piece_red_highlight
                                gameViewModel.state.currentPlayer == 2&&resID == R.drawable.piece_blue&&highlight[boardID]->R.drawable.piece_blue_highlight
                                else->resID
                            }
                            Log.d("GAMETEST","[PAINT]currentPlayer:${gameViewModel.state.currentPlayer} rowID:$rowID boardID:$boardID")
                            Image(
                                painter = painterResource(id = resID),
                                modifier = Modifier
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Fit,//适合大小
                                contentDescription = null
                            )

                        }
                    }
                }
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
        //获胜
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
                        Log.d("GAMETEST","[RESET]")
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
    Log.d("GAMETEST","[HIGHLIGHT]currentPlayer:${gameViewModel.state.currentPlayer} highlight:$highlight")
    return highlight
}

//移动逻辑
fun movePiece(clickIndex: Int, gameViewModel: GameViewModel) {
    val newPosition:Int = clickIndex+gameViewModel.state.diceNum
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
    // 且已经判定过是否可以移动
    else {
        currentPlayerPieces[clickIndex] = false
        currentPlayerPieces[newPosition] = true
        if (clickIndex == 0) {
            if (currentPlayerPiecesLeft != 1) {
                currentPlayerPieces[0] = true
            }
            currentPlayerPiecesLeft -= 1
            Log.d("GAMETEST","[MOVEPIECE]currentPlayer:${gameViewModel.state.currentPlayer} piecesLeft:$currentPlayerPiecesLeft")
        }
        //踢人
        if (newPosition in 5..12 && opponentPieces[newPosition]) {
            opponentPieces[newPosition] = false
            opponentPiecesLeft += 1
            if (!opponentPieces[0]) {
                opponentPieces[0] = true
            }
        }
    }
    //设置新的状态（根据当前玩家来判断）
    if (gameViewModel.state.currentPlayer == 1) {
        gameViewModel.movePiece(currentPlayerPieces, opponentPieces)
        gameViewModel.setPiecesLeft(currentPlayerPiecesLeft, opponentPiecesLeft)
    } else {
        gameViewModel.movePiece(opponentPieces, currentPlayerPieces)
        gameViewModel.setPiecesLeft(opponentPiecesLeft, currentPlayerPiecesLeft)
    }
    Log.d("GAMETEST","[MOVEPIECE]currentPlayer:${gameViewModel.state.currentPlayer} Piece $clickIndex move to $newPosition  Array:$currentPlayerPieces")
    gameViewModel.setGameStep(GameStep.REPAINTING)
}
