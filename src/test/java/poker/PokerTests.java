// PokerTests
// Plain-Java checks for the rules engine and hand evaluator. No test framework needed.
//
// Compile and run from the project folder:
//   javac -d out *.java tests/PokerTests.java
//   java -cp out PokerTests

package poker;

import java.util.Arrays;

public class PokerTests {

	private static int passed = 0;
	private static int failed = 0;

	// card helper: rank 0..12 (0 = two, 12 = ace), suit 0..3
	private static int c(int rank, int suit) {
		return suit * 13 + rank;
	}

	private static final int TWO = 0, THREE = 1, FOUR = 2, FIVE = 3, SIX = 4, SEVEN = 5, EIGHT = 6, NINE = 7, TEN = 8, JACK = 9, QUEEN = 10, KING = 11, ACE = 12;

	private static void check(String label, boolean ok) {
		if (ok) {
			passed++;
		} else {
			failed++;
			System.out.println("FAIL: " + label);
		}
	}

	private static void checkEquals(String label, Object expected, Object actual) {
		boolean ok = expected == null ? actual == null : expected.equals(actual);
		if (!ok) {
			System.out.println("FAIL: " + label + " expected [" + expected + "] got [" + actual + "]");
			failed++;
		} else {
			passed++;
		}
	}

	private static String hand(int... cards) {
		return HandEvaluator.evaluate(cards).describe();
	}

	private static int compare(int[] a, int[] b) {
		return HandEvaluator.evaluate(a).compareTo(HandEvaluator.evaluate(b));
	}

	// ------------------------------------------------------------ evaluator

	static void evaluatorTests() {
		checkEquals("royal flush", "royal flush", hand(c(TEN, 0), c(JACK, 0), c(QUEEN, 0), c(KING, 0), c(ACE, 0), c(TWO, 1), c(THREE, 2)));
		checkEquals("straight flush", "nine high straight flush", hand(c(FIVE, 1), c(SIX, 1), c(SEVEN, 1), c(EIGHT, 1), c(NINE, 1), c(ACE, 0), c(ACE, 2)));
		checkEquals("wheel straight flush", "five high straight flush", hand(c(ACE, 2), c(TWO, 2), c(THREE, 2), c(FOUR, 2), c(FIVE, 2), c(KING, 0), c(KING, 1)));
		checkEquals("wheel straight, mixed suits", "five high straight", hand(c(ACE, 0), c(TWO, 1), c(THREE, 2), c(FOUR, 3), c(FIVE, 0), c(NINE, 1), c(JACK, 2)));
		checkEquals("six suited cards keeps the ace", "ace high flush", hand(c(TWO, 0), c(FOUR, 0), c(SIX, 0), c(EIGHT, 0), c(TEN, 0), c(ACE, 0), c(NINE, 1)));
		checkEquals("flush plus unsuited straight is a flush", "ten high flush", hand(c(TWO, 0), c(THREE, 0), c(FOUR, 0), c(FIVE, 0), c(TEN, 0), c(SIX, 1), c(SEVEN, 1)));
		checkEquals("quads", "quad aces", hand(c(ACE, 0), c(ACE, 1), c(ACE, 2), c(ACE, 3), c(TWO, 0), c(THREE, 0), c(FOUR, 0)));
		checkEquals("full house", "aces full of kings", hand(c(ACE, 0), c(ACE, 1), c(ACE, 2), c(KING, 0), c(KING, 1), c(TWO, 0), c(THREE, 0)));
		checkEquals("full house picks the bigger trips", "kings full of aces", hand(c(ACE, 0), c(ACE, 1), c(KING, 2), c(KING, 0), c(KING, 1), c(TWO, 0), c(THREE, 0)));
		checkEquals("straight", "queen high straight", hand(c(EIGHT, 0), c(NINE, 1), c(TEN, 2), c(JACK, 3), c(QUEEN, 0), c(TWO, 1), c(TWO, 2)));
		checkEquals("trips", "trip aces", hand(c(ACE, 0), c(ACE, 1), c(ACE, 2), c(THREE, 3), c(FOUR, 0), c(NINE, 1), c(JACK, 2)));
		checkEquals("two pair uses the top two pairs", "two pairs: aces and kings", hand(c(ACE, 0), c(ACE, 1), c(KING, 2), c(KING, 3), c(TWO, 0), c(TWO, 1), c(NINE, 2)));
		checkEquals("pair", "pair of aces", hand(c(ACE, 0), c(ACE, 1), c(THREE, 2), c(FOUR, 3), c(SIX, 0), c(NINE, 1), c(JACK, 2)));
		checkEquals("high card", "ace high", hand(c(ACE, 0), c(THREE, 1), c(FOUR, 2), c(SIX, 3), c(EIGHT, 0), c(NINE, 1), c(JACK, 2)));

		// kickers decide ties
		int[] board = {c(ACE, 1), c(FIVE, 2), c(SEVEN, 3), c(NINE, 1), c(JACK, 2)};
		int[] aceKing = {c(ACE, 0), c(KING, 0), board[0], board[1], board[2], board[3], board[4]};
		int[] aceDeuce = {c(ACE, 2), c(TWO, 0), board[0], board[1], board[2], board[3], board[4]};
		check("pair of aces with king kicker beats jack kicker", compare(aceKing, aceDeuce) > 0);

		int[] flushK = {c(ACE, 0), c(KING, 0), c(FOUR, 0), c(SIX, 0), c(EIGHT, 0), c(NINE, 1), c(JACK, 2)};
		int[] flushQ = {c(ACE, 0), c(QUEEN, 0), c(FOUR, 0), c(SIX, 0), c(EIGHT, 0), c(NINE, 1), c(JACK, 2)};
		check("flush second card breaks the tie", compare(flushK, flushQ) > 0);

		int[] samePlay = {c(TWO, 0), c(THREE, 1), c(TEN, 2), c(JACK, 3), c(QUEEN, 0), c(KING, 1), c(ACE, 2)};
		int[] samePlay2 = {c(FOUR, 0), c(FIVE, 1), c(TEN, 2), c(JACK, 3), c(QUEEN, 0), c(KING, 1), c(ACE, 2)};
		check("board straight is a true tie", compare(samePlay, samePlay2) == 0);

		check("straight flush beats quads", compare(
			new int[] {c(FIVE, 1), c(SIX, 1), c(SEVEN, 1), c(EIGHT, 1), c(NINE, 1), c(ACE, 0), c(ACE, 2)},
			new int[] {c(ACE, 0), c(ACE, 1), c(ACE, 2), c(ACE, 3), c(TWO, 0), c(THREE, 0), c(FOUR, 0)}) > 0);
		check("wheel loses to six high straight", compare(
			new int[] {c(ACE, 0), c(TWO, 1), c(THREE, 2), c(FOUR, 3), c(FIVE, 0), c(NINE, 1), c(JACK, 2)},
			new int[] {c(SIX, 0), c(TWO, 1), c(THREE, 2), c(FOUR, 3), c(FIVE, 0), c(NINE, 1), c(JACK, 2)}) < 0);
	}

