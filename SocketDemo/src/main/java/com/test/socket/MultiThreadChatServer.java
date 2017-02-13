package com.test.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class MultiThreadChatServer {
	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	// This chat server can accept up to maxClientsCount clients' connections.
	private static final int maxClientsCount = 10;
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];

	public static void main(String args[]) {

		// The default port number.
		int portNumber = 2222;
		if (args.length < 1) {
			System.out
					.println("Usage: java MultiThreadChatServer <portNumber>\n"
							+ "Now using port number=" + portNumber);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
		}

		/*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it to a new
		 * client thread.
		 */
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						(threads[i] = new ClientThread(clientSocket, threads))
								.start();
						break;
					}
				}
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(
							clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */
class ClientThread extends Thread {

	private InputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	private String name = "";

	public ClientThread(Socket clientSocket, ClientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}

	public void run() {
		int maxClientsCount = this.maxClientsCount;
		ClientThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 */
			is = clientSocket.getInputStream();
			os = new PrintStream(clientSocket.getOutputStream());
			os.println("Enter your name.");

			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			name = br.readLine().trim();

			/*
			 * Online people.
			 */
			Set<String> names = new HashSet<>();
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					names.add(threads[i].name);
				}
			}

			/*
			 * Validate repeat name or blank.
			 */
			boolean isRepeat = names.contains(name);
			while (name == null || name.equals("") || isRepeat) {
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				name = br.readLine().trim();

				names = new HashSet<>();
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						names.add(threads[i].name);
					}
				}

				isRepeat = names.contains(name);
			}

			os.println("Hello " + name
					+ " to our chat room.\nTo leave enter /quit in a new line");

			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					threads[i].os.println("*** A new user " + threads[i].name
							+ " entered the chat room !!! ***");
				}

				if (threads[i] != null && threads[i] == this) {
					StringBuilder stringBuilder = new StringBuilder("User: ");
					for (int j = 0; j < maxClientsCount; j++) {
						if (threads[j] != null && !threads[j].name.equals("")) {
							stringBuilder.append(threads[j].name).append(", ");
						}
					}

					String tmpReverse = stringBuilder.reverse().toString()
							.replaceFirst(" ,", "");
					String users = new StringBuilder(tmpReverse).reverse()
							.toString();
					this.os.println(users);
				}

			}

			while (true) {
				String line = br.readLine();

				if (line.startsWith("/quit")) {
					break;
				}

				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].os.println("<" + threads[i].name + ">: "
								+ line);
					}
				}
			}

			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					threads[i].os.println("*** The user " + name
							+ " is leaving the chat room !!! ***");
				}
			}

			os.println("*** Bye " + name + " ***");

			/*
			 * Clean up. Set the current thread variable to null so that a new
			 * client could be accepted by the server.
			 */
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] == this) {
					threads[i] = null;
				}
			}

			/*
			 * Close the output stream, close the input stream, close the
			 * socket.
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {

		}
	}
}