package TTT;

import java.util.*;


public class TicTacToe
{
    private String[][] board; 
    static String X = "X";
    static String O = "O";    
   
    
    public TicTacToe()
    {
        // initialize instance variables
        board = new String[3][3]; 
    }

    public synchronized void printBoard()
    {
        System.out.println();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    System.out.print("_");
                } else {
                    System.out.print(board[i][j]);
                }
                if (j < 2) {
                    System.out.print("|");
                } else {
                    System.out.println();
                } 
             }
         }
         System.out.println("\n");
    }
    /* Check if player wins.  Check right after X makes a play 
     * 
     */
    public synchronized Boolean checkWinner(String play) {
        int playInRow = 0;
        int playD1 = 0;
        int playD2 = 0;
        int[] playInColumn = new int[board[0].length];   // assumes square board
        for (int i = 0; i < board.length; i++) {
            playInRow = 0;
            for (int j = 0; j < board[i].length; j++) {
                if (null == board[i][j]) {
                    continue;
                }
                if (board[i][j].equals(play)) {
                    playInRow++;
                    playInColumn[j]++;
                    if (i == j) {
                        playD1++;
                    } else if (2 == i + j) {
                        playD2++;
                    }
                }
                
            }
            if (playInRow == 3) {
                return true;
            }
        }
        if (3 == playD1 || 3 == playD2) {
            return true;
        }
        for (int i = 0; i < playInColumn.length; i++) {
            if (playInColumn[i] == 3) {
                return true;
            }
        }
        return false;    
    }
    /* 
     * makeMove gets a legal coordinate for the move that is not occupied
     *    and marks it with the play string
     */
    public synchronized int[] makeMove(Scanner stdin, String play) {
        int r;
        int c;
        int[] a = null;
        Boolean goodInput = false;
        while(!goodInput) {
            r = -1;
            c = -1;
            System.out.println ("Enter coordinates to play your " + play);
            if (stdin.hasNextInt()) {  // must be integers
                r = stdin.nextInt();
            }
            if (stdin.hasNextInt()) {
                c = stdin.nextInt();
            }
            else {
                stdin.nextLine();      // consume a line without an integer
                System.out.println("Both inputs must be integers between 0 and 2.");
                continue;
            }
            // must be in the right coordinate range
            if ((r < 0) || (r > 2) || (c < 0) || (c > 2)) {
                System.out.println("Both inputs must be integers between 0 and 2.");
                continue;                
            } 
            // make sure the space is not occupied
            else if (board[r][c] != null ){  
                System.out.println("That location is occupied");
                continue; 
            }
            else {
                board[r][c] = play;
                a = new int[2];
                a[0]=r;
                a[1]=c;
                return a;
            }
        }
        return a;
    }
    
    public synchronized void addPos(int[] a, String play){
    	int x= a[0];
    	int y=a[1];
    	
    	if(board[x][y] ==null) board[x][y]= play;
    	else System.out.println("Error going back board!");
    }
    
   /* public static void main(String[] args) {
        
        TicTacToe ttt = new TicTacToe();            // allocate a board
        Scanner stdin = new Scanner(System.in);     // read from standard in
              
        int moves = 0;
        System.out.println("Let's play TicTacToe -- X goes first");
        ttt.printBoard();
        while (moves < 9) {
            ttt.makeMove(stdin, X);
            moves++;
            if (moves > 4) {
                if (ttt.checkWinner(X)) {
                   System.out.println(X + " You Win!!!");
                   break;
                }
            }
            ttt.printBoard();
            ttt.makeMove(stdin, O);
            moves++;
            if (moves > 4) {
                if (ttt.checkWinner(O)) {
                   System.out.println(O + " You Win!!!");
                   break;
                }
            }
            ttt.printBoard();
            
        }
    }*/
}
