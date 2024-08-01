package de.dnb.basics.mvc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JPanel contentPane;
  JButton btnPress;
  private JMenuBar menuBar;
  private JMenu mnMenu;
  JMenuItem mntmInfo;
  private JLabel lblInput;
  private JLabel lblOutput_1;
  JTextField textFieldOutput;
  JTextField textFieldInput;

  /**
   * Create the frame.
   */
  public GUI() {
    initialize();
  }

  private void initialize() {
    setTitle("XXX");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 1170, 311);

    menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    mnMenu = new JMenu("?");
    menuBar.add(mnMenu);

    mntmInfo = new JMenuItem("Info");
    mnMenu.add(mntmInfo);

    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    final GridBagLayout gbl_contentPane = new GridBagLayout();
    gbl_contentPane.columnWidths =
      new int[] { 0, 0, 0, 0, 0, 96, 62, 89, 72, 77, 0, 49, 41, 27, 0, 0 };
    gbl_contentPane.rowHeights = new int[] { 32, 0, 0, 30, 0, 0, 0 };
    gbl_contentPane.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0,
      0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
    gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
    contentPane.setLayout(gbl_contentPane);

    btnPress = new JButton("Press");

    final GridBagConstraints gbc_btnPress = new GridBagConstraints();
    gbc_btnPress.fill = GridBagConstraints.HORIZONTAL;
    gbc_btnPress.insets = new Insets(0, 0, 5, 5);
    gbc_btnPress.gridx = 1;
    gbc_btnPress.gridy = 2;
    contentPane.add(btnPress, gbc_btnPress);

    lblInput = new JLabel("Input:");
    final GridBagConstraints gbc_lblInput = new GridBagConstraints();
    gbc_lblInput.insets = new Insets(0, 0, 5, 5);
    gbc_lblInput.gridx = 5;
    gbc_lblInput.gridy = 2;
    contentPane.add(lblInput, gbc_lblInput);

    textFieldInput = new JTextField();
    textFieldInput.setText("aaa");
    textFieldInput.setColumns(10);
    final GridBagConstraints gbc_textFieldInput = new GridBagConstraints();
    gbc_textFieldInput.insets = new Insets(0, 0, 5, 5);
    gbc_textFieldInput.fill = GridBagConstraints.HORIZONTAL;
    gbc_textFieldInput.gridx = 6;
    gbc_textFieldInput.gridy = 2;
    contentPane.add(textFieldInput, gbc_textFieldInput);

    lblOutput_1 = new JLabel("Output:");
    final GridBagConstraints gbc_lblOutput_1 = new GridBagConstraints();
    gbc_lblOutput_1.insets = new Insets(0, 0, 5, 5);
    gbc_lblOutput_1.gridx = 5;
    gbc_lblOutput_1.gridy = 3;
    contentPane.add(lblOutput_1, gbc_lblOutput_1);

    textFieldOutput = new JTextField();
    textFieldOutput.setColumns(10);
    final GridBagConstraints gbc_textFieldOutput = new GridBagConstraints();
    gbc_textFieldOutput.insets = new Insets(0, 0, 5, 5);
    gbc_textFieldOutput.fill = GridBagConstraints.HORIZONTAL;
    gbc_textFieldOutput.gridx = 6;
    gbc_textFieldOutput.gridy = 3;
    contentPane.add(textFieldOutput, gbc_textFieldOutput);
  }

}
