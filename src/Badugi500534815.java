import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class Badugi500534815 implements BadugiPlayer{

    private int bets;
    private boolean raised;
    private boolean pending;

    private static final Random r = new Random(System.currentTimeMillis());
    private String history;
    int position;


    public static void main(String[] args){
        //Random r = new Random(System.currentTimeMillis());
        //EfficientDeck d = new EfficientDeck(r);
        ////BadugiPlayer hp = new RuleBasedBadugiPlayer("Rulester");
        //BadugiPlayer hp = new HumanBadugiPlayer();
        //BadugiPlayer rp = new Badugi500534815();
        ////HumanBadugiPlayer rp = new HumanBadugiPlayer(){
        ////    @Override
        ////    public int bettingAction(int drawsRemaining, BadugiHand hand, int bets, int pot, int toCall, int opponentDrew) {
        ////        System.out.println("B:"+bets);
        ////        return super.bettingAction(drawsRemaining, hand, bets, pot, toCall, opponentDrew);
        ////    }
        ////};
        //BadugiPlayer[] players = new BadugiPlayer[]{rp,hp};
        //int score = BadugiRunner.playHeadsUp(d,players, null, 10000);
        //System.out.println(score);



    }
    @Override
    public void startNewHand(int position, int handsToGo, int currentScore) {
        history = "";
        this.position = position;

    }


    @Override
    public int bettingAction(int drawsRemaining, BadugiHand hand, int bets, int pot, int toCall, int opponentDrew) {
        //if(position == 0 && )
        List<Card> activeCards= hand.getActiveCards();
        char code = getCode(activeCards);
        if(position == 0){
            if (bets == 0 && drawsRemaining !=3){
                history += opponentDrew + 5;
            }else if(bets == 2){
                history += 'R';
            }
        }
        if(position == 1){
            if(bets == 0) history+='H';
            if(bets == 1) history+='B';
            if(bets == 3) history +='M';
        }
        if(history.endsWith("BR") || history.endsWith("RM")){
            return 1;
        }
        //System.out.println("You did: " + history.charAt(history.length()-1));
        int decision = getDecision(code + history)-1;

        switch(decision){
            case -1: history += 'F'; break;
            case 0:
                if(position == 0) history += (bets == 0? 'H':'S');
                else history += 'C';
                break;
            case 1: history += position == 0? 'B':'R';
        }
        history +="X";
        return 1;
    }

    private int getDecision(String s) {
        int res = r.nextInt(3);
        InputStream is = null;
        try {
        URL url = new URL("http://54.200.225.168:8080/"+s);
            is = url.openStream();
        Scanner sc = new Scanner(is);
            res = sc.nextInt();

        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return  res;
    }

    private char getCode(List<Card> activeCards) {
        char code = 'u';
        int s = activeCards.size();
        switch (s){
            case 1: code = 'u'; break;
            case 2: code = 'v'; break;
            case 3: code = (activeCards.get(s-1).getRank() >8)?'w':'x'; break;
            case 4: code = (activeCards.get(s-1).getRank() >8)?'y':'z'; break;
        }
        return code;
    }

    @Override
    public List<Card> drawingAction(int drawsRemaining, BadugiHand hand, int pot, int dealerDrew) {

        if(history.endsWith("R")) history+='S';
        else if(history.endsWith("H")) history += 'C';
        //System.out.println("You did: " + history.charAt(history.length()-1));

        if(dealerDrew != -1) {
            history += dealerDrew;
            //System.out.println("You did: " + history.charAt(history.length()-1));

        }


        int digit = getDecision(history);
        history += digit + (dealerDrew == -1?0:5);
        ArrayList<Card> cards = new ArrayList<>();
        List<Card> ac = hand.getInactiveCards();
        for (int i = ac.size()-1; i >= 0; i--) {
            if(digit <= 0) break;

            cards.add(ac.get(i));
            digit--;
        }
        ac = hand.getActiveCards();
        for (int i = ac.size()-1; i >= 0; i--) {
            if(digit <= 0) break;

            cards.add(ac.get(i));
            digit--;
        }


        return cards;
    }

    @Override
    public void showdown(BadugiHand yourHand, BadugiHand opponentHand) {
    }

    @Override
    public String getAgentName() {
        return "500534815";
    }


    @Override
    public String getAuthor() {
        return "Badugi500534815";
    }
}
