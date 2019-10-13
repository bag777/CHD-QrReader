package test;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;
import org.jvnet.winp.WinProcess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ActivateTest {

    public interface User32 extends W32APIOptions {

        User32 instance = (User32) Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);


        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        WinDef.HWND FindWindow(String winClass, String title);

        int SW_SHOW = 1;

    }

    private static void loadApp() {
        String path = "C:\\Users\\junhee\\AppData\\Roaming\\stephen_cash";
        String file = "디모데재정자동업데이트.exe";

        ProcessBuilder pb = new ProcessBuilder(path + "\\" + file, "/c", path);
        try {
            Process process = pb.start();
            WinProcess wp = new WinProcess(process);
            int pid = wp.getPid();

            System.err.println(" PID : " + pid);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    public static void getPid(){
        try{
            String line;
            Process p = Runtime.getRuntime().exec("tasklist.exe /nh");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), "euc-kr"));
            while((line = input.readLine()) != null){
                if (!line.trim().equals("")){
                    // keep only the proecess name
                    String[] split = line.split("[ ]+");
                    if (split[0].equals("디모데재정관리.exe")) {
                        System.out.println(" > " + split[1] + ":" + split[0]);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static final Color DetectColor = new Color(0xFFFFE0);
    private static final Color BoundsColor = new Color(0x646464);
    private static final int DetectWidth = 84;
    private static final int DetectHeight = 20;
    private static final int AreaWidth = 89;
    private static final int AreaHeight = 22;
    public static void capture() {
        try {
            Robot robot = new Robot();
            String format = "jpg";
            String fileName = "Screenshot." + format;

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//            Rectangle screenRect = new Rectangle(100, 100, 150, 150);
            BufferedImage scrImg = robot.createScreenCapture(screenRect);
            ImageIO.write(scrImg, format, new File(fileName));

            int width = scrImg.getWidth();
            int height = scrImg.getHeight();

            int fx = 0, fy = 0;
            // find DetectColor
            boolean find = false;
            for (int x = 0; x < width; x += DetectWidth) {
                for (int y = 0; y < height; y+=DetectHeight) {
                    if (scrImg.getRGB(x, y) == DetectColor.getRGB()) {
                        // find corner x
                        int cornerX = -1;
                        for (int rx = 1; rx < AreaWidth; rx++) {
                            if (scrImg.getRGB(x-rx, y) == BoundsColor.getRGB()) {
                                cornerX = x - rx;
                                fx = cornerX;
                                break;
                            }
                        }
                        if (cornerX < 0) continue;

                        // find corner y
                        int cornerY = -1;
                        for (int ry = 1; ry < AreaHeight; ry++) {
                            if (scrImg.getRGB(x, y - ry) == BoundsColor.getRGB()) {
                                cornerY = y - ry;
                                fy = cornerY;
                                break;
                            }
                        }
                        if (cornerY < 0) continue;

                        find = true;
                        // check is code input area
                        for (int cx = 0; cx < AreaWidth; cx++) {
                            if (scrImg.getRGB(cornerX + cx, cornerY) != BoundsColor.getRGB() // Top line
                                    || scrImg.getRGB(cornerX + cx, cornerY + AreaHeight - 1) != BoundsColor.getRGB()) { // Bottom line
                                find = false;
                                break;
                            }
                        }
                        if (!find) continue;

                        for (int cy = 0; cy < AreaHeight; cy++) {
                            if (scrImg.getRGB(cornerX, cornerY + cy) != BoundsColor.getRGB() // Left line
                                    || scrImg.getRGB(cornerX + AreaWidth - 1, cornerY + cy) != BoundsColor.getRGB()) { // Right line
                                find = false;
                                break;
                            }
                        }
                    }
                }
                if (find) break;
            }
            if (find) {
                Rectangle inputRect = new Rectangle(fx, fy, AreaWidth, AreaHeight);
                BufferedImage inputImg = robot.createScreenCapture(inputRect);
                ImageIO.write(inputImg, format, new File("InputArea.png"));
                System.out.println("Saved..");
            }
        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
    }

    public static void testCompreaImages() {
        try {
            Image img1 = ImageIO.read(new File("ready1.png"));
            Image img2 = ImageIO.read(new File("ready2.png"));

            BufferedImage bufImg1 = (BufferedImage) img1;
            BufferedImage bufImg2 = (BufferedImage) img2;

            int width = bufImg1.getWidth();
            int height = bufImg1.getHeight();

            for (int w = 0; w < width; w++) {
                if (w == 2 || w == 3) continue;
                for (int h = 0; h < height; h++) {
                    if (bufImg1.getRGB(w, h) != bufImg2.getRGB(w, h)) {
                        System.err.println(" > no match.");
                        return;
                    }
                }
            }
            System.err.println(" > match...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        loadApp();
        getPid();
        User32 user32 = User32.instance;
//        WinDef.HWND hWnd = user32.FindWindow(null, "Downloads"); // Sets focus to my opened 'Downloads' folder
//        WinDef.HWND hWnd = user32.FindWindow(null, "Study"); // Sets focus to my opened 'Downloads' folder
        WinDef.HWND hWnd = user32.FindWindow(null, "디모데재정관리");
//        WinDef.HWND myProc = user32.
        user32.ShowWindow(hWnd, User32.SW_SHOW);
        user32.SetForegroundWindow(hWnd);

        capture();
        testCompreaImages();
    }
}