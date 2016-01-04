package net.roy.android.filebrowser.model;

import com.android.ddmlib.FileListingService;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;
import java.util.*;

import static com.android.ddmlib.FileListingService.*;

/**
 * Created by Roy on 2016/1/3.
 */
public class AndroidFSTableSorter extends RowSorter<AndroidFSTableModel> implements Comparator<AndroidFSTableSorter.Row> {

    private AndroidFSTableModel model;


    private List<Row> rows=new ArrayList<>();
    private List<SortKey> sortKeys= Collections.EMPTY_LIST;
    private int[] viewToModel=null;
    private int[] modelToView=null;
    private Map<Integer,RowComparator> comparators=new HashMap<>();

    private final static RowComparator DEFAULT_ROW_COMPARATOR=new RowComparator() {

        @Override
        protected int doCompare(Row row1, Row row2) {
            return Integer.compare(row1.getRowIndex(),row2.getRowIndex());
        }
    };

    public final static RowComparator BY_NAME_COMPARATOR =new RowComparator() {
        @Override
        protected int doCompare(AndroidFSTableSorter.Row row1, AndroidFSTableSorter.Row row2) {
            return Files.getNameWithoutExtension(row1.getFileEntry().getName()).compareTo(
                    row2.getFileEntry().getName()
            );
        }
    };
    public final static RowComparator BY_EXT_COMPARATOR =new RowComparator() {
        @Override
        protected int doCompare(AndroidFSTableSorter.Row row1, AndroidFSTableSorter.Row row2) {
            return Files.getFileExtension(row1.getFileEntry().getName()).compareTo(
                    Files.getFileExtension(row2.getFileEntry().getName()));
        }
    };

    public final static RowComparator BY_SIZE_COMPARATOR=new RowComparator() {
        @Override
        protected int doCompare(AndroidFSTableSorter.Row row1, AndroidFSTableSorter.Row row2) {
            return Integer.compare(
                    row1.getFileEntry().getSizeValue(),
                    row2.getFileEntry().getSizeValue()
            );
        }
    } ;

    public static final RowComparator BY_DATE_COMPARATOR = new RowComparator() {
        @Override
        protected int doCompare(Row row1, Row row2) {
            int result=row1.getFileEntry().getDate().compareTo(row2.getFileEntry().getDate());
            if (result==0) {
                result=row1.getFileEntry().getTime().compareTo(row1.getFileEntry().getTime());
            }
            return result;
        }
    };

    public AndroidFSTableSorter(AndroidFSTableModel model) {
        super();
        this.model=model;
        model.addTableModelListener(this::onModelChanged);
        reloadModel();
    }

    private void onModelChanged(TableModelEvent tableModelEvent) {
        reloadModel();
    }

    private void reloadModel() {
        reloadData();
    }

    private void reloadData() {
        rows.clear();
        List<FileEntry> fileEntryList = model.getFiles();
        for (int i = 0; i < fileEntryList.size(); i++) {
            FileEntry entry = fileEntryList.get(i);
            rows.add(new Row(entry, i));
        }
        sort();
    }


    public  void setComparator(int column,RowComparator comparator) {
        Preconditions.checkElementIndex(column,model.getColumnCount());
        comparators.put(column, comparator);
    }

    public RowComparator  getComparator(int column) {
        RowComparator comparator=comparators.get(column);
        if (comparator==null) {
            comparator=DEFAULT_ROW_COMPARATOR;
        }
        return comparator;
    };



    @Override
    public AndroidFSTableModel getModel() {
        return model;
    }