	// ------------------------------------------------------------ game flow

	private static Game newGame(int... chips) {
		String[] names = new String[chips.length];
		for (int i = 0; i < chips.length; i++) {
			names[i] = "P" + i;
		}
		Game g = new Game(names);
		for (int i = 0; i < chips.length; i++) {
			g.players[i].chips = chips[i];
		}
		return g;
	}

	private static int totalChips(Game g) {
		int total = g.pot;
		for (Player p : g.players) {
			total += p.chips + p.streetBet;
		}
		return total;
	}

	static void blindsAndOrderTests() {
		Game g = newGame(1000, 1000, 1000);
		g.startHand();
		checkEquals("first dealer is seat 0", 0, g.dealer);
		checkEquals("small blind left of dealer", 1, g.smallBlindSeat);
		checkEquals("big blind left of small blind", 2, g.bigBlindSeat);
		checkEquals("small blind posted", 995, g.players[1].chips);
		checkEquals("big blind posted", 990, g.players[2].chips);
		checkEquals("preflop action starts left of the big blind", 0, g.acting);
		checkEquals("current bet is the big blind", 10, g.currentBet);

		g.applyAction(Game.CHECK_CALL, 0); // seat 0 calls 10
		checkEquals("action moves to the small blind", 1, g.acting);
		g.applyAction(Game.CHECK_CALL, 0); // seat 1 completes
		checkEquals("big blind gets the option", 2, g.acting);
		checkEquals("big blind owes nothing", 0, g.callAmount(g.players[2]));
		g.applyAction(Game.CHECK_CALL, 0); // big blind checks
		checkEquals("flop after the option", Game.FLOP, g.street);
		checkEquals("bets collected into pot", 30, g.pot);
		checkEquals("postflop action starts left of the dealer", 1, g.acting);
		checkEquals("current bet resets", 0, g.currentBet);

		// dealer button moves each hand
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		checkEquals("turn card street", Game.TURN, g.street);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		check("hand ends after the river", g.handOver);
		check("showdown reached", g.showdownReached);
		checkEquals("chips conserved", 3000, totalChips(g));
		g.startHand();
		checkEquals("button moves", 1, g.dealer);

		// heads-up rules
		Game h = newGame(1000, 1000);
		h.startHand();
		checkEquals("heads-up dealer posts the small blind", h.dealer, h.smallBlindSeat);
		checkEquals("heads-up dealer acts first preflop", h.dealer, h.acting);
		h.applyAction(Game.CHECK_CALL, 0);
		h.applyAction(Game.CHECK_CALL, 0);
		checkEquals("heads-up big blind acts first postflop", h.bigBlindSeat, h.acting);
	}

	static void raiseTests() {
		Game g = newGame(1000, 1000, 1000);
		g.startHand();
		checkEquals("minimum raise preflop is 20", 20, g.minRaiseTo());
		checkEquals("double raises to 20", 20, g.targetBet(Game.DOUBLE, 0));
		checkEquals("pot raise: 15 in pot + 10 call = 25 on top of 10", 35, g.targetBet(Game.POT, 0));
		g.applyAction(Game.DOUBLE, 0); // seat 0 raises to 20
		checkEquals("current bet after raise", 20, g.currentBet);
		checkEquals("min raise is now 10 more", 30, g.minRaiseTo());
		g.applyAction(Game.CUSTOM, 60); // seat 1 re-raises to 60
		checkEquals("min raise tracks the largest raise", 100, g.minRaiseTo());
		check("raiser cannot re-raise a below-minimum amount", !g.isValidCustom(70));
		check("all-in for less than a min raise is allowed", g.isValidCustom(g.maxTo(g.players[2])));
		g.applyAction(Game.CHECK_CALL, 0); // seat 2 calls 60
		checkEquals("raise reopens action for the original raiser", 0, g.acting);
		g.applyAction(Game.CHECK_CALL, 0);
		checkEquals("round ends once everyone matches", Game.FLOP, g.street);
		checkEquals("pot holds 180", 180, g.pot);

		// postflop minimum bet is the big blind, even from the Double button
		checkEquals("postflop double is a min bet", 10, g.targetBet(Game.DOUBLE, 0));
	}

	static void betCapTests() {
		Game g = newGame(1000, 1000, 15);
		g.startHand();
		g.applyAction(Game.CUSTOM, 500); // seat 0 raises to 500
		g.applyAction(Game.CHECK_CALL, 0); // seat 1 calls
		Player shortStack = g.players[2];
		checkEquals("short stack is the big blind and to act", 2, g.acting);
		checkEquals("call is capped by chips", 5, g.callAmount(shortStack));
		checkEquals("double capped at all-in", 15, g.targetBet(Game.DOUBLE, 0));
		g.applyAction(Game.DOUBLE, 0);
		check("short stack is all-in, not negative", shortStack.chips == 0 && shortStack.allIn);
		check("no deadlock: hand continues without the all-in player", !g.handOver && g.street == Game.FLOP);
		checkEquals("only the two big stacks act on the flop", 2, g.countCanAct());
		checkEquals("chips conserved", 2015, totalChips(g));
	}

	static void sidePotTests() {
		Game g = newGame(1000, 1000, 100);
		g.startHand(); // dealer 0, sb 1, bb 2 (the short stack)
		g.setCommunity(c(TWO, 1), c(SEVEN, 2), c(NINE, 3), c(JACK, 1), c(THREE, 2));
		g.setPlayerCards(2, c(ACE, 0), c(ACE, 1)); // pair of aces
		g.setPlayerCards(1, c(KING, 0), c(KING, 1)); // pair of kings
		g.setPlayerCards(0, c(QUEEN, 0), c(QUEEN, 1)); // pair of queens
		g.applyAction(Game.CUSTOM, 300); // seat 0 raises to 300
		g.applyAction(Game.CHECK_CALL, 0); // seat 1 calls 300
		g.applyAction(Game.CHECK_CALL, 0); // seat 2 calls all-in for 100
		checkEquals("flop reached", Game.FLOP, g.street);
		// short stack wins the main pot only; seat 1 wins the side pot
		for (int i = 0; i < 6; i++) {
			g.applyAction(Game.CHECK_CALL, 0); // check it down
		}
		check("hand over", g.handOver);
		checkEquals("short stack wins the main pot of 300", 300, g.players[2].chips);
		checkEquals("seat 1 wins the side pot of 400", 1100, g.players[1].chips);
		checkEquals("seat 0 loses 300", 700, g.players[0].chips);
		checkEquals("chips conserved", 2100, totalChips(g));
		checkEquals("main pot text", "Winner: P2", g.winnerText);
		checkEquals("one side pot line", 1, g.extraResultLines.size());
		check("side pot line names seat 1", g.extraResultLines.get(0).contains("P1"));

		// uncalled bet is returned when the all-in player is called by only one player
		Game u = newGame(1000, 1000, 50);
		u.startHand();
		u.setCommunity(c(TWO, 1), c(SEVEN, 2), c(NINE, 3), c(JACK, 1), c(THREE, 2));
		u.setPlayerCards(2, c(ACE, 0), c(ACE, 1));
		u.setPlayerCards(0, c(QUEEN, 0), c(QUEEN, 1));
		u.applyAction(Game.CUSTOM, 400); // seat 0 raises to 400
		u.applyAction(Game.FOLD, 0); // seat 1 folds
		u.applyAction(Game.CHECK_CALL, 0); // seat 2 all-in for 50
		check("nobody left to bet: straight to showdown", u.handOver && u.showdownReached);
		checkEquals("short stack triples up including the dead small blind", 105, u.players[2].chips);
		checkEquals("uncalled 350 returned to raiser", 950, u.players[0].chips);
		checkEquals("small blind lost 5", 995, u.players[1].chips);

		// a folded player's chips join the main pot instead of making a pot of their own
		Game m = newGame(1000, 1000, 1000, 40);
		m.startHand(); // dealer 0, sb 1, bb 2, seat 3 acts first
		m.setCommunity(c(TWO, 1), c(SEVEN, 2), c(NINE, 3), c(JACK, 1), c(THREE, 2));
		m.setPlayerCards(3, c(ACE, 0), c(ACE, 1));
		m.setPlayerCards(2, c(KING, 0), c(KING, 1));
		m.setPlayerCards(0, c(QUEEN, 0), c(QUEEN, 1));
		m.applyAction(Game.ALL_IN, 0);       // seat 3 all-in for 40
		m.applyAction(Game.CUSTOM, 200);     // seat 0 raises to 200
		m.applyAction(Game.FOLD, 0);         // small blind folds, 5 chips dead
		m.applyAction(Game.CHECK_CALL, 0);   // seat 2 calls 200
		while (!m.handOver) {
			m.applyAction(Game.CHECK_CALL, 0);
		}
		checkEquals("main pot is 40+40+40+5", 125, m.players[3].chips);
		checkEquals("main pot winner text", "Winner: P3", m.winnerText);
		checkEquals("exactly one side pot", 1, m.extraResultLines.size());
		check("side pot is the 320 between the big stacks", m.extraResultLines.get(0).startsWith("Side pot 1 (320)"));
		checkEquals("chips conserved", 3040, totalChips(m));
	}

	static void foldWinTests() {
		Game g = newGame(1000, 1000, 1000);
		g.startHand();
		g.applyAction(Game.CUSTOM, 100); // seat 0 raises
		g.applyAction(Game.FOLD, 0);
		g.applyAction(Game.FOLD, 0);
		check("hand over by folds", g.handOver && !g.showdownReached);
		checkEquals("raiser collects the blinds and their own bet back", 1015, g.players[0].chips);
		checkEquals("winner text", "Winner: P0", g.winnerText);
		checkEquals("chips conserved", 3000, totalChips(g));
	}

	static void splitPotTests() {
		Game g = newGame(1000, 1000, 1000);
		g.startHand();
		g.setCommunity(c(TEN, 0), c(JACK, 1), c(QUEEN, 2), c(KING, 3), c(ACE, 0)); // straight on the board
		g.setPlayerCards(0, c(TWO, 1), c(THREE, 1));
		g.setPlayerCards(1, c(TWO, 2), c(THREE, 2));
		g.setPlayerCards(2, c(TWO, 3), c(THREE, 3));
		g.applyAction(Game.CUSTOM, 25); // pot will be 75, which does not split into 3 evenly
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.CHECK_CALL, 0);
		for (int i = 0; i < 9; i++) {
			g.applyAction(Game.CHECK_CALL, 0);
		}
		check("three way chop", g.handOver);
		checkEquals("no chips lost to rounding", 3000, totalChips(g));
		int[] chips = {g.players[0].chips, g.players[1].chips, g.players[2].chips};
		Arrays.sort(chips);
		check("odd chip goes to exactly one player", chips[0] == 1000 && chips[1] == 1000 && chips[2] == 1000 || (chips[2] - chips[0] == 1));
		check("winner text lists all three", g.winnerText.contains("P0") && g.winnerText.contains("P1") && g.winnerText.contains("P2"));

