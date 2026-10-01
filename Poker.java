// Poker ISU
// Zachary Li & Sarah Zhou
// 01-22-2024
// This is a recreation of the famous Texas Hold'em Poker.
// Blinds are 5/10 and every player starts with 1000 chips.
//
// This file is the window and host panel. It owns the current Screen (menu, setup,
// table, about), the sound clips, the one-second turn clock, and routes mouse and
// keyboard events. The rules live in Game.java, hand ranking in HandEvaluator.java.

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.net.URISyntaxException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Poker extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	public static final int TURN_SECONDS = 30; // time each player has once they start their turn

	private static Clip backgroundMusic;
	private static Clip soundEffect;
	private static boolean muted = false;

	private Screen screen;
	private final Timer clock;

	public Poker() {
		setPreferredSize(new Dimension(1000, 760));
		setBackground(Theme.BG);
		setFocusable(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		clock = new Timer(1000, e -> {
			screen.tick();
			repaint();
		});

		backgroundMusic = loadClip("gdmusic.wav");
		soundEffect = loadClip("end.wav");
		screen = new MenuScreen(this);
	}

	// ---------------------------------------------------------------- host services

	public void show(Screen next) {
		clock.stop();
		screen = next;
		repaint();
	}

	public void startClock() {
		clock.restart();
	}

	public void stopClock() {
		clock.stop();
	}

	public boolean isMuted() {
		return muted;
	}

	public void toggleMute() {
		muted = !muted;
		if (backgroundMusic != null) {
			if (muted) {
				backgroundMusic.stop();
			} else {
				backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}
	}

	public void quit() {
		System.exit(0);
	}

	// ---------------------------------------------------------------- assets

	// Finds a sound file. Looks in the working directory first, then next to the compiled
	// classes and one folder above them (where "javac -d out" leaves them).
	private static File asset(String name) {
		File direct = new File(name);
		if (direct.exists()) {
			return direct;
		}
		try {
			File codeDir = new File(Poker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (codeDir.isFile()) {
				codeDir = codeDir.getParentFile();
			}
			File[] candidates = {new File(codeDir, name), new File(codeDir.getParentFile(), name)};
			for (File candidate : candidates) {
				if (candidate.exists()) {
					return candidate;
				}
			}
		} catch (URISyntaxException | NullPointerException e) {
			// fall through and report the plain name
		}
		System.err.println("Missing file: " + name + " (run the game from the project folder)");
		return direct;
	}

	private static Clip loadClip(String file) {
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(asset(file));
			Clip clip = AudioSystem.getClip();
			clip.open(sound);
			return clip;
		} catch (Exception e) {
			return null;
		}
	}

	// ---------------------------------------------------------------- painting and input

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		Theme.antialias(g2);
		screen.layout(getWidth(), getHeight());
		screen.paint(g2, getWidth(), getHeight());
	}

	public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
		if (soundEffect != null && !muted) {
			soundEffect.setFramePosition(0);
			soundEffect.start();
		}
		screen.mousePressed(e.getX(), e.getY());
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		screen.mouseReleased(e.getX(), e.getY());
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		screen.mouseMoved(e.getX(), e.getY());
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
		screen.mouseDragged(e.getX(), e.getY());
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		boolean used = screen.keyPressed(e);
		if (!used && e.getKeyCode() == KeyEvent.VK_M) {
			toggleMute();
		}
		repaint();
	}

	public void keyTyped(KeyEvent e) {
		screen.keyTyped(e.getKeyChar());
		repaint();
	}

	public void keyReleased(KeyEvent e) {
	}

	// ---------------------------------------------------------------- main

	public static void main(String[] args) {
		Poker panel = new Poker();
		JFrame frame = new JFrame("Texas Hold'em Poker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		panel.setFocusTraversalKeysEnabled(false); // so Tab reaches the setup screen
		panel.requestFocusInWindow();
		if (backgroundMusic != null) {
			backgroundMusic.setFramePosition(0);
			backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}
}
