import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class BadugiCFRTrainer {
    public static  int PASS = 0, BET = 1;
    private static final Random random = new Random();
    public static HashMap<String, Node> nodeMap = new HashMap<>();



    private enum NodeType{
        //ISDEALER,
        BET_INIT,
        BET_RESPONSE,
        BET_CLOSE,
        FOLD, SHOWDOWN, DRAW0, DRAW1

    }
    public static class Node{
        static HashMap<NodeType, char[]> ActionTable = new HashMap<>();
        static {
            //ActionTable.put(NodeType.ISDEALER, "YN".toCharArray());
            ActionTable.put(NodeType.BET_INIT, "FHB".toCharArray());
            ActionTable.put(NodeType.BET_RESPONSE, "FCR".toCharArray());
            ActionTable.put(NodeType.BET_CLOSE, "FSM".toCharArray());
            //ActionTable.put(NodeType.DRAW_PRIVATE, "ASB".toCharArray());
            ActionTable.put(NodeType.DRAW0, "01234".toCharArray());
            ActionTable.put(NodeType.DRAW1, "56789".toCharArray());
        }

        //String infoSet;
        int numActions;
        char[] actions;
        double[] regretSum;
        double[] strategy;
        double[] strategySum;

        Node(NodeType nodeType){
            actions = ActionTable.get(nodeType);
            this.numActions = actions.length;
            regretSum= new double[numActions];
            strategy= new double[numActions];
            strategySum= new double[numActions];
        }

        private double[] getStrategy(double realizationWeight) {
            double normalizingSum = 0;
            for (int a = 0; a < numActions; a++) {
                strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
                normalizingSum += strategy[a];
            }
            for (int a = 0; a < numActions; a++) {
                if (normalizingSum > 0)
                    strategy[a] /= normalizingSum;
                else
                    strategy[a] = 1.0 / numActions;
                strategySum[a] += realizationWeight * strategy[a];
            }
            return strategy;
        }


        double[] getAverageStrategy() {
            double[] avgStrategy = new double[numActions];
            double normalizingSum = 0;
            for (int a = 0; a < numActions; a++)
                normalizingSum += strategySum[a];
            for (int a = 0; a < numActions; a++)
                if (normalizingSum > 0)
                    avgStrategy[a] = strategySum[a] / normalizingSum;
                else
                    avgStrategy[a] = 1.0 / numActions;
            return avgStrategy;
        }


        public String toString() {
            return String.format("%s", Arrays.toString(getAverageStrategy()));
        }

    }


    //public NodeType Transition(String history) {
//
    //}

    private void train(int iterations) {

        double util = 0;

        long time = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            DeckState d = new DeckState(random);

            util += cfr(d,NodeType.BET_INIT,"",3, 0 ,0,0, 1, 1);
        }

        time = System.currentTimeMillis() - time;
        System.out.println(time);
        System.out.println("Average game value: " + util / iterations);
        for(String k :nodeMap.keySet()) {
            System.out.printf("%s : %s\n", k, nodeMap.get(k));
        }
    }

    //public int playerFromState(NodeType stateType){
    //    switch (stateType){
