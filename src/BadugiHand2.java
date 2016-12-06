import java.util.*;

/**
 * A class whose objects represent four-card badugi hands.
 * @author Ilkka Kokkarinen
 */
public class BadugiHand2 implements Comparable<BadugiHand2> {

    // All four cards in this hand.
    private List<Card> allCards = new ArrayList<Card>(4);
    // The best badugi hand that can be constructed by choosing from the four cards.
    private List<Card> activeCards = new ArrayList<Card>(4);
    public char code;

    /**
     * The constructor to create a hand from given list of cards.
     * @param cards The cards that make up this badugi hand.
     */
    public BadugiHand2(List<Card> cards) {
        this.allCards.addAll(cards);
        updateActiveHand();
    }
    
    public BadugiHand2(String cards) {
        if(cards.length() != 8) {
            throw new IllegalArgumentException(cards + " does not consist of four cards");
        }
        for(int i = 0; i < 4; i++) {
            this.allCards.add(Card.from(cards.substring(2*i, 2*i+2)));
        }
        updateActiveHand();
    }
    
    // A private utility method to update the active hand.
    // Bug and fix found by Ilya Bakoulin Nov 6 2016
    private void updateActiveHand() {
        activeCards.clear();
        Comparator<Card> comp = new Comparator<Card>() {
            public int compare(Card c1, Card c2) { return c1.compareTo(c2); }
        };
        Collections.sort(allCards, comp);
        backtrack(0, new ArrayList<Card>(4), activeCards);
        Collections.sort(activeCards, comp);
        int s = activeCards.size();
        switch (s){
            case 1: code = 'u'; break;
            case 2: code = 'v'; break;
            case 3: code = (activeCards.get(0).getRank() >8)?'w':'x'; break;
            case 4: code = (activeCards.get(0).getRank() >8)?'y':'z'; break;
        }
    }
    
    /**
     * The string representation of this hand.
     * @return The string representation of this hand.
     */
    public String toString() {
        String ranks = "";
        for(int rank: getActiveRanks()) {
            ranks += Card.ranks.charAt(rank - 1);
        }
        return allCards.toString() + "(" + ranks + ")";
    }
    
    /**
     * Get the list of all four cards in this hand.
     * @return An unmodifiable view to the four cards in this hand.
     */
    public List<Card> getAllCards() {
        return Collections.unmodifiableList(allCards);
    }
    
    /**
     * Get the list of the active badugi cards in this hand.
     * @return An unmodifiable view to the active badugi cards in this hand.
     * A card is considered active if it is part of the best badugi hand that
     * can be made out of the four cards that comprise the entire hand.
     */
    public List<Card> getActiveCards() {
        return Collections.unmodifiableList(activeCards);
    }
    
    /**
     * Get the list of cards in this hand that are not active in the badugi hand.
     * @return The list of the inactive badugi cards in this hand.
     */
    public List<Card> getInactiveCards() {
        List<Card> inactiveCards = new ArrayList<Card>();
        for(Card c : allCards) {
            if(!activeCards.contains(c)) { inactiveCards.add(c); }
        }
        return inactiveCards;
    }
    
    /**
     * Get an array of ranks of the active badugi cards in this hand, suitable for
     * the needs of the AI agents to evaluate the power of this hand.
     */
    public int[] getActiveRanks() {
        int[] result = new int[activeCards.size()];
        for(int i = 0; i < result.length; i++) {
            result[i] = activeCards.get(i).getRank();
        }
        return result;
    }
    
    /**
     * The order comparison between the two badugi hands.
     * @param other The other badugi hand in this comparison
     * @return The result of the comparison as an int whose sign determines the order.
     * Any positive number means that this hand is better, any negative number means that
     * the other hand is better, and result zero means that the hands are equal value.
     */
    public int compareTo(BadugiHand2 other) {
        return compare(this.activeCards, other.activeCards);
    }
    
    /**
     * A utility function to compare two lists of active badugi cards.
     * @param c1 The first list of cards in this comparison.
     * @param c2 The second list of cards in this comparison.
     * @return The result of the comparison as an int whose sign determines the order.
     */
    public static int compare(List<Card> c1, List<Card> c2) {
        // Badugi with more active cards always beats the one with fewer active cards.
        if(c1.size() > c2.size()) return +1;
        if(c1.size() < c2.size()) return -1;
        // Of two badugis of same length, the hand with a higher card is weaker.
        for(int idx = 0; idx < c1.size(); idx++) {
            int diff = c1.get(idx).getRank() - c2.get(idx).getRank();
            if(diff > 0) return -1;
            if(diff < 0) return +1;
        }
        return 0;
    }
    
    /**
     * Replace one card from this hand with another card drawn from the given deck.
     * @param toReplace The card in this hand that is to be replaced with a drawn card.
     * @param deck The deck from which the new card is drawn.
     */
    public void replaceCard(Card toReplace, DeckState deck) {
        int idx = 0;
        while(idx < 4 && !allCards.get(idx).equals(toReplace)) { idx++; }
        allCards.set(idx, deck.drawCard());
        updateActiveHand();
    }
    
    // To find the best possible active badugi hand that can be made of this hand,
    // use backtracking to iterate through all 16 subsets, using the parameter
    // bestSoFar to store the best combination that we have found so far.
    private void backtrack(int idx, List<Card> buildHand, List<Card> bestSoFar) {
        // Base case of recursion
        if(idx == allCards.size()) {
            if(compare(buildHand, bestSoFar) > 0) {                
                bestSoFar.clear(); bestSoFar.addAll(buildHand);
            }
            return;
        }
        // Check if the current card can be taken in with what is already in
        Card curr = allCards.get(idx);
        boolean noConflict = true;
        for(Card taken: buildHand) {
            if(curr.badugiConflict(taken)) {
                noConflict = false; break;
            }
        }
        // Try taking in the current card, if possible
        if(noConflict) {
            buildHand.add(curr);
            backtrack(idx+1, buildHand, bestSoFar);
            buildHand.remove(buildHand.size()-1);
        }
        // Try not taking in the current card, if this could find a better solution
        if(buildHand.size() + (allCards.size() - idx) > bestSoFar.size()) {
            backtrack(idx+1, buildHand, bestSoFar);
        }
    }
    
    // For testing purposes for now.
    public static void test() {
        BadugiHand bh = new BadugiHand("kh9d5h2c");
        System.out.println(bh);
        Random rng = new Random();
        ArrayList<BadugiHand> hands = new ArrayList<BadugiHand>();
        for(int i = 0; i < 20; i++) {
            EfficientDeck deck = new EfficientDeck(rng);
            ArrayList<Card> cards = new ArrayList<Card>();
            for(int j = 0; j < 4; j++) {
                cards.add(deck.drawCard());
            }
            bh = new BadugiHand(cards);
            hands.add(bh);
        }
        Collections.sort(hands);
        for(BadugiHand hand: hands) {
            System.out.println(hand + " : " + Arrays.toString(hand.getActiveRanks()));
        }
    }
    
}
