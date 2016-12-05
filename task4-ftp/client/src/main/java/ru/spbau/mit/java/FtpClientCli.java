package ru.spbau.mit.java;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;

/**
 * Simple cli.
 *
 * It runs as REPL with commands:
 * 1) list {dir name}
 * 2) get {file name on server} {file name to download to}
 */
public class FtpClientCli {
    private static void help() {
        System.out.println("USAGE: java FtpClientCli <host> <port>");
    }

    private static void printCliHelp() {
        System.out.println("Commands: ");
        System.out.println("     - list ");
        System.out.println("            list files available on the server ");
        System.out.println("     - get <Filename> [<destination>]");
        System.out.println("            download file from server (if destination not specified file is printed)");
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            help();
            return;
        }

        String host = args[0];
        int port = Integer.valueOf(args[1]);

        FtpClient client = new FtpClient(host, port);
        System.out.println("Connecting to: " + host + ":" + port);
        try {
            if (client.connect()) {
                System.out.println("OK");
            } else {
                System.out.println("ERROR: can't connect");
                return;
            }
        } catch (IOException e) {
            System.out.println("ERROR: can't connect due to exception: " + e.getMessage());
            return;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("CMD >> ");
            String str = br.readLine();
            if (str == null) {
                try {
                    client.disconnect();
                } catch (Exception e) {
                    System.out.println("ERROR: can't disconnect " + e.getMessage());
                }
                break;
            }

            String[] split = str.split(" ");
            if (split[0].equals("list") && split.length == 2) {
                try {
                    List<FileInfo> fileInfos = client.executeList(split[1]);
                    if (fileInfos == null) {
                        System.out.println("No such directory (or error)");
                    } else {
                        fileInfos.forEach(fi -> System.out.println(fi.getName() + " " + fi.isDirectory()));
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: can't execute list: " + e.getMessage());
                }
            } else if (split[0].equals("get") && split.length == 3) {
                try {
                    FtpFile ftpFile = client.executeGet(split[1]);
                    if (ftpFile == null) {
                        System.out.println("No such file");
                    } else {
                        OutputStream out = new FileOutputStream(split[2]);
                        IOUtils.copy(ftpFile.getInputStream(), out);
                        out.close();
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: can't execute get: " + e.getMessage());
                }
            } else {
                printCliHelp();
            }
        }
    }
}
