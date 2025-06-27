import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack; // 添加Stack类的导入

// 这个类负责处理AI的逻辑，包括不同难度下的落子策略和评估函数
class AILogic {
    private static final int BOARD_SIZE = 15;

    // 简单AI策略，优先在自己能连成线的地方落子，其次阻止对手连成线
    public static Move findBestMoveEasy(Boolean[][] board) {
        Move bestMove = null;
        int bestScore = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    // 计算AI自己落子的分数
                    board[i][j] = false;
                    int aiScore = evaluatePosition(i, j, false, board);
                    board[i][j] = null;

                    // 计算阻止对手落子的分数
                    board[i][j] = true;
                    int playerScore = evaluatePosition(i, j, true, board);
                    board[i][j] = null;

                    // 取较大的分数
                    int score = Math.max(aiScore, playerScore);

                    // 如果分数更高，或者分数相同但更靠近中心，则选择这个位置
                    if (score > bestScore || (score == bestScore && isCloserToCenter(i, j, bestMove))) {
                        bestScore = score;
                        bestMove = new Move(i, j, false);
                    }
                }
            }
        }

        return bestMove;
    }

    // 中等AI策略，考虑两步之后的情况
    public static Move findBestMoveMedium(Boolean[][] board, Stack<Move> moveHistory) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        // 尝试每个可能的位置
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    // 模拟AI落子
                    board[i][j] = false;
                    moveHistory.push(new Move(i, j, false));

                    // 评估这个位置
                    int score = minimax(1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, board, moveHistory);

                    // 撤销模拟落子
                    board[i][j] = null;
                    moveHistory.pop();

                    // 如果分数更高，或者分数相同但更靠近中心，则选择这个位置
                    if (score > bestScore || (score == bestScore && isCloserToCenter(i, j, bestMove))) {
                        bestScore = score;
                        bestMove = new Move(i, j, false);
                    }
                }
            }
        }

        return bestMove;
    }

    // 困难AI策略，考虑三步之后的情况，并且有一定的随机性
    public static Move findBestMoveHard(Boolean[][] board, Stack<Move> moveHistory) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();

        // 尝试每个可能的位置
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    // 模拟AI落子
                    board[i][j] = false;
                    moveHistory.push(new Move(i, j, false));

                    // 评估这个位置
                    int score = minimax(2, false, Integer.MIN_VALUE, Integer.MAX_VALUE, board, moveHistory);

                    // 撤销模拟落子
                    board[i][j] = null;
                    moveHistory.pop();

                    // 如果分数更高，则清空最佳列表并添加这个位置
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear();
                        bestMoves.add(new Move(i, j, false));
                    } 
                    // 如果分数相同，则添加到最佳列表
                    else if (score == bestScore) {
                        bestMoves.add(new Move(i, j, false));
                    }
                }
            }
        }

        // 从最佳列表中随机选择一个位置，增加一些随机性
        if (!bestMoves.isEmpty()) {
            Random random = new Random();
            bestMove = bestMoves.get(random.nextInt(bestMoves.size()));
        }

        return bestMove;
    }

    // 判断位置是否更靠近中心
    public static boolean isCloserToCenter(int x, int y, Move move) {
        if (move == null) {
            return true;
        }

        int center = (BOARD_SIZE - 1) / 2;
        int dist1 = Math.abs(x - center) + Math.abs(y - center);
        int dist2 = Math.abs(move.x - center) + Math.abs(move.y - center);

        return dist1 < dist2;
    }

    // 极小极大算法，带Alpha-Beta剪枝
    private static int minimax(int depth, boolean isMaximizing, int alpha, int beta, Boolean[][] board, Stack<Move> moveHistory) {
        // 检查游戏是否结束或达到最大深度
        if (depth == 0) {
            return evaluateBoard(board);
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;

            // 尝试每个可能的位置
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] == null) {
                        // 模拟AI落子
                        board[i][j] = false;

                        // 递归评估
                        int score = minimax(depth - 1, false, alpha, beta, board, moveHistory);

                        // 撤销模拟落子
                        board[i][j] = null;

                        bestScore = Math.max(score, bestScore);
                        alpha = Math.max(alpha, bestScore);

                        // Alpha-Beta剪枝
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }

                if (beta <= alpha) {
                    break;
                }
            }

            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;

            // 尝试每个可能的位置
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] == null) {
                        // 模拟玩家落子
                        board[i][j] = true;

                        // 递归评估
                        int score = minimax(depth - 1, true, alpha, beta, board, moveHistory);

                        // 撤销模拟落子
                        board[i][j] = null;

                        bestScore = Math.min(score, bestScore);
                        beta = Math.min(beta, bestScore);

                        // Alpha-Beta剪枝
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }

                if (beta <= alpha) {
                    break;
                }
            }

            return bestScore;
        }
    }

    // 评估整个棋盘的分数
    private static int evaluateBoard(Boolean[][] board) {
        int aiScore = 0;
        int playerScore = 0;

        // 评估每个位置
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    if (board[i][j]) {
                        // 玩家棋子
                        playerScore += evaluatePosition(i, j, true, board);
                    } else {
                        // AI棋子
                        aiScore += evaluatePosition(i, j, false, board);
                    }
                }
            }
        }

        return aiScore - playerScore;
    }

    // 评估单个位置的分数
    public static int evaluatePosition(int x, int y, boolean isPlayer, Boolean[][] board) {
        Boolean player = isPlayer ? true : false;
        int score = 0;

        // 定义方向数组：水平、垂直、左上到右下、右上到左下
        int[][] directions = {
            {1, 0}, {0, 1}, {1, 1}, {1, -1}
        };

        // 检查每个方向
        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            // 连续相同棋子的数量
            int count = 1;
            // 两端可延伸的空格数
            int spaceLeft = 0;
            int spaceRight = 0;

            // 检查正方向
            for (int i = 1; i < 5; i++) {
                int nx = x + dx * i;
                int ny = y + dy * i;

                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
                    if (board[nx][ny] == player) {
                        count++;
                    } else if (board[nx][ny] == null) {
                        spaceRight++;
                        break;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            // 检查反方向
            for (int i = 1; i < 5; i++) {
                int nx = x - dx * i;
                int ny = y - dy * i;

                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
                    if (board[nx][ny] == player) {
                        count++;
                    } else if (board[nx][ny] == null) {
                        spaceLeft++;
                        break;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            // 根据连续棋子数量和可延伸性评分
            if (count == 5) {
                // 五连，赢
                return 100000;
            } else if (count == 4) {
                // 四连
                if (spaceLeft + spaceRight >= 1) {
                    // 活四或冲四
                    score += 10000;
                }
            } else if (count == 3) {
                // 三连
                if (spaceLeft + spaceRight >= 2) {
                    // 活三
                    score += 1000;
                } else if (spaceLeft + spaceRight >= 1) {
                    // 冲三
                    score += 100;
                }
            } else if (count == 2) {
                // 两连
                if (spaceLeft + spaceRight >= 3) {
                    // 活二
                    score += 10;
                } else if (spaceLeft + spaceRight >= 2) {
                    // 冲二
                    score += 5;
                }
            }
        }

        return score;
    }
}