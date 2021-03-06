package student_player;

import Saboteur.cardClasses.*;
import Saboteur.*;
import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;


import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;
/**
 * To implement our own Clone
 */
public class SaboteurBoardStateCopy extends BoardState {
    public static final int BOARD_SIZE = 14;
    public static final int originPos = 5;
    public static final int EMPTY = -1;
    public static final int TUNNEL = 1;
    public static final int WALL = 0;
    public static int AGENT_ID; //a constant, store agent's id.

    private static int FIRST_PLAYER = 1;

    private SaboteurTile[][] board;
    private int[][] intBoard;
    //player variables:
    // Note: Player 1 is active when turnplayer is 1;
    private ArrayList<SaboteurCard> player1Cards; //hand of player 1
    private ArrayList<SaboteurCard> player2Cards; //hand of player 2
    private int player1nbMalus;
    private int player2nbMalus;
    private boolean[] player1hiddenRevealed = {false,false,false};
    private boolean[] player2hiddenRevealed = {false,false,false};

    private ArrayList<SaboteurCard> Deck; //deck form which player pick
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
    protected static SaboteurTile[] hiddenCards = new SaboteurTile[3];
    private boolean[] hiddenRevealed = {false,false,false}; //whether hidden at pos1 is revealed, hidden at pos2 is revealed, hidden at pos3 is revealed.


    private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand = new Random(2019);


    public SaboteurBoardStateCopy() { //CHANGE THE ACCESSOR FOR TESTING PURPOSES@
        super();
        this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = null;
            }
        }
        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                this.intBoard[i][j] = EMPTY;
            }
        }
        // initialize the hidden position:
        ArrayList<String> list =new ArrayList<String>();
        list.add("hidden1");
        list.add("hidden2");
        list.add("nugget");
        Random startRand = new Random();
        for(int i = 0; i < 3; i++){
            int idx = startRand.nextInt(list.size());
            this.board[hiddenPos[i][0]][hiddenPos[i][1]] = new SaboteurTile(list.remove(idx));
            this.hiddenCards[i] = this.board[hiddenPos[i][0]][hiddenPos[i][1]];
        }
        //initialize the entrance
        this.board[originPos][originPos] = new SaboteurTile("entrance");
        //initialize the deck.
        this.Deck = SaboteurCard.getDeck();
        //shuffle the deck.
        //TODO: shuffle so that every player will get at least a forward tile during the game...
        Collections.shuffle(this.Deck);
        //initialize the player effects:
        player1nbMalus = 0;
        player2nbMalus = 0;
        //initialize the players hands:
        this.player1Cards = new ArrayList<SaboteurCard>();
        this.player2Cards = new ArrayList<SaboteurCard>();
        for(int i=0;i<7;i++){
            this.player1Cards.add(this.Deck.remove(0));
            this.player2Cards.add(this.Deck.remove(0));
        }
        rand = new Random(2019);
        winner = Board.NOBODY;
        turnPlayer = FIRST_PLAYER;
        turnNumber = 0;
    }




    // The purpose of this copied class. We want a copy!
    // We will generate a SaboteurBoardStateCopy given the SaboteurBoardStateCopy.
    public SaboteurBoardStateCopy(student_player.SaboteurBoardStateCopy pbs) {
        super();
        this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(pbs.board[i], 0, this.board[i], 0, BOARD_SIZE);
        }

        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        getIntBoard(); //updates int board
        this.rand = pbs.rand;

        //we are not looking for shallow copy (where element are not copied) but deep copy, so that the user can't destroy the board that is sent to him...
        this.player1Cards = new ArrayList<SaboteurCard>();
        for (int i = 0; i < pbs.player1Cards.size(); i++) {
            this.player1Cards.add(i, SaboteurCard.copyACard(pbs.player1Cards.get(i).getName()));
        }
        this.player2Cards = new ArrayList<SaboteurCard>();
        for (int i = 0; i < pbs.player2Cards.size(); i++) {
            this.player2Cards.add(i, SaboteurCard.copyACard(pbs.player2Cards.get(i).getName()));
        }
        this.Deck = new ArrayList<SaboteurCard>();
        for (int i = 0; i < pbs.Deck.size(); i++) {
            this.Deck.add(i, SaboteurCard.copyACard(pbs.Deck.get(i).getName()));
        }

        System.arraycopy(pbs.player1hiddenRevealed, 0, this.player1hiddenRevealed, 0, pbs.player1hiddenRevealed.length);
        System.arraycopy(pbs.player2hiddenRevealed, 0, this.player2hiddenRevealed, 0, pbs.player2hiddenRevealed.length);
        System.arraycopy(pbs.hiddenRevealed, 0, this.hiddenRevealed, 0, pbs.hiddenRevealed.length);

        //Make it static would change all hidden cards right?
        // Problem: we also need to make a copy of the private array where hiddenCards are stored.... So we make it protected!
