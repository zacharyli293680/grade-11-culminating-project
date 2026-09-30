// Game
// All of the Texas Hold'em rules, with no graphics in it: blinds, dealer button,
// action order, betting rounds, all-ins, side pots, showdown, elimination, and
// the overall game winner. Poker.java draws this state and feeds it player actions.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Predicate;

public class Game {

	// actions a player can take on their turn
	public static final int FOLD = 1;
	public static final int CHECK_CALL = 2;
	public static final int DOUBLE = 3;   // raise to double the current bet
	public static final int POT = 4;      // pot sized raise
	public static final int ALL_IN = 5;
	public static final int CUSTOM = 6;   // raise to a typed amount

	// betting streets
	public static final int PREFLOP = 1;
	public static final int FLOP = 2;
	public static final int TURN = 3;
	public static final int RIVER = 4;
	public static final int SHOWDOWN = 5;

	public static final int STARTING_CHIPS = 1000;
	public static final int SMALL_BLIND = 5;
	public static final int BIG_BLIND = 10;

	public final Player[] players;
	public final int[] community = new int[5];

	public int dealer = -1;
	public int smallBlindSeat = -1;
	public int bigBlindSeat = -1;
	public int street = PREFLOP;
	public int pot;            // chips collected from finished betting rounds
	public int currentBet;     // amount each player must have in to stay in this round
	public int minRaise;       // size of the last raise; the next raise must be at least this much
	public int acting = -1;    // seat whose turn it is, or -1 when nobody is to act

	public boolean handOver;
	public boolean showdownReached; // true when the hand ended by showdown rather than folds
	public boolean gameOver;
	public int gameWinner = -1;
	public int handsPlayed;

	// results of the last hand, for the screen
	public String winnerText = "";
	public String winningHandText = "";
	public final List<String> extraResultLines = new ArrayList<>();

	private final boolean[] drawn = new boolean[52];
	private final Random random = new Random();

	public Game(String[] names) {
		players = new Player[names.length];
		for (int i = 0; i < names.length; i++) {
			players[i] = new Player(names[i], i, STARTING_CHIPS);
		}
	}

	// ---------------------------------------------------------------- hand setup

	// Deals a new hand, moves the button, and posts the blinds.
	public void startHand() {
		if (gameOver) {
			return;
		}
		handsPlayed++;
		handOver = false;
		showdownReached = false;
		winnerText = "";
		winningHandText = "";
		extraResultLines.clear();
		pot = 0;
		street = PREFLOP;
		currentBet = 0;
		minRaise = BIG_BLIND;
		Arrays.fill(drawn, false);

		for (Player p : players) {
			p.resetForHand();
			if (!p.eliminated) {
				p.cards[0] = drawCard();
				p.cards[1] = drawCard();
			}
		}
		for (int i = 0; i < community.length; i++) {
			community[i] = drawCard();
		}

		dealer = nextSeat(dealer, p -> !p.eliminated);
		if (countAlive() == 2) {
			// heads-up: the dealer posts the small blind and acts first preflop
			smallBlindSeat = dealer;
			bigBlindSeat = nextSeat(dealer, p -> !p.eliminated);
		} else {
			smallBlindSeat = nextSeat(dealer, p -> !p.eliminated);
			bigBlindSeat = nextSeat(smallBlindSeat, p -> !p.eliminated);
		}
		putIn(players[smallBlindSeat], SMALL_BLIND);
		putIn(players[bigBlindSeat], BIG_BLIND);
		currentBet = BIG_BLIND; // the full big blind is owed even if the big blind was short
		minRaise = BIG_BLIND;

		acting = nextSeat(bigBlindSeat, Player::canAct);
		if (acting == -1) {
			// everybody went all-in just from posting blinds: run the board out
			advanceStreet();
		}
	}

	// Test hooks: force specific cards after startHand(). Cards use the 0..51 encoding.
	public void setPlayerCards(int seat, int card1, int card2) {
		players[seat].cards[0] = card1;
		players[seat].cards[1] = card2;
	}

	public void setCommunity(int flop1, int flop2, int flop3, int turn, int river) {
		community[0] = flop1;
		community[1] = flop2;
		community[2] = flop3;
		community[3] = turn;
		community[4] = river;
	}

	private int drawCard() {
		int card;
		do {
			card = random.nextInt(52);
		} while (drawn[card]);
		drawn[card] = true;
		return card;
	}

	// ---------------------------------------------------------------- queries

	public Player actingPlayer() {
		return acting < 0 ? null : players[acting];
	}

	public int countAlive() {
		int n = 0;
		for (Player p : players) {
			if (!p.eliminated) {
				n++;
			}
		}
		return n;
	}

	public int countInHand() {
		int n = 0;
		for (Player p : players) {
			if (p.inHand()) {
				n++;
			}
		}
		return n;
	}

