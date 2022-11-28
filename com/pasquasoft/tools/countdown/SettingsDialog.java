package com.pasquasoft.tools.countdown;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;

public class SettingsDialog extends JDialog implements ActionListener
{
  /**
   * Generated serial version UID.
   */
  private static final long serialVersionUID = -6821579667836387884L;

  private static final String REGEX_TIME_MASK = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

  private JTextField textField = new JTextField(20);

  private JFormattedTextField dateField;
  private JFormattedTextField timeField;

  private JButton ok = new JButton("OK");
  private JButton cancel = new JButton("Cancel");
  private JButton colorChooser = new JButton("Color Chooser...");

  private JPanel center = new JPanel();
  private JPanel south = new JPanel();

  private Properties prop;

  public SettingsDialog(Frame owner, String title, boolean modal, Properties prop)
  {
    super(owner, title, modal);

    this.prop = prop;

    try
    {
      MaskFormatter dateMask = new MaskFormatter("##/##/####");
      MaskFormatter timeMask = new MaskFormatter("##:##");

      dateField = new JFormattedTextField(dateMask);
      timeField = new JFormattedTextField(timeMask);

      dateField.setText(prop.getProperty("countdown.date"));
      timeField.setText(prop.getProperty("countdown.time"));
    }
    catch (ParseException pe)
    {
    }

    textField.setDocument(new RequiredDocument());
    textField.setText(prop.getProperty("countdown.text"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 3, 3);

    center.setLayout(gbl);
    center.add(new Label("Countdown Date:"), constraintsHelper(gbc, 0, 0));
    center.add(dateField, constraintsHelper(gbc, 1, 0));
    center.add(new Label("Countdown Time:"), constraintsHelper(gbc, 0, 1));
    center.add(timeField, constraintsHelper(gbc, 1, 1));
    center.add(new Label("Countdown Text:"), constraintsHelper(gbc, 0, 2));
    center.add(textField, constraintsHelper(gbc, 1, 2));
    center.add(new Label("Text Color:"), constraintsHelper(gbc, 0, 3));
    center.add(colorChooser, constraintsHelper(gbc, 1, 3));

    south.add(ok);
    south.add(cancel);

    ok.addActionListener(this);
    cancel.addActionListener(this);
    colorChooser.addActionListener(this);

    ok.setEnabled(textField.getText().length() != 0);

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

    colorChooser.setForeground(Color.decode(prop.getProperty("countdown.color", "0")));

    getRootPane().setDefaultButton(ok);

    /* Size the dialog */
    pack();

    /* Center the dialog */
    setLocationRelativeTo(owner);

    setResizable(false);

    /* Show the dialog */
    setVisible(true);
  }

  private Object constraintsHelper(GridBagConstraints gbc, int x, int y)
  {
    gbc.gridx = x;
    gbc.gridy = y;

    return gbc;
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object obj = evt.getSource();

    if (obj == ok)
    {
      try
      {
        if (timeField.getText().matches(REGEX_TIME_MASK) && isValid(dateField.getText()))
        {
          prop.setProperty("countdown.date", dateField.getText());
          prop.setProperty("countdown.time", timeField.getText());
          prop.setProperty("countdown.text", textField.getText().trim());

          Util.saveProperties(prop);

          dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
        else
        {
          JOptionPane.showMessageDialog(this, "Entered date and/or time invalid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (IOException ioe)
      {
        JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    else if (obj == cancel)
    {
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    else if (obj == colorChooser)
    {
      String color = prop.getProperty("countdown.color", "0");

      Color selected = JColorChooser.showDialog(this, "Text Color", Color.decode(color));

      colorChooser.setForeground(selected == null ? Color.decode(color) : selected);

      prop.setProperty("countdown.color", selected != null ? selected.hashCode() + "" : color);
    }
  }

  private boolean isValid(String dateStr)
  {
    try
    {
      FORMATTER.parse(dateStr);
    }
    catch (DateTimeParseException e)
    {
      return false;
    }

    return true;
  }

  private class RequiredDocument extends PlainDocument
  {
    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 7140928933430520858L;

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
      super.insertString(offs, str, a);
      ok.setEnabled(textField.getText().trim().length() != 0);
    }

    @Override
    public void remove(int offset, int length) throws BadLocationException
    {
      super.remove(offset, length);
      ok.setEnabled(!(textField.getText().trim().length() == 0));
    }
  }
}