//        this.hiddenCards = new SaboteurTile[pbs.hiddenCards.length];
//        for (int i = 0; i < pbs.hiddenCards.length; i++) {
//            this.hiddenCards[i] = new SaboteurTile(pbs.hiddenCards[i].getName().split(":")[1]); //Note: we might encounter a problem here, and should define card as cloneable
//        }

        this.player1nbMalus = pbs.player1nbMalus;
        this.player2nbMalus = pbs.player2nbMalus;
        this.winner = pbs.winner;
        this.turnPlayer = pbs.turnPlayer;
        this.turnNumber = pbs.turnNumber;
        this.AGENT_ID = pbs.AGENT_ID;
    }






    // Second copy constructor, takes a SaboteurBoardState object and updates hand/tiles/

    public SaboteurBoardStateCopy(SaboteurBoardState pbs) {
        super();
        this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        SaboteurTile[][] hiddenboard = pbs.getHiddenBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(hiddenboard[i], 0, this.board[i], 0, BOARD_SIZE);
        }

        boolean[] opponentReveal = {true,true,true}; //assume opponents has the best info

        if(pbs.getTurnPlayer()==0){
            this.turnPlayer=0;
            this.player2Cards = pbs.getCurrentPlayerCards();  //get agent's hand
            System.arraycopy(opponentReveal, 0, this.player1hiddenRevealed, 0, opponentReveal.length);
            AGENT_ID=0;
        }
        else{  //if I am the first player
            this.turnPlayer=1;
            this.player1Cards = pbs.getCurrentPlayerCards();  //get agent's hand
            System.arraycopy(opponentReveal, 0, this.player2hiddenRevealed, 0, opponentReveal.length);
            AGENT_ID=1;

        }

        int numOfHidden = setTrackHiddenstatus(hiddenboard); //updates hiddenCards,hiddenstatus, return non-hidden cards number


        this.player1nbMalus = pbs.getNbMalus(1);
        this.player2nbMalus = pbs.getNbMalus(0);
        this.winner = pbs.getWinner();
        this.turnNumber = pbs.getTurnNumber();

        guessCurrentDeck(hiddenboard,numOfHidden);

        CreateOpponentHand();
        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        getIntBoard();
        if(this.turnNumber==0){  //special case when first round.
            this.turnPlayer=pbs.getTurnPlayer();
        }

    }

    public void guessCurrentDeck(SaboteurTile[][] pTile,int map){
        this.Deck = SaboteurCard.getDeck();
        ArrayList<SaboteurCard> pCards = this.getCurrentPlayerCards();
        ArrayList<SaboteurCard> deck = this.Deck;
        //Collections.shuffle(deck);

        //remove player's hand from deck
        for(SaboteurCard card:pCards){
            Iterator<SaboteurCard> iter = deck.iterator();   //updates a new iter
            while(iter.hasNext()){
                if(iter.next().getName().equals(card.getName())){ //when we have found the same card in the deck.
                    iter.remove();   //remove it
                    break;
                }
            }
        }

        //remove Appeared Tiles from deck
        for(int i=0; i <BOARD_SIZE;i++){
            for(int j=0; j <BOARD_SIZE;j++) {
                if (pTile[i][j] == null) { continue;}    //skip null
                if (i == 5 && j == 5) {continue;}       //skip entrance
                if (i == 12 && j == 3) { continue; }      //skip 3 hidden boards
                if (i == 12 && j == 5) { continue;}
                if (i == 12 && j == 7) { continue;}
                String cardname = pTile[i][j].getIdx();  //get its index
                String flippedcardname = pTile[i][j].getFlipped().getIdx();  //get flipped's index

                Iterator<SaboteurCard> iter = deck.iterator();   //updates a new iter
                while (iter.hasNext()) {
                    SaboteurCard card = iter.next();
                    if (card.getName().equals(cardname)) { //when we have found the same card in the deck.
                        iter.remove();   //remove it
                        break;
                    } else if (card.getName().equals(flippedcardname)) {
                        iter.remove();   //remove it
                        break;
                    }
                }
            }
        }


        Iterator<SaboteurCard> iter3 = deck.iterator();   //removes map
        int k = 0;
        map = map +3; //assume opponents know everything.
        while(iter3.hasNext() && k<map){
            if(iter3.next().getName().equals("Map")){
                iter3.remove();
                k++;
            }
        }

        k=0;
        //THIS SOMETIMES CAUSE ERRORS.
        int malus = getNbMalus(0)+getNbMalus(1);
        for(Iterator<SaboteurCard> iterator = deck.iterator();iterator.hasNext();){
            SaboteurCard card = iterator.next();
            if((card.getName()).equals("Malus") && k<malus){
                iterator.remove();
                k++;
            }

        }
        Collections.shuffle(deck);

    }




    public void CreateOpponentHand(){
        if(this.turnPlayer==1){
            this.player2Cards = new ArrayList<SaboteurCard>();
            if(this.Deck.size()>0){
                for(int i=0;i<this.player1Cards.size();i++){
                    if(this.Deck.size()>0){
                        this.player2Cards.add(this.Deck.remove(0)); //draw same number of cards as player1.
                    }
                }
            }
        }else{
            this.player1Cards = new ArrayList<SaboteurCard>();
            if(this.Deck.size()>0){
                for(int i=0;i<this.player2Cards.size();i++){
                    if(this.Deck.size()>0){
                        this.player1Cards.add(this.Deck.remove(0)); //draw same number of cards as player1.
                    }
                }
            }

        }

    }

    //return hidden status. Remove hidden cards.
    public int setTrackHiddenstatus(SaboteurTile[][] tiles){
        // initialize the hidden position: Randomly
        ArrayList<String> list =new ArrayList<String>();
        list.add("hidden1");
        list.add("hidden2");
        list.add("nugget");
        //generates hiddenCards
        this.hiddenCards[0] = new SaboteurTile("hidden1");
        this.hiddenCards[1] = new SaboteurTile("nugget");
        this.hiddenCards[2] = new SaboteurTile("hidden2");

        int sum=0; //sum of unhidden cards
        //if three hidden objectives are not visible;
        //do nothing
        if(tiles[12][3].getIdx() == "8" && tiles[12][5].getIdx() == "8" &&tiles[12][7].getIdx() == "8") {
            boolean[] temp = {false,false,false};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{  this.player2hiddenRevealed = temp;  }
        }
        //if one nugget is visible or the other two hidden1/2 are visible
        else if(tiles[12][3].getIdx().equals("nugget") || (tiles[12][5].getIdx().equals("hidden1") && tiles[12][7].getIdx().equals("hidden2")) || (tiles[12][7].getIdx().equals("hidden1") && tiles[12][5].getIdx().equals("hidden2")) ) {
            boolean[] temp = {true,true,true};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp;  }
            this.hiddenCards[0] = new SaboteurTile("nugget");
            this.hiddenCards[1] = new SaboteurTile("hidden1");
            this.hiddenCards[2] = new SaboteurTile("hidden2");
            sum = 3;
        }
        else if(tiles[12][5].getIdx().equals("nugget") || (tiles[12][3].getIdx().equals("hidden1") && tiles[12][7].getIdx().equals("hidden2")) || (tiles[12][7].getIdx().equals("hidden1") && tiles[12][3].getIdx().equals("hidden2")) ) {
            boolean[] temp = {true,true,true};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp;  }
            this.hiddenCards[1] = new SaboteurTile("nugget");
            this.hiddenCards[0] = new SaboteurTile("hidden1");
            this.hiddenCards[2] = new SaboteurTile("hidden2");
            sum = 3;
        }
        else if(tiles[12][7].getIdx().equals("nugget") || (tiles[12][5].getIdx().equals("hidden1") && tiles[12][3].getIdx().equals("hidden2")) || (tiles[12][3].getIdx().equals("hidden1") && tiles[12][5].getIdx().equals("hidden2")) ) {
            boolean[] temp = {true,true,true};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp;  }
            this.hiddenCards[2] = new SaboteurTile("nugget");
            this.hiddenCards[0] = new SaboteurTile("hidden1");
            this.hiddenCards[1] = new SaboteurTile("hidden2");
            sum = 3;
        }
        //if one hidden is visible:
        else if (tiles[12][3].getIdx().equals("hidden1") || tiles[12][3].getIdx().equals("hidden2")) {
            boolean[] temp = {true,false,false};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp; }
            sum = 1;
        }
        else if (tiles[12][5].getIdx().equals("hidden1") || tiles[12][5].getIdx().equals("hidden2")) {
            boolean[] temp = {false,true,false};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp; }
            sum = 1;
        }
        else if (tiles[12][7].getIdx().equals("hidden1") || tiles[12][7].getIdx().equals("hidden2")) {
            boolean[] temp = {false,false,true};
            if(this.turnPlayer==1){ this.player1hiddenRevealed = temp; }
            else{ this.player2hiddenRevealed = temp; }
            sum = 1;
        }

        this.board[12][3] =  this.hiddenCards[0];
        this.board[12][5] =  this.hiddenCards[1];
        this.board[12][7] =  this.hiddenCards[2];

        return sum;
    }


    // For sever purpose, used at initialization
    private void initializeFromStringForInitialCopy(String init){
        String[] perBar = init.split("Init:")[1].split("-");
        String[] deckString = perBar[0].split(",");
        for(int i=0;i<this.Deck.size();i++){
            this.Deck.set(i,SaboteurCard.copyACard(deckString[i]));
        }
        String[] player1CardsString = perBar[1].split(",");
        for(int i=0;i<this.player1Cards.size();i++){
            this.player1Cards.set(i,SaboteurCard.copyACard(player1CardsString[i]));
        }
        String[] player2CardsString = perBar[2].split(",");
        for(int i=0;i<this.player2Cards.size();i++){
            this.player2Cards.set(i,SaboteurCard.copyACard(player2CardsString[i]));
        }
        String[] hiddenCardsString = perBar[3].split(",");;
        for(int i=0;i<this.hiddenCards.length;i++){
            this.hiddenCards[i] = (SaboteurTile) SaboteurCard.copyACard(hiddenCardsString[i]);
            this.board[hiddenPos[i][0]][hiddenPos[i][1]] = (SaboteurTile) SaboteurCard.copyACard(hiddenCardsString[i]);
        }
    }