//
    //        case BET_CLOSE:
    //        case DRAW0:
    //        case BET_INIT:
    //            return 0;
    //        case BET_RESPONSE:
    //        case DRAW1:
    //            return  1;
    //        default:
    //            return  -1;
    //    }
    //}

    private static int[] betAmounts = new int[]{8,4,2,2};

    private double cfr(DeckState deckState,
                       NodeType stateType,
                       String history,
                       int drawsRemaining,
                       int player,
                       int pot,
                       int toCall,
                       double p0,
                       double p1) {

        //int player = playerFromState(stateType);
        //player = player == -1? 1-prevPlayer:player;

        if (stateType == NodeType.FOLD) {
            pot -= toCall;
            //System.out.println(history);
            return pot/2;
        }
        if (stateType == NodeType.SHOWDOWN)
        {
            //System.out.println(history);

            int handWin = deckState.hand0.compareTo(deckState.hand1);
            if(handWin == 0) return 0;
            return (pot/2)*(player == 0?1:-1)*(handWin > 0?1:-1);
        }

        BadugiHand2 hand = player == 0 ? deckState.hand0 :deckState.hand1;


        String infoSet = hand.code + history;
        //node.infoSet = infoSet;
        Node node = nodeMap.computeIfAbsent(infoSet, k -> new Node(stateType));

        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double[] util = new double[node.numActions];
        double nodeUtil = 0;
        for (int a = 0; a < node.numActions; a++) {
            char action = node.actions[a];
            String nextHistory = history + action;






            int betAmount = betAmounts[drawsRemaining];

            DeckState nd = deckState;
            NodeType nt = null;
            int nDraws = drawsRemaining;
            int nP = 1-player;
            int nPot = pot;
            int nCall = 0;


            switch (action) {
                case 'F': nt = NodeType.FOLD; break;
                case 'H': nt = NodeType.BET_RESPONSE; break;
                case 'B':
                    nt = NodeType.BET_RESPONSE;
                    nCall = betAmount;
                    nPot += betAmount;
                    nP = 1;
                    break;
                case 'C':
                    nt = NodeType.DRAW0;
                    nPot += toCall;
                    nP = 0;
                    break;
                case 'R':
                    nt = NodeType.BET_CLOSE;
                    nCall = betAmount;
                    nPot+= toCall + betAmount;
                    nP = 0;
                    break;
                case 'S':
                    nt = NodeType.DRAW0;
                    nPot+= toCall + betAmount;
                    nP = 0;
                    break;
                case 'M':
                    nt = NodeType.DRAW0;
                    if(history.endsWith("HR")) nPot += 7* betAmount;
                    else if (history.endsWith("BR")) nPot += 5* betAmount;
                    else throw new RuntimeException("Wtf");
                    nP = 0;
                    break;
                default:{
                    if (Character.isDigit(action)){
                        int digit = Character.digit(action,10);
                        nd = deckState.Copy();
                        if(stateType == NodeType.DRAW0){
                            hand = nd.hand0;
                            nt = NodeType.DRAW1;
                        }else if (stateType == NodeType.DRAW1){
                            hand = nd.hand1;
                            digit-=5;
                            nt= NodeType.BET_INIT;
                            //if(drawsRemaining == 3) System.out.println(nextHistory);

                            nDraws = drawsRemaining-1;

                        }else throw new RuntimeException("Ayy lmao");
                        List<Card> ac = hand.getInactiveCards();
                        for (int i = ac.size()-1; i >= 0; i--) {
                            if(digit <= 0) break;

                            hand.replaceCard(ac.get(i), nd);
                            digit--;
                        }
                        ac = hand.getActiveCards();
                        for (int i = ac.size()-1; i >= 0; i--) {
                            if(digit <= 0) break;

                            hand.replaceCard(ac.get(i), nd);
                            digit--;
                        }
                        break;
                    }
                    throw new IllegalStateException();
                }

            }
            if(NodeType.DRAW0 == nt && drawsRemaining == 0){
                nt = NodeType.SHOWDOWN;
            }


            util[a] = player == 0
                    ? cfr(nd,nt,nextHistory,nDraws,nP,nPot,nCall, p0 * strategy[a], p1)
                    : cfr(nd,nt,nextHistory,nDraws,nP,nPot,nCall, p0, p1 * strategy[a]);

            util[a] *= player == nP ? 1:-1;
            nodeUtil += strategy[a] * util[a];
        }

        for (int a = 0; a < node.numActions; a++) {
            double regret = util[a] - nodeUtil;
            node.regretSum[a] += (player == 0 ? p1 : p0) * regret;
        }

        return nodeUtil;
    }


    public static void main(String[] args) {
        int iterations = 10;
        BadugiCFRTrainer t;
        try {
            System.setOut(new PrintStream(new File("output-file.txt")));

            t = new BadugiCFRTrainer();
            t.train(iterations);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}