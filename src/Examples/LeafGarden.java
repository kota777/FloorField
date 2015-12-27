package Examples;

/*
出口から脱出できるモデル
今度は出口に達したエージェントが消えていく，つまりどこか外に出ていくという モデルに改良してみよう。

このモデルで渋滞が 引き起こされるのはどんな状況でだろうか？
パラメータを変化させたり， ソースの中に書かれているエージェントの数を変更したりしてみると，
それなり に面白い結果がえられるかも知れない。
*/
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LeafGarden extends JFrame{
	private static final long serialVersionUID = 8250582189314558027L;
	// 描画のためのパラメータ
	private double frameWidth   = 680.0;
	private double frameHeight  = 680.0;
	private final double topMargin	=  50.0;
	private final double bottomMargin =  50.0;
	private final double sideMargin   =  50.0;

	private final Color bgcolor = Color.white;
	private final Color fgcolor = new Color(100,0,200);
	private final BasicStroke thinStroke	 = new BasicStroke(0.1f);
	private double height,width;

	// エージェントの情報
	private int nAgent = 500;
	private int [] agentX = new int [nAgent];
	private int [] agentY = new int [nAgent];
	private Boolean [] agentLive = new Boolean [nAgent];

	// ２次元セル
	private int[][] cell;
	private double dx,dy;

	// 出口の設定
	private final int exitNum = 10;
	private int[][] exits = new int[exitNum][2];
	// 各出口の待ち時間
	private int[] wait = new int[exitNum];
	// 出口の１人当りの使用時間
	private final int WAIT_TIME = 20;
	// 入り口の設定
	private int[][] start = new int[2][2];

	private final double ks = 1.0; // 近づく傾向の大きさ
	// コマ送りの間隔
	private final int delay = 70;

	private AnimationPane animationPane;
	public LeafGarden(){
		super( "Floor Field Model of LeafGarden" );
		// セルの初期配置の読み込み
		this.loadMap();

		this.setSize( (int)this.frameWidth, (int)this.frameHeight );
		this.setBackground( this.bgcolor );
		this.setForeground( this.fgcolor );
		this.buildUI( this.getContentPane() );
		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
		this.initCells();
		this.setVisible( true );
	}
	// マップの初期設定を読み込む
	public void loadMap(){
		List<String[]> temp = new ArrayList<String[]>();
		try {
			BufferedReader br = new BufferedReader( new FileReader( new File( "map.csv" ) ) );
			String line;
			// バッファから読み込み
			while( (line = br.readLine()) != null ){
				temp.add( line.split( "," ) );
			}
			int size = temp.size();
			this.cell = new int[size][size];
			// int型に変換しつつ配列に変換
			for( int i = 0; i < cell.length; i++ ){
				for( int j = 0; j < cell.length; j++ ){
					this.cell[i][j] = Integer.parseInt( (temp.get(i))[j] );
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// セルに nAgent 人を配置する
	public void initCells(){
		// スタート位置の初期設定(範囲)
		start[0][0] = 70; start[0][1] = 70;
		start[1][0] = 79; start[1][1] = 79;
		// 出口位置の初期設定(上)
		exits[1][0] = 79; exits[1][1] = 0;
		exits[2][0] = 74; exits[2][1] = 0;
		// 出口位置の初期設定(左)
		exits[0][0] = 0; exits[0][1] = 79;
		exits[3][0] = 0; exits[3][1] = 65;
		exits[4][0] = 2; exits[4][1] = 79;
		exits[5][0] = 2; exits[5][1] = 65;
		exits[6][0] = 4; exits[6][1] = 79;
		exits[7][0] = 4; exits[7][1] = 65;
		// 出口位置の初期設定(その他)
		exits[8][0] = 50; exits[8][1] = 69;
		exits[9][0] = 6; exits[9][1] = 35;
		for(int i=0;i<nAgent;i++){
			int x = (int)(Math.random() *  (start[1][0] - start[0][0]) ) + start[0][0];
			int y = (int)(Math.random() *  (start[1][1] - start[0][1]) ) + start[0][1];
			agentX[i] = x;
			agentY[i] = y;
			agentLive[i] = true;
			cell[y][x] = 1;
		}
	}

	// 次のステップの計算
	public void goNextStep(Graphics2D g2){
		int i,x,y;
		double qq,qr,ql,qu,qd,
			pr,pl,pu,pd,psum,r,
			px,py;
		Boolean moved = false;
		//System.out.println("goNextStep");
		for(i=0;i<nAgent;i++){
			double min = Math.sqrt( this.cell.length*this.cell.length + this.cell.length*this.cell.length );
			int ek = 0;
			for( int k = 0; k < exits.length; k++ ){
				// エージェントにおける最短距離の出口の算出
				double dis = Math.sqrt( Math.pow( exits[k][0] - agentX[i], 2 ) + Math.pow( exits[k][1] - agentY[i], 2 ) );
				// 最短距離を求める。ただし、満席の出口は考慮しない
				if( min > dis && wait[k] <= 0 ){
					min = dis;
					ek = k;
				}
			}
			if(agentLive[i]){
				x = agentX[i];
				y = agentY[i];
				qq = Math.sqrt((x-exits[ek][0])*(x-exits[ek][0]) + (y-exits[ek][1])*(y-exits[ek][1]));

				qr = Math.sqrt((x-exits[ek][0]+1)*(x-exits[ek][0]+1) + (y-exits[ek][1]  )*(y-exits[ek][1]  ));
				qd = Math.sqrt((x-exits[ek][0]  )*(x-exits[ek][0]  ) + (y-exits[ek][1]+1)*(y-exits[ek][1]+1));
				ql = Math.sqrt((x-exits[ek][0]-1)*(x-exits[ek][0]-1) + (y-exits[ek][1]  )*(y-exits[ek][1]  ));
				qu = Math.sqrt((x-exits[ek][0]  )*(x-exits[ek][0]  ) + (y-exits[ek][1]-1)*(y-exits[ek][1]-1));

				pr = Math.exp(ks*(qq-qr));
				pd = Math.exp(ks*(qq-qd));
				pl = Math.exp(ks*(qq-ql));
				pu = Math.exp(ks*(qq-qu));

				psum = pr + pl + pu + pd;
				pr /= psum;
				pl /= psum;
				pu /= psum;
				pd /= psum;

				r = Math.random();
				if (r < pr){
					if(x < this.cell.length - 1 && cell[y][x+1] == 0){
					agentX[i] ++;
					moved = true;
					}
				}
				else if (r < pr + pd){
					if(y < this.cell.length - 1 && cell[y+1][x] == 0){
					agentY[i] ++;
					moved = true;
					}
				}
				else if (r < pr + pd + pl){
					if(x > 0 && cell[y][x-1] == 0){
					agentX[i] --;
					moved = true;
					}
				}
				else{
					if(y > 0 && cell[y-1][x] == 0){
					agentY[i] --;
					moved = true;
					}
				}
				if(moved){
					cell[y][x] = 0;
					cell[agentY[i]][agentX[i]] = 1;
					g2.setPaint(Color.pink);
					px = sideMargin + x * dx;
					py = topMargin  + y * dy;
					g2.fill(new Rectangle2D.Double(px,py,dy,dx));


					// エージェントの描画
					g2.setPaint(Color.red);
					px = sideMargin + agentX[i] * dx;
					py = topMargin  + agentY[i] * dy;
					g2.fill(new Rectangle2D.Double(px,py,dy,dx));
					// エージェントが出口に辿り着いたときの処理
					if(agentX[i] == exits[ek][0] && agentY[i] == exits[ek][1]){
						//g2.setPaint(Color.blue);
						//g2.fill(new Rectangle2D.Double(px,py,dy,dx));

						agentLive[i] = false;
						// 待ち時間をセット
						wait[ek] = WAIT_TIME;
						// 他のエージェントが出口に入れないようにする
						cell[exits[ek][1]][exits[ek][0]] = 3;
					}
				}
			}
		}



	}

	// 動かないパラメータの設定はここで行う
	public void buildUI(Container container){
		animationPane = new AnimationPane();
		container.add(animationPane, BorderLayout.CENTER);
		Dimension d = getSize();
		frameWidth  = d.width;
		frameHeight = d.height;
		width =  frameWidth - 2 * sideMargin;
		height = frameHeight - (topMargin + bottomMargin);
		dx = width / this.cell.length;
		dy = height / this.cell.length;
	}



	public class AnimationPane extends JPanel implements ActionListener{
		private static final long serialVersionUID = -1950795353433541909L;
        private Timer timer;

		public AnimationPane(){
			timer = new Timer(delay, this);
			timer.setInitialDelay(0);
			timer.setCoalesce(true);
			timer.start();
		}

		// 以下がアニメーションの１コマに相当：ここで描画処理など。
		public void actionPerformed(ActionEvent e){
			this.repaint();
		}

		// Draw the current frame of animation.
		public void paintComponent(Graphics g){
			double fx,fy;

			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(thinStroke);
			g2.setPaint(fgcolor);
			// 枠の矩形を描画する
			g2.draw(new Rectangle2D.Double(sideMargin,topMargin,width,height));

			// 出口の満席空席判定
			for( int i = 0; i < wait.length; i++ ){

				// 待ち時間を減らす
				wait[i]--;
				// 満席の場合の描画
				if( wait[i] > 0 && wait[i] != WAIT_TIME){
					g2.setPaint(Color.magenta);
					g2.fill(new Rectangle2D.Double(sideMargin + exits[i][0] * dx, topMargin  + exits[i][1] * dy,dx, dy));
				}
				// 満席になったばかりの描画
				else if( wait[i] == WAIT_TIME ){
					// 待ち時間を減らす
					//wait[i]--;
					g2.setPaint(Color.cyan);
					g2.fill(new Rectangle2D.Double(sideMargin + exits[i][0] * dx, topMargin  + exits[i][1] * dy,dx, dy));
				}
				// 空席の場合の描画
				else{
					// 出口のマスにエージェントが移動できるようにする
					cell[exits[i][1]][exits[i][0]] = 0;
					g2.setPaint(Color.green);
					g2.fill(new Rectangle2D.Double(sideMargin + exits[i][0] * dx, topMargin  + exits[i][1] * dy,dx, dy));
				}
			}
			goNextStep(g2);
			for ( int i = 0; i < cell.length; i++){
				for (int j = 0; j < cell.length; j++){
					// 壁の塗りつぶし
					if( cell[i][j]==2){
						g2.setPaint(Color.black);
						fx = sideMargin + i * dx;
						fy = topMargin  + j * dy;
						g2.fill(new Rectangle2D.Double(fy,fx,dy,dx));
					}
				}
			}
		}
	}

	public static void main(String argv[]) {
		new LeafGarden();
	}
}