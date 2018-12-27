package textcollage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * A panel that contains a large drawing area where strings can be drawn. The
 * strings are represented by objects of type DrawTextItem. An input box under
 * the panel allows the user to specify what string will be drawn when the user
 * clicks on the drawing area.
 */
public class DrawTextPanel extends JPanel {

    // As it now stands, this class can only show one string at at
    // a time!  The data for that string is in the DrawTextItem object
    // named theString.  (If it's null, nothing is shown.  This
    // variable should be replaced by a variable of type
    // ArrayList<DrawStringItem> that can store multiple items.
    private ArrayList<DrawTextItem> theString = new ArrayList<>();  // change to an ArrayList<DrawTextItem> !

    private Color currentTextColor = Color.BLACK;  // Color applied to new strings.

    private Canvas canvas;  // the drawing area.
    private JTextField input;  // where the user inputs the string that will be added to the canvas
    private SimpleFileChooser fileChooser;  // for letting the user select files
    private JMenuBar menuBar; // a menu bar with command that affect this panel
    private MenuHandler menuHandler; // a listener that responds whenever the user selects a menu command
    private JMenuItem undoMenuItem;  // the "Remove Item" command from the edit menu
    private JTextField rotateItem; //rotate the item.

    /**
     * An object of type Canvas is used for the drawing area. The canvas simply
     * displays all the DrawTextItems that are stored in the ArrayList, strings.
     */
    private class Canvas extends JPanel {

