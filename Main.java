// 这个类是程序的入口点，负责创建GameCore和GomokuGameUI对象并启动游戏
public class Main {
    public static void main(String[] args) {
        GameCore gameCore = new GameCore();
        new GomokuGameUI(gameCore);
    }
}