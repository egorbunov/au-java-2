package ru.spbau.mit.java;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

class Srv implements Runnable {
    ServerSocket sock;

    public void run() {
        try {
            sock = new ServerSocket(4444);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                Socket x = sock.accept();
                SocketAddress addr = x.getRemoteSocketAddress();
                System.out.println("SERVER ACCEPTED CONNECTION FROM: " + addr);
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

    }
}

class Client implements Runnable {
    public void run() {
        while (true) {
            try {
                Socket conn = new Socket("localhost", 4444);
                System.out.println("CLIENT GOT CONNECTION: " + conn.getLocalSocketAddress());
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}


public class Playground {
    public static void main(String[] args) throws InterruptedException {
        Thread srv = new Thread(new Srv());
        Thread client = new Thread(new Client());
        srv.start();
        client.start();
        srv.join();
        client.join();
    }
}
