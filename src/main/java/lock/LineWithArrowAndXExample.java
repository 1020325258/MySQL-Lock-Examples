package lock;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class LineWithArrowAndXExample extends JPanel {

    // x 轴放大倍数
    static final int rate = 40;

    static final int offset = 100;

    private List<Point> primaryIndex;

    private List<Point> cIndex;

    public LineWithArrowAndXExample(List<Point> primaryIndex, List<Point> cIndex) {
        for (Point p : primaryIndex) {
            p.setX(translateX(p.getX()));
        }
        for (Point p : cIndex) {
            p.setX(translateX(p.getX()));
        }
        this.primaryIndex = primaryIndex;
        this.cIndex = cIndex;
    }

    private static int translateX(int x) {
        return x * LineWithArrowAndXExample.rate + LineWithArrowAndXExample.offset;
    }

    private static int recoverX(int x) {
        return (x - LineWithArrowAndXExample.offset) / LineWithArrowAndXExample.rate;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的 paintComponent 方法

        // 转换 Graphics 为 Graphics2D，可以使用更多图形绘制方法
        Graphics2D g2d = (Graphics2D) g;
        Graphics2D g2d_back = (Graphics2D) g2d.create();


        // 横坐标点（5, 10, 15, 20）
        int[] xCoords = {0, 5, 10, 15, 20};
        int yCoord = 100; // 固定 y 坐标为 100，绘制水平线
        for (int i = 0; i < xCoords.length; i++) {
            xCoords[i] = translateX(xCoords[i]);
        }
        paintAxex(g2d, primaryIndex, xCoords, yCoord, "主键索引");


        // 重置 g2d
        g2d = g2d_back;
        // 横坐标点（5, 10, 15, 20）
        xCoords = new int[]{0, 5, 10, 15, 20};
        yCoord = 300; // 固定 y 坐标为 100，绘制水平线
        for (int i = 0; i < xCoords.length; i++) {
            xCoords[i] = translateX(xCoords[i]);
        }
        paintAxex(g2d, cIndex, xCoords, yCoord, "c 索引");

    }

    private void paintAxex(Graphics2D g2d, List<Point> points, int[] xCoords, int yCoord, String index) {
        // 设置线条颜色
        g2d.setColor(Color.BLACK);

        g2d.drawString(index, xCoords[0] - 50, yCoord);
        
        // 绘制多条横向线段
        for (int i = 0; i < xCoords.length - 1; i++) {
            g2d.drawLine(xCoords[i], yCoord, xCoords[i + 1], yCoord);
        }

        // 在每个横坐标点上方绘制坐标值
        for (int i = 0; i < xCoords.length; i++) {
            g2d.drawString(Integer.toString(recoverX(xCoords[i])), xCoords[i] - 5, yCoord + 20);
            g2d.drawLine(xCoords[i], yCoord, xCoords[i], yCoord - 5);
        }

        int y = yCoord;    // y 坐标 0 对应的 y 值
        paintArrowhead(g2d, points, y);
    }

    private void paintArrowhead(Graphics2D g2d, List<Point> points, int y) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int x = point.getX();
            boolean blocked = point.isBlocked();
            ImageIcon icon;
            String txt;
            if (blocked) {
                // 画箭身（从 [5, 0] 到 [5, 20]）
                g2d.setColor(Color.RED); // 设置箭头颜色为红色
                icon = new ImageIcon(getClass().getResource("/img/wrong.png"));
                // 缩放图片到 32x32
                Image img = icon.getImage();
                Image newImg = img.getScaledInstance(10, 10, Image.SCALE_SMOOTH); // 平滑缩放
                icon = new ImageIcon(newImg);
                txt = "阻塞";
            } else {
                g2d.setColor(new Color(0, 100, 0));
                icon = new ImageIcon(getClass().getResource("/img/right.png"));
                // 缩放图片到 10x10
                Image img = icon.getImage();
                Image newImg = img.getScaledInstance(10, 10, Image.SCALE_SMOOTH); // 平滑缩放
                icon = new ImageIcon(newImg);
                txt = "成功";
            }

            // 绘制箭头
            paintArrow(g2d, x, y, txt, icon);
        }
    }

    private void paintArrow(Graphics2D g2d, int x, int y, String txt, ImageIcon icon) {
        txt = txt + recoverX(x);
        g2d.setStroke(new BasicStroke(2)); // 设置箭身的线宽
        // 绘制箭头的线（箭身）
        g2d.drawLine(x, y - 15, x, y - 25); // 箭身的竖线
        // 绘制箭头的尖端（用三角形）
        int[] xArrow = {x - 5, x, x + 5}; // 箭头尖端的宽度
        int[] yArrow = {y - 15, y - 5, y - 15}; // 箭头尖端的高度
        // 绘制箭头的尖端
        g2d.fillPolygon(xArrow, yArrow, 3);
        // 阻塞/成功
        g2d.drawString(txt, x - 5, y - 45);
        // 对号/错号
        icon.paintIcon(this, g2d, x - 5, y - 40);
    }

    public static void main(String[] args) {
        // 创建 JFrame
        JFrame frame = new JFrame("Line with Arrow and X");
        List<Point> primaryIndex = Arrays.asList(new Point(5, false), new Point(6, true));
        List<Point> cIndex = Arrays.asList(new Point(5, false), new Point(11, true));
        LineWithArrowAndXExample panel = new LineWithArrowAndXExample(primaryIndex, cIndex);

        // 设置 JFrame 属性（增加窗口宽度以适应更长的线段）
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 将自定义的 JPanel 添加到 JFrame
        frame.add(panel);

        // 显示窗口
        frame.setVisible(true);
    }
}
