package com.pasquasoft.tools.countdown;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class CountdownFrame extends JFrame implements ActionListener
{
  /*
   * Generated serial version UID.
   */
  private static final long serialVersionUID = 4657914421139681570L;

  private static final String PREFERENCES_LOOK_AND_FEEL = "Countdown.laf";
  private static final String PREFERENCES_DATE = "Countdown.date";
  private static final String PREFERENCES_TIME = "Countdown.time";
  private static final String PREFERENCES_TEXT = "Countdown.text";
  private static final String PREFERENCES_COLOR = "Countdown.color";

  private DrawPanel drawPanel = new DrawPanel();

  private JMenuBar mb = new JMenuBar();

  private JMenu configure = new JMenu("Configure");
  private JMenu view = new JMenu("View");
  private JMenu look = new JMenu("Look and Feel");
  private JMenu help = new JMenu("Help");

  private JMenuItem configureStart = new JMenuItem("Start");
  private JMenuItem configureStop = new JMenuItem("Stop");
  private JMenuItem configureSettings = new JMenuItem("Settings...");
  private JMenuItem configureExit = new JMenuItem("Exit");
  private JMenuItem helpAbout = new JMenuItem("About...");

  private ButtonGroup looksGroup = new ButtonGroup();

  private JRadioButtonMenuItem looks[];

  private long countdownSeconds;
  private long currentDateSeconds;

  private String date;
  private String time;
  private String textStr;

  private Preferences preferences = Preferences.userNodeForPackage(Countdown.class);

  private java.util.Timer countdownTimer;

  public CountdownFrame()
  {
    super("Countdown");

    UIManager.LookAndFeelInfo[] installedLooks = UIManager.getInstalledLookAndFeels();

    looks = new JRadioButtonMenuItem[installedLooks.length];

    String defaultLaf = UIManager.getLookAndFeel().getName();

    defaultLaf = preferences.get(PREFERENCES_LOOK_AND_FEEL, defaultLaf);

    /*
     * Create menu items, add action listener, set the action command to the
     * Look and Feel class name, set selected menu item, and add to Look and
     * Feel menu and group.
     */
    for (int i = 0; i < looks.length; i++)
    {
      String installedLaf = installedLooks[i].getName();

      looks[i] = new JRadioButtonMenuItem(installedLaf);
      looks[i].addActionListener(this);
      looks[i].setActionCommand(installedLooks[i].getClassName());

      if (installedLaf.equals(defaultLaf))
      {
        looks[i].setSelected(true);
      }

      looksGroup.add(looks[i]);
      look.add(looks[i]);
    }

    date = preferences.get(PREFERENCES_DATE, "");
    time = preferences.get(PREFERENCES_TIME, "");

    configureStop.setEnabled(false);

    /* Components should be added to the container's content pane */
    Container cp = getContentPane();

    cp.add(BorderLayout.CENTER, drawPanel);

    /* Add menu items to menus */
    configure.add(configureStart);
    configure.add(configureStop);
    configure.addSeparator();
    configure.add(configureSettings);
    configure.addSeparator();
    configure.add(configureExit);
    view.add(look);
    help.add(helpAbout);

    /* Add menus to menubar */
    mb.add(configure);
    mb.add(view);
    mb.add(help);

    /* Set menubar */
    setJMenuBar(mb);

    /* Add the menu listener */
    configure.addMenuListener(new MenuListener() {

      @Override
      public void menuCanceled(MenuEvent evt)
      {
      }

      @Override
      public void menuDeselected(MenuEvent evt)
      {
      }

      @Override
      public void menuSelected(MenuEvent evt)
      {
        configureStart.setEnabled(isValidConfigData() && !configureStop.isEnabled());
      }
    });

    /* Add the action listeners */
    configureSettings.addActionListener(this);
    configureStart.addActionListener(this);
    configureStop.addActionListener(this);
    configureExit.addActionListener(this);
    helpAbout.addActionListener(this);

    /* Add the window listener */
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        /* Exit gracefully */
        if (configureStop.isEnabled())
        {
          CountdownFrame.this.configureStop.doClick();
        }

        dispose();
        System.exit(0);
      }
    });

    /* Determine selected look and feel and ensure it's applied */
    for (JRadioButtonMenuItem item : looks)
    {
      if (item.isSelected())
      {
        item.doClick();
        break;
      }
    }

    /* Size the frame */
    setSize(400, 200);

    /* Center the frame */
    setLocationRelativeTo(null);

    /* Show the frame */
    setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    Object obj = evt.getSource();

    if (obj == configureSettings)
    {
      new SettingsDialog(this, "Settings", true);

      date = preferences.get(PREFERENCES_DATE, "");
      time = preferences.get(PREFERENCES_TIME, "");
    }
    else if (obj == configureStart)
    {
      countdownSeconds = calculateCountdownSeconds(date, time);
      currentDateSeconds = ZonedDateTime.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId()).toEpochSecond();

      if (currentDateSeconds >= countdownSeconds)
      {
        JOptionPane.showMessageDialog(CountdownFrame.this, "Configured date and/or time has passed.", "Error",
            JOptionPane.INFORMATION_MESSAGE);
      }
      else
      {
        configureSettings.setEnabled(false);
        configureStart.setEnabled(false);
        configureStop.setEnabled(true);

        textStr = preferences.get(PREFERENCES_TEXT, "");

        countdownTimer = new java.util.Timer();
        countdownTimer.scheduleAtFixedRate(new CountdownTask(), 0, 1000);
      }
    }
    else if (obj == configureStop)
    {
      countdownTimer.cancel();

      countdownSeconds = 0;

      drawPanel.repaint();

      configureSettings.setEnabled(true);
      configureStart.setEnabled(true);
      configureStop.setEnabled(false);
    }
    else if (obj == configureExit)
    {
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    else if (obj == helpAbout)
    {
      JOptionPane.showMessageDialog(this,
          "<html><center>Countdown Application<br>Pat Paternostro<br>Copyright &copy; 2005-2026</center></html>",
          "About Countdown", JOptionPane.INFORMATION_MESSAGE);
    }
    else if (obj instanceof JRadioButtonMenuItem)
    {
      try
      {
        /*
         * The radio button menu item's action command is set to the associated
         * Look and Feel class name.
         */
        AbstractButton ab = ((AbstractButton) obj);
        UIManager.setLookAndFeel(ab.getActionCommand());
        preferences.put(PREFERENCES_LOOK_AND_FEEL, ab.getText());
        preferences.flush();
        SwingUtilities.updateComponentTreeUI(CountdownFrame.this);
      }
      catch (final Throwable th)
      {
        JOptionPane.showMessageDialog(CountdownFrame.this, th.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private long calculateCountdownSeconds(String date, String time)
  {
    String dateParts[] = date.split("/");
    String timeParts[] = time.split(":");

    ZonedDateTime calendar = ZonedDateTime.of(
        LocalDate.of(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1])),
        LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])), TimeZone.getDefault().toZoneId());

    return calendar.toEpochSecond();
  }

  private boolean isValidConfigData()
  {
    return Util.isValidDate(date) && Util.isValidTime(time);
  }

  private class DrawPanel extends JPanel
  {
    /*
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6102645309158997819L;

    @Override
    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);

      if (countdownSeconds != 0 && countdownSeconds > currentDateSeconds)
      {
        int width = getWidth();
        int height = getHeight();

        Color color = Color.decode(preferences.get(PREFERENCES_COLOR, "0"));

        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        String countdownStr = getCountdownString();

        /* Retrieve string width for centering purposes */
        FontMetrics fm = g.getFontMetrics();
        int countdownStrWidth = fm.stringWidth(countdownStr);
        int textHeight = fm.getHeight();

        if (textStr != null && !textStr.equals(""))
        {
          int textStrWidth = fm.stringWidth(textStr);
          g.drawString(textStr, (width - textStrWidth) / 2, height / 2 - textHeight);
        }

        g.drawString(countdownStr, (width - countdownStrWidth) / 2, height / 2);
      }
    }

    private String getCountdownString()
    {
      ZoneId zone = ZoneId.systemDefault();

      ZonedDateTime now = ZonedDateTime.now(zone);
      ZonedDateTime target = Instant.ofEpochSecond(countdownSeconds).atZone(zone);

      /*
       * Calculate the period (years, months, days) which uses calendar logic
       * (e.g., Feb 28 to Mar 28 is 1 month regardless of length)
       */
      Period period = Period.between(now.toLocalDate(), target.toLocalDate());

      /*
       * Adjust for the 'time' portion. If 'now' time is later than 'target'
       * time, Period.between might over-count a day
       */
      long hours = ChronoUnit.HOURS.between(now, target) % 24;
      long minutes = ChronoUnit.MINUTES.between(now, target) % 60;
      long seconds = ChronoUnit.SECONDS.between(now, target) % 60;

      // Refine days and weeks
      long totalDays = period.getDays();
      if (target.toLocalTime().isBefore(now.toLocalTime()))
      {
        totalDays--; // Adjust because a full calendar day hasn't elapsed yet
      }

      // Handle negative day adjustment wrapping into months
      if (totalDays < 0)
      {
        // Complex calendar math: simplify by using ChronoUnit for total days
        totalDays = ChronoUnit.DAYS.between(now.plus(period.withDays(0)), target);
      }

      long years = period.getYears();
      long months = period.getMonths();
      long weeks = totalDays / 7;
      long days = totalDays % 7;

      return (years != 0 ? years + " year(s) " : "") + (months != 0 ? months + " month(s) " : "")
          + (weeks != 0 ? weeks + " week(s) " : "") + (days != 0 ? days + " day(s) " : "")
          + (hours != 0 ? hours + " hour(s) " : "") + (minutes != 0 ? minutes + " minute(s) " : "") + seconds
          + " second(s) ";
    }
  }

  private class CountdownTask extends TimerTask
  {
    @Override
    public void run()
    {
      currentDateSeconds = ZonedDateTime.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId()).toEpochSecond();

      CountdownFrame.this.drawPanel.repaint();

      if (currentDateSeconds >= countdownSeconds)
      {
        CountdownFrame.this.configureStop.doClick();

        JOptionPane.showMessageDialog(CountdownFrame.this,
            "<html><center><i>My friend, the end is near<br>And so I face the final curtain</i><br>Frank Sinatra - My Way</center></html>",
            "Countdown", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }
}