        Canvas() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.LIGHT_GRAY);
            setFont(new Font("Serif", Font.BOLD, 24));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (!theString.isEmpty()) {
                for (DrawTextItem i : theString) {
                    i.draw(g);
                }
            }
        }
    }

    /**
     * An object of type MenuHandler is registered as the ActionListener for all
     * the commands in the menu bar. The MenuHandler object simply calls
     * doMenuCommand() when the user selects a command from the menu.
     */
    private class MenuHandler implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            doMenuCommand(evt.getActionCommand());
        }
    }

    /**
     * Creates a DrawTextPanel. The panel has a large drawing area and a text
     * input box where the user can specify a string. When the user clicks the
     * drawing area, the string is added to the drawing area at the point where
     * the user clicked.
     */
    public DrawTextPanel() {
        fileChooser = new SimpleFileChooser();
        undoMenuItem = new JMenuItem("Remove Item");
        undoMenuItem.setEnabled(false);
        menuHandler = new MenuHandler();
        setLayout(new BorderLayout(3, 3));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        canvas = new Canvas();
        add(canvas, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.add(new JLabel("Text to add: "));
        input = new JTextField("Hello World!", 40);
        bottom.add(input);
        //add(bottom, BorderLayout.SOUTH);

        /**
         * Adding rotation feature, setting intfilter to the textfield to prevent 
         * input that is not Double and not in (0 360) range
         * 
         */
        bottom.add(new JLabel("Rotation: "));
        rotateItem = new JTextField("25", 5);
        PlainDocument doc = (PlainDocument) rotateItem.getDocument();
        doc.setDocumentFilter(new IntFilter());
        bottom.add(rotateItem);
        add(bottom, BorderLayout.SOUTH);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                doMousePress(e);
            }
        });
    }

    /**
     * This method is called when the user clicks the drawing area. A new string
     * is added to the drawing area. The center of the string is at the point
     * where the user clicked.
     *
     * @param e the mouse event that was generated when the user clicked
     */
    public void doMousePress(MouseEvent e) {
        String text = input.getText().trim();
        String rotation = rotateItem.getText().trim();
        if (text.length() == 0) {
            input.setText("Hello World!");
            text = "Hello World!";
        }
        if (rotation.length() == 0){
            rotateItem.setText("0");
            rotation = "0";
        }
        DrawTextItem s = new DrawTextItem(text, e.getX(), e.getY());
        s.setTextColor(currentTextColor);  // Default is null, meaning default color of the canvas (black).

//   SOME OTHER OPTIONS THAT CAN BE APPLIED TO TEXT ITEMS:
//		s.setFont( new Font( "Serif", Font.ITALIC + Font.BOLD, 12 ));  // Default is null, meaning font of canvas.
//		s.setMagnification(3);  // Default is 1, meaning no magnification.
//		s.setBorder(true);  // Default is false, meaning don't draw a border.
        s.setRotationAngle(Double.parseDouble(rotation));  // Default is 0, meaning no rotation.
//		s.setTextTransparency(0.3); // Default is 0, meaning text is not at all transparent.
//		s.setBackground(Color.BLUE);  // Default is null, meaning don't draw a background area.
//		s.setBackgroundTransparency(0.7);  // Default is 0, meaning background is not transparent.

        theString.add(s);  // Set this string as the ONLY string to be drawn on the canvas!

        undoMenuItem.setEnabled(true);
        canvas.repaint();
    }

    /**
     * Returns a menu bar containing commands that affect this panel. The menu
     * bar is meant to appear in the same window that contains this panel.
     */
    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBar();

            String commandKey; // for making keyboard accelerators for menu commands
            if (System.getProperty("mrj.version") == null) {
                commandKey = "control ";  // command key for non-Mac OS
            } else {
                commandKey = "meta ";  // command key for Mac OS
            }
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            JMenuItem saveItem = new JMenuItem("Save...");
            saveItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "N"));
            saveItem.addActionListener(menuHandler);
            fileMenu.add(saveItem);
            JMenuItem openItem = new JMenuItem("Open...");
            openItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "O"));
            openItem.addActionListener(menuHandler);
            fileMenu.add(openItem);
            fileMenu.addSeparator();
            JMenuItem saveImageItem = new JMenuItem("Save Image...");
            saveImageItem.addActionListener(menuHandler);
            fileMenu.add(saveImageItem);

            JMenu editMenu = new JMenu("Edit");
            menuBar.add(editMenu);
            undoMenuItem.addActionListener(menuHandler); // undoItem was created in the constructor
            undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "Z"));
            editMenu.add(undoMenuItem);
            editMenu.addSeparator();
            JMenuItem clearItem = new JMenuItem("Clear");
            clearItem.addActionListener(menuHandler);
            editMenu.add(clearItem);

            JMenu optionsMenu = new JMenu("Options");
            menuBar.add(optionsMenu);
            JMenuItem colorItem = new JMenuItem("Set Text Color...");
            colorItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "T"));
            colorItem.addActionListener(menuHandler);
            optionsMenu.add(colorItem);
            JMenuItem bgColorItem = new JMenuItem("Set Background Color...");
            bgColorItem.addActionListener(menuHandler);
            optionsMenu.add(bgColorItem);

        }
        return menuBar;
    }

    /**
     * Carry out one of the commands from the menu bar.
     *
     * @param command the text of the menu command.
     */
    private void doMenuCommand(String command) {
        if (command.equals("Save...")) { // save all the string info to a file
            /**
             * Procedure:
             * make a printWriter, write down data as strings
             * data includes: 
             * Background:
             * RGB
             * Strings
             * X Y rotation
             * RGB
             */
            PrintWriter writer;
            try {
                writer = new PrintWriter(fileChooser.getOutputFile(this, "Choose a file name :", "pictureFormat.txt"));
                Color c = canvas.getBackground();
                writer.println("Background");
                writer.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
                for (int i = 0; i < theString.size(); i++) {
                    Color cString = theString.get(i).getTextColor();
                    writer.println(theString.get(i).getString());
                    writer.println(theString.get(i).getX() + " " + theString.get(i).getY() + " " + theString.get(i).getRotationAngle());
                    writer.println(cString.getRed() + " " + cString.getGreen() + " " + cString.getBlue());
                }
                writer.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DrawTextPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            /**
             * open the file using BufferedReader, read through each line, parse
             * data into variable accordingly.
             * 
             */
            
        } else if (command.equals("Open...")) { // read a previously saved file, and reconstruct the list of strings
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(fileChooser.getInputFile(this, "Choose a file name :")));
                String line;
                String[] color, coor;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("Background")) {
                        color = reader.readLine().split(" ");
                        Color c = new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
                        canvas.setBackground(c);

                    } else if (line.equals("\n")) {
                        break;
                    } else {
                        String theStringBuffer = line;
                        coor = reader.readLine().split(" ");
                        theString.add(new DrawTextItem(theStringBuffer, Integer.parseInt(coor[0]), Integer.parseInt(coor[1])));
                        theString.get(theString.size() - 1).setRotationAngle(Double.parseDouble(coor[2]));
                        color = reader.readLine().split(" ");
                        Color c = new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
                        theString.get(theString.size() - 1).setTextColor(c);
                    }

                }

                reader.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(DrawTextPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DrawTextPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

            canvas.repaint(); // (you'll need this to make the new list of strings take effect)
        } else if (command.equals("Clear")) {  // remove all strings
            theString.clear();   // Remove the ONLY string from the canvas.

            undoMenuItem.setEnabled(false);
            canvas.repaint();
        } else if (command.equals("Remove Item")) { // remove the most recently added string
            theString.remove(theString.size() - 1);   // Remove the ONLY string from the canvas.

            if (theString.isEmpty()) {
                undoMenuItem.setEnabled(false);
            }
            canvas.repaint();
        } else if (command.equals("Set Text Color...")) {
            Color c = JColorChooser.showDialog(this, "Select Text Color", currentTextColor);
            if (c != null) {
                currentTextColor = c;
            }
        } else if (command.equals("Set Background Color...")) {
            Color c = JColorChooser.showDialog(this, "Select Background Color", canvas.getBackground());
            if (c != null) {
                canvas.setBackground(c);
                canvas.repaint();
            }
        } else if (command.equals("Save Image...")) {  // save a PNG image of the drawing area
            File imageFile = fileChooser.getOutputFile(this, "Select Image File Name", "textimage.png");
            if (imageFile == null) {
                return;
            }
            try {
                // Because the image is not available, I will make a new BufferedImage and
                // draw the same data to the BufferedImage as is shown in the panel.
                // A BufferedImage is an image that is stored in memory, not on the screen.
                // There is a convenient method for writing a BufferedImage to a file.
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = image.getGraphics();
                g.setFont(canvas.getFont());
                canvas.paintComponent(g);  // draws the canvas onto the BufferedImage, not the screen!
                boolean ok = ImageIO.write(image, "PNG", imageFile); // write to the file
                if (ok == false) {
                    throw new Exception("PNG format not supported (this shouldn't happen!).");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Sorry, an error occurred while trying to save the image:\n" + e);
            }
        }

        //add filter to document, modify test() function accordingly.
    }
}
