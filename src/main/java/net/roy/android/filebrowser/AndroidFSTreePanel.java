package net.roy.android.filebrowser;

import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import net.roy.android.filebrowser.model.AndroidFSTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Roy on 2016/1/2.
 */
public class AndroidFSTreePanel {
    private JPanel mainPanel;
    private JTree treeFS;

    private AndroidFSTreeModel treeFSModel;

    public AndroidFSTreePanel() {
        treeFSModel=new AndroidFSTreeModel();
        treeFS.setModel(treeFSModel);
        treeFS.setCellRenderer(new AndroidFSTreeRenderer());
    }

    public void setFileListingService(FileListingService fileListingService) {
        treeFSModel.setFileListingService(fileListingService);
        treeFS.setSelectionPath(new TreePath(treeFSModel.getRoot()));
    }

    public Container getPanel() {
        return mainPanel;
    }

    public FileEntry getCurrentSelectedDir() {
        TreePath treePath=treeFS.getSelectionPath();
        if (treePath==null) {
            return null;
        }
        return (FileEntry)(treePath.getLastPathComponent());
    }

    public void setSelect(FileEntry currentDir) {
        List<FileEntry> enterPath =new ArrayList<>();
        FileEntry e=currentDir;
        while (e!=null) {
            enterPath.add(0,e);
            e=e.getParent();
        }
        treeFS.setSelectionPath(new TreePath(enterPath.toArray()));
    }

    private class AndroidFSTreeRenderer extends DefaultTreeCellRenderer{
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            FileEntry entry=(FileEntry) value;
            if (entry!=null) {
                if (entry.isRoot()) {
                    setText("/");
                } else {
                    setText(entry.getName());
                }
            }
            return component;
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeFS.addTreeSelectionListener(treeSelectionListener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeFS.removeTreeSelectionListener(treeSelectionListener);
    }
}