//    public SaboteurTile[][] getBoardForDisplay(){

    public ArrayList<SaboteurCard> getPlayerCardsForDisplay(int playernb){
        if(playernb==turnPlayer){
            if(turnPlayer==1){
                return player1Cards;
            }
            else{
                return player2Cards;
            }
        }
        else{
            if(turnPlayer==1){
                ArrayList<SaboteurCard> p1hidden = new ArrayList<>();
                for (SaboteurCard c : this.player1Cards){
                    p1hidden.add(new SaboteurTile("goalTile"));
                }
                return p1hidden;
            }
            else{
                ArrayList<SaboteurCard> p2hidden = new ArrayList<>();
                for (SaboteurCard c : this.player2Cards){
                    p2hidden.add(new SaboteurTile("goalTile"));
                }
                return p2hidden;
            }
        }
    }

    public ArrayList<SaboteurCard> getCurrentPlayerCards(){
        if(turnPlayer==1){
            ArrayList<SaboteurCard> p1Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player1Cards.size();i++){
                p1Cards.add(i,SaboteurCard.copyACard(this.player1Cards.get(i).getName()));
            }
            return p1Cards;
        }
        else{
            ArrayList<SaboteurCard> p2Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player2Cards.size();i++){
                p2Cards.add(i,SaboteurCard.copyACard(this.player2Cards.get(i).getName()));
            }
            return p2Cards;
        }
    }

    //helper method returning hidden cards status from current turnplayer:
    public boolean[] thisPlayerHiddenStatus(){
        boolean[] hidden = {false,false,false};
        if(turnPlayer==1){
            System.arraycopy(this.player1hiddenRevealed, 0, hidden, 0, 3);
        }
        else{
            System.arraycopy(this.player2hiddenRevealed, 0, hidden, 0, 3);
        }
        return hidden;
    }

    private int[][] getIntBoard() {
        //update the int board.
        //Note that this tool is not available to the player.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    int[][] path = this.board[i][j].getPath();
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                        }
                    }
                }
            }
        }

        return this.intBoard; }

    
    public int[][] getHiddenIntBoard() {
        //update the int board, and provide it to the player with the hidden objectives set at EMPTY.
        //Note that this function is available to the player.
        //Corrected version.
        boolean[] listHiddenRevealed;
        if(turnPlayer==1) listHiddenRevealed= player1hiddenRevealed;
        else listHiddenRevealed = player2hiddenRevealed;
        int[][] playerIntBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                        	playerIntBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    boolean isAnHiddenObjective = false;
                    for(int h=0;h<3;h++) {
                        if(this.board[i][j].getIdx().equals(this.hiddenCards[h].getIdx())){
                            if(!listHiddenRevealed[h]){
                                isAnHiddenObjective = true;
                            }
                            break;
                        }
                    }
                    if(!isAnHiddenObjective) {
                        int[][] path = this.board[i][j].getPath();
                        for (int k = 0; k < 3; k++) {
                            for (int h = 0; h < 3; h++) {
                                playerIntBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                            }
                        }
                    }
                    else {
                    	int[][] path = this.board[i][j].getPath();
                        for (int k = 0; k < 3; k++) {
                            for (int h = 0; h < 3; h++) {
                                playerIntBoard[i * 3 + k][j * 3 + h] = EMPTY;
                            }
                        }
                    }
                }
            }
        }
        return playerIntBoard; }

    public SaboteurTile[][] getHiddenBoard(){
        // returns the board in SaboteurTile format, where the objectives become the 8 tiles.
        // Note the inconsistency with the getHiddenIntBoard where the objectives become only -1
        // this is to stress that hidden cards are considered as empty cards which you can't either destroy or build on before they
        // are revealed.
        SaboteurTile[][] hiddenboard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(this.board[i], 0, hiddenboard[i], 0, BOARD_SIZE);
        }
        for(int h=0;h<3;h++){
            if(turnPlayer==1 && !player1hiddenRevealed[h] || turnPlayer==0 && !player2hiddenRevealed[h]){
                hiddenboard[hiddenPos[h][0]][hiddenPos[h][1]] = new SaboteurTile("8");
            }
        }
        return hiddenboard;
    }

    @Override
    public Object clone() {
        // You are not allowed to use this method for your agent.
        return new student_player.SaboteurBoardStateCopy(this);
    }
    @Override
    public int getWinner() { return winner; }
    @Override
    public void setWinner(int win) { winner = win; }
    @Override
    public int getTurnPlayer() {
        if(turnNumber==0){ //at first, the board is playing
            return Board.BOARD;
        }
        return turnPlayer;
    }
    @Override
    public int getTurnNumber() { return turnNumber; }
    @Override
    public boolean isInitialized() { return board != null; }
    @Override
    public int firstPlayer() { return FIRST_PLAYER; }
    @Override
    public SaboteurMove getRandomMove() {
        ArrayList<SaboteurMove> moves = getAllLegalMoves();
        return moves.get(rand.nextInt(moves.size()));
    }

    public int getNbMalus(int playerNb){
        if(playerNb==1) return this.player1nbMalus;
        return this.player2nbMalus;
    }

    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (!hiddenRevealed[i]){
                objHiddenList.add(this.board[hiddenPos[i][0]][hiddenPos[i][1]]);
            }
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = this.board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
        ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
    }

    public ArrayList<SaboteurMove> getAllLegalMoves() {
        // Given the current player hand, gives back all legal moves he can play.
        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;
        }
        else {
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;
        }

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1],turnPlayer));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1],turnPlayer));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(turnPlayer ==1){
                    if(player1nbMalus > 0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
                }
                else if(player2nbMalus>0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMap){
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.hiddenRevealed[i]) legalMoves.add(new SaboteurMove(card,hiddenPos[i][0],hiddenPos[i][1],turnPlayer));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=originPos || j!= originPos) && (i != hiddenPos[0][0] || j!=hiddenPos[0][1] )
                                && (i != hiddenPos[1][0] || j!=hiddenPos[1][1] ) && (i != hiddenPos[2][0] || j!=hiddenPos[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j,turnPlayer));
                        }
                    }
                }
            }
        }
        // we can also drop any of the card in our hand
        for(int i=0;i<hand.size();i++) {
            legalMoves.add(new SaboteurMove(new SaboteurDrop(), i, 0, turnPlayer));
        }
        return legalMoves;
    }

    public boolean isLegal(SaboteurMove m) {
        // For a move to be legal, the player must have the card in its hand
        // and then the game rules apply.
        // Note that we do not test the flipped version. To test it: use the flipped card in the SaboteurMove object.

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();
        int currentPlayer = m.getPlayerID();

        if (currentPlayer != turnPlayer) return false;

        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;

        }
        else {
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;

        }
        if(testCard instanceof SaboteurDrop){
            if(hand.size()>=pos[0]){
                return true;
            }
        }

        boolean legal = false;
        for(SaboteurCard card : hand){
            if (card instanceof SaboteurTile && testCard instanceof SaboteurTile && !isBlocked) {
                if(((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())){

                    return verifyLegit(((SaboteurTile) card).getPath(),pos);
                }
                else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())){

                    return verifyLegit(((SaboteurTile) card).getFlipped().getPath(),pos);
                }
            }
            else if (card instanceof SaboteurBonus && testCard instanceof SaboteurBonus) {
                if (turnPlayer == 1) {
                    if (player1nbMalus > 0) return true;
                } else if (player2nbMalus > 0) return true;

                return false;

            }
            else if (card instanceof SaboteurMalus && testCard instanceof SaboteurMalus ) {

                return true;

            }
            else if (card instanceof SaboteurMap && testCard instanceof SaboteurMap) {
                int ph = 0;
                for(int j=0;j<3;j++) {
                    if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                }
                if (!this.hiddenRevealed[ph])

                    return true;
            }
            else if (card instanceof SaboteurDestroy && testCard instanceof SaboteurDestroy) {
                int i = pos[0];
                int j = pos[1];
                if (this.board[i][j] != null && (i != originPos || j != originPos) && (i != hiddenPos[0][0] || j != hiddenPos[0][1])
                        && (i != hiddenPos[1][0] || j != hiddenPos[1][1]) && (i != hiddenPos[2][0] || j != hiddenPos[2][1])) {
                    return true;
                }

            }
        }
        return legal;
    }

    private void draw(){
        if(this.Deck.size()>0){
            if(turnPlayer==1){
                this.player1Cards.add(this.Deck.remove(0));
            }
            else{
                this.player2Cards.add(this.Deck.remove(0));
            }
        }
    }

    public void processMove(SaboteurMove m) throws IllegalArgumentException {

        if(m.getFromBoard()){
            this.initializeFromStringForInitialCopy(m.getBoardInit());
            System.out.println("inititalized"+this.hashCode());
            turnNumber++;
            return;
        }

        // Verify that a move is legal (if not throw an IllegalArgumentException)
        // And then execute the move.
        // Concerning the map observation, the player then has to check by himself the result of its observation.
        //Note: this method is ran in a BoardState ran by the server as well as in a BoardState ran by the player.
        if (!isLegal(m)) {
//            System.out.println("Found an invalid Move for player " + this.turnPlayer+" of board"+ this.hashCode());
//            ArrayList<SaboteurCard> hand = this.turnPlayer==1? this.player1Cards : this.player2Cards;
//            System.out.println("in hand:");
//            for(SaboteurCard card : hand) {
//                if (card instanceof SaboteurTile){
//                    System.out.println(card.getName());
//                }
//                else{
//                    System.out.println(card.getName());
//                }
//            }
            throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString());
        }

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();

        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            if(turnPlayer==1){
                //Remove from the player card the card that was used.
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
            else {
                for (SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurBonus){
            if(turnPlayer==1){
                player1nbMalus --;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player2nbMalus --;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMalus){
            if(turnPlayer==1){
                player2nbMalus ++;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player1nbMalus ++;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player1Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player1hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player2Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player2hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player1Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player2Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurDrop){
            if(turnPlayer==1) this.player1Cards.remove(pos[0]);
            else this.player2Cards.remove(pos[0]);
        }
        this.draw();
        this.updateWinner();
        turnPlayer = 1 - turnPlayer; // Swap player
        turnNumber++;
    }

    private boolean containsIntArray(ArrayList<int[]> a,int[] o){ //the .equals used in Arraylist.contains is not working between arrays..
        if (o == null) {
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i) == null)
                    return true;
            }
        } else {
            for (int i = 0; i < a.size(); i++) {
                if (Arrays.equals(o, a.get(i)))
                    return true;
            }
        }
        return false;
    }

    private Boolean cardPath(ArrayList<int[]> originTargets,int[] targetPos,Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);
        if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE,usingCard);
        else addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE*3,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(containsIntArray(originTargets,visitingPos)){
                return true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE,usingCard);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE*3,usingCard);
