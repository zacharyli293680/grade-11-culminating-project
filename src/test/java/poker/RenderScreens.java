// RenderScreens
// Paints every screen, and several table states, to PNG files so the layout can be
// checked without opening a window. Runs headless.
//
//   sh scripts/build.sh   (or scriptsuild.bat)
//   java -Djava.awt.headless=true -cp "out/main:out/test:src/main/resources" poker.RenderScreens [outputFolder] [width] [height]
//
// With no arguments it writes into docs/screenshots, which the README links to.

package poker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderScreens {

	private static File outDir;
	private static int width = 1000;
	private static int height = 760;

	public static void main(String[] args) throws Exception {
		outDir = new File(args.length > 0 ? args[0] : "docs/screenshots");
		if (args.length > 2) {
			width = Integer.parseInt(args[1]);
			height = Integer.parseInt(args[2]);
		}
		outDir.mkdirs();
		Poker host = new Poker();

		save(new MenuScreen(host), "menu");
		save(new SetupScreen(host), "setup");
		save(new AboutScreen(host), "about");

		// waiting for the first player to start
		Game g = new Game(new String[] {"Ann", "Bob", "Cy", "Dee"});
		g.startHand();
		TableScreen table = new TableScreen(host, g);
		save(table, "table_handoff");

		// mid-turn with the raise row open and a pot raise selected
		table.beginTurn();
		table.debugPending(Game.POT, 0, true, 27);
		save(table, "table_raise");

		// heads-up on the river with a custom amount typed
		Game hu = new Game(new String[] {"Ann", "Bob"});
		hu.startHand();
		for (int i = 0; i < 6; i++) {
			hu.applyAction(Game.CHECK_CALL, 0);
		}
		TableScreen huTable = new TableScreen(host, hu);
		huTable.beginTurn();
		huTable.debugPending(Game.CUSTOM, 40, true, 4);
		save(huTable, "table_river_heads_up");

		// eight players, pre-flop, waiting
		String[] eight = {"Ann", "Bob", "Cy", "Dee", "Eve", "Fay", "Gus", "Hal"};
		Game g8 = new Game(eight);
		g8.startHand();
		save(new TableScreen(host, g8), "table_eight");

		// showdown with a side pot
		Game sd = new Game(new String[] {"Ann", "Bob", "Cy", "Dee"});
		sd.startHand();
		sd.players[3].chips = 40;
		sd.applyAction(Game.ALL_IN, 0);
		sd.applyAction(Game.CUSTOM, 200);
		sd.applyAction(Game.FOLD, 0);
		sd.applyAction(Game.CHECK_CALL, 0);
		while (!sd.handOver) {
			sd.applyAction(Game.CHECK_CALL, 0);
		}
		save(new TableScreen(host, sd), "table_showdown");

		// game over
		Game go = new Game(new String[] {"Ann", "Bob"});
		go.startHand();
		go.applyAction(Game.ALL_IN, 0);
		go.applyAction(Game.CHECK_CALL, 0);
		int guard = 0;
		while (!go.gameOver && guard++ < 20) {
			go.startHand();
			go.applyAction(Game.ALL_IN, 0);
			go.applyAction(Game.CHECK_CALL, 0);
		}
		save(new TableScreen(host, go), "table_game_over");

		System.out.println("wrote screens to " + outDir.getAbsolutePath());
	}

	private static void save(Screen screen, String name) throws Exception {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		Theme.antialias(g);
		screen.layout(width, height);
		screen.paint(g, width, height);
		g.dispose();
		ImageIO.write(img, "png", new File(outDir, name + ".png"));
	}
}
