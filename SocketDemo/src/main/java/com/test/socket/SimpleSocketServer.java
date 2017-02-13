package com.test.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleSocketServer {

	// static ServerSocket variable
	private static ServerSocket server;
	// socket server port on which it will listen
	private static int port = 9875;

	public static void main(String args[]) throws IOException,
			ClassNotFoundException {
		// create the socket server object
		server = new ServerSocket(port);

		// keep listens indefinitely until receives 'exit' call or program
		// terminates
		System.out.println("----------------------------");
		System.out.println("Waiting for client request");
		System.out.println("----------------------------");

		// creating socket and waiting for client connection
		Socket socket = server.accept();

		// read from socket to ObjectInputStream object
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

		// convert ObjectInputStream object to String
		String message = (String) ois.readObject();
		System.out.println("Message Received: " + message);

		// create ObjectOutputStream object
		ObjectOutputStream oos = new ObjectOutputStream(
				socket.getOutputStream());
		// write object to Socket
		oos.writeObject("Hi Client, I get your message");

		// close resources
		ois.close();
		oos.close();
		socket.close();

		// System.out.println("Shutting down Socket server!!");
		// close the ServerSocket object
		server.close();

		main(args);
	}
}