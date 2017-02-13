package com.test.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This class implements java socket client
 * 
 * @author pankaj
 * 
 */
public class SimpleSocketClient {

	public static void main(String[] args) throws UnknownHostException,
			IOException, ClassNotFoundException, InterruptedException {
		// get the localhost IP address, if server is running on some other IP,
		// you need to use that
		InetAddress host = InetAddress.getLocalHost();
		System.out.println("----------------------------");
		System.out.println("please type some messages : ");
		System.out.println("----------------------------");
		Socket socket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		// establish socket connection to server
		socket = new Socket(host.getHostName(), 9875);

		// write to socket using ObjectOutputStream
		oos = new ObjectOutputStream(socket.getOutputStream());
		Scanner scanner = new Scanner(System.in);
		oos.writeObject(scanner.next());

		// read the server response message
		ois = new ObjectInputStream(socket.getInputStream());

		System.out.println("----------------------------");
		System.out.println(ois.readObject().toString());
		System.out.println("----------------------------");

		// close resources
		ois.close();
		oos.close();

		Thread.sleep(3000);

		System.out.println("Exit? y/N");
		Scanner scanner2 = new Scanner(System.in);

		if (scanner2.next().toUpperCase().equals("Y")) {
			scanner.close();
			scanner2.close();
			socket.close();
		} else {
			main(args);
		}

	}
}