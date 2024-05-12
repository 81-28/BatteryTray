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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

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

	final JSpinner[][] colorSpinners = new JSpinner[5][3];
	final String[] eachBatteryLevel = {"~20%         ", 
									   "~50%         ", 
									   "~80%         ", 
									   "~100%      ", 
									   "PluggedIn", };
	final static String[] BatteryConfig = {"upto20", "upto50", "upto80", "upto100", "Plugged"};

	static int[][] colorList = readProperties();
    
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
	
	static int[][] FullBattery = {
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

	static int[][] ErrorBattery = {
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

	private void openSettingsDialog() {
		JFrame settingsFrame = new JFrame("Settings");
		settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsFrame.setLayout(new BoxLayout(settingsFrame.getContentPane(), BoxLayout.Y_AXIS));  // レイアウトをBoxLayoutに変更

		// 各バッテリーレベルの色を設定するためのスピンボックスを作成します。
		for (int i = 0; i < 5; i++) {
			JPanel panel = new JPanel();
			panel.add(new JLabel(eachBatteryLevel[i] + " - "));

			colorSpinners[i][0] = new JSpinner(new SpinnerNumberModel(colorList[i][0], 0, 255, 1));
			panel.add(new JLabel("R"));
			panel.add(colorSpinners[i][0]);

			colorSpinners[i][1] = new JSpinner(new SpinnerNumberModel(colorList[i][1], 0, 255, 1));
			panel.add(new JLabel("G"));
			panel.add(colorSpinners[i][1]);

			colorSpinners[i][2] = new JSpinner(new SpinnerNumberModel(colorList[i][2], 0, 255, 1));
			panel.add(new JLabel("B"));
			panel.add(colorSpinners[i][2]);

			settingsFrame.add(panel);  // パネルをフレームに追加
		}

		// "Save"ボタンを作成します。
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			// ボタンがクリックされたときにRGBの各値を出力します。
			for (int i = 0; i < 5; i++) {
				int red = (Integer) colorSpinners[i][0].getValue();
				int green = (Integer) colorSpinners[i][1].getValue();
				int blue = (Integer) colorSpinners[i][2].getValue();
				colorList[i][0] = red;
				colorList[i][1] = green;
				colorList[i][2] = blue;
			}

			writeProperties(colorList);
			JOptionPane.showMessageDialog(settingsFrame, "Restart the program to apply changes", "Info", JOptionPane.INFORMATION_MESSAGE);
			settingsFrame.dispose();
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> {
			settingsFrame.dispose();
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		// ボタンをフレームに追加します。
		settingsFrame.add(buttonPanel);

		settingsFrame.pack();
		settingsFrame.setVisible(true);
	}

	//Configファイルに書き出し
    public static void writeProperties(int[][] array) {
        Map<String, String> map = new LinkedHashMap<>();

        for (int i = 0; i < array.length; i++) {
            String rowKey = BatteryConfig[i];
            String rowValue = "";
            for (int j = 0; j < array[i].length; j++) {
                rowValue += Integer.toString(array[i][j]);
                if (j < array[i].length - 1) {
                    rowValue += ",";
                }
            }
            map.put(rowKey, rowValue);
        }

        try {
            FileOutputStream fos = new FileOutputStream("Config.properties");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                fos.write((entry.getKey() + "=" + entry.getValue() + "\n").getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	//Configファイルから読み出し
	public static int[][] readProperties() {
		int[][] DefaultColor = {
			{255, 143,  63},
			{255, 255,   0},
			{255, 255, 255},
			{  0, 255,   0},
			{  0, 255, 255}
		};

		int[][] array = new int[5][3];
		Properties prop = new Properties();
	
		try {
			File f = new File("Config.properties");
			if(f.exists() && !f.isDirectory()) { 
				prop.load(new FileInputStream("Config.properties"));
	
				for (int i = 0; i < array.length; i++) {
					String rowKey = BatteryConfig[i];
					String[] rowValues = prop.getProperty(rowKey).split(",");
					for (int j = 0; j < array[i].length; j++) {
						array[i][j] = Integer.parseInt(rowValues[j]);
					}
				}
			//Configファイルがない場合はデフォルトの値を使用
			} else {
				array = DefaultColor;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return array;
	}
	

    private void createTrayIcon() {
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            tray = SystemTray.getSystemTray();

            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = (e) -> System.exit(0);

            // create a popup menu
            PopupMenu popup = new PopupMenu();

			java.awt.MenuItem settingsItem = new java.awt.MenuItem("Settings");
			settingsItem.addActionListener(e -> openSettingsDialog());
			// settingsItem.addActionListener(closeListener);
			popup.add(settingsItem);

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
					num_canvas = ErrorBattery.clone();
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
	            	num_canvas = FullBattery.clone();
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
        if (isPluggedIn == true) g2d.setColor(new Color(colorList[4][0], colorList[4][1], colorList[4][2]));
		else if (isError == true) g2d.setColor(Color.RED);
        else {
        	if (battery > 80) g2d.setColor(new Color(colorList[3][0], colorList[3][1], colorList[3][2]));
        	else if(battery > 50) g2d.setColor(new Color(colorList[2][0], colorList[2][1], colorList[2][2]));
        	else if (battery > 20) g2d.setColor(new Color(colorList[1][0], colorList[1][1], colorList[1][2]));
        	else g2d.setColor(new Color(colorList[0][0], colorList[0][1], colorList[0][2]));
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
