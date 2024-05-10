import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class BatteryTrayApp {

    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;
    private SystemTray tray;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        new BatteryTrayApp().start();
    }

    public void start() {
        createTrayIcon();

        // Update the battery level every minute
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTray();
            }
        }, 0, 60000);
    }

    private void createTrayIcon() {
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            tray = SystemTray.getSystemTray();

            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = (e) -> System.exit(0);

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            java.awt.MenuItem closeItem = new java.awt.MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);

            /// ... add other items

            // construct a TrayIcon
            trayIcon = new TrayIcon(createImage("0"), "Battery Level: " + getBatteryLevel(), popup);

            // set the TrayIcon properties
            trayIcon.addActionListener(closeListener);

            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }

        }
    }

    private String getBatteryLevel() {
        String batteryLevel = "";
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "WMIC Path Win32_Battery Get EstimatedChargeRemaining");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                if (!line.trim().equals("") && !line.trim().equals("EstimatedChargeRemaining")) {
                    batteryLevel = line.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return batteryLevel;
    }

    private void updateTray() {
        if (tray != null && trayIcon != null) {
            String batteryLevel = getBatteryLevel();
            trayIcon.setToolTip("Battery Level: " + batteryLevel + "%");
            trayIcon.setImage(createImage(batteryLevel));
        }
    }

    private Image createImage(String text) {
        BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);  // Change to TYPE_INT_RGB
        Graphics2D g2d = bufferedImage.createGraphics();

        // 背景を白に設定
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // テキストを赤に設定
        g2d.setColor(java.awt.Color.RED);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));  // フォントサイズを小さくする

        // テキストを中央に描画
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int x = (bufferedImage.getWidth() - fm.stringWidth(text)) / 2;
        int y = ((bufferedImage.getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(text, x, y);

        g2d.dispose();

        // 画像を表示するウィンドウを作成
//        new ImageWindow(bufferedImage);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", os);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Toolkit.getDefaultToolkit().createImage(os.toByteArray());
    }

    // 内部クラスとしてImageWindowを定義
    class ImageWindow extends JFrame {
        private Image image;

        public ImageWindow(Image image) {
            this.image = image;
            setSize(new Dimension(image.getWidth(null), image.getHeight(null)));
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, null);
        }
    }
}
