package ru.spbau.mit.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simple cli.
 *
 * It runs as REPL with commands:
 * 1) stop -- stops server
 */
public class FtpServerCli {
    private static void help() {
        System.out.println("USAGE: java FtpServerCli <port>");
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            help();
            return;
        }

        int port = Integer.valueOf(args[0]);

        FtpServer server = new FtpServer(port);
        if (server.start()) {
            System.out.println("Server started at port: " + port);
        } else {
            System.err.println("Error starting server...");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("CMD >> ");
            String str = br.readLine();
            if (str == null) {
                server.stop();
                break;
            }
            if (str.equals("stop")) {
                server.stop();
                break;
            }
        }
    }
}
