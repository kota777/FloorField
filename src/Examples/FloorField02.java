/*
排除効果をもたせたモデル
エージェントが動くときに，あらかじめ
他人がいる場所には進入できないという制限を設けたもの．
このような制限はしばしば排除(Exclusive)効果と呼ばれる．
実際にプログラムを走らせてみると，
最後は出口付近にみんなが集まって動きがとれなくなってしまう．

*/
package Examples;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

public class FloorField02 extends FloorField01 implements ActionListener
{

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
				if(x < nCell-1 && cell[y][x+1] == 0){
					agentX[i] ++;
					moved = true;
				}
			}
			else if (r < pr + pd){
				if(y < nCell - 1 && cell[y+1][x] == 0){
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
			}
		}
	}

	public static void main(String argv[]) {
		/* フレームの宣言 */
		final FloorField02 controller = new FloorField02();

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
