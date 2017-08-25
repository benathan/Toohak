// MIT License
 
// Copyright (c) 2017 Matthew Chen, Arc676/Alessandro Vinciguerra
 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
 
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation (version 3).

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.
//See README.txt and LICENSE.txt for more details

package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;

import backend.GameState;
import backend.View;
import net.MessageHandler;
import net.MsgThread;
import net.NetworkMessages;

public class ClientView extends JFrame {

	private static final long serialVersionUID = -2058238427240422768L;
	
	private final int width = 700, height = 500;
	
	//inner drawing class
	private class DrawingView extends JPanel implements MouseListener, MessageHandler {

		private static final long serialVersionUID = -2592273103017659873L;
		
		private GameState currentState = GameState.WAITING_FOR_PLAYERS;
		private Thread gameThread;
		private boolean running;
		
		public void paint(Graphics g) {
			g.setColor(Color.CYAN);
			g.fillRect(0, 0, width, height);
			switch (currentState) {
			case GAME_OVER:
				g.drawString("Back to Main", width / 4, height / 2);
				g.drawRect(width / 8, height / 4, width * 3 / 4, height / 2);
				break;
			case WAITING_FOR_ANSWERS:
				break;
			case WAITING_FOR_NEXT_Q:
				break;
			case WAITING_FOR_PLAYERS:
				break;
			default:
				break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			switch (currentState) {
			case GAME_OVER:
				currentState = GameState.WAITING_FOR_ANSWERS;
				break;
			case WAITING_FOR_ANSWERS:
				currentState = GameState.WAITING_FOR_NEXT_Q;
				break;
			case WAITING_FOR_NEXT_Q:
				currentState = GameState.WAITING_FOR_PLAYERS;
				break;
			case WAITING_FOR_PLAYERS:
				currentState = GameState.GAME_OVER;
				break;
			default:
				break;
			}
		}
		
		private void startRunning() {
			running = true;
			gameThread = new Thread(new Runnable() {
				@Override
				public void run() {
					long lastUpdate = System.currentTimeMillis();
					while (running) {
						long now = System.currentTimeMillis();
						if (lastUpdate - now >= 500) {
							lastUpdate = now;
							update();
							repaint();
						}
					}
				}
			});
			gameThread.start();
		}
		
		private void update() {
			//
		}
		
		@Override
		public void handleMessage(String msg) {
			if (msg.equals(NetworkMessages.userKicked)) {
				closeClient();
			}
		}

		//unnecessary mouse methods
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}

	}
	
	// network IO
	private Socket sock;
	private PrintWriter out;
	private BufferedReader in;
	private int port;
	private String host;
	private MsgThread msgThread;
	
	private String username;

	private Main main;
	
	private DrawingView drawView;

	public ClientView(Main main) {
		setBounds(200, 200, width, height);
		this.main = main;
		drawView = new DrawingView();
	}
	
	public void startRunning() {
		drawView.startRunning();
	}
	
	private boolean connect(String uname, String host, int port) {
		username = uname;
		this.host = host;
		this.port = port;

		try {
			sock = new Socket(this.host, this.port);
			out = new PrintWriter(sock.getOutputStream(), true);
			out.println(username);
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			msgThread = new MsgThread(in, null, username, drawView);
			msgThread.start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private void backToMain() {
		setVisible(false);
		main.showView(View.MAIN_MENU);
	}

	private void closeClient() {
		try {
			msgThread.running = false;
			sock.close();
		} catch (IOException e) {
		}
	}

}