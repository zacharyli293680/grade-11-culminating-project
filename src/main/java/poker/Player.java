// Player
// Holds everything the game needs to know about one seat at the table.

package poker;

public class Player {

	public final String name;
	public final int seat;
	public int chips;
	public int streetBet;        // chips put in during the current betting round
	public int handContribution; // chips put in during the whole hand (used for side pots)
	public boolean folded;
	public boolean allIn;
	public boolean eliminated;   // out of chips, no longer dealt in
	public boolean acted;        // has acted since the last bet or raise this round
	public int[] cards = new int[2];

	public Player(String name, int seat, int chips) {
		this.name = name;
		this.seat = seat;
		this.chips = chips;
	}

	// Still holding cards in this hand (may be all-in).
	public boolean inHand() {
		return !folded && !eliminated;
	}

	// Still able to make a decision this hand.
	public boolean canAct() {
		return inHand() && !allIn;
	}

	public void resetForHand() {
		streetBet = 0;
		handContribution = 0;
		folded = false;
		allIn = false;
		acted = false;
		cards[0] = -1;
		cards[1] = -1;
	}
}
