package lock;

import javax.swing.*;
import java.awt.*;

public class HorizontalLineWithLabelsExample extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的 paintComponent 方法
        
        // 转换 Graphics 为 Graphics2D，可以使用更多图形绘制方法
        Graphics2D g2d = (Graphics2D) g;
        
        // 设置线条颜色
        g2d.setColor(Color.BLUE);
        
        // 横坐标点（5, 10, 15, 20, 25）
        int[] xCoords = {5, 10, 15, 20, 25};
        int yCoord = 100; // 固定 y 坐标为 100，绘制水平线
        
        // 绘制多条横向线段
        for (int i = 0; i < xCoords.length - 1; i++) {
            g2d.drawLine(xCoords[i], yCoord, xCoords[i + 1], yCoord);
        }
        
        // 设置文字颜色
        g2d.setColor(Color.BLACK);
        
        // 在每个横坐标点上方绘制坐标值
        for (int i = 0; i < xCoords.length; i++) {
            g2d.drawString(Integer.toString(xCoords[i]), xCoords[i] - 5, yCoord - 10);
        }
        
        // 画分隔线
        for (int i = 0; i < xCoords.length - 1; i++) {
            g2d.drawLine(xCoords[i], yCoord + 5, xCoords[i], yCoord - 5); // 分隔线
        }
    }

    public static void main(String[] args) {
        // 创建 JFrame
        JFrame frame = new JFrame("Horizontal Line with Labels");
        HorizontalLineWithLabelsExample panel = new HorizontalLineWithLabelsExample();
        
        // 设置 JFrame 属性
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 将自定义的 JPanel 添加到 JFrame
        frame.add(panel);
        
        // 显示窗口
        frame.setVisible(true);
    }
}