	public int countCanAct() {
		int n = 0;
		for (Player p : players) {
			if (p.canAct()) {
				n++;
			}
		}
		return n;
	}

	// How many community cards are face up on the given street.
	public int communityCardsShown() {
		switch (street) {
			case PREFLOP: return 0;
			case FLOP: return 3;
			case TURN: return 4;
			default: return 5;
		}
	}

	// Chips the player must add to call, limited by what they have.
	public int callAmount(Player p) {
		return Math.min(Math.max(0, currentBet - p.streetBet), p.chips);
	}

	// The smallest legal amount a raise can bring the player's round bet to.
	public int minRaiseTo() {
		return currentBet == 0 ? BIG_BLIND : currentBet + minRaise;
	}

	// The most a player can have in this round: everything they have.
	public int maxTo(Player p) {
		return p.chips + p.streetBet;
	}

	// Pot size after this player calls, which is what a pot sized raise is measured from.
	public int potSizeAfterCall(Player p) {
		int total = pot;
		for (Player o : players) {
			total += o.streetBet;
		}
		return total + Math.max(0, currentBet - p.streetBet);
	}

	// Whether a typed raise amount is allowed for the acting player.
	public boolean isValidCustom(int to) {
		Player p = actingPlayer();
		if (p == null) {
			return false;
		}
		int max = maxTo(p);
		return to == max || (to >= minRaiseTo() && to <= max);
	}

	// What the acting player's round bet would be after taking the action (for previews).
	public int targetBet(int action, int customTo) {
		Player p = actingPlayer();
		if (p == null) {
			return 0;
		}
		int max = maxTo(p);
		int to;
		switch (action) {
			case FOLD:
				return p.streetBet;
			case CHECK_CALL:
				return p.streetBet + callAmount(p);
			case DOUBLE:
				to = currentBet == 0 ? BIG_BLIND : currentBet * 2;
				break;
			case POT:
				to = currentBet + potSizeAfterCall(p);
				break;
			case ALL_IN:
				to = max;
				break;
			case CUSTOM:
				to = customTo;
				break;
			default:
				return p.streetBet;
		}
		if (action != CUSTOM) {
			to = Math.max(to, minRaiseTo());
		}
		return Math.min(to, max);
	}

	// Chips the acting player would have left after the action.
	public int chipsAfter(int action, int customTo) {
		Player p = actingPlayer();
		if (p == null) {
			return 0;
		}
		return p.chips - (targetBet(action, customTo) - p.streetBet);
	}

	public String handDescription(int seat) {
		return handValue(seat).describe();
	}

	public HandEvaluator.HandValue handValue(int seat) {
		Player p = players[seat];
		int[] cards = {p.cards[0], p.cards[1], community[0], community[1], community[2], community[3], community[4]};
		return HandEvaluator.evaluate(cards);
	}

	// ---------------------------------------------------------------- actions

	// Applies the acting player's decision and moves the action along.
	public void applyAction(int action, int customTo) {
		Player p = actingPlayer();
		if (handOver || p == null) {
			return;
		}
		if (action < FOLD || action > CUSTOM) {
			throw new IllegalArgumentException("unknown action " + action);
		}
		if (action == FOLD) {
			p.folded = true;
		} else if (action == CHECK_CALL) {
			putIn(p, callAmount(p));
		} else {
			int to = targetBet(action, customTo);
			int add = to - p.streetBet;
			if (add > 0) {
				putIn(p, add);
			}
			if (p.streetBet > currentBet) {
				int raiseSize = p.streetBet - currentBet;
				if (raiseSize > minRaise) {
					minRaise = raiseSize;
				}
				currentBet = p.streetBet;
				// a raise gives everyone else another decision
				for (Player o : players) {
					if (o != p) {
						o.acted = false;
					}
				}
			}
		}
		p.acted = true;
		advance();
	}

	private void putIn(Player p, int amount) {
		amount = Math.min(amount, p.chips);
		p.chips -= amount;
		p.streetBet += amount;
		p.handContribution += amount;
		if (p.chips == 0) {
			p.allIn = true;
		}
	}

	private void advance() {
		if (countInHand() <= 1) {
			endByFolds();
			return;
		}
		if (roundComplete()) {
			advanceStreet();
			return;
		}
		acting = nextSeat(acting, Player::canAct);
		if (acting == -1) {
			advanceStreet();
		}
	}

	// A round is over once everyone still able to act has acted since the last raise
	// and has matched the current bet.
	private boolean roundComplete() {
		for (Player p : players) {
			if (p.canAct() && (!p.acted || p.streetBet != currentBet)) {
				return false;
			}
		}
		return true;
	}

