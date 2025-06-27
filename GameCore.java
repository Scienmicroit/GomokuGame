import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// 这个类负责游戏的核心逻辑，包括初始化棋盘、落子、检查胜负、悔棋等操作
class GameCore {
    private static final int BOARD_SIZE = 15;
    private static final int UNDO_LIMIT = 10;
    private GomokuGameUI gameUI;

    // 当前玩家，true为黑棋，false为白棋
    private boolean currentPlayer = true;
    // 游戏是否结束
    private boolean gameOver = false;
    // 棋盘状态，null表示空，true表示黑棋，false表示白棋
    private Boolean[][] board = new Boolean[BOARD_SIZE][BOARD_SIZE];
    // 历史记录，用于悔棋
    private Stack<Move> moveHistory = new Stack<>();
    // 人机对战模式
    private boolean aiMode = false;
    // AI难度，1-3级
    private int aiDifficulty = 2;

    // 记录获胜的连珠位置
    private List<Move> winningMoves = new ArrayList<>();

    // 提示的落子位置
    private Move hintMove = null;

    // 初始化棋盘
    public void initBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = null;
            }
        }
        currentPlayer = true;
        gameOver = false;
        moveHistory.clear();
        winningMoves.clear(); // 清空获胜连珠记录
        hintMove = null; // 清空提示
    }

    // 落子
    public boolean placeStone(int x, int y) {
        // 检查坐标是否在棋盘内
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }

        // 检查该位置是否已经有棋子
        if (board[x][y] != null) {
            return false;
        }

        // 检查游戏是否结束
        if (gameOver) {
            return false;
        }

        // 落子
        board[x][y] = currentPlayer;
        moveHistory.push(new Move(x, y, currentPlayer));
        hintMove = null; // 落子后清空提示

        // 检查是否有玩家获胜
        if (checkWin(x, y)) {
            gameOver = true;
            JOptionPane.showMessageDialog(null,
                    (currentPlayer ? "黑棋" : "白棋") + "获胜！",
                    "游戏结束",
                    JOptionPane.INFORMATION_MESSAGE);
        if (gameUI != null) {
            gameUI.getBoardPanel().repaint();
            gameUI.getStatusPanel().updateStatus();
        }
            return true;
        }

        // 检查是否平局
        if (checkDraw()) {
            gameOver = true;
            JOptionPane.showMessageDialog(null, "平局！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            if (gameUI != null) {
                gameUI.getBoardPanel().repaint();
                gameUI.getStatusPanel().updateStatus(); // 更新状态面板
            }
            return true;
        }

        // 切换玩家
        currentPlayer = !currentPlayer;

        return true;
    }

    // 添加设置游戏界面的方法
    public void setGameUI(GomokuGameUI gameUI) {
        this.gameUI = gameUI;
    }
    // AI落子
    public void makeAIMove() {
        // 根据AI难度选择不同的策略
        Move bestMove = null;

        switch (aiDifficulty) {
            case 1:
                bestMove = AILogic.findBestMoveEasy(board);
                break;
            case 2:
                bestMove = AILogic.findBestMoveMedium(board, moveHistory);
                break;
            case 3:
                bestMove = AILogic.findBestMoveHard(board, moveHistory);
                break;
            default:
                bestMove = AILogic.findBestMoveMedium(board, moveHistory);
        }

        if (bestMove != null) {
            board[bestMove.x][bestMove.y] = currentPlayer;
            moveHistory.push(bestMove);
            hintMove = null; // AI落子后清空提示

            // 检查是否有玩家获胜
            if (checkWin(bestMove.x, bestMove.y)) {
                gameOver = true;
                JOptionPane.showMessageDialog(null,
                        "白棋(AI)获胜！",
                        "游戏结束",
                        JOptionPane.INFORMATION_MESSAGE);
                if (gameUI != null) {
                    gameUI.getBoardPanel().repaint();
                    gameUI.getStatusPanel().updateStatus(); // 更新状态面板
                }
                return;
            }

            // 检查是否平局
            if (checkDraw()) {
                gameOver = true;
                JOptionPane.showMessageDialog(null, "平局！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                if (gameUI != null) {
                    gameUI.getBoardPanel().repaint();
                    gameUI.getStatusPanel().updateStatus(); // 更新状态面板
                }
                return;
            }

            // 切换玩家
            currentPlayer = !currentPlayer;
        }
    }

    // 检查是否有玩家获胜
    private boolean checkWin(int x, int y) {
        Boolean player = board[x][y];

        // 定义方向数组：水平、垂直、左上到右下、右上到左下
        int[][] directions = {
            {1, 0}, {0, 1}, {1, 1}, {1, -1}
        };

        // 检查每个方向
        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];
            int count = 1;
            List<Move> currentMoves = new ArrayList<>();
            currentMoves.add(new Move(x, y, player));

            // 检查正方向
            for (int i = 1; i < 5; i++) {
                int nx = x + dx * i;
                int ny = y + dy * i;

                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && 
                    board[nx][ny] == player) {
                    count++;
                    currentMoves.add(new Move(nx, ny, player));
                } else {
                    break;
                }
            }

            // 检查反方向
            for (int i = 1; i < 5; i++) {
                int nx = x - dx * i;
                int ny = y - dy * i;

                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && 
                    board[nx][ny] == player) {
                    count++;
                    currentMoves.add(new Move(nx, ny, player));
                } else {
                    break;
                }
            }

            // 如果有连续五个相同的棋子，则获胜
            if (count >= 5) {
                winningMoves = currentMoves; // 记录获胜的连珠位置
                return true;
            }
        }

        return false;
    }

    // 检查是否平局
    private boolean checkDraw() {
        // 如果棋盘已满且没有人获胜，则平局
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    // 悔棋
    public void undo(int steps) {
        // 检查是否可以悔棋
        if (moveHistory.isEmpty()) {
            JOptionPane.showMessageDialog(null, "无法悔棋，没有历史记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 限制悔棋步数
        steps = Math.min(steps, moveHistory.size());
        steps = Math.min(steps, UNDO_LIMIT);

        // 悔棋
        for (int i = 0; i < steps; i++) {
            Move move = moveHistory.pop();
            board[move.x][move.y] = null;
            currentPlayer = move.player; // 回到上一个玩家
        }

        // 更新游戏状态
        gameOver = false;
        winningMoves.clear(); // 清空获胜连珠记录
        hintMove = null; // 悔棋后清空提示
    }

    // 投降
    public void surrender() {
        if (gameOver) {
            JOptionPane.showMessageDialog(null, "游戏已结束！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 当前玩家投降
        gameOver = true;

        // 显示提示框
        JOptionPane.showMessageDialog(null, 
                (currentPlayer ? "黑棋" : "白棋") + "投降！\n" + 
                (!currentPlayer ? "黑棋" : "白棋") + "获胜！", 
                "游戏结束", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    // 设置AI难度
    public void setAIDifficulty(int difficulty) {
        if (difficulty < 1 || difficulty > 3) {
            JOptionPane.showMessageDialog(null, "难度级别必须在1-3之间！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        aiDifficulty = difficulty;
        JOptionPane.showMessageDialog(null, "AI难度已设置为：" + getDifficultyName(aiDifficulty), "设置成功", JOptionPane.INFORMATION_MESSAGE);
    }

    // 获取难度名称 - 修改为public访问修饰符
    public String getDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1:
                return "困难";
            case 2:
                return "中等";
            case 3:
                return "简单";
            default:
                return "中等";
        }
    }

    // 显示提示
    public void showHint() {
        if (gameOver) {
            JOptionPane.showMessageDialog(null, "游戏已结束，无法提示！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (aiMode && !currentPlayer) {
            JOptionPane.showMessageDialog(null, "当前轮到AI，无法提示！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Move bestMove = null;
        if (currentPlayer) {
            // 玩家是黑棋，模拟玩家落子
            bestMove = findBestMoveForPlayer(true);
        } else {
            // 玩家是白棋，模拟玩家落子
            bestMove = findBestMoveForPlayer(false);
        }

        if (bestMove != null) {
            hintMove = bestMove;
        }
    }

    // 为玩家找到最佳落子位置
    private Move findBestMoveForPlayer(boolean isPlayer) {
        Move bestMove = null;
        int bestScore = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    // 计算玩家落子的分数
                    board[i][j] = isPlayer;
                    int score = AILogic.evaluatePosition(i, j, isPlayer, board);
                    board[i][j] = null;

                    // 如果分数更高，或者分数相同但更靠近中心，则选择这个位置
                    if (score > bestScore || (score == bestScore && AILogic.isCloserToCenter(i, j, bestMove))) {
                        bestScore = score;
                        bestMove = new Move(i, j, isPlayer);
                    }
                }
            }
        }

        return bestMove;
    }

    // 保存游戏存档
    public void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存游戏存档");
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileToSave))) {
                oos.writeObject(currentPlayer);
                oos.writeObject(gameOver);
                oos.writeObject(board);
                oos.writeObject(moveHistory);
                oos.writeObject(aiMode);
                oos.writeObject(aiDifficulty);
                oos.writeObject(winningMoves);
                oos.writeObject(hintMove);
                JOptionPane.showMessageDialog(null, "游戏存档保存成功！", "保存成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "保存游戏存档时出错：" + e.getMessage(), "保存失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 读取游戏存档
    public void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择游戏存档文件");
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToLoad = fileChooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoad))) {
                currentPlayer = (boolean) ois.readObject();
                gameOver = (boolean) ois.readObject();
                board = (Boolean[][]) ois.readObject();
                
                // 使用注解抑制类型安全警告
                @SuppressWarnings("unchecked")
                Stack<Move> loadedMoveHistory = (Stack<Move>) ois.readObject();
                moveHistory = loadedMoveHistory;
                
                aiMode = (boolean) ois.readObject();
                aiDifficulty = (int) ois.readObject();
                
                @SuppressWarnings("unchecked")
                List<Move> loadedWinningMoves = (List<Move>) ois.readObject();
                winningMoves = loadedWinningMoves;
                
                hintMove = (Move) ois.readObject();
                JOptionPane.showMessageDialog(null, "游戏存档加载成功！", "加载成功", JOptionPane.INFORMATION_MESSAGE);
                
                // 加载成功后刷新界面
                refreshBoard();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "加载游戏存档时出错：" + e.getMessage(), "加载失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 获取落子历史记录
    public Stack<Move> getMoveHistory() {
        return moveHistory;
    }

    // 设置当前玩家
    public void setCurrentPlayer(boolean currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    // 修改刷新界面的方法
    private void refreshBoard() {
        if (gameUI != null) {
            gameUI.repaint();
            gameUI.getBoardPanel().repaint(); 
        }
    }

    // Getter和Setter方法
    public boolean isGameOver() {
        return gameOver;
    }

    public Boolean[][] getBoard() {
        return board;
    }

    public boolean isAiMode() {
        return aiMode;
    }

    public void setAiMode(boolean aiMode) {
        this.aiMode = aiMode;
    }

    public boolean isCurrentPlayer() {
        return currentPlayer;
    }

    public Move getHintMove() {
        return hintMove;
    }

    public List<Move> getWinningMoves() {
        return winningMoves;
    }

    // 添加缺少的getter方法
    public int getAiDifficulty() {
        return aiDifficulty;
    }
}