import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A class whose objects represent individual decks of cards. A deck is shuffled using
 * the given RNG when created. 
 * @author Ilkka Kokkarinen
 */
public class DeckState {

    // Use the same immutable 52 cards between all decks.
    private static Card[] cards = new Card[52];
    static {
        for(int suit = 0; suit < 4; suit++) {
            for(int rank = 0; rank < 13; rank++) {
                cards[suit*13+rank] = new Card(suit, rank);
            }
        }
    }

    // The cards that are still in the deck.
    private ArrayList<Card> currentCards;
    public BadugiHand2 hand0;
    public BadugiHand2 hand1;

    private DeckState(ArrayList<Card> cards, BadugiHand2 p1, BadugiHand2 p2){

        currentCards = new ArrayList<>(cards);
        hand0 = new BadugiHand2(p1.getAllCards());
        hand1 = new BadugiHand2(p2.getAllCards());

    }
    /**
     * Constructor for the class.
     * @param rng The random number generator used to shuffle the deck.
     */
    public DeckState(Random rng) {
        currentCards = new ArrayList<Card>(52);
        for(Card c: cards) { currentCards.add(c); }
        Collections.shuffle(currentCards, rng);
        hand0 = drawBadugiHand();
        hand1 = drawBadugiHand();
    }

    public DeckState Copy(){
        return new DeckState(currentCards, hand0, hand1);
    }

    /** 
     * Draw one card from top of the deck.
     * @return The card that was drawn and removed from this deck.
     */
    public Card drawCard() {
        if(currentCards.size() < 1) { 
            throw new IllegalStateException("Trying to draw a card from an empty deck.");
        }
        return currentCards.remove(currentCards.size() - 1);
    }
    
    /**
     * Create a new four-card badugi hand by drawing from the top of this deck.
     * @return The badugi hand object thus created.
     */
    public BadugiHand2 drawBadugiHand() {
        List<Card> cards = new ArrayList<Card>();
        for(int i = 0; i < 4; i++) {
            cards.add(drawCard());
        }
        return new BadugiHand2(cards);
    }
}