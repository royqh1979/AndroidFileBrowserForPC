package net.roy.android.filebrowser;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.google.common.io.Files;
import net.roy.android.filebrowser.model.AndroidFSTableModel;
import net.roy.android.filebrowser.model.AndroidFSTableSorter;
import net.roy.android.filebrowser.model.AndroidFSTableSorter.RowComparator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created by Roy on 2016/1/3.
 */
public class AndroidFSTablePanel {
    private JPanel mainPanel;
    private JTable tblFiles;

    private AndroidFSTableModel tblFilesModel;

    private FileListingService fileListingService;

    private FileEntry currentDir;

    private List<ActionListener> actionListenerList=new ArrayList<>();


    public AndroidFSTablePanel() {
        tblFilesModel = new AndroidFSTableModel();
        tblFiles.setModel(tblFilesModel);
        tblFiles.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        AndroidFSTableCellRenderer renderer=new AndroidFSTableCellRenderer();
        tblFiles.getColumnModel().getColumn(0).setCellRenderer(renderer);
        tblFiles.getColumnModel().getColumn(1).setCellRenderer(renderer);
        tblFiles.getColumnModel().getColumn(2).setCellRenderer(renderer);
        tblFiles.getColumnModel().getColumn(3).setCellRenderer(renderer);

        initSorter();

        tblFiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount()>=2) {
                    FileEntry entry=currentDir.getCachedChildren()[tblFiles.convertRowIndexToModel(tblFiles.getSelectedRow())];
                    setCurrentDir(entry);
                }
                fireDirChanged();
            }
        });


    }

    private void fireDirChanged() {
        ActionEvent event=new ActionEvent(this,0,"");
        for (ActionListener actionListener: actionListenerList) {
            actionListener.actionPerformed(event) ;
        }
    }

    public void addActionListener(ActionListener actionListener) {
        actionListenerList.add(actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        actionListenerList.remove(actionListener);
    }

    private void initSorter() {
        AndroidFSTableSorter sorter=new AndroidFSTableSorter(tblFilesModel);


        sorter.setComparator(AndroidFSTableModel.NAME_COLUMN, AndroidFSTableSorter.BY_NAME_COMPARATOR);
        sorter.setComparator(AndroidFSTableModel.EXT_COLUMN, AndroidFSTableSorter.BY_EXT_COMPARATOR);
        sorter.setComparator(AndroidFSTableModel.SIZE_COLUMN, AndroidFSTableSorter.BY_SIZE_COMPARATOR);
        sorter.setComparator(AndroidFSTableModel.DATE_COLUMN, AndroidFSTableSorter.BY_DATE_COMPARATOR);


        tblFiles.setRowSorter(sorter);
        tblFiles.getRowSorter().toggleSortOrder(0);
    }

    public FileListingService getFileListingService() {
        return fileListingService;
    }

    public void setFileListingService(FileListingService fileListingService) {
        this.fileListingService = fileListingService;
    }

    public FileEntry getCurrentDir() {
        return currentDir;
    }

    public void gotoParent() {
        FileEntry parent=currentDir.getParent();
        if (parent!=null) {
            setCurrentDir(parent);
        }
    }

    public void setCurrentDir(FileEntry currentDir) {
        this.currentDir = currentDir;
        refresh();
    }

    public List<FileEntry> getSelectedFiles() {
        int[] idxs=tblFiles.getSelectedRows();
        List<FileEntry> fileEntries=new ArrayList<>();
        FileEntry[] children=currentDir.getCachedChildren();
        for (int i:idxs) {
            int realIndex=tblFiles.convertRowIndexToModel(i); //Must change view index (sorted) to real index first
            fileEntries.add(children[realIndex]);
        }
        return fileEntries;
    }

    public void refresh() {
        if (currentDir==null) {
            tblFilesModel.setLstFiles(Collections.EMPTY_LIST);
        } else {
            List<FileEntry> childs = Arrays.asList(fileListingService.getChildren(currentDir,false,null));
            tblFilesModel.setLstFiles(childs);
        }
    }

    private class AndroidFSTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            FileEntry entry=(FileEntry) value;
            switch (column) {
                case 0:
                    showNameColumn(entry);
                    break;
                case 1:
                    showExtColumn(entry);
                    break;
                case 2:
                    showSizeColumn(entry);
                    break;
                case 3:
                    showDateColumn(entry);
                    break;
            }
            return this;
        }

        private void showDateColumn(FileEntry entry) {
            setIcon(null);
            setText(entry.getDate()+" "+entry.getTime());
        }

        private void showSizeColumn(FileEntry entry) {
            setIcon(null);
            if (entry.isDirectory()) {
                setText("");
            } else {
              setText(entry.getSize());
            }
        }



        private void showExtColumn(FileEntry entry) {
            setIcon(null);
            if (entry.isDirectory()) {
                setText("");
            }else{
                setText(Files.getFileExtension(entry.getName()));
            }
        }

        private void showNameColumn(FileEntry entry) {
            setText(Files.getNameWithoutExtension(entry.getName()));
            if (entry.isDirectory()) {
                setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
        }
    }

}
