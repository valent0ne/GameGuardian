import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by Stefano on 14/05/2015.
 */
public class gui extends JFrame {
    private JPanel rootPanel;
    private JButton start;
    private JTextArea log;
    private JButton exit;
    private JScrollPane scroll;
    private JProgressBar progressBar;
    private JLabel timer;
    private JLabel logTitle;
    private JButton abortButton;
    boolean success=true;
    boolean nothingDone=true;

    public gui() {
        super();

        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        setSize(new Dimension(500, 350));
        log.setEditable(false);
        progressBar.setVisible(false);

        /**
         * START
         */

        success = tryStart();

        //exit button terminate execution
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(success)
                    System.exit(0);
                else {
                    success=tryStart();
                }

            }
        });

    }

    public boolean tryStart() {
        try {
            nothingDone=true;
            success = start("path.txt");
            if(nothingDone)
                logAppend("All files are up to date, there is nothing to backup.");

        } catch (IOException e1) {
            timer.setText("Fatal error.");
            logAppend("Something went wrong.\nCheck the config file and retry.\n");
            e1.printStackTrace();
            progressBar.setVisible(false);
            exit.setEnabled(true);
            exit.setText("Retry");
            success = false;
        }
        return success;
    }

    public void logAppend(String text) {
        log.append(text + "\n");
        log.select(2147483647, 0);
    }

    /**
     * gui ends
     *
     * @param path
     * @throws IOException
     */

    public boolean start(String path) throws IOException {

        log.setText("");

        for(int i=5;i>0;i--){
            if(i==1)
                timer.setText("The process will start in 1 second, close this window to abort.");
            else
                timer.setText("The process will start in "+i+" seconds, close this window to abort.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        timer.setText("Processing...");
        progressBar.setVisible(true);

        //Get file with paths
        BufferedReader in = new BufferedReader(new FileReader("path.txt"));
        //Static prefix of destination Cloud directory
        String StaticCloudPath = in.readLine();
        //Static prefix of destination HDD directory
        String StaticHDDPath = in.readLine();
         //Static prefix of destination NAS directory
    //    String StaticNASPath = in.readLine();

        //Declaration of variables used as source and dest
        File destinationFolderCloud;
        File destinationFolderHDD;
        //File destinationFolderNAS;
        File sourceFolder;

        //Get the destination suffix
        String destSuffix = in.readLine();
        //Get the source path
        String tempS = in.readLine();


        while (destSuffix != null) { //scan the file

            if (tempS != null) {
                //source become the one i read before
                sourceFolder = new File(tempS);
                //dest become static prefix+actual suffix
                destinationFolderCloud = new File(StaticCloudPath + destSuffix);
                destinationFolderHDD = new File(StaticHDDPath + destSuffix);
              //  destinationFolderNAS = new File(StaticNASPath + destSuffix);

                //copy function call
                copyFolder(sourceFolder, destinationFolderCloud);
                copyFolder(sourceFolder, destinationFolderHDD);
             //   copyFolder(sourceFolder, destinationFolderNAS);

                //update destSuffix and source
                destSuffix = in.readLine();
                tempS = in.readLine();
            } else break;
        }

        in.close();

        progressBar.setVisible(false);
        timer.setText("Process completed successfully.");

        exit.setText("Exit");
        exit.setEnabled(true);

        return true;

    }


    /**
     * This function recursively copy all the sub folder and files from sourceFolder to destinationFolder
     */
    private void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
                logAppend("Directory created :: " + destinationFolder);
                nothingDone=false;
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {

                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        } else {

            long dateSrc = sourceFolder.lastModified();
            long dateDest = destinationFolder.lastModified();

            if (dateSrc > dateDest) {
                //Copy the file content from one place to another
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logAppend("File copied :: " + destinationFolder);
                nothingDone=false;
            }

        }
    }


}
