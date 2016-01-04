package net.roy.android.filebrowser;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.IDevice;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Roy on 2016/1/3.
 */
public class AndroidFSPanel {
    private JPanel mainPanel;
    private AndroidFSTreePanel treeFS;
    private AndroidFSTablePanel tblFS;
    private JToolBar toolBar;
    private IDevice iDevice;

    public AndroidFSPanel(){
        treeFS.addTreeSelectionListener(e -> {
            tblFS.setCurrentDir(treeFS.getCurrentSelectedDir());
        });

        Action exportAction=new AbstractAction("导出...", new ImageIcon(this.getClass().getResource("/icons/export.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExport();
            }
        };

        Action importAction=new AbstractAction("导入...", new ImageIcon(this.getClass().getResource("/icons/import.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onImport();
            }
        };
        toolBar.add(exportAction);
        toolBar.add(importAction);

        tblFS.addActionListener(this::onDirChanged);
    }

    private void onDirChanged(ActionEvent actionEvent) {
        treeFS.setSelect(tblFS.getCurrentDir());
    }

    private void onImport() {
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnval=fileChooser.showDialog(mainPanel,"导入");
        if (returnval==JFileChooser.APPROVE_OPTION) {
            try {
                for (File file: fileChooser.getSelectedFiles()){
                        iDevice.pushFile(file.getAbsolutePath(), tblFS.getCurrentDir().getFullPath() + "/" + file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this.getPanel(),"导出失败:"+e.getMessage(),"导出失败",JOptionPane.OK_OPTION);
            }
            tblFS.refresh();
        }
    }

    private void onExport() {
        List<FileEntry> fileEntries=tblFS.getSelectedFiles();

        if (fileEntries.size()==0) {
            return ;
        }

        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnval=fileChooser.showDialog(mainPanel,"导出");
        if (returnval==JFileChooser.APPROVE_OPTION) {
            try {
                File dir = fileChooser.getSelectedFile();
                for (FileEntry entry : fileEntries) {
                    iDevice.pullFile(entry.getFullPath(), dir.getAbsolutePath()+File.separator+entry.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this.getPanel(),"导出失败:"+e.getMessage(),"导出失败",JOptionPane.OK_OPTION);
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
        setFileListingService(iDevice.getFileListingService());
    }

    private void setFileListingService(FileListingService fileListingService) {
        tblFS.setFileListingService(fileListingService);
        treeFS.setFileListingService(fileListingService);
    }
}
