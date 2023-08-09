package editor;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

//    private final String DIR = System.getProperty("user.dir") + "/Text Editor/task/"; // for local file loading and saving
    private final String DIR = System.getProperty("user.dir"); // for testing

    public TextEditor() {
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        JTextField searchField = new JTextField(25);
        ImageIcon saveIcon = new ImageIcon(DIR + "/saveIcon.png");
        ImageIcon loadIcon = new ImageIcon(DIR + "/loadIcon.png");
        ImageIcon searchIcon = new ImageIcon(DIR + "/searchIcon.png");
        ImageIcon previousIcon = new ImageIcon(DIR + "/previousIcon.png");
        ImageIcon nextIcon = new ImageIcon(DIR + "/nextIcon.png");

        int buttonSize = 25;

        Image scaledSaveImage = saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        Image scaledLoadImage = loadIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        Image scaledSearchImage = searchIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        Image scaledPrevImage = previousIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        Image scaledNextImage = nextIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        ImageIcon scaledSaveIcon = new ImageIcon(scaledSaveImage);
        ImageIcon scaledLoadIcon = new ImageIcon(scaledLoadImage);
        ImageIcon scaledSearchIcon = new ImageIcon(scaledSearchImage);
        ImageIcon scaledPrevIcon = new ImageIcon(scaledPrevImage);
        ImageIcon scaledNextIcon = new ImageIcon(scaledNextImage);
        JButton saveButton = new JButton(scaledSaveIcon);
        JButton loadButton = new JButton(scaledLoadIcon);
        JButton searchButton = new JButton(scaledSearchIcon);
        JButton previousButton = new JButton(scaledPrevIcon);
        JButton nextButton = new JButton(scaledNextIcon);

        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, buttonSize + 10)); // Match button's height

        JCheckBox regexCheckBox = new JCheckBox("Use regex");

        topRow.add(saveButton);
        topRow.add(loadButton);
        topRow.add(searchField);
        topRow.add(searchButton);
        topRow.add(previousButton);
        topRow.add(nextButton);
        topRow.add(regexCheckBox);

        JTextArea textArea = new JTextArea(10, 25);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), // Margin
                BorderFactory.createLineBorder(Color.BLACK) // Optional: Add border around text area
        ));

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        JMenu searchMenu = new JMenu("Search");
        JMenuItem startSearchMenuItem = new JMenuItem("Start search");
        JMenuItem prevMatchMenuItem = new JMenuItem("Previous match");
        JMenuItem nextMatchMenuItem = new JMenuItem("Next match");
        JMenuItem regexpMenuItem = new JMenuItem("Use regular expressions");

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(prevMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(regexpMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(searchMenu);

        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        textArea.setName("TextArea");
        searchField.setName("SearchField");
        saveButton.setName("SaveButton");
        loadButton.setName("OpenButton");
        searchButton.setName("StartSearchButton");
        previousButton.setName("PreviousMatchButton");
        nextButton.setName("NextMatchButton");
        regexCheckBox.setName("UseRegExCheckbox");
        scrollPane.setName("ScrollPane");
        fileMenu.setName("MenuFile");
        searchMenu.setName("MenuSearch");
        openMenuItem.setName("MenuOpen");
        saveMenuItem.setName("MenuSave");
        exitMenuItem.setName("MenuExit");
        startSearchMenuItem.setName("MenuStartSearch");
        prevMatchMenuItem.setName("MenuPreviousMatch");
        nextMatchMenuItem.setName("MenuNextMatch");
        regexpMenuItem.setName("MenuUseRegExp");

        saveButton.addActionListener(e -> writeToFile(fileChooser, textArea));
        saveMenuItem.addActionListener(e -> writeToFile(fileChooser, textArea));

        loadButton.addActionListener(e -> readFromFile(fileChooser, textArea));
        openMenuItem.addActionListener(e -> readFromFile(fileChooser, textArea));

        AtomicInteger count = new AtomicInteger();

        startSearchMenuItem.addActionListener(e -> multiThreadedSearch(searchField, searchButton, startSearchMenuItem, textArea, count));
        searchButton.addActionListener(e -> multiThreadedSearch(searchField, searchButton, startSearchMenuItem, textArea, count));

        nextMatchMenuItem.addActionListener(e -> nextMatch(searchField, textArea, count));
        nextButton.addActionListener(e -> nextMatch(searchField, textArea, count));
        prevMatchMenuItem.addActionListener(e -> prevMatch(searchField, textArea, count));
        previousButton.addActionListener(e -> prevMatch(searchField, textArea, count));

        regexpMenuItem.addActionListener(e -> regexCheckBox.setSelected(true));

        exitMenuItem.addActionListener(e -> exit());

        setJMenuBar(menuBar);
        add(panel);

        setVisible(true);
        JFileChooser jfc = new JFileChooser();
        jfc.setName("FileChooser");
        jfc.setVisible(false);
        add(jfc);

    }

    private void prevMatch(JTextField searchField, JTextArea textArea, AtomicInteger count) {
        var matchList = searchText(searchField, textArea, false, count);
        count.decrementAndGet();
        try {
            textMatch(textArea, matchList.get(count.get())[0], matchList.get(count.get())[1]);
        } catch (ArrayIndexOutOfBoundsException exception) {
            count.set(matchList.size() - 1);
            textMatch(textArea, matchList.get(count.get())[0], matchList.get(count.get())[1]);
        }
    }

    private void nextMatch(JTextField searchField, JTextArea textArea, AtomicInteger count) {
        var matchList = searchText(searchField, textArea, false, count);
        count.incrementAndGet();
        try {
            textMatch(textArea, matchList.get(count.get())[0], matchList.get(count.get())[1]);
        } catch (ArrayIndexOutOfBoundsException exception) {
            count.set(0);
            textMatch(textArea, matchList.get(count.get())[0], matchList.get(count.get())[1]);
        }
    }

    private void multiThreadedSearch(JTextField searchField, JButton searchButton, JMenuItem startSearchMenuItem,
                                     JTextArea textArea, AtomicInteger count) {
        searchButton.setEnabled(false);
        startSearchMenuItem.setEnabled(false);
        SwingWorker<List<int[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<int[]> doInBackground() {
                return searchText(searchField, textArea, true, count);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                startSearchMenuItem.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void textMatch(JTextArea textArea, int start, int end) {
        System.out.println("end = " + end);
        textArea.setCaretPosition(end);
        textArea.select(start, end);
        textArea.grabFocus();
    }

    private List<int[]> searchText(JTextField searchField, JTextArea textArea, boolean searchStart,
                                   AtomicInteger count) {
        List<int[]> matchedList = new CopyOnWriteArrayList<>();
        String text = textArea.getText();
        String patternString = searchField.getText(); // Your pattern here

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            matchedList.add(new int[]{start, end});
        }
        if (searchStart && !matchedList.isEmpty()) {
            textMatch(textArea, matchedList.get(0)[0], matchedList.get(0)[1]);
            count.set(0);
        }
        return matchedList;
    }

    private String dialogBoxSaveFile(JFileChooser fileChooser) {
         int returnValue = fileChooser.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String selectedFilePath = selectedFile.getAbsolutePath();
            System.out.println(selectedFilePath);
            return selectedFilePath;
        }
        System.out.println("load failed");
        return null;
    }

    private String dialogBoxLoadFile(JFileChooser fileChooser) {
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String selectedFilePath = selectedFile.getAbsolutePath();
            System.out.println(selectedFilePath);
            return selectedFilePath;
        }
        System.out.println("load failed");
        return null;
    }

    private void readFromFile(JFileChooser fileChooser, JTextArea textArea) {
        String filename = dialogBoxLoadFile(fileChooser);
        String input = "";
        if (filename != null) {
            try {
                input = Files.readString(Paths.get(filename));
            } catch (FileNotFoundException e) {
                System.out.println("The file was not found.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                textArea.setText(input);
            }
        }
    }

    private void writeToFile(JFileChooser fileChooser, JTextArea textArea) {
        String filename = dialogBoxSaveFile(fileChooser);
        if (filename != null) {
            try (FileWriter fileWriter = new FileWriter(filename)) {
                fileWriter.write(textArea.getText());
                // Flush and close the FileWriter to ensure data is written to the file
                fileWriter.flush();
            } catch (IOException e) {
                System.out.println("An error occurred while writing to the file.");
                e.printStackTrace();
            }
        }
    }

    private void exit() {
        dispose();
        System.exit(0);
    }
}