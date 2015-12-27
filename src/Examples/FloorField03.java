/*
出口から脱出できるモデル
今度は出口に達したエージェントが消えていく，
つまりどこか外に出ていくという モデルに改良したもの

このモデルで渋滞が 引き起こされるのはどんな状況でだろうか？
パラメータを変化させたり， ソースの中に書かれている
エージェントの数を変更したりしてみると，
それなりに面白い結果が得られるだろう．
*/

package Examples;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

public class FloorField03 extends FloorField01 implements ActionListener
{

	static Boolean [] agentLive = new Boolean [nAgent];


	/** エージェントのランダム配置
	*/
	void initCells(){
		for(int i=0;i<nAgent;i++){
			int x = rand.nextInt(nCell);
			int y = rand.nextInt(nCell);
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
		Boolean moved = false;
		//System.out.println("goNextStep");
		for(i=0;i<nAgent;i++){
			if(agentLive[i]){
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
					if(x < nCell - 1 && cell[y][x+1] == 0){
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
					if(agentX[i] == exitX && agentY[i] == exitY){
						g2.setPaint(Color.blue);
						g2.fill(new Rectangle2D.Double(px,py,dy,dx));
						agentLive[i] = false;
						cell[exitY][exitX] = 0;
					}
				}
			}
		}
	}

	public static void main(String argv[]) {

		/* フレームの宣言 */
		final FloorField03 controller = new FloorField03();

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
