package net.roy.android.filebrowser.model;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roy on 2016/1/3.
 */
public class AndroidFSTableModel extends AbstractTableModel  {
    public final static int NAME_COLUMN=0;
    public final static int EXT_COLUMN=1;
    public final static int SIZE_COLUMN=2;
    public final static int DATE_COLUMN=3;

    List<FileEntry> lstFiles= Collections.EMPTY_LIST;

    public List<FileEntry> getLstFiles() {
        return lstFiles;
    }

    public void setLstFiles(List<FileEntry> lstFiles) {
        Preconditions.checkNotNull(lstFiles);
        this.lstFiles = lstFiles;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return lstFiles.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "名称";
            case 1:
                return "后缀名";
            case 2:
                return "大小";
            case 3:
                return "修改日期";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileEntry entry=lstFiles.get(rowIndex);
        return entry;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }


    public List<FileEntry> getFiles() {
        return lstFiles;
    }
}
