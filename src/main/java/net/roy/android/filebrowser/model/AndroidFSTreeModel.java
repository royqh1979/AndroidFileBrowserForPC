package net.roy.android.filebrowser.model;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model for andrid file system
 * Created by Roy on 2016/1/2.
 */
public class AndroidFSTreeModel implements TreeModel {
    FileListingService fileListingService;
    FileEntry currentParent=null;
    FileEntry[] currentChilds={};

    private static final Comparator<FileEntry> FILE_ENTRY_COMPARATOR =  (o1, o2) -> {
        if (o1==o2)
            return 0;
        if (o1.isDirectory() && !o2.isDirectory()){
            return 1;
        }
        if (!o1.isDirectory() && o2.isDirectory() ){
            return -1;
        }
        return o1.getName().compareTo(o2.getName());
    };

    private List<TreeModelListener> treeModelListeners=new ArrayList<>();

    public FileListingService getFileListingService() {
        return fileListingService;
    }

    public void setFileListingService(FileListingService fileListingService) {
        this.fileListingService = fileListingService;
        //fireDataChanged();
    }

    private void fireDataChanged() {
        TreeModelEvent event=new TreeModelEvent(this,(TreePath)null);
        for (TreeModelListener treeModelListener:treeModelListeners) {
            treeModelListener.treeStructureChanged(event);
        }

    }

    @Override
    public Object getRoot() {
        if (fileListingService==null)
            return null;
        return fileListingService.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        refreshCache(parent);
        return currentChilds[index];
    }

    @Override
    public int getChildCount(Object parent) {
        refreshCache(parent);
        return currentChilds.length;
    }

    /**
     * 更新缓存
     * @param parent
     */
    private void refreshCache(Object parent) {


        if (parent!=currentParent) {
            currentParent = (FileEntry) parent;
            FileEntry[] tmpChilds = fileListingService.getChildren(currentParent, false, null);
            List<FileEntry> childs=Arrays.asList(tmpChilds);
            currentChilds=childs.stream()
                    .filter(fileEntry -> fileEntry.isDirectory())
                    .collect(Collectors.toList()).toArray(new FileEntry[]{});
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        FileEntry entry=(FileEntry)node;
        return !entry.isDirectory();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
          //TODO:
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        refreshCache(parent);
        return ArrayUtils.indexOf(currentChilds, child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }
}
