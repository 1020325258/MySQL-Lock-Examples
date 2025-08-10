package lock;

import javax.swing.*;
import java.awt.*;

public class HorizontalLineWithLargerSegmentsExample extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的 paintComponent 方法
        
        // 转换 Graphics 为 Graphics2D，可以使用更多图形绘制方法
        Graphics2D g2d = (Graphics2D) g;
        
        // 设置线条颜色
        g2d.setColor(Color.BLUE);
        
        // 增加横坐标点（例如：从 5 到 100，步长为 10）
        int[] xCoords = {5, 20, 40, 60, 80, 100}; // 设置更大的间隔
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
        JFrame frame = new JFrame("Horizontal Line with Larger Segments");
        HorizontalLineWithLargerSegmentsExample panel = new HorizontalLineWithLargerSegmentsExample();
        
        // 设置 JFrame 属性（增加窗口宽度以适应更长的线段）
        frame.setSize(600, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 将自定义的 JPanel 添加到 JFrame
        frame.add(panel);
        
        // 显示窗口
        frame.setVisible(true);
    }
}
