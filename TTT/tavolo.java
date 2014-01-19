package TTT;

public class tavolo {
	
	private String[][] board;
	private String id;
	private Player Player1;
	private Player Player2;
	
	
	public tavolo(String id){
		this.id =id;
		board = new String[3][3];
	}
	
	public void setPlayer1(Player a){
		if (Player1.equals(null))this.Player1 = a;
	}
	
	public void setPlayer2(Player a){
		if (Player2.equals(null))this.Player2 = a;
	}
	
	public boolean isEmpty(int i, int j){
		boolean c =false;
		if(!board[i][j].equals("X") || !board[i][j].equals("O")) c= true;
		else c = false;
		return c;
	}
	
	public boolean isSet(int i, int j){
		boolean c = false;
		if (board[i][j].equals("X") || board[i][j].equals("O")) c=true;
		else c=false;
		return c;
	}
	
	public String getCasella(int i, int j){
		
		return board[i][j];
	}
	
	public String getId(){
		
		return id;
	}

}
