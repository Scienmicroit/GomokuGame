import java.io.Serializable; // 添加Serializable接口的导入

// 这个类表示一个落子动作，包含落子的坐标和玩家信息
class Move implements Serializable {
    int x;
    int y;
    boolean player;

    public Move(int x, int y, boolean player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }
}