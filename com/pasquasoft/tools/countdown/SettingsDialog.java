package com.pasquasoft.tools.countdown;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

public class SettingsDialog extends JDialog implements ActionListener
{
  /**
   * Generated serial version UID.
   */
  private static final long serialVersionUID = -6821579667836387884L;

  private JTextField textField = new JTextField(20);

  private JFormattedTextField dateField;
  private JFormattedTextField timeField;

  private JButton ok = new JButton("OK");
  private JButton cancel = new JButton("Cancel");
  private JButton colorChooser = new JButton("Color Chooser...");

  private JPanel center = new JPanel();
  private JPanel south = new JPanel();

  private Properties prop;

  public SettingsDialog(Frame owner, String title, boolean modal,
      Properties prop)
  {
    super(owner, title, modal);

    this.prop = prop;

    try
    {
      MaskFormatter dateMask = new MaskFormatter("##/##/####");
      MaskFormatter timeMask = new MaskFormatter("##:##");

      dateField = new JFormattedTextField(dateMask);
      timeField = new JFormattedTextField(timeMask);

      dateField.setValue(prop.getProperty("countdown.date"));
      timeField.setValue(prop.getProperty("countdown.time"));
    }
    catch (ParseException pe)
    {
    }

    textField.setText(prop.getProperty("countdown.text"));

    center.setLayout(new GridLayout(4, 2));
    center.add(new Label("Countdown Date:"));
    center.add(dateField);
    center.add(new Label("Countdown Time:"));
    center.add(timeField);
    center.add(new Label("Countdown Text:"));
    center.add(textField);
    center.add(new Label("Text Color:"));
    center.add(colorChooser);

    south.add(ok);
    south.add(cancel);

    ok.addActionListener(this);
    cancel.addActionListener(this);
    colorChooser.addActionListener(this);

    /* Components should be added to the container's content pane */
    Container cp = getContentPane();

    cp.add(BorderLayout.CENTER, center);
    cp.add(BorderLayout.SOUTH, south);

    /* Add the window listener */
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt)
      {
        dispose();
      }
    });

    colorChooser
        .setForeground(Color.decode(prop.getProperty("countdown.color", "0")));

    getRootPane().setDefaultButton(ok);

    /* Size the dialog */
    setSize(300, 170);

    /* Center the dialog */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2,
        (screenDim.height - frameDim.height) / 2);

    setResizable(false);

    /* Show the dialog */
    setVisible(true);
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object obj = evt.getSource();

    if (obj == ok)
    {
      try
      {
        prop.setProperty("countdown.date", (String) dateField.getValue());
        prop.setProperty("countdown.time", (String) timeField.getValue());
        prop.setProperty("countdown.text", textField.getText());

        Util.saveProperties(prop);

        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
      }
      catch (IOException ioe)
      {
        JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
    else if (obj == cancel)
    {
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    else if (obj == colorChooser)
    {
      String color = prop.getProperty("countdown.color", "0");

      Color selected = JColorChooser.showDialog(this, "Text Color",
          Color.decode(color));

      colorChooser
          .setForeground(selected == null ? Color.decode(color) : selected);

      prop.setProperty("countdown.color",
          selected != null ? selected.hashCode() + "" : color);
    }
  }
}
