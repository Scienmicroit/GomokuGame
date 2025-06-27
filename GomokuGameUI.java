import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;
import javax.swing.*;

// 这个类负责游戏的用户界面，包括创建窗口、面板和按钮，以及处理用户交互
class GomokuGameUI extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 50;
    private static final int STONE_SIZE = 36;

    private GameCore gameCore;

    // 主面板
    private JPanel mainPanel;
    // 棋盘面板
    private BoardPanel boardPanel;
    // 控制面板
    private ControlPanel controlPanel;
    // 状态面板
    private StatusPanel statusPanel;
    // 图片标签
    private JLabel imageLabel;

    // 鼠标所在的格子坐标
    private int mouseX = -1;
    private int mouseY = -1;

    public GomokuGameUI(GameCore gameCore) {
        this.gameCore = gameCore;
        gameCore.setGameUI(this); // 设置 gameUI

        // 初始化窗口
        setTitle("五子棋游戏");
        setSize(BOARD_SIZE * CELL_SIZE + MARGIN * 2, BOARD_SIZE * CELL_SIZE + MARGIN * 2 + 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 初始化棋盘
        gameCore.initBoard();

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());

        // 创建棋盘面板
        boardPanel = new BoardPanel();
        boardPanel.addMouseListener(new BoardMouseListener());
        boardPanel.addMouseMotionListener(new BoardMouseMotionListener());

        // 创建控制面板
        controlPanel = new ControlPanel();

        // 创建状态面板
        statusPanel = new StatusPanel();

        // 加载图片并调整大小
        ImageIcon resizedIcon = loadAndResizeImage("GomokuGame.png");
        if (resizedIcon != null) {
            imageLabel = new JLabel(resizedIcon);
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
        } else {
            // 图片加载失败，显示提示信息
            imageLabel = new JLabel("图片加载失败");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
        }

        // 添加面板到主面板
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(imageLabel, BorderLayout.BEFORE_FIRST_LINE);

        // 添加主面板到窗口
        add(mainPanel);

        // 显示窗口
        setVisible(true);
    }

    private ImageIcon loadAndResizeImage(String fileName) {
        try {
            // 尝试使用绝对路径加载图片
            File imageFile = new File(fileName);
            if (!imageFile.exists()) {
                System.out.println("图片文件不存在: " + imageFile.getAbsolutePath());
                return null;
            }
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());

            // 获取原始图片尺寸
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();

            // 设置图片缩放比例 (0.0 - 1.0)
            double scaleFactor = 0.3; // 默认为原始大小的60%

            // 计算新的图片尺寸
            int newWidth = (int) (originalWidth * scaleFactor);
            int newHeight = (int) (originalHeight * scaleFactor);

            // 高质量图像缩放方法
            BufferedImage originalImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = originalImage.createGraphics();
            g2d.drawImage(originalIcon.getImage(), 0, 0, null);
            g2d.dispose();

            // 创建目标尺寸的缓冲图像
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();

            // 设置高质量渲染提示
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制缩放后的图像
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // 创建新的ImageIcon
            return new ImageIcon(resizedImage);
        } catch (Exception e) {
            System.out.println("加载图片时出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 棋盘面板类
     */
    class BoardPanel extends JPanel {
        // 落子动画的定时器
        private Timer animationTimer;
        // 动画的当前大小
        private int currentSize;
        // 动画的目标大小
        private int targetSize = STONE_SIZE;
        // 动画是否正在进行
        private boolean isAnimating = false;
        // 动画的棋子颜色
        private Color animationColor;
        // 动画的棋子坐标
        private int animationX;
        private int animationY;

        public BoardPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE + MARGIN * 2,
                    BOARD_SIZE * CELL_SIZE + MARGIN * 2));
            setBackground(new Color(240, 180, 100)); // 棋盘背景色

            // 初始化动画定时器
            animationTimer = new Timer(10, e -> {
                if (currentSize < targetSize) {
                    currentSize += 2;
                    if (currentSize > targetSize) {
                        currentSize = targetSize;
                    }
                    repaint();
                } else {
                    isAnimating = false;
                    animationTimer.stop();
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 绘制棋盘网格
            for (int i = 0; i < BOARD_SIZE; i++) {
                // 水平线
                g.drawLine(MARGIN, MARGIN + i * CELL_SIZE,
                        MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, MARGIN + i * CELL_SIZE);
                // 垂直线
                g.drawLine(MARGIN + i * CELL_SIZE, MARGIN,
                        MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
            }

            // 绘制天元和星位
            drawStarPoint(g, 7, 7); // 天元
            drawStarPoint(g, 3, 3); // 左上星
            drawStarPoint(g, 3, 11); // 右上星
            drawStarPoint(g, 11, 3); // 左下星
            drawStarPoint(g, 11, 11); // 右下星

            // 绘制棋子
            Boolean[][] board = gameCore.getBoard();
            Stack<Move> moveHistory = gameCore.getMoveHistory(); // 获取正确的落子历史记录
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] != null) {
                        if (isAnimating && i == animationX && j == animationY) {
                            // 如果是正在动画的棋子，根据当前大小绘制
                            int x = MARGIN + i * CELL_SIZE - currentSize / 2;
                            int y = MARGIN + j * CELL_SIZE - currentSize / 2;
                            g.setColor(animationColor);
                            g.fillOval(x, y, currentSize, currentSize);
                            if (animationColor == Color.WHITE) {
                                g.setColor(Color.BLACK);
                                g.drawOval(x, y, currentSize, currentSize);
                            }
                        } else {
                            // 正常绘制棋子
                            int x = MARGIN + i * CELL_SIZE - STONE_SIZE / 2;
                            int y = MARGIN + j * CELL_SIZE - STONE_SIZE / 2;

                            if (board[i][j]) {
                                // 黑棋
                                g.setColor(Color.BLACK);
                            } else {
                                // 白棋
                                g.setColor(Color.WHITE);
                            }

                            g.fillOval(x, y, STONE_SIZE, STONE_SIZE);

                            // 给白棋加边框
                            if (!board[i][j]) {
                                g.setColor(Color.BLACK);
                                g.drawOval(x, y, STONE_SIZE, STONE_SIZE);
                            }

                            // 如果是最后一步，标记数字
                            if (!moveHistory.isEmpty() && moveHistory.peek().x == i && moveHistory.peek().y == j) {
                                g.setColor(Color.RED);
                                g.setFont(new Font("SimHei", Font.BOLD, 14));
                                g.drawString(String.valueOf(moveHistory.size()), x + STONE_SIZE / 2 - 5, y + STONE_SIZE / 2 + 5);
                            }
                        }
                    }
                }
            }

            // 高亮显示获胜的连珠棋子，无论双人对战还是人机对战
            if (gameCore.isGameOver() && gameCore.getWinningMoves() != null && !gameCore.getWinningMoves().isEmpty()) {
                g.setColor(Color.RED);
                for (Move move : gameCore.getWinningMoves()) {
                    int x = MARGIN + move.x * CELL_SIZE - STONE_SIZE / 2;
                    int y = MARGIN + move.y * CELL_SIZE - STONE_SIZE / 2;
                    g.drawOval(x, y, STONE_SIZE, STONE_SIZE);
                }
            }

            // 显示提示
            if (gameCore.getHintMove() != null) {
                int x = MARGIN + gameCore.getHintMove().x * CELL_SIZE - STONE_SIZE / 2;
                int y = MARGIN + gameCore.getHintMove().y * CELL_SIZE - STONE_SIZE / 2;
                g.setColor(Color.GREEN);
                g.drawOval(x, y, STONE_SIZE, STONE_SIZE);
            }

            // 显示鼠标阴影提示
            if (mouseX != -1 && mouseY != -1 && board[mouseX][mouseY] == null) {
                int x = MARGIN + mouseX * CELL_SIZE - STONE_SIZE / 2;
                int y = MARGIN + mouseY * CELL_SIZE - STONE_SIZE / 2;
                Color shadowColor = gameCore.isCurrentPlayer() ? new Color(0, 0, 0, 100) : new Color(255, 255, 255, 100);
                g.setColor(shadowColor);
                g.fillOval(x, y, STONE_SIZE, STONE_SIZE);
            }

            if (gameCore.isGameOver() && gameCore.getWinningMoves() != null) {
                g.setColor(new Color(255, 0, 0, 150));
                g.setFont(new Font("SimHei", Font.BOLD, 40));
                FontMetrics fm = g.getFontMetrics();
                String text;
                if (gameCore.isAiMode() && !gameCore.isCurrentPlayer()) {
                    text = "白棋(AI)获胜！";
                } else {
                    text = (gameCore.isCurrentPlayer() ? "黑棋" : "白棋") + "获胜！";
                }
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g.drawString(text, getWidth() / 2 - textWidth / 2, getHeight() / 2 - textHeight / 2);
            }
        }

        /**
         * 绘制星位
         */
        private void drawStarPoint(Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            int size = 6;
            g.fillOval(MARGIN + x * CELL_SIZE - size / 2,
                    MARGIN + y * CELL_SIZE - size / 2,
                    size, size);
        }

        // 开始落子动画
        public void startAnimation(int x, int y) {
            animationX = x;
            animationY = y;
            currentSize = 0;
            // 修改动画颜色的设置逻辑
            animationColor = !gameCore.isCurrentPlayer() ? Color.BLACK : Color.WHITE; 
            isAnimating = true;
            animationTimer.start();
        }
    }

    /**
     * 棋盘鼠标监听器类
     */
    private class BoardMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            // 如果游戏结束，不处理点击事件
            if (gameCore.isGameOver()) {
                return;
            }

            // 如果是人机模式且轮到AI，不处理点击事件
            if (gameCore.isAiMode() && !gameCore.isCurrentPlayer()) {
                return;
            }

            // 计算点击的格子坐标
            int x = (e.getX() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
            int y = (e.getY() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;

            // 检查坐标是否在棋盘内
            if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                // 落子
                if (gameCore.placeStone(x, y)) {
                    // 开始落子动画
                    boardPanel.startAnimation(x, y);
                    if (gameCore.isAiMode() && !gameCore.isCurrentPlayer()) {
                        // 延迟一下，让AI思考
                        java.util.Timer timer = new java.util.Timer();
                        timer.schedule(new java.util.TimerTask() {
                            @Override
                            public void run() {
                                // AI落子
                                gameCore.makeAIMove();
                                if (gameCore.getMoveHistory().size() > 0) {
                                    Move lastMove = gameCore.getMoveHistory().peek();
                                    boardPanel.startAnimation(lastMove.x, lastMove.y);
                                }
                            }
                        }, 500);
                    }
                }
            }
        }
    }

    /**
     * 棋盘鼠标移动监听器类
     */
    private class BoardMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            // 计算鼠标所在的格子坐标
            int x = (e.getX() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
            int y = (e.getY() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;

            // 检查坐标是否在棋盘内
            if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                mouseX = x;
                mouseY = y;
            } else {
                mouseX = -1;
                mouseY = -1;
            }

            boardPanel.repaint();
        }
    }

    /**
     * 控制面板类
     */
    private class ControlPanel extends JPanel {
        private JButton newGameButton;
        private JButton undoButton;
        private JButton surrenderButton;
        private JButton aiModeButton;
        private JButton difficultyButton;
        private JButton hintButton;
        private JButton saveButton;
        private JButton loadButton;

        public ControlPanel() {
            setLayout(new FlowLayout());

            // 新建游戏按钮
            newGameButton = new JButton("重新开始");
            newGameButton.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                        GomokuGameUI.this,
                        "确定要重新开始游戏吗？",
                        "确认",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    gameCore.initBoard();
                    boardPanel.repaint();
                }
            });

            // 悔棋按钮
            undoButton = new JButton("悔棋");
            undoButton.addActionListener(e -> {
                int steps = 1;
                if (gameCore.isAiMode() && !gameCore.isCurrentPlayer()) {
                    // 如果是人机模式且轮到AI，需要悔两步（AI一步和玩家一步）
                    steps = 2;
                }

                gameCore.undo(steps);
                boardPanel.repaint();
            });

            // 投降按钮
            surrenderButton = new JButton("投降");
            surrenderButton.addActionListener(e -> {
                gameCore.surrender();
                boardPanel.repaint();
            });

            // 人机模式按钮
            aiModeButton = new JButton("人机对战");
            aiModeButton.addActionListener(e -> {
                gameCore.setAiMode(!gameCore.isAiMode());
                if (gameCore.isAiMode()) {
                    aiModeButton.setText("双人对战");
                    JOptionPane.showMessageDialog(GomokuGameUI.this, "已切换至人机对战模式\n你将使用黑棋，AI使用白棋", "模式切换", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    aiModeButton.setText("人机对战");
                    JOptionPane.showMessageDialog(GomokuGameUI.this, "已切换至双人对战模式", "模式切换", JOptionPane.INFORMATION_MESSAGE);
                }

                // 重新开始游戏
                gameCore.initBoard();
                boardPanel.repaint();
            });

            // 难度设置按钮
            difficultyButton = new JButton("AI难度: " + gameCore.getDifficultyName(gameCore.getAiDifficulty()));
            difficultyButton.addActionListener(e -> {
                String[] options = {"困难", "中等", "简单"};
                int choice = JOptionPane.showOptionDialog(
                        GomokuGameUI.this,
                        "选择AI难度",
                        "AI难度设置",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[gameCore.getAiDifficulty() - 1]);

                if (choice != -1) {
                    gameCore.setAIDifficulty(choice + 1);
                    difficultyButton.setText("AI难度: " + gameCore.getDifficultyName(gameCore.getAiDifficulty()));
                }
            });

            // 提示按钮
            hintButton = new JButton("提示");
            hintButton.addActionListener(e -> {
                gameCore.showHint();
                boardPanel.repaint();
            });

            // 保存按钮
            saveButton = new JButton("保存游戏");
            saveButton.addActionListener(e -> gameCore.saveGame());

            // 加载按钮
            loadButton = new JButton("加载游戏");
            loadButton.addActionListener(e -> gameCore.loadGame());

            add(newGameButton);
            add(undoButton);
            add(surrenderButton);
            add(aiModeButton);
            add(difficultyButton);
            add(hintButton);
            add(saveButton);
            add(loadButton);
        }
    }
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
    /**
     * 状态面板类
     */
    public class StatusPanel extends JPanel {
        private JLabel statusLabel;

        public StatusPanel() {
            statusLabel = new JLabel();
            add(statusLabel);
            updateStatus();
        }

        public void updateStatus() {
            String status = gameCore.isCurrentPlayer() ? "当前轮到黑棋" : "当前轮到白棋";
            if (gameCore.isGameOver()) {
                if (gameCore.getWinningMoves() != null) {
                    if (gameCore.isAiMode() && !gameCore.isCurrentPlayer()) {
                        status = "白棋(AI)获胜！";
                    } else {
                        status = gameCore.isCurrentPlayer() ? "黑棋获胜！" : "白棋获胜！";
                    }
                } else {
                    status = "平局！";
                }
            }
            statusLabel.setText(status);
        }
    }
}