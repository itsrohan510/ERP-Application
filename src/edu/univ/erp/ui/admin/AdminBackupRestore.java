package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DataStore;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AdminBackupRestore {
    
    public static void performBackup(Frame parent, Runnable onComplete) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("erp_backup.bin"));
        int option = chooser.showSaveDialog(parent);
        if (option != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            Files.copy(Path.of(DataStore.getDataFilePath()), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(parent, "Backup created at " + file.getAbsolutePath(), "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
            if (onComplete != null) onComplete.run();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Backup failed: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void performRestore(Frame parent, Runnable onReload) {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(parent);
        if (option != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        int confirm = JOptionPane.showConfirmDialog(parent,
            "Restore data from " + file.getName() + "? This will overwrite current data.",
            "Confirm Restore",
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Files.copy(file.toPath(), Path.of(DataStore.getDataFilePath()), StandardCopyOption.REPLACE_EXISTING);
            DataStore.reload();
            if (onReload != null) onReload.run();
            JOptionPane.showMessageDialog(parent, "Restore complete. Data reloaded from backup.", "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Restore failed: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