//            System.out.println(queue.size());
        }
        return false;
    }
    private void addUnvisitedNeighborToQueue(int[] pos,ArrayList<int[]> queue, ArrayList<int[]> visited,int maxSize,boolean usingCard){
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!containsIntArray(visited,neighborPos)){
                    if(usingCard && this.board[neighborPos[0]][neighborPos[1]]!=null) queue.add(neighborPos);
                    else if(!usingCard && this.intBoard[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
                }
            }
        }
    }
    private boolean pathToHidden(SaboteurTile[] objectives){
        /* This function look if a path is linking the starting point to the states among objectives.
            :return: if there exists one: true
                     if not: false
                     In Addition it changes each reached states hidden variable to true:  self.hidden[foundState] <- true
            Implementation details:
            For each hidden objectives:
                We verify there is a path of cards between the start and the hidden objectives.
                    If there is one, we do the same but with the 0-1s matrix!

            To verify a path, we use a simple search algorithm where we propagate a front of visited neighbor.
               TODO To speed up: The neighbor are added ranked on their distance to the origin... (simply use a PriorityQueue with a Comparator)
        */
        this.getIntBoard(); //update the int board.
        boolean atLeastOnefound = false;
        for(SaboteurTile target : objectives){
            ArrayList<int[]> originTargets = new ArrayList<>();
            originTargets.add(new int[]{originPos,originPos}); //the starting points
            //get the target position
            int[] targetPos = {0,0};
            int currentTargetIdx = -1;
            for(int i =0;i<3;i++){
                if(this.hiddenCards[i].getIdx().equals(target.getIdx())){
                    targetPos = SaboteurBoardState.hiddenPos[i];
                    currentTargetIdx = i;
                    break;
                }
            }
            if(!this.hiddenRevealed[currentTargetIdx]) {  //verify that the current target has not been already discovered. Even if there is a destruction event, the target keeps being revealed!

                if (cardPath(originTargets, targetPos, true)) { //checks that there is a cardPath
//                    System.out.println("card path found"); //todo remove
//                    this.printBoard();
                    //next: checks that there is a path of ones.
                    ArrayList<int[]> originTargets2 = new ArrayList<>();
                    //the starting points
                    originTargets2.add(new int[]{originPos*3+1, originPos*3+1});
                    originTargets2.add(new int[]{originPos*3+1, originPos*3+2});
                    originTargets2.add(new int[]{originPos*3+1, originPos*3});
                    originTargets2.add(new int[]{originPos*3, originPos*3+1});
                    originTargets2.add(new int[]{originPos*3+2, originPos*3+1});
                    //get the target position in 0-1 coordinate
                    int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
                    if (cardPath(originTargets2, targetPos2, false)) {
//                        System.out.println("0-1 path found");

                        this.hiddenRevealed[currentTargetIdx] = true;
                        this.player1hiddenRevealed[currentTargetIdx] = true;
                        this.player2hiddenRevealed[currentTargetIdx] = true;
                        atLeastOnefound =true;
                    }
                    else{
//                        System.out.println("0-1 path was not found");
                    }
                }
            }
            else{
                System.out.println("hidden already revealed");
                atLeastOnefound = true;
            }
        }
        return atLeastOnefound;
    }

    private void updateWinner() {

        pathToHidden(new SaboteurTile[]{new SaboteurTile("nugget"),new SaboteurTile("hidden1"),new SaboteurTile("hidden2")});
        int nuggetIdx = -1;
        for(int i =0;i<3;i++){
            if(this.hiddenCards[i].getIdx().equals("nugget")){
                nuggetIdx = i;
                break;
            }
        }
        boolean playerWin = this.hiddenRevealed[nuggetIdx];
        if (playerWin) { // Current player has won
            winner = turnPlayer;
        } else if (gameOver() && winner==Board.NOBODY) {
            winner = Board.DRAW;
        }

    }

    public void printBothHands(){
        System.out.println();
        System.out.println("Player1 Hand:");
        for(SaboteurCard b:this.player1Cards){
            System.out.print(b.getName());
            System.out.print("  ");
        }
        System.out.println();
        System.out.println("Opponent Hand:");
        for(SaboteurCard a:this.player2Cards){
            System.out.print(a.getName());
            System.out.print("  ");
        }
        System.out.println();

    }

    public void printDeck(){
        System.out.println("Current Deck(size + cards):");
        System.out.println(this.Deck.size());
        for(SaboteurCard a:this.Deck){
            System.out.print(a.getName());
            System.out.print("  ");
        }
        System.out.println();
    }

    public void printHiddenStatus(){
        if(turnPlayer==1){
            for(int i=0;i<3;i++){
                System.out.print(this.player1hiddenRevealed[i]);
            }
        }
        System.out.println();

    }


    @Override
    public boolean gameOver() {
        return this.Deck.size()==0 && this.player1Cards.size()==0 && this.player2Cards.size()==0 || winner != Board.NOBODY;
    }

    public void printBoard() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        this.getIntBoard();
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                boardString.append(intBoard[i][j]);
                boardString.append(",");
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

    public static void main(String[] args) {



    }
}
