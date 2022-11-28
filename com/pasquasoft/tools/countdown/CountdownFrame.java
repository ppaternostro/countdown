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
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class CountdownFrame extends JFrame implements ActionListener
{
  /**
   * Generated serial version UID.
   */
  private static final long serialVersionUID = 4657914421139681570L;

  private static final long SECONDS_IN_YEAR = 31556926;
  private static final long SECONDS_IN_MONTH = 2629743;
  private static final long SECONDS_IN_WEEK = 604800;
  private static final long SECONDS_IN_DAY = 86400;
  private static final long SECONDS_IN_HOUR = 3600;
  private static final long SECONDS_IN_MINUTE = 60;
  private static final long MILLIS_IN_SECOND = 1000;

  private DrawPanel drawPanel = new DrawPanel();

  private JMenuBar mb = new JMenuBar();

  private JMenu configure = new JMenu("Configure");
  private JMenu help = new JMenu("Help");

  private JMenuItem configureStart = new JMenuItem("Start");
  private JMenuItem configureStop = new JMenuItem("Stop");
  private JMenuItem configureSettings = new JMenuItem("Settings...");
  private JMenuItem configureExit = new JMenuItem("Exit");
  private JMenuItem helpAbout = new JMenuItem("About...");

  private long countdownMillis;
  private long currentDateMillis;

  private String date;
  private String time;
  private String textStr;

  private Properties prop = Util.readProperties();

  private java.util.Timer countdownTimer;

  public CountdownFrame()
  {
    super("Countdown");

    date = prop.getProperty("countdown.date");
    time = prop.getProperty("countdown.time");

    configureStart.setEnabled(date != null && !date.equals(""));
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
    help.add(helpAbout);

    /* Add menus to menubar */
    mb.add(configure);
    mb.add(help);

    /* Set menubar */
    setJMenuBar(mb);

    /* Add the menu listener */
    configure.addMenuListener(new MenuListener() {

      public void menuCanceled(MenuEvent evt)
      {
      }

      public void menuDeselected(MenuEvent evt)
      {
      }

      public void menuSelected(MenuEvent evt)
      {
        configureStart.setEnabled(date != null && !date.equals("") && !configureStop.isEnabled());
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

    /* Size the frame */
    setSize(400, 200);

    /* Center the frame */
    setLocationRelativeTo(null);

    /* Show the frame */
    setVisible(true);
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object obj = evt.getSource();

    if (obj == configureSettings)
    {
      new SettingsDialog(this, "Settings", true, prop);

      date = prop.getProperty("countdown.date");
      time = prop.getProperty("countdown.time");
    }
    else if (obj == configureStart)
    {
      countdownMillis = calculateMillis(date, time);
      currentDateMillis = ZonedDateTime.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId()).toInstant()
          .toEpochMilli();

      if (currentDateMillis >= countdownMillis)
      {
        JOptionPane.showMessageDialog(CountdownFrame.this, "Configured date and/or time has passed.", "Countdown",
            JOptionPane.INFORMATION_MESSAGE);
      }
      else
      {
        configureSettings.setEnabled(false);
        configureStart.setEnabled(false);
        configureStop.setEnabled(true);

        textStr = prop.getProperty("countdown.text");

        countdownTimer = new java.util.Timer();
        countdownTimer.scheduleAtFixedRate(new CountdownTask(), 0, 1000);
      }
    }
    else if (obj == configureStop)
    {
      countdownTimer.cancel();

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
          "<html><center>Countdown Application<br>Pat Paternostro<br>Copyright &copy; 2005-2022</center></html>",
          "About Countdown", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private long calculateMillis(String date, String time)
  {
    String dateParts[] = date.split("/");
    String timeParts[] = time.split(":");

    if (timeParts.length < 2)
    {
      timeParts = new String[] { "12", "00" };
    }

    ZonedDateTime calendar = ZonedDateTime.of(
        LocalDate.of(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1])),
        LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])), TimeZone.getDefault().toZoneId());

    return calendar.toInstant().toEpochMilli();
  }

  private class DrawPanel extends JPanel
  {
    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6102645309158997819L;

    public void paintComponent(Graphics g)
    {
      if (countdownMillis != 0 && countdownMillis > currentDateMillis)
      {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        Color color = Color.decode(prop.getProperty("countdown.color", "0"));

        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        long diffInSeconds = (countdownMillis - currentDateMillis) / MILLIS_IN_SECOND;

        int years = (int) (diffInSeconds / SECONDS_IN_YEAR);
        diffInSeconds -= years * SECONDS_IN_YEAR;

        int months = (int) (diffInSeconds / SECONDS_IN_MONTH);
        diffInSeconds -= months * SECONDS_IN_MONTH;

        int weeks = (int) (diffInSeconds / SECONDS_IN_WEEK);
        diffInSeconds -= weeks * SECONDS_IN_WEEK;

        int days = (int) (diffInSeconds / SECONDS_IN_DAY);
        diffInSeconds -= days * SECONDS_IN_DAY;

        int hours = (int) (diffInSeconds / SECONDS_IN_HOUR);
        diffInSeconds -= hours * SECONDS_IN_HOUR;

        int minutes = (int) (diffInSeconds / SECONDS_IN_MINUTE);
        diffInSeconds -= minutes * SECONDS_IN_MINUTE;

        int seconds = (int) diffInSeconds;

        String dateStr = (years != 0 ? years + " year(s) " : "") + (months != 0 ? months + " month(s) " : "")
            + (weeks != 0 ? weeks + " week(s) " : "") + (days != 0 ? days + " day(s) " : "")
            + (hours != 0 ? hours + " hour(s) " : "") + (minutes != 0 ? minutes + " minute(s) " : "") + seconds
            + " second(s) ";

        /* Retrieve string width for centering purposes */
        FontMetrics fm = g.getFontMetrics();
        int dateStrWidth = fm.stringWidth(dateStr);
        int textHeight = fm.getHeight();

        if (textStr != null && !textStr.equals(""))
        {
          int textStrWidth = fm.stringWidth(textStr);
          g.drawString(textStr, (width - textStrWidth) / 2, height / 2 - textHeight);
        }

        g.drawString(dateStr, (width - dateStrWidth) / 2, height / 2);
      }
    }
  }

  private class CountdownTask extends TimerTask
  {
    public void run()
    {
      currentDateMillis = ZonedDateTime.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId()).toInstant()
          .toEpochMilli();

      CountdownFrame.this.drawPanel.repaint();

      if (currentDateMillis >= countdownMillis)
      {
        CountdownFrame.this.configureStop.doClick();

        JOptionPane.showMessageDialog(CountdownFrame.this,
            "<html><center><i>My friend, the end is near<br>And so I face the final curtain</i><br>Frank Sinatra - My Way</center></html>",
            "Countdown", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }
}
