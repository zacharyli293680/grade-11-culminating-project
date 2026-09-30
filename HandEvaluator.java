// HandEvaluator
// Finds the best five-card poker hand out of any 5 to 7 cards and produces
// a HandValue that can be compared against other hands, including kickers.
//
// Card encoding (unchanged from the original game):
//   rank = card % 13   (0 = two ... 8 = ten, 9 = jack, 10 = queen, 11 = king, 12 = ace)
//   suit = card / 13   (0..3)

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandEvaluator {

	public static final String[] RANK_NAMES = {"two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king", "ace"};
	public static final String[] RANK_PLURALS = {"twos", "threes", "fours", "fives", "sixes", "sevens", "eights", "nines", "tens", "jacks", "queens", "kings", "aces"};

	public static final int HIGH_CARD = 1;
	public static final int PAIR = 2;
	public static final int TWO_PAIR = 3;
	public static final int TRIPS = 4;
	public static final int STRAIGHT = 5;
	public static final int FLUSH = 6;
	public static final int FULL_HOUSE = 7;
	public static final int QUADS = 8;
	public static final int STRAIGHT_FLUSH = 9;
	public static final int ROYAL_FLUSH = 10;

	public static int rank(int card) {
		return card % 13;
	}

	public static int suit(int card) {
		return card / 13;
	}

	// The strength of one five-card hand. Hands compare by category first, then by the
	// tiebreak ranks in order (for example pair rank, then each kicker from high to low).
	public static class HandValue implements Comparable<HandValue> {
		public final int category;
		public final int[] tiebreak;
		public final int[] cards; // the five cards that make up the hand

		HandValue(int category, int[] tiebreak, int[] cards) {
			this.category = category;
			this.tiebreak = tiebreak;
			this.cards = cards;
		}

		public int compareTo(HandValue other) {
			if (category != other.category) {
				return Integer.compare(category, other.category);
			}
			int n = Math.min(tiebreak.length, other.tiebreak.length);
			for (int i = 0; i < n; i++) {
				if (tiebreak[i] != other.tiebreak[i]) {
					return Integer.compare(tiebreak[i], other.tiebreak[i]);
				}
			}
			return 0;
		}

		// Human readable description, in the same style as the original game.
		public String describe() {
			switch (category) {
				case ROYAL_FLUSH:
					return "royal flush";
				case STRAIGHT_FLUSH:
					return RANK_NAMES[tiebreak[0]] + " high straight flush";
				case QUADS:
					return "quad " + RANK_PLURALS[tiebreak[0]];
				case FULL_HOUSE:
					return RANK_PLURALS[tiebreak[0]] + " full of " + RANK_PLURALS[tiebreak[1]];
				case FLUSH:
					return RANK_NAMES[tiebreak[0]] + " high flush";
				case STRAIGHT:
					return RANK_NAMES[tiebreak[0]] + " high straight";
				case TRIPS:
					return "trip " + RANK_PLURALS[tiebreak[0]];
				case TWO_PAIR:
					return "two pairs: " + RANK_PLURALS[tiebreak[0]] + " and " + RANK_PLURALS[tiebreak[1]];
				case PAIR:
					return "pair of " + RANK_PLURALS[tiebreak[0]];
				default:
					return RANK_NAMES[tiebreak[0]] + " high";
			}
		}

		public String toString() {
			return describe();
		}
	}

	// Evaluates the best five-card hand available from the given cards (5 to 7 of them).
	public static HandValue evaluate(int[] cards) {
		if (cards.length < 5) {
			throw new IllegalArgumentException("need at least 5 cards, got " + cards.length);
		}
		if (cards.length == 5) {
			return evaluateFive(cards);
		}
		HandValue best = null;
		int[] combo = new int[5];
		best = bestOfCombos(cards, combo, 0, 0, best);
		return best;
	}

	// Tries every way of choosing 5 cards out of the input and keeps the strongest.
	private static HandValue bestOfCombos(int[] cards, int[] combo, int start, int depth, HandValue best) {
		if (depth == 5) {
			HandValue value = evaluateFive(combo.clone());
			if (best == null || value.compareTo(best) > 0) {
				return value;
			}
			return best;
		}
		for (int i = start; i <= cards.length - (5 - depth); i++) {
			combo[depth] = cards[i];
			best = bestOfCombos(cards, combo, i + 1, depth + 1, best);
		}
		return best;
	}

	// Evaluates exactly five cards.
	static HandValue evaluateFive(int[] five) {
		int[] ranks = new int[5];
		boolean flush = true;
		for (int i = 0; i < 5; i++) {
			ranks[i] = rank(five[i]);
			if (suit(five[i]) != suit(five[0])) {
				flush = false;
			}
		}
		// sort ranks from high to low
		Arrays.sort(ranks);
		for (int i = 0; i < 2; i++) {
			int tmp = ranks[i];
			ranks[i] = ranks[4 - i];
			ranks[4 - i] = tmp;
		}

		// count how many of each rank we have
		int[] count = new int[13];
		for (int r : ranks) {
			count[r]++;
		}
		// groups ordered by count (desc) then rank (desc): e.g. a full house gives [trip, pair]
		List<int[]> groups = new ArrayList<>();
		for (int c = 4; c >= 1; c--) {
			for (int r = 12; r >= 0; r--) {
				if (count[r] == c) {
					groups.add(new int[] {c, r});
				}
			}
		}

		boolean distinct = groups.size() == 5;
		boolean straight = false;
		int straightHigh = -1;
		if (distinct) {
			if (ranks[0] - ranks[4] == 4) {
				straight = true;
				straightHigh = ranks[0];
			} else if (ranks[0] == 12 && ranks[1] == 3 && ranks[2] == 2 && ranks[3] == 1 && ranks[4] == 0) {
				straight = true; // the wheel: A-2-3-4-5, which is five high
				straightHigh = 3;
			}
		}

		int first = groups.get(0)[0];
		int second = groups.size() > 1 ? groups.get(1)[0] : 0;

		if (straight && flush) {
			int category = straightHigh == 12 ? ROYAL_FLUSH : STRAIGHT_FLUSH;
			return new HandValue(category, new int[] {straightHigh}, five);
		}
		if (first == 4) {
			return new HandValue(QUADS, new int[] {groups.get(0)[1], groups.get(1)[1]}, five);
		}
		if (first == 3 && second == 2) {
			return new HandValue(FULL_HOUSE, new int[] {groups.get(0)[1], groups.get(1)[1]}, five);
		}
		if (flush) {
			return new HandValue(FLUSH, ranks, five);
		}
		if (straight) {
			return new HandValue(STRAIGHT, new int[] {straightHigh}, five);
		}
		if (first == 3) {
			return new HandValue(TRIPS, new int[] {groups.get(0)[1], groups.get(1)[1], groups.get(2)[1]}, five);
		}
		if (first == 2 && second == 2) {
			return new HandValue(TWO_PAIR, new int[] {groups.get(0)[1], groups.get(1)[1], groups.get(2)[1]}, five);
		}
		if (first == 2) {
			return new HandValue(PAIR, new int[] {groups.get(0)[1], groups.get(1)[1], groups.get(2)[1], groups.get(3)[1]}, five);
		}
		return new HandValue(HIGH_CARD, ranks, five);
	}
}
