package installer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.IOException; // Add this import
import java.net.URL;

public class GUI implements ActionListener {
    public JLabel label;
    public JFrame frame;
    public JPanel panel;
    public JComboBox<String> minecraftVersionDropdown;
    public JComboBox<String> clientVersionDropdown;
    public JTextField minecraftPathTextField;
    public JTextField javaPathTextField;
    public JButton installButton;
    public JLabel statusLabel;
    public JProgressBar progressBar;
    public JCheckBox addLauncherProfileCheckbox; // Add this line
    private Client client;

    public GUI() {
        frame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(true);
        client = new Client(this);

        label = new JLabel("Morch Client installer");
        // Set the logo/icon for the frame from the URL
        try {
            URL logoUrl = new URL("https://avatars.githubusercontent.com/u/159921759?s=200&v=4");
            ImageIcon icon = new ImageIcon(logoUrl);
            frame.setIconImage(icon.getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        installButton = new JButton("Install");
        label.setHorizontalTextPosition(0);

        installButton.addActionListener(this);
        // Create JComboBox for Minecraft versions
        List<String> minecraftVersions = getMinecraftVersions();
        minecraftVersionDropdown = new JComboBox<>(minecraftVersions.toArray(new String[0]));
        minecraftVersionDropdown.addActionListener(e -> updateClientVersions());

        // Create JComboBox for client versions
        clientVersionDropdown = new JComboBox<>();
        updateClientVersions();

        // Create JTextField for .minecraft path
        minecraftPathTextField = new JTextField(System.getenv("APPDATA") + "/.minecraft");
        javaPathTextField = new JTextField("");
        javaPathTextField.setEnabled(false); // Disable Java path text field by default

        // Create status label
        statusLabel = new JLabel(" ");

        // Create progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        // Create the checkbox
        addLauncherProfileCheckbox = new JCheckBox("Add launcher profile");
        addLauncherProfileCheckbox.addActionListener(e -> {
            boolean selected = addLauncherProfileCheckbox.isSelected();
            javaPathTextField.setEnabled(selected); // Enable/disable Java path text field based on checkbox state
        });

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0, 1));

        frame.add(panel, BorderLayout.CENTER);
        panel.add(label);

        // Add Minecraft version dropdown to the panel
        panel.add(minecraftVersionDropdown);

        // Add client version dropdown to the panel
        panel.add(clientVersionDropdown);

        

        // Add .minecraft path input to the panel
        panel.add(new JLabel("Path to .minecraft:"));
        panel.add(minecraftPathTextField);
        // Add "Add launcher profile" checkbox to the panel
        panel.add(addLauncherProfileCheckbox);
        // Add Java path input to the panel
        panel.add(new JLabel("Java path:"));
        panel.add(javaPathTextField);

        JPanel p =new JPanel();
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(p);
        panel.add(installButton);

        // Add progress bar to the panel
        panel.add(progressBar);

        // Add status label to the panel
        panel.add(statusLabel);

        // Set fixed sizes for the panel and prevent resizing
        panel.setPreferredSize(new Dimension(400, 300)); // Increased height to accommodate progress bar and status
        // label
        panel.setMaximumSize(new Dimension(400, 300));
        panel.setMinimumSize(new Dimension(400, 300));

        // Disable window resizing
        frame.setResizable(false);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Morch Client installer");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultLookAndFeelDecorated(true);
        try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (UnsupportedLookAndFeelException e1) {}
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == installButton) {
            String selectedMinecraftVersion = (String) minecraftVersionDropdown.getSelectedItem();
            String selectedClientVersion = (String) clientVersionDropdown.getSelectedItem();
            String minecraftPath = minecraftPathTextField.getText();
            String javaPath = javaPathTextField.getText();
            boolean addLauncherProfile = addLauncherProfileCheckbox.isSelected(); // Get the state of the checkbox

            System.out.println("Selected Minecraft version: " + selectedMinecraftVersion);
            System.out.println("Selected Client version: " + selectedClientVersion);
            System.out.println("Path to .minecraft: " + minecraftPath);
            System.out.println("Java path: " + javaPath);
            System.out.println("Add launcher profile: " + addLauncherProfile); // Print the state of the checkbox

            client.installClient(selectedClientVersion, selectedMinecraftVersion, minecraftPath, javaPath, addLauncherProfile); // Pass the state of the checkbox
        }
    }

    // Other methods that were not moved to Client class
    public void updateClientVersions() {
        String selectedMinecraftVersion = (String) minecraftVersionDropdown.getSelectedItem();
        List<String> clientVersions = client.getClientVersions(selectedMinecraftVersion);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(clientVersions.toArray(new String[0]));
        clientVersionDropdown.setModel(model);
    }

    public List<String> getMinecraftVersions() {
        return client.getMinecraftVersions();
    }

}