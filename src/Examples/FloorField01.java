/*
フロアフィールドモデル(Floor-Field Model) というのは，
文字通り床の上という場を人が動き回るような交通モデルである．
ここでは，部屋の壁の中央に出口があってそこへ人が逃げていくような，
脱出の動きのシミュレーションを行う．

赤で示されたエージェントは， 緑の出口に向かってランダムさをもちながら，
ふらふらと進む。 この仕組みは，４本の矢印の長さに比例した確率で
上下左右への進路を選択していく．

プリミティブなモデル
最初に登場するのは，エージェントが出口に引力のようなもので引き付けられるだけのモデル．
この場合，エージェント同士は同じ位置に重なって立つことができるので，
最終的に出口のところにみんなが立つことになる．

Timerの使い方
http://allabout.co.jp/gm/gc/80603/2/

*/

package Examples;
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

public class FloorField01 extends JFrame implements ActionListener
{

	/* 描画のためのパラメータ */
	static double frameWidth   = 680.0;
	static double frameHeight  = 680.0;
	final static double topMargin	=  50.0;
	final static double bottomMargin =  50.0;
	final static double sideMargin   =  50.0;
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

	/* ２次元セル */
	final static int nCell	 =  80;
	/* セルにエージェントが存在するかしないか */
	static int [][] cell = new int [nCell][nCell];
	/* セルの描画サイズ */
	static double dx,dy;

	/* 出口の設定 */
	static int exitX = nCell/2;
	static int exitY = 0;

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
			int x = rand.nextInt(nCell);
			int y = rand.nextInt(nCell);
			agentX[i] = x;
			agentY[i] = y;
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
		Boolean moved = false;

		for(i=0;i<nAgent;i++){
			x = agentX[i];
			y = agentY[i];
			qq = Math.sqrt((x-exitX)*(x-exitX) + (y-exitY)*(y-exitY));

			qr = Math.sqrt((x-exitX+1)*(x-exitX+1) + (y-exitY  )*(y-exitY  ));
			qd = Math.sqrt((x-exitX  )*(x-exitX  ) + (y-exitY+1)*(y-exitY+1));
			ql = Math.sqrt((x-exitX-1)*(x-exitX-1) + (y-exitY  )*(y-exitY  ));
			qu = Math.sqrt((x-exitX  )*(x-exitX  ) + (y-exitY-1)*(y-exitY-1));

			pr = Math.exp(ks*(qq-qr));
			pd = Math.exp(ks*(qq-qd));
			pl = Math.exp(ks*(qq-ql));
			pu = Math.exp(ks*(qq-qu));

			psum = pr + pl + pu + pd;
			pr /= psum;
			pl /= psum;
			pu /= psum;
			pd /= psum;

			r = rand.nextDouble();
			if (r < pr){
				if(x<nCell-1){
					agentX[i] ++;
					moved = true;
				}
			}
			else if (r < pr + pd){
				if(y<nCell-1){
					agentY[i] ++;
					moved = true;
				}
			}
			else if (r < pr + pd + pl){
				if(x > 0){
					agentX[i] --;
					moved = true;
				}
			}
			else{
				if(y > 0){
					agentY[i] --;
					moved = true;
				}
			}
			if(moved){
				g2.setPaint(Color.white);
				px = sideMargin + x * dx;
				py = topMargin  + y * dy;
				g2.fill(new Rectangle2D.Double(px,py,dy,dx));
				g2.setPaint(Color.red);
				px = sideMargin + agentX[i] * dx;
				py = topMargin  + agentY[i] * dy;
				g2.fill(new Rectangle2D.Double(px,py,dy,dx));
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
		dx = width / nCell;
		dy = height / nCell;
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
			g2.fill(new Rectangle2D.Double(sideMargin + exitX * dx,
				topMargin  + exitY * dy, dx, dy));
		}
	}

	public static void main(String argv[]) {

		/* フレームの宣言 */
		final FloorField01 controller = new FloorField01();

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