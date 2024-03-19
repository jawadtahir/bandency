package org.debs.gc2023;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.debs.gc2023.bandency.Benchmark;
import org.debs.gc2023.challenger.BenchmarkPhase;
import org.debs.gc2023.challenger.BenchmarkState;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FailureInjector {

    private static final int TIMER_DURATION = 900000;

    private boolean removedInjection;
    private BenchmarkPhase phase;

    public FailureInjector() {
        this.phase = BenchmarkPhase.PRE_FAILURE_INJECTION;
        this.removedInjection = false;
    }

    public void startLatencyInjection(long delay, String ip, String port, String groupName) {
        // this key should be the private key of the hostVM
        String publicKeyFilePath = "~/.ssh/id_rsa";
        // Specify the command to execute
        String command = "sudo tc qdisc add dev enp1s0 root netem delay " + delay + "ms";
        Logger.info("failure command :" + command);
        // Connect to the host machine via SSH and execute the command
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(publicKeyFilePath);
            Session session = jsch.getSession(groupName, ip, Integer.valueOf(port));
            session.setConfig("StrictHostKeyChecking", "no");

            // Connect to the SSH session
            try {
                session.setTimeout(10000); // Set timeout in milliseconds (e.g., 10 seconds
                session.connect();
            } catch (Exception e) {
                Logger.info(e.getMessage());
                e.printStackTrace();
            }

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

            channelExec.setCommand(command);

            InputStream commandOutput = channelExec.getInputStream();

            channelExec.connect();

            StringBuilder response = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = commandOutput.read(buffer)) > 0) {
                response.append(new String(buffer, 0, bytesRead));
            }
            Logger.info(
                    "response of the main vm after executing the command : " + response.toString());

            // Thread.sleep(5000); // Adjust as needed
            this.removedInjection = false;
            channelExec.disconnect();

            session.disconnect();
            this.phase = BenchmarkPhase.FAILURE_INJECTION;
            System.out.println("Command executed successfully via SSH.");


            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // After a certain TIMER_DURATION , call the stopLatencyInjection method
                    if (!removedInjection) {
                        Logger.info("Timer: removing latency from " + ip + ":" + port);
                        stopLatencyInjection(ip, port, groupName);
                    } else {
                        Logger.info("already removed latency..");
                    }
                }
            }, TIMER_DURATION);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void stopLatencyInjection(String address, String port, String groupname) {
        // private key of hostVM
        String publicKeyFilePath = "~/.ssh/id_rsa";

        String command = "sudo tc qdisc del dev enp1s0 root";
        Logger.info("removing failure command :" + command);
        // Connect to the host machine via SSH and execute the command
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(publicKeyFilePath);
            Session session = jsch.getSession(groupname, address, Integer.valueOf(port));
            session.setConfig("StrictHostKeyChecking", "no");

            // Connect to the SSH session
            try {
                session.setTimeout(10000); // Set timeout in milliseconds (e.g., 10 seconds
                session.connect();
            } catch (Exception e) {
                Logger.info(e.getMessage());
                e.printStackTrace();
            }

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

            channelExec.setCommand(command);

            channelExec.connect();
            this.removedInjection = true;
            // Thread.sleep(5000); // Adjust as needed
            channelExec.disconnect();
            session.disconnect();
            this.phase = BenchmarkPhase.POST_FAILURE_INJECTION;
            System.out.println("Command executed successfully via SSH.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BenchmarkPhase getPhase() {
        return this.phase;
    }

}
