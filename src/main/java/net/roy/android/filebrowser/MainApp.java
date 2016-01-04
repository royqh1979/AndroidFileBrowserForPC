package net.roy.android.filebrowser;

import com.android.ddmlib.*;

import javax.swing.*;

/**
 * Created by Roy on 2016/1/2.
 */
public class MainApp {

    private IDevice myPhone;
    private AndroidDebugBridge bridge;

    public static void main(String args[]){
        MainApp mainApp=new MainApp();
        mainApp.initADB();
        while(!mainApp.initDevice()){
            JOptionPane.showMessageDialog(null,"未找到设备,请重试");
            int retCode=JOptionPane.showConfirmDialog(null,"未找到设备,请确认设备连接后重试","未找到设备",JOptionPane.YES_NO_OPTION);
            if (retCode==JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
        System.out.println("???");
        try {
            JFrame mainFrame=new JFrame();
            AndroidFSPanel fsPanel=new AndroidFSPanel();

            fsPanel.setIDevice(mainApp.getMyPhone());
            mainFrame.setTitle("Android File Browser");
            mainFrame.setContentPane(fsPanel.getPanel());
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.setBounds(0,0,1024,768);
            //frame.pack();
            mainFrame.setVisible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void initADB() {
        AndroidDebugBridge.init(false);
        String path = MainApp.class.getResource("/adb.exe").getPath();
        System.out.println(path);
        bridge = AndroidDebugBridge.createBridge(path, false);
    }
    private boolean initDevice() {
        waitDevicesList(bridge);
        IDevice[] devices=bridge.getDevices();
        System.out.println(devices.length);
        if (devices.length<=0)
            return false;
        myPhone=devices[0];
        return true;

    }

    public IDevice getMyPhone() {
        return myPhone;
    }

    private  void waitDevicesList(AndroidDebugBridge bridge) {
        int count = 0;
        while (bridge.hasInitialDeviceList() == false) {
            try {
                Thread.sleep(500);
                count++;
            } catch (InterruptedException e) {
            }
            if (count > 240) {
                System.err.print("等待获取设备超时");
                break;
            }
        }
    }

}
