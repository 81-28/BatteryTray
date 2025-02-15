import java.awt.AWTException;
import java.awt.Color;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class BatteryTrayApp {
	//一時格納用変数の定義
	int temp_BatteryLevel = 0;
	boolean temp_isPluggedIn = false;
	
	private static final int CANVAS_SIZE = 16;

    private SystemTray tray;
    private TrayIcon trayIcon;
    
    //数値格納用のキャンバスの配列を定義
    int[][] num_canvas;
    
	//カウンタ変数の定義
    int cnt = 0;

	//バッテリ残量を0であらかじめ初期化(12回に1回更新する関係で初期化しないとundefinedとなる)
    int battery = 0;

	//電源接続状態更新間隔の定義(ms), バッテリ残量更新間隔はこの6倍
    int update_interval = 5000;
    
	static int[][][] points = {
			{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
			{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
			{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,0,0,0,0,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0}
    		},{
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0}
    		}
	};
	
	static int[][] battery_100 = {
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
			{1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,0,0,1,1,0,1,1,0,0,1,1},
			{1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
			{1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
			};

	static int[][] battery_error = {
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0}
	};
 
    public static void main(String[] args) {
        new BatteryTrayApp().start();
    }

    public void start() {
        createTrayIcon();

        // Update the battery level
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTray();
            }
        }, 0, update_interval);
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

    //配列を水平方向に結合する関数
    public static int[][] concatenate(int[][] a, int[][] b) {
        int[][] result = new int[a.length][a[0].length + b[0].length];

        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, result[i], 0, a[i].length);
            System.arraycopy(b[i], 0, result[i], a[i].length, b[i].length);
        }

        return result;
    }
    
    public static boolean isPluggedIn() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("WMIC Path Win32_Battery Get BatteryStatus");
        BufferedReader systemOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = systemOut.readLine()) != null) {
            // The BatteryStatus=2 when the AC power is online, and 1 when offline.
            if (line.trim().equals("2")) {
                return true;
            }
        }
        return false;
    }
    
    private void updateTray() {
        if (tray != null && trayIcon != null) {
        	
        	if(cnt % 6 == 0) {
	            String batteryLevel = getBatteryLevel();
	            
	            //バッテリ残量が数値で受け取れた場合はint型に変換
	            if (batteryLevel.matches("-?\\d+")) {
		            trayIcon.setToolTip("Battery Level: " + batteryLevel + "%");
		            battery = Integer.parseInt(batteryLevel);
		            
		            //ReLu関数と同様にして例外をはじく
		            if(battery < 0) battery = 0;
		            else if(battery > 100) battery = 100;
		            cnt = 0;
//		            System.out.println("batteryLevel updated");
		           
	            }
		            
	            //取得できなかった場合終了
	            else {
					num_canvas = battery_error.clone();
					boolean isPluggedIn = false;
					boolean isError = true;
					trayIcon.setImage(createCanvas(num_canvas, battery, isPluggedIn, isError));
					return;
				}
        	}
            cnt++;

			boolean isError = false;

            //電源接続状態を確認
            boolean isPluggedIn;
            try {
            	isPluggedIn = isPluggedIn();
//            	System.out.println(cnt + "isPluggedIn updated");
            } catch(Exception e) {
            	isPluggedIn = false;
            }
            
            
            if (battery != temp_BatteryLevel || temp_isPluggedIn != isPluggedIn) {
            	//バッテリ残量, 電源接続状態のいずれかに変化があった場合再描画
	            temp_BatteryLevel = battery;
	            temp_isPluggedIn = isPluggedIn;

	            
	//            デバッグ用バッテリ残量を指定
	//            battery = 100;
	            
	            //バッテリ100%のみ例外的に処理
	            if(battery == 100) {
	            	num_canvas = battery_100.clone();
	            }
	            else {
	            	int tens_place = battery / 10, ones_place = battery % 10;
	            	if (tens_place == 0) tens_place = 10;
	            	num_canvas = concatenate(points[tens_place], points[ones_place]);
	            }
	            trayIcon.setImage(createCanvas(num_canvas, battery, isPluggedIn, isError));
//	            System.out.println("Canvas updated");
	        }
        }
    }
    
    public static BufferedImage createCanvas(int[][] num_canvas, int battery, boolean isPluggedIn, boolean isError) {
        // Create a transparent canvas
        BufferedImage canvas = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        
        //バッテリ残量に応じて色を変更
        
        //電源接続時にはバッテリ残量をシアンで表示
        if (isPluggedIn == true) g2d.setColor(Color.CYAN);
		else if (isError == true) g2d.setColor(Color.RED);
        else {
        	if (battery > 80) g2d.setColor(Color.GREEN);
        	else if(battery > 50) g2d.setColor(Color.WHITE);
        	else if (battery > 20) g2d.setColor(Color.YELLOW);
        	else g2d.setColor(new Color(255, 143, 63));
        }
        

        // Plot the points
        for (int i = 0; i < CANVAS_SIZE; i++) {
            for (int j = 0; j < CANVAS_SIZE; j++) {
                if (num_canvas[i][j] == 1) {
                    g2d.fillRect(j, i, 1, 1);
                }
            }
        }

        g2d.dispose();
        return canvas;
    }

    private Image createImage(String text) {
        BufferedImage bufferedImage = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);  // Change to TYPE_INT_RGB

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