    @Override
    public void toggleSortOrder(int column) {
        List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
        SortKey sortKey;
        int sortIndex;
        for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
            if (keys.get(sortIndex).getColumn() == column) {
                break;
            }
        }
        if (sortIndex == -1) {
            // Key doesn't exist
            sortKey = new SortKey(column, SortOrder.ASCENDING);
            keys.add(0, sortKey);
        }
        else if (sortIndex == 0) {
            // It's the primary sorting key, toggle it
            keys.set(0, toggle(keys.get(0)));
        }
        else {
            // It's not the first, but was sorted on, remove old
            // entry, insert as first with ascending.
            keys.remove(sortIndex);
            keys.add(0, new SortKey(column, SortOrder.ASCENDING));
        }
        if (keys.size() > getMaxSortKeys()) {
            keys = keys.subList(0, getMaxSortKeys());
        }
        setSortKeys(keys);
    }

    private SortKey toggle(SortKey key) {
        if (key.getSortOrder() == SortOrder.ASCENDING) {
            return new SortKey(key.getColumn(), SortOrder.DESCENDING);
        }
        return new SortKey(key.getColumn(), SortOrder.ASCENDING);
    }

    private int getMaxSortKeys() {
        return 1;
    }

    @Override
    public int convertRowIndexToModel(int index) {
        if (viewToModel==null) {
            return index;
        }
        return viewToModel[index];
    }

    @Override
    public int convertRowIndexToView(int index) {
        if (modelToView==null){
            return index;
        }
        return modelToView[index];
    }

    @Override
    public void setSortKeys(List<? extends SortKey> keys) {
        if ((keys==null) || (keys.size()==0) ) {
            this.sortKeys=Collections.EMPTY_LIST;
        } else {
            if (keys.stream().anyMatch(sortKey -> (sortKey == null || sortKey.getColumn() < 0 || sortKey.getColumn() >= model.getColumnCount()))){
                throw new IllegalArgumentException("Invalid SortKey");
            }
            this.sortKeys=Collections.unmodifiableList(new ArrayList<>(keys));
        }
        sort();
    }

    private void sort() {
        if (isUnsorted()) {
            viewToModel = null;
            modelToView = null;
        } else {
            Collections.sort(rows, this);
            viewToModel=new int[rows.size()];
            modelToView=new int[rows.size()];
            for (int i=0;i<viewToModel.length;i++) {
                viewToModel[i]=rows.get(i).getRowIndex();
                modelToView[rows.get(i).getRowIndex()]=i;
            }

        }

    }

    private boolean isUnsorted() {
        List<? extends SortKey> keys = getSortKeys();
        int keySize = keys.size();
        return (keySize == 0 || keys.get(0).getSortOrder() ==
                SortOrder.UNSORTED);
    }

    @Override
    public List<? extends SortKey> getSortKeys() {
        return sortKeys;
    }

    @Override
    public int getViewRowCount() {
        return model.getRowCount();
    }

    @Override
    public int getModelRowCount() {
        return model.getRowCount();
    }

    @Override
    public void modelStructureChanged() {
        reloadData();
    }

    @Override
    public void allRowsChanged() {
        reloadData();
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
        reloadData();
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        reloadData();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        sort();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
        sort();
    }

    @Override
    public int compare(Row o1, Row o2) {
        if (o1==o2) {
            return 0;
        }
        if (o1.getFileEntry().isDirectory() && !o2.getFileEntry().isDirectory()) {
            return -1;
        }
        if (!o1.getFileEntry().isDirectory() && o2.getFileEntry().isDirectory())  {
            return 1;
        }
        if (o1.getFileEntry().isDirectory() && o2.getFileEntry().isDirectory()) {
           return BY_NAME_COMPARATOR.compare(o1,o2);
        }
        int result;
        for (SortKey key:getSortKeys()) {
            int column=key.getColumn();
            SortOrder order=key.getSortOrder();
            if (order==SortOrder.UNSORTED) {
                result=Integer.compare(o1.getRowIndex(),o2.getRowIndex());
            } else {
                RowComparator comparator=getComparator(column);
                result=comparator.compare(o1,o2);
                if (order==SortOrder.DESCENDING) {
                    result=-result;
                }
                if (result==0) {
                    return BY_NAME_COMPARATOR.compare(o1,o2);
                }
            }
            if (result!=0)
                return result;
        }
        return Integer.compare(o1.getRowIndex(),o2.getRowIndex());
    }


    public static class Row {
        private FileEntry fileEntry;
        private int rowIndex;

        public Row(FileEntry fileEntry, int rowIndex) {
            this.fileEntry = fileEntry;
            this.rowIndex = rowIndex;
        }

        public FileEntry getFileEntry() {
            return fileEntry;
        }

        public int getRowIndex() {
            return rowIndex;
        }
    }

    public static abstract class RowComparator implements Comparator<Row> {
        @Override
        public int compare(Row o1, Row o2) {
            if (o1==o2) {
                return 0;
            }
            /* no need for this compare, has done in AndroidFSTableSorter.compare()
            if (o1.getFileEntry().isDirectory() && !o2.getFileEntry().isDirectory()) {
                return -1;
            }
            if (!o1.getFileEntry().isDirectory() && o2.getFileEntry().isDirectory())  {
                return 1;
            }
            */
            return doCompare(o1,o2);
        }

        protected abstract int doCompare(Row row1, Row row2);
    }
}
