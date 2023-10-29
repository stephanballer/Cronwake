package com.jalopi.cronwake;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter; import java.nio.charset.StandardCharsets;
import java.util.List;

public class AlarmData {
    private String name;
    private int hour;
    private int minute;
    private boolean[] days;
    private boolean activated = true;

    AlarmData() {}
    AlarmData(String name, int hour, int minute, boolean[] days) {
        this.name = name;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
    }

    public static void writeToFile(long timeWritten, List<AlarmData> alarms, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("alarms.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("#" + timeWritten + "\n");
            for (AlarmData alarm : alarms) {
                outputStreamWriter.write(alarm.toString() + "\n");
                //System.out.println(alarm);
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }
    public static long readFromFile(Context context, List<AlarmData> alarms) {
        long timeWritten = 0;

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.openFileInput("alarms.txt")));
            String line;

            if ((line = bufferedReader.readLine()) != null) try {
                timeWritten = Long.parseLong(line.substring(1, line.length()));
            }
            catch (Exception e) {}
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println("\"" + line + "\"");
                AlarmData alarm = parseAlarmData(line);
                if (alarm != null) {
                    alarms.add(alarm);
                }
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }

        return timeWritten;
    }

    public static void sendAlarms(long timeWritten, List<AlarmData> alarms, String host, int port, String user, String password, String command) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(5000);
        session.connect();

        Channel channel = session.openChannel("exec");
        //OutputStream outputStream = channel.getOutputStream();
        String alarms_str = "#" + timeWritten + "\n";
        for (AlarmData alarm : alarms) {
            String alarm_str = alarm.toString().replace("$", command) + "\n";
            alarms_str += alarm_str;
            //outputStream.write(alarm_str.getBytes(StandardCharsets.UTF_8));
        }
        ((ChannelExec) channel).setCommand("echo -e \"" + alarms_str + "\" | crontab -");
        channel.connect();
        //outputStream.write("\0".getBytes(StandardCharsets.UTF_8));

        channel.disconnect();
        session.disconnect();
    }

    public static long receiveAlarms(List<AlarmData> alarms, String host, int port, String user, String password) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(5000);
        session.connect();

        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand("crontab -l");
        channel.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        String line;
        long timeWritten = 0;
        AlarmData newAlarm;

        if ((line = bufferedReader.readLine()) != null) try {
            timeWritten = Long.parseLong(line.substring(1, line.length()));
        }
        catch (Exception e) {}
        while ((line = bufferedReader.readLine()) != null) {
            newAlarm = AlarmData.parseAlarmData(line);
            if (newAlarm != null) {
                alarms.add(newAlarm);
            }
        }

        channel.disconnect();
        session.disconnect();

        return timeWritten;
    }

    public static void stopAlarm(String host, int port, String user, String password, String stopCommand) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(5000);
        session.connect();

        System.out.println(session.isConnected());
        Channel channel = session.openChannel("exec");
        //OutputStream outputStream = channel.getOutputStream();
        ((ChannelExec) channel).setCommand(stopCommand.getBytes(StandardCharsets.UTF_8));
        channel.connect();
        //outputStream.write("\0".getBytes(StandardCharsets.UTF_8));

        channel.disconnect();
        session.disconnect();
    }

    public String getName() {
        return name;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public boolean[] getDays() {
        return days;
    }
    public boolean getActivated() {
        return activated;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }
    public void setDays(boolean[] days) {
        this.days = days;
    }
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    public String toString() {
        String s = activated ? "" : "#";
        s += minute + " " + hour + " * * ";
        for (int i = 0; i < 7; i++) {
            if (days[i]) {
                s += String.valueOf(i + 1) + ',';
            }
        }
        s = s.substring(0, s.length()-1);
        s += " $ # " + name;
        return s;
    }
    public boolean fromString(String alarm) {
        if (alarm == null || alarm.length() == 0)
            return false;
        if (alarm.charAt(0) == '#') {
            activated = false;
            alarm = alarm.substring(1);
        }
        String[] split = alarm.split("# ");
        if (split.length != 2) {
            return false;
        }
        name = split[1];
        split = split[0].split(" ");
        minute = Integer.parseInt(split[0]);
        hour = Integer.parseInt(split[1]);
        days = new boolean[]{false, false, false, false, false, false, false};
        split = split[4].split(",");
        for (String day_str : split) {
            if (day_str.length() != 1) {
                return false;
            }
            char c = day_str.charAt(0);
            if (c < '1' || c > '7') {
                return false;
            }
            days[c - '1'] = true;
        }

        return true;
    }

    public static AlarmData parseAlarmData(String s) {
        AlarmData alarm = new AlarmData();
        if (alarm.fromString(s)) {
            return alarm;
        }
        return null;
    }
}