	private void advanceStreet() {
		collectBets();
		currentBet = 0;
		minRaise = BIG_BLIND;
		for (Player p : players) {
			p.acted = false;
		}
		street++;
		if (street >= SHOWDOWN || countCanAct() <= 1) {
			// either the river is done, or nobody is left who can bet: go to showdown
			street = SHOWDOWN;
			showdown();
			return;
		}
		acting = nextSeat(dealer, Player::canAct);
	}

	private void collectBets() {
		for (Player p : players) {
			pot += p.streetBet;
			p.streetBet = 0;
		}
	}

	// ---------------------------------------------------------------- ending a hand

	private void endByFolds() {
		collectBets();
		acting = -1;
		Player winner = null;
		for (Player p : players) {
			if (p.inHand()) {
				winner = p;
			}
		}
		awardPots();
		winnerText = "Winner: " + winner.name;
		winningHandText = "Everyone else folded";
		finishHand();
	}

	private void showdown() {
		acting = -1;
		showdownReached = true;
		awardPots();
		finishHand();
	}

	// Splits the chips into a main pot and side pots based on how much each player put
	// in over the hand, then pays each pot to the best eligible hand(s).
	private void awardPots() {
		HandEvaluator.HandValue[] values = new HandEvaluator.HandValue[players.length];
		for (Player p : players) {
			if (p.inHand()) {
				values[p.seat] = handValue(p.seat);
			}
		}

		TreeSet<Integer> levels = new TreeSet<>();
		for (Player p : players) {
			if (p.handContribution > 0) {
				levels.add(p.handContribution);
			}
		}

		// build the pots: one per contribution level, then merge neighbours that have the
		// same eligible players (a folded player's chips do not make a pot of their own)
		List<Integer> amounts = new ArrayList<>();
		List<List<Player>> eligibles = new ArrayList<>();
		int previous = 0;
		for (int level : levels) {
			int amount = 0;
			for (Player p : players) {
				amount += Math.max(0, Math.min(p.handContribution, level) - previous);
			}
			List<Player> eligible = new ArrayList<>();
			for (Player p : players) {
				if (p.inHand() && p.handContribution >= level) {
					eligible.add(p);
				}
			}
			if (eligible.isEmpty()) {
				for (Player p : players) {
					if (p.inHand()) {
						eligible.add(p);
					}
				}
			}
			int last = amounts.size() - 1;
			if (last >= 0 && eligibles.get(last).equals(eligible)) {
				amounts.set(last, amounts.get(last) + amount);
			} else {
				amounts.add(amount);
				eligibles.add(eligible);
			}
			previous = level;
		}

		boolean mainReported = false;
		for (int potIndex = 0; potIndex < amounts.size(); potIndex++) {
			int amount = amounts.get(potIndex);
			List<Player> eligible = eligibles.get(potIndex);

			HandEvaluator.HandValue best = null;
			for (Player p : eligible) {
				if (best == null || values[p.seat].compareTo(best) > 0) {
					best = values[p.seat];
				}
			}
			List<Player> winners = new ArrayList<>();
			// in clockwise order from the dealer, so the odd chip goes to the first winner after the button
			for (int k = 1; k <= players.length; k++) {
				Player p = players[(dealer + k) % players.length];
				if (eligible.contains(p) && values[p.seat].compareTo(best) == 0) {
					winners.add(p);
				}
			}
			int share = amount / winners.size();
			int remainder = amount % winners.size();
			for (int i = 0; i < winners.size(); i++) {
				winners.get(i).chips += share + (i == 0 ? remainder : 0);
			}

			if (eligible.size() > 1) {
				String names = "";
				for (Player w : winners) {
					names += (names.isEmpty() ? "" : ", ") + w.name;
				}
				if (!mainReported) {
					winnerText = "Winner: " + names;
					winningHandText = "Winning Hand: " + best.describe();
					mainReported = true;
				} else {
					extraResultLines.add("Side pot " + potIndex + " (" + amount + "): " + names + ", " + best.describe());
				}
			}
		}
		pot = 0;
	}

	private void finishHand() {
		handOver = true;
		acting = -1;
		for (Player p : players) {
			if (!p.eliminated && p.chips == 0) {
				p.eliminated = true;
			}
		}
		if (countAlive() == 1) {
			gameOver = true;
			for (Player p : players) {
				if (!p.eliminated) {
					gameWinner = p.seat;
				}
			}
		}
	}

	// ---------------------------------------------------------------- helpers

	// The next seat clockwise from 'from' whose player passes the test, or -1 if none.
	private int nextSeat(int from, Predicate<Player> test) {
		for (int k = 1; k <= players.length; k++) {
			int i = ((from + k) % players.length + players.length) % players.length;
			if (test.test(players[i])) {
				return i;
			}
		}
		return -1;
	}
}
