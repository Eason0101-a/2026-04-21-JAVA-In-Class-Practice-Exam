import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImagePartialDerivatives {

    public static void main(String[] args) {
        try {
            String inputPath = args.length > 0 ? args[0] : "image.png";
            String dxPath = args.length > 1 ? args[1] : "output_dx.png";
            String dyPath = args.length > 2 ? args[2] : "output_dy.png";
            String edgePath = args.length > 3 ? args[3] : "output_edge.png";
            int strongEdgeThreshold = args.length > 4 ? Integer.parseInt(args[4]) : 60;

            BufferedImage input = ImageIO.read(new File(inputPath));
            if (input == null) {
                throw new IllegalArgumentException("無法讀取圖片: " + inputPath);
            }

            int width = input.getWidth();
            int height = input.getHeight();

            int[][] gray = toGray(input);
            BufferedImage dxImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage dyImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            int[][] dxAbs = new int[height][width];
            int[][] dyAbs = new int[height][width];
            int[][] edgeMag = new int[height][width];
            int maxDx = 0;
            int maxDy = 0;
            int maxEdge = 0;

            // 第一種方法：使用一階差分核 [-1, 1] 估計偏微分
            // fx(x, y) = f(x + 1, y) - f(x, y)
            // fy(x, y) = f(x, y + 1) - f(x, y)
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int gx = 0;
                    int gy = 0;

                    if (x < width - 1) {
                        gx = gray[y][x + 1] - gray[y][x];
                    }
                    if (y < height - 1) {
                        gy = gray[y + 1][x] - gray[y][x];
                    }

                    int dxPixel = Math.abs(gx);
                    int dyPixel = Math.abs(gy);
                    int edge = (int) Math.round(Math.sqrt(gx * gx + gy * gy));

                    dxAbs[y][x] = dxPixel;
                    dyAbs[y][x] = dyPixel;
                    edgeMag[y][x] = edge;

                    if (dxPixel > maxDx) {
                        maxDx = dxPixel;
                    }
                    if (dyPixel > maxDy) {
                        maxDy = dyPixel;
                    }
                    if (edge > maxEdge) {
                        maxEdge = edge;
                    }
                }
            }

            // 將梯度值依全圖最大值正規化，讓結果更清楚。
            // 同時套用小門檻，避免非常弱的噪聲被放大。
            int noiseFloor = 12;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int dxPixel = normalizeToByte(dxAbs[y][x], maxDx, noiseFloor);
                    int dyPixel = normalizeToByte(dyAbs[y][x], maxDy, noiseFloor);
                    int edgePixel = normalizeToByte(edgeMag[y][x], maxEdge, noiseFloor);
                    int strongEdgePixel = edgePixel >= strongEdgeThreshold ? 255 : 0;

                    setGray(dxImage, x, y, dxPixel);
                    setGray(dyImage, x, y, dyPixel);
                    setGray(edgeImage, x, y, strongEdgePixel);
                }
            }

            ImageIO.write(dxImage, "png", new File(dxPath));
            ImageIO.write(dyImage, "png", new File(dyPath));
            ImageIO.write(edgeImage, "png", new File(edgePath));

            System.out.println("完成");
            System.out.println("輸入圖片: " + inputPath);
            System.out.println("dx 輸出: " + dxPath);
            System.out.println("dy 輸出: " + dyPath);
            System.out.println("邊緣輸出: " + edgePath);
            System.out.println("強邊緣閾值: " + strongEdgeThreshold);

        } catch (Exception e) {
            System.err.println("執行失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[][] toGray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] gray = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // 使用亮度加權轉灰階
                int value = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                gray[y][x] = value;
            }
        }

        return gray;
    }

    private static void setGray(BufferedImage image, int x, int y, int value) {
        int v = clamp(value);
        int rgb = (v << 16) | (v << 8) | v;
        image.setRGB(x, y, rgb);
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }

    private static int normalizeToByte(int value, int maxValue, int floor) {
        if (value <= floor || maxValue <= floor) {
            return 0;
        }

        double normalized = (value - floor) * 255.0 / (maxValue - floor);
        return clamp((int) Math.round(normalized));
    }
}
