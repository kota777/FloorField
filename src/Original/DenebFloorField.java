/*
 * デネブホールメモ
 * 全601席
 * 1セット5席
 * 横:5セット
 * 縦:25列
 * 出入り口4つ
 * 扉は2人スペース
 * 
 * セット間は1人スペース
 * 壁際は2人スペース
 * 9列ごとに2人スペースの通路
 */

package Original;
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
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class DenebFloorField extends JFrame implements ActionListener
{
	/* ２次元セル */
	final static int xCell	 =  33;
	final static int yCell	 =  62;
	/* セルにエージェントが存在するかしないか */
	static int [][] cell = new int [yCell][xCell];
	/* セルの描画サイズ */
	static double dx,dy;
	
	/* 描画のためのパラメータ */	
	static double frameWidth   = xCell * 8;
	static double frameHeight  = yCell * 8;
	final static double topMargin	=  25.0;
	final static double bottomMargin =  25.0;
	final static double sideMargin   =  25.0;
	final static Color bgcolor = Color.white;
	final static Color fgcolor = new Color(100,0,20);
	final static BasicStroke normalStroke	 = new BasicStroke(1.0f);
	final static BasicStroke thinStroke	 = new BasicStroke(0.1f);

	/* フィールドのサイズ */
	static double height,width;

	/* エージェント数 */
	static int nAgent = 100;
	/* エージェントX座標 */
	static int [] agentX = new int [nAgent];
	/* エージェントY座標 */
	static int [] agentY = new int [nAgent];
	/* エージェントの脱出判定 */
	static Boolean [] agentLive = new Boolean [nAgent];

	/* 出口の設定 */
	static int exitX[] = {(int)(xCell * 0.33), (int)(xCell * 0.66), 0, xCell-1};
	static int exitY[] = {0, 0, (int)(yCell * 0.9), (int)(yCell * 0.9)};

	static Random rand = new Random();

	/* 出口に近付きやすくなる係数 */
	static double ks = 1.0;
	/* コマ送りの間隔 */
	static int delay = 10;

	Timer timer;
	AnimationPane animationPane;

	/** エージェントのランダム配置
	*/
	void initCells(){
		for(int i=0;i<nAgent;i++){
			int x = rand.nextInt(xCell);
			int y = rand.nextInt(yCell);
			agentX[i] = x;
			agentY[i] = y;
			agentLive[i] = true;
			cell[y][x] = 1;
		}
	}

	/** 次のステップの計算
	*@param g2 描画用グラフィックス
	*/
	void goNextStep(Graphics2D g2){
		int i,x,y;
		double qq,qr,ql,qu,qd,
			pr,pl,pu,pd,psum,r,
			px,py;
		double minQq;
		int nearlyExit = 0;
		Boolean moved = false;

		for(i=0;i<nAgent;i++){
			if(agentLive[i]){
				x = agentX[i];
				y = agentY[i];
				minQq = Double.MAX_VALUE;
				for (int j = 0; j < exitX.length; j++) {
					qq = Math.sqrt((x-exitX[j])*(x-exitX[j]) + (y-exitY[j])*(y-exitY[j]));	
					if(qq < minQq){
						minQq = qq;
						nearlyExit = j;
					}
				}
				
				qr = Math.sqrt((x-exitX[nearlyExit]+1)*(x-exitX[nearlyExit]+1) + (y-exitY[nearlyExit]  )*(y-exitY[nearlyExit]  ));
				qd = Math.sqrt((x-exitX[nearlyExit]  )*(x-exitX[nearlyExit]  ) + (y-exitY[nearlyExit]+1)*(y-exitY[nearlyExit]+1));
				ql = Math.sqrt((x-exitX[nearlyExit]-1)*(x-exitX[nearlyExit]-1) + (y-exitY[nearlyExit]  )*(y-exitY[nearlyExit]  ));
				qu = Math.sqrt((x-exitX[nearlyExit]  )*(x-exitX[nearlyExit]  ) + (y-exitY[nearlyExit]-1)*(y-exitY[nearlyExit]-1));
	
				pr = Math.exp(ks*(minQq-qr));
				pd = Math.exp(ks*(minQq-qd));
				pl = Math.exp(ks*(minQq-ql));
				pu = Math.exp(ks*(minQq-qu));
	
				psum = pr + pl + pu + pd;
				pr /= psum;
				pl /= psum;
				pu /= psum;
				pd /= psum;
	
				r = rand.nextDouble();
				if (r < pr){
					if(x < xCell-1 && cell[y][x+1] == 0){
						agentX[i] ++;
						moved = true;
					}
				}
				else if (r < pr + pd){
					if(y < yCell - 1 && cell[y+1][x] == 0){
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
					g2.setPaint(Color.white);
					px = sideMargin + x * dx;
					py = topMargin  + y * dy;
					g2.fill(new Rectangle2D.Double(px,py,dy,dx));
					g2.setPaint(Color.red);
					px = sideMargin + agentX[i] * dx;
					py = topMargin  + agentY[i] * dy;
					g2.fill(new Rectangle2D.Double(px,py,dy,dx));
					if(agentX[i] == exitX[nearlyExit] && agentY[i] == exitY[nearlyExit]){
						g2.setPaint(Color.blue);
						g2.fill(new Rectangle2D.Double(px,py,dy,dx));
						agentLive[i] = false;
						cell[exitY[nearlyExit]][exitX[nearlyExit]] = 0;
					}
				}
			}
		}
	}

	/** 初期値の設定
	*@param container フレーム
	*/
	void buildUI(Container container){
		timer = new Timer(delay, this);
		timer.setInitialDelay(0);
		timer.setCoalesce(true);
		animationPane = new AnimationPane();
		container.add(animationPane, BorderLayout.CENTER);
		Dimension d = getSize();
		frameWidth  = d.width;
		frameHeight = d.height;
		width =  frameWidth - 2 * sideMargin;
		height = frameHeight - (topMargin + bottomMargin);
		dx = width / xCell;
		dy = height / yCell;
	}

	/**
	* イベントが発生したときに実行されるメソッド
	* アニメーションの１コマに相当
	*/
	public void actionPerformed(ActionEvent e){
		animationPane.repaint();
	}

	/**
	* タイマーにより一定の時間間隔ごとに呼び出される
	*/
	class AnimationPane extends JPanel{
		// Draw the current frame of animation.
		public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(thinStroke);
			g2.setPaint(fgcolor);
			// 枠の矩形を描画する
			g2.draw(new Rectangle2D.Double(sideMargin,topMargin,width,height));
			goNextStep(g2);
			g2.setPaint(Color.green);
			for (int i = 0; i < exitX.length; i++) {
				g2.fill(new Rectangle2D.Double(sideMargin + exitX[i] * dx,
						topMargin  + exitY[i] * dy, dx, dy));				
			}

		}
	}

	public static void main(String argv[]) {

		/* フレームの宣言 */
		final DenebFloorField controller = new DenebFloorField();

		/* フレームのサイズ宣言 */
		controller.setSize(new Dimension((int)frameWidth,
			(int)frameHeight));
		controller.setBackground(bgcolor);
		controller.setForeground(fgcolor);

		/* パラメータ設定 */
		controller.buildUI(controller.getContentPane());

		/* フレームの可視化 */
		controller.setVisible(true);

		/* アプリケーションのとき，コマンドライン引数から密度を入力 */
		//ks = Double.parseDouble(argv[0]);

		/* セルにエージェントを配置 */
		controller.initCells();

		/* タイマーの開始 */
		controller.timer.start();
	}

}