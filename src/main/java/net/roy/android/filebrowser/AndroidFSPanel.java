package net.roy.android.filebrowser;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.IDevice;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private Action exportAction;
    private Action importAction;

    public AndroidFSPanel() {
        treeFS.addTreeSelectionListener(e -> {
            tblFS.setCurrentDir(treeFS.getCurrentSelectedDir());
        });

        exportAction = new AbstractAction("导出...", new ImageIcon(this.getClass().getResource("/icons/export.png"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExport();
            }
        };

        importAction = new AbstractAction("导入...", new ImageIcon(this.getClass().getResource("/icons/import.png"))) {
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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnval = fileChooser.showDialog(mainPanel, "导入");
        if (returnval == JFileChooser.APPROVE_OPTION) {
            try {
                ProgressMonitor monitor = new ProgressMonitor(this.getPanel(), "导入", "正在导入", 0, fileChooser.getSelectedFiles().length);
                monitor.setProgress(0);
                exportAction.setEnabled(false);
                importAction.setEnabled(false);
                ImportTask importTask=new ImportTask(monitor,tblFS.getCurrentDir(),fileChooser.getSelectedFiles());
                importTask.execute();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this.getPanel(), "导出失败:" + e.getMessage(), "导出失败", JOptionPane.OK_OPTION);
            }
            tblFS.refresh();
        }
    }

    private void onExport() {
        List<FileEntry> fileEntries = tblFS.getSelectedFiles();

        if (fileEntries.size() == 0) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnval = fileChooser.showDialog(mainPanel, "导出");
        if (returnval == JFileChooser.APPROVE_OPTION) {
            try {
                ProgressMonitor monitor = new ProgressMonitor(this.getPanel(), "导出", "正在导出", 0, fileEntries.size());
                monitor.setProgress(0);
                exportAction.setEnabled(false);
                importAction.setEnabled(false);
                ExportTask exportTask=new ExportTask(monitor,fileChooser.getSelectedFile(),fileEntries);
                exportTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this.getPanel(), "导出失败:" + e.getMessage(), "导出失败", JOptionPane.OK_OPTION);
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

    private class ExportTask extends SwingWorker<Void,Void> {
        private ProgressMonitor monitor;
        private File dir;
        private List<FileEntry> fileEntries;
        private boolean canceled=false;

        public ExportTask(ProgressMonitor monitor, File dir, List<FileEntry> fileEntries) {
            this.monitor = monitor;
            this.dir = dir;
            this.fileEntries = fileEntries;
            this.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName() ) {
                        if (monitor.isCanceled() || isDone()) {
                            Toolkit.getDefaultToolkit().beep();
                            if (monitor.isCanceled()) {
                                canceled=true;
                            }
                        }  else {
                            int progress = (Integer) evt.getNewValue();
                            monitor.setProgress(progress);
                            monitor.setNote("已完成"+progress+"/"+monitor.getMaximum());
                        }
                    }
                }
            });
        }

        @Override
        protected Void doInBackground() throws Exception {
            int progess = 0;
            for (FileEntry entry : fileEntries) {
                if (canceled)
                    break;
                iDevice.pullFile(entry.getFullPath(), dir.getAbsolutePath() + File.separator + entry.getName());
                progess++;
                setProgress(progess);
            }
            exportAction.setEnabled(true);
            importAction.setEnabled(true);
            monitor.close();
            return null;
        }
    }

    private class ImportTask extends SwingWorker<Void,Void> {
        private ProgressMonitor monitor;
        private FileEntry dir;
        private File[] files;
        private boolean canceled=false;

        public ImportTask(ProgressMonitor monitor, FileEntry dir, File[] files) {
            this.monitor = monitor;
            this.dir = dir;
            this.files = files;
            this.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName() ) {
                        if (monitor.isCanceled() || isDone()) {
                            Toolkit.getDefaultToolkit().beep();
                            if (monitor.isCanceled()) {
                                canceled=true;
                            }
                        }   else {
                            int progress = (Integer) evt.getNewValue();
                            monitor.setProgress(progress);
                            monitor.setNote("已完成"+progress+"/"+monitor.getMaximum());
                        }
                    }
                }
            });
        }

        @Override
        protected Void doInBackground() throws Exception {
            int progess=0;
            for (File file : files) {
                if (monitor.isCanceled()) {
                    break;
                }
                iDevice.pushFile(file.getAbsolutePath(), dir.getFullPath() + "/" + file.getName());
                progess++;
                monitor.setProgress(progess);
            }
            monitor.close();
            exportAction.setEnabled(true);
            importAction.setEnabled(true);
            return null;
        }

    }
}