		// an odd pot: the folded small blind's 5 chips make it 45, chopped two ways
		Game o = newGame(1000, 1000, 1000);
		o.startHand(); // dealer 0, sb 1, bb 2
		o.setCommunity(c(TEN, 0), c(JACK, 1), c(QUEEN, 2), c(KING, 3), c(ACE, 0));
		o.setPlayerCards(0, c(TWO, 1), c(THREE, 1));
		o.setPlayerCards(2, c(TWO, 3), c(THREE, 3));
		o.applyAction(Game.CUSTOM, 20);    // seat 0 raises to 20
		o.applyAction(Game.FOLD, 0);       // small blind folds
		o.applyAction(Game.CHECK_CALL, 0); // big blind calls
		while (!o.handOver) {
			o.applyAction(Game.CHECK_CALL, 0);
		}
		checkEquals("odd chip goes to the first winner left of the dealer", 1003, o.players[2].chips);
		checkEquals("other winner gets the smaller half", 1002, o.players[0].chips);
		checkEquals("folded small blind lost 5", 995, o.players[1].chips);
		checkEquals("chips conserved", 3000, totalChips(o));
	}

	static void unknownActionTests() {
		Game g = newGame(1000, 1000, 1000);
		g.startHand();
		boolean rejected = false;
		try {
			g.applyAction(0, 0);
		} catch (IllegalArgumentException e) {
			rejected = true;
		}
		check("unknown action is rejected", rejected);
		checkEquals("state untouched after a rejected action", 0, g.acting);
		check("player has not acted", !g.players[0].acted);
	}

	static void eliminationTests() {
		Game g = newGame(1000, 1000, 30);
		g.startHand(); // seat 2 is the big blind with 30
		g.setCommunity(c(TWO, 1), c(SEVEN, 2), c(NINE, 3), c(JACK, 1), c(THREE, 2));
		g.setPlayerCards(0, c(ACE, 0), c(ACE, 1));
		g.setPlayerCards(1, c(KING, 0), c(KING, 1));
		g.setPlayerCards(2, c(QUEEN, 0), c(QUEEN, 1));
		g.applyAction(Game.CUSTOM, 100);
		g.applyAction(Game.CHECK_CALL, 0);
		g.applyAction(Game.ALL_IN, 0);
		for (int i = 0; i < 6; i++) {
			g.applyAction(Game.CHECK_CALL, 0);
		}
		check("busted player is eliminated", g.players[2].eliminated);
		check("game continues with two players", !g.gameOver);
		g.startHand();
		check("eliminated player is not dealt in", g.players[2].cards[0] == -1);
		checkEquals("play is now heads-up", 2, g.countAlive());
		check("eliminated player never gets the button", g.dealer != 2 && g.smallBlindSeat != 2 && g.bigBlindSeat != 2);

		// finish the game
		Game f = newGame(100, 100);
		f.startHand();
		f.setCommunity(c(TWO, 1), c(SEVEN, 2), c(NINE, 3), c(JACK, 1), c(THREE, 2));
		f.setPlayerCards(0, c(ACE, 0), c(ACE, 1));
		f.setPlayerCards(1, c(KING, 0), c(KING, 1));
		f.applyAction(Game.ALL_IN, 0);
		f.applyAction(Game.CHECK_CALL, 0);
		// both all-in before the flop: board runs out on its own
		check("all-in preflop runs out the board", f.handOver && f.showdownReached);
		check("game over", f.gameOver);
		checkEquals("champion", 0, f.gameWinner);
		checkEquals("champion has everything", 200, f.players[0].chips);
	}

	public static void main(String[] args) {
		evaluatorTests();
		blindsAndOrderTests();
		raiseTests();
		betCapTests();
		sidePotTests();
		foldWinTests();
		splitPotTests();
		unknownActionTests();
		eliminationTests();
		System.out.println(passed + " passed, " + failed + " failed");
		if (failed > 0) {
			System.exit(1);
		}
	}
}
