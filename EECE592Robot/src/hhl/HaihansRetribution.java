package hhl;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import org.jfree.ui.RefineryUtilities;

import java.io.OutputStream.*;

import net1.*;


public class HaihansRetribution extends AdvancedRobot 
{
	static boolean activeNN = true;
	static boolean loadW = true;
	
	static int epochs = 3000;
	static int inputs = 15;
	static int hidden = 8;
	static int i=0,j=0;
	
	static int callCount = 0;
	
	static double gamma_ = 0.009;
	static double mu_ = 0.9;
	
	static double A = -1;
	static double B = 1;
	
	
	static double gamma = 0.99;
	static double alpha = 0.2;
	static double epsilon = 0.01;
	
	
	static NNet net = new NNet(inputs,hidden,gamma_,mu_,A,B, epochs, loadW);
	
	//double alphaDecay = 1000000.0;

	static int perRound = 100;
	
	static String datFile = "datnn105.csv";
	static String qFile = "Tablenn105.csv";
	static String winFile = "winsnn105.csv";
	
	static boolean test = false;
	static boolean Qlearn = true;
	static boolean training = true;
	static boolean loadFile = false;
	static boolean terminalRew = false;
	static boolean dimRed = true;
	

	
	
	static ArrayList <Double> rewards = new ArrayList<Double>();
	
	static Data d = new Data();
	static stateData sd = new stateData();
	
	// QBOT PARAMTERS
	int states = 6;
	
	double reward = 0;
	
	
	long state, prevState;
	int prevAction;
	
	int terminalRewards = 0;
	
	static int Xgrid = 8;
	static int Ygrid = 6;
	static int bearingBin = 12;
	static int distanceBin = 5;
	static int lifeBin = 2;
	static int heatBin = 2;
	
	
	
	int itCount = 0;
	

	
	int index = 0;
	double culmR = 0;
	/////////////////////////////////////////
	
	double bulletSuperWeak = 0.5;
	double bulletWeak = 1;
	double bulletMedium = 2;
	double bulletStrong =3;
	
	double sx = 800, sy=600;
	
	
	/*
	 * Actions:
	    MoveUp, MoveDown, MoveLeft, MoveRight,
	    MoveNW, MoveNE, MoveSW, MoveSE,
	    Fire
	    
	*/
	
	
	static int aLen = 9;
	int []a = {0,1,2,3,4,5,6,7,8};
	
	
	double absBearing; //enemies absolute bearing
	double enemyDistance; // enemies distance
	double enemyLife; // enemies energy
	double latVel;//enemies later velocity
	double ownLife;
	double ownX, ownY, EX, EY;
	double enemyHeading;
	double enemyVel;
	double gunHeat;
	double gunHeading;
	double gunAngleDiff;
	
	// signal for action complete (transitioned to the next state)
	boolean actionComplete = false;
	boolean executingAction = false;
	boolean enemyLocked = false;
	boolean alreadyMoved = false;
	boolean shootingBlanks = false;
	
	int currentAction = 0;
	
	
	int binnedAbsBearing;
	int binnedEnemyDistance;
	int binnedEnemyLife;
	//int binnedLatVel;
	int binnedOwnLife;
	int binnedX;
	int binnedY;
	int binnedEX;
	int binnedEY;
	int binnedGunHeat;
	int binnedGunHeading;
	int binnedGunAngleDiff;
	int maxIndex = 0;
	
	
	long actionTimeStart;
	long actionTimeFinish;
	
	Bullet bul;
	
	static int roundNum=0;
	
	ArrayList <Long> stateList;
	ArrayList <Long> prevStateList;
	
	
	RobocodeFileOutputStream rfos = null;
	
	static LUT lut = new LUT(heatBin*Xgrid*Ygrid*bearingBin /*bearingBin*/ * 3*lifeBin, aLen, loadFile, qFile);
	
	double rewFoo;
	
	public HaihansRetribution() throws IOException
	{

		
			//writeIndex();
		
		Random r = new Random();
		
		
		absBearing = 0;
		enemyDistance = 0;
		enemyLife = 100;
		ownLife = 100;
		//latVel = r.nextInt(8);
		gunAngleDiff = 0;
		
		
		binnedAbsBearing = 0;
		binnedEnemyDistance = 0;
		binnedEnemyLife = 0;
		//binnedLatVel = l.quantize(latVel, 0, 8, 5);
		binnedOwnLife = 0;
		binnedX = 0;
		binnedY = 0;
		binnedEX = 0;
		binnedEY = 0;
		binnedGunHeading = 0;
		binnedGunAngleDiff = 0;
		
		stateList = new ArrayList<Long>();

		
		for(int i = 0;i<states;i++)
		{
			stateList.add((long)0);
		}
		
		
	}
	


	
	public void run() 
	{
		
		setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
		setBodyColor(new Color(128, 128, 128));
		setGunColor(new Color(50, 50, 50));
		setRadarColor(new Color(255, 255, 255));
		setScanColor(Color.white);
		setBulletColor(Color.red);
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		
		turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
		

		//System.out.println("In run...\n");
	}
 
	
	public void onStatus(StatusEvent e)
	{
		
		
		
		
		int i,j,k;
		RobotStatus s =  e.getStatus();
		ownX = s.getX();
		ownY = s.getY();
		ownLife = getEnergy();
		gunHeat = getGunHeat();
		gunHeading = getGunHeading();
		
		
		// map raw input to quantized state space
		
		binnedAbsBearing = lut.quantize(absBearing, -Math.PI, 3*Math.PI, bearingBin-1);
		binnedEnemyDistance = lut.quantizeDist(enemyDistance);
		binnedEnemyLife = lut.quantize(ownLife, 0, 100, lifeBin-1);
		binnedOwnLife = lut.quantize(ownLife, 0, 100, lifeBin-1);
		
		
		binnedGunHeat = lut.quantize(gunHeat, 0, 1.6, heatBin-1);
		
		
		binnedGunHeading = lut.quantize(gunHeading, 0, 359, bearingBin-1);
		
		
		
		// divide X & Y
		binnedX = lut.quantize(ownX, 0, 800, Xgrid);
		binnedY = lut.quantize(ownY, 0, 600, Ygrid);
		
		binnedGunAngleDiff = lut.quantize(gunAngleDiff, 0, 359,  bearingBin-1);
		
		stateList.set(0,(long) binnedAbsBearing);
		stateList.set(1,(long) binnedEnemyDistance);
		//stateList.add(binnedEnemyLife);
		//state[3] = binnedLatVel;
		stateList.set(2,(long) binnedOwnLife);
		stateList.set(3,(long) binnedX);
		stateList.set(4,(long) binnedY);
		stateList.set(5,(long) binnedGunHeat);
		//stateList.set(6,(long) binnedGunAngleDiff);

		//System.out.print("state ");
		for(i=0;i<stateList.size(); i++)
		{
			System.out.format("%d", stateList.get(i));
		}
		//System.out.format(" index: %d time: %d\n", index, e.getTime());
		
		// this is where the magic happens
		if(enemyLocked)
		{
			
			if(shootingBlanks && !terminalRew)
			{
				reward = -5;
				shootingBlanks = false;
			}
			
			// action has been completed, transitioned to next state, apply Q Learning update
			
			double maxVal = -9999999999.0;
			
			boolean yoloFlag = false;
			double QMax = 300;
			double QMin = -100;
			if(Qlearn && actionComplete && !executingAction && training)
			{
				
				culmR += reward;
				
				actionComplete = false;
				executingAction = false;
				alreadyMoved = false;
				
				prevState = index;
				prevAction = currentAction;
				
				index = (int) getIndex();
				
			
				
				yoloFlag = true;
				
				
				for(i=0; i <lut.QTable.get(index).size(); i++)
				{
					if(lut.QTable.get(index).get(i) > maxVal)
					{
						maxVal = lut.QTable.get(index).get(i);
					}
				}
				//prevFoo = lut.QTable.get((int)prevState).get(prevAction);
				
				//prevFoo = (prevFoo - QMin)/ (QMax - QMin);
				
				double temp1 = (1-alpha)*lut.QTable.get((int)prevState).get(prevAction);
				double temp2 = alpha*(reward + gamma*maxVal);
				

				//System.out.format("Q-Learning update reward %f update %f \n", reward, temp1+temp2);
				rewFoo = reward;
				reward = 0;
				
				
				
				lut.QTable.get((int)prevState).set(prevAction, temp1+temp2);
				
				enemyLocked = false;
			}
			else if(!Qlearn && actionComplete && !executingAction)
			{
				actionComplete = false;
				executingAction = false;
				alreadyMoved = false;
				
				
				
				enemyLocked = false;
			}
			
			// backpropogation error
			if(activeNN == true && yoloFlag)
			{
				yoloFlag = false;
				
				
				double max = getNNMax();
				
				
				
				
				trainNN(max);
				
					
				
				
			}
			
			
			// start action
			if(!actionComplete && !executingAction)
			{
				
				prevStateList = (ArrayList<Long>)stateList.clone();
				
				// given current state, perform action
				Random r = new Random();
				
				double rand = r.nextDouble();
				
				// select best action
				
					
				if(!Qlearn && training)
				{
					prevState = index;
					prevAction = currentAction;
				}
				
				index =  (int) getIndex();
				
				maxVal = -9999999999.0;
				
				
				for(i=0; i <lut.QTable.get(index).size(); i++)
				{
					if(lut.QTable.get(index).get(i) > maxVal)
					{
						maxVal = lut.QTable.get(index).get(i);
						maxIndex = i;
					}
				}
				
				if(activeNN == true)
				{
					getNNMax();
				}
				
				if(test == false)
				{
					if(rand <= 1 - epsilon)
						currentAction = a[maxIndex];
					else
					{
						int randAction = r.nextInt(aLen);
						currentAction = a[randAction];
					}
				}
				else
				{
					currentAction = a[r.nextInt(aLen)];
				}
				
				//System.out.format("a: %d ", currentAction);
				
				// apply SARSA update
				if(!Qlearn  && training)
				{
					
					culmR += reward;
					double temp1 = (1-alpha)*lut.QTable.get((int)prevState).get(prevAction);
					double temp2 = alpha*(reward + gamma*lut.QTable.get(index).get(currentAction));
					
					lut.QTable.get((int)prevState).set(prevAction, temp1+temp2);
					
					reward = 0;
					
				}
				
				actionComplete = false;
				executingAction = true;
				
				
				
				switch(currentAction)
				{
				// move up to next block
				case 0:
					double heading = s.getHeading();
					
					if(heading <= 180)
					{
						turnLeft(heading);
					}
					else
					{
						turnRight(360-heading);
					}
					
					
					break;
					
					
				// move down to the next block	
				case 1:
					
					heading = s.getHeading();
					
					if(heading <= 180)
					{
						turnRight(180-heading);
					}
					else
					{
						turnLeft(heading-180);
					}
					
					break;
					
				// move left one block
				case 2:
					heading = s.getHeading();
					
					if(heading <= 90 && heading >= 0)
					{
						turnLeft(90 + heading);
					}
					else if(heading >= 270 && heading <= 359)
					{
						turnLeft(heading-270);
					}
					else if(heading < 270 && heading > 90)
					{
						turnRight(270 - heading); 
					}
					
					break;
				
				// move right one block
				case 3:
					heading = s.getHeading();
					
					if(heading <= 90 && heading >= 0)
					{
						turnRight(90-heading);
					}
					else if(heading >= 270 && heading <= 359)
					{
						turnRight((360-heading)+90);
					}
					else if(heading < 270 && heading > 90)
					{
						turnLeft(heading-90); 
					}
					
					
					break;
				
				// move NW one block
				case 4:
					
					heading = s.getHeading();
					
					if(heading <= 315 && heading >= 135)
					{
						turnRight(315-heading);
					}
					else if(heading > 315 && heading <= 359)
					{
						turnLeft((heading-315));
					}
					else if(heading < 135 && heading >= 0)
					{
						turnRight((135-heading)+180); 
					}
					
					break;
					
				// move NE one block
				case 5:
					
					heading = s.getHeading();
					
					if(heading <= 45 && heading >= 0)
					{
						turnRight(45-heading);
					}
					else if(heading > 45 && heading <= 315)
					{
						turnLeft((heading-45));
					}
					else if(heading > 315 && heading <= 359)
					{
						turnRight((360-heading)+45); 
					}
					
					break;
					
				// move SW one block
				case 6:
					
					heading = s.getHeading();
					
					if(heading <= 225 && heading >= 0)
					{
						turnRight(225-heading);
					}
					else if(heading > 225 && heading <= 359)
					{
						turnLeft((heading-225));
					}
					
					
					
					break;
				
				
				// move SE one block
				case 7:
					
					heading = s.getHeading();
					
					if(heading <= 135 && heading >= 0)
					{
						turnRight(135-heading);
					}
					else if(heading > 135 && heading <= 359)
					{
						turnLeft((heading-135));
					}
					
					break;
					
				
					
				case 8:
						

					if(getGunHeat() > 0)
					{
						shootingBlanks = true;
						executingAction = false;
						actionComplete = true;
						break;
					}
				
					bul = setFireBullet(bulletStrong);
					//execute();
					
					
					executingAction = false;
					actionComplete = true;
					
					break;


				case 12:
					
					turnGunRight(360.0/(double)bearingBin);
					executingAction = false;
					actionComplete = true;
					
					gunAngleDiff =robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians());
					
					if(Math.abs(gunAngleDiff) <= (bearingBin*Math.PI/180))
					{
						//reward += 20;
						reward = 20;
					}	
					else
					{
						reward = -1;
					}
					
					break;
					
				//move gun CCW	
				case 13:
					turnGunLeft(360.0/(double)bearingBin);
					executingAction = false;
					actionComplete = true;
					
					gunAngleDiff =robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians());
					
					if(Math.abs(gunAngleDiff) <= (bearingBin*Math.PI/180))
					{
						//reward += 20;
						reward = 20;
					}	
					else
					{
						reward = -1;
					}
					break;
				
					
				}
				
			}
			// select random action
			else
			{
				actionComplete = false;
				executingAction = true;
			}
				
			
		}
		
		
		// complete action
		if(!actionComplete && executingAction)
		{
			switch(currentAction)
			{
			// move up to next block
			case 0:
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upY = ((sy/Ygrid)*(binnedY + 1)) + (sy/Ygrid)/2;
					double dist = upY - s.getY();
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 1:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upY = ((sy/Ygrid)*(binnedY - 1)) + (sy/Ygrid)/2;
					double dist = upY - s.getY();
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
					
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 2:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX - 1)) + (sx/Xgrid)/2;
					double dist = upX - s.getX();
					setAhead(-dist);
					//execute();
					
					alreadyMoved = true;
					
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 3:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX + 1)) + (sx/Xgrid)/2;
					double dist = upX - s.getX();
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
					
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 4:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX + 1)) + (sx/Xgrid)/2;
					double distX = upX - s.getX();
					double upY = ((sy/Ygrid)*(binnedY + 1)) + (sy/Ygrid)/2;
					double distY = upY - s.getY();
					
					double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
					
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 5:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX - 1)) + (sx/Xgrid)/2;
					double distX = upX - s.getX();
					double upY = ((sy/Ygrid)*(binnedY + 1)) + (sy/Ygrid)/2;
					double distY = upY - s.getY();
					
					double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 6:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX - 1)) + (sx/Xgrid)/2;
					double distX = upX - s.getX();
					double upY = ((sy/Ygrid)*(binnedY - 1)) + (sy/Ygrid)/2;
					double distY = upY - s.getY();
					
					double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			case 7:
				
				if(getTurnRemaining() == 0 && !alreadyMoved)
				{
					double upX = ((sx/Xgrid)*(binnedX + 1)) + (sx/Xgrid)/2;
					double distX = upX - s.getX();
					double upY = ((sy/Ygrid)*(binnedY - 1)) + (sy/Ygrid)/2;
					double distY = upY - s.getY();
					
					double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
					setAhead(dist);
					//execute();
					
					alreadyMoved = true;
				}
				
				
				
				if(getDistanceRemaining() == 0)
				{
					executingAction = false;
					actionComplete = true;
					
					break;
				}
				
				break;
			
			}
		}
		

	
	}

	public void onScannedRobot(ScannedRobotEvent e) 
	{
	
		enemyLocked = true;
		// read raw sensor input upon enemy scanned
		absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
		enemyDistance = e.getDistance(); // enemies distance
		enemyLife = e.getEnergy(); // enemies energy
		latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
		enemyHeading = e.getHeading();
		enemyVel = e.getVelocity();

		
		
		
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
		
		// turn gun to face enemy
		
		double gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
		setTurnGunRightRadians(gunTurnAmt); //turn our gun
		
		
		
		
		//System.out.format("Enemy Scanned, time %d\n", e.getTime());
	}
	
	
	

	
	
	public long getIndex()
	{
		return stateList.get(0) + stateList.get(1) * (bearingBin) + stateList.get(2) * (distanceBin * bearingBin) +  stateList.get(3) * (lifeBin*distanceBin * bearingBin) + stateList.get(4) * (Xgrid * lifeBin*distanceBin * bearingBin ) +  stateList.get(5) * (Ygrid * Xgrid * lifeBin*distanceBin * bearingBin )
				/*+ stateList.get(6) * (heatBin * Ygrid * Xgrid * lifeBin*distanceBin * bearingBin )*/;
	}
	
	
	
	
	//////////////////////////////////// 
	
	public void onHitRobot(HitRobotEvent event)
	{
		if(!terminalRew)
			reward = -20;
	}
	
	
	public void onBulletHit(BulletHitEvent e)
	{
		if(!terminalRew)
			reward = e.getBullet().getPower() * 50;
	}
	
	public void onBulletMissed(BulletHitEvent e)
	{
		if(!terminalRew)
			reward = 0*-e.getBullet().getPower() * 10;
		//System.out.format("Bullet missed\n");
		
	}
	
	public void onBulletHitBullet(BulletHitEvent e)
	{
		if(!terminalRew)
			reward = -e.getBullet().getPower() * 2;
		//System.out.format("Own bullet hit enemy bullet");
	}
	
	public void onHitByBullet(HitByBulletEvent e)
	{
		if(!terminalRew)
			reward = -20;
		//System.out.format("Hit by enemy bullet\n");
	}
	
	
	public void onHitWall(HitWallEvent event)
	{
		if(!terminalRew)
			reward = -20;
	}
	
	public void onDeath(DeathEvent event)
	{

		
		double temp1 = (1-alpha)*lut.QTable.get((int)index).get(currentAction);
		double temp2 = alpha*(-100);
		lut.QTable.get((int)index).set(currentAction, temp1+temp2);
		
		//rewFoo = -100;
		
		//trainNN(-100);
		
		reward = 0;
		culmR -= 100;
		rewards.add(culmR);
		
		//System.out.format("*****Culm R %f", culmR);
		
		d.thisRoundNum = roundNum;
		d.roundCulmR= culmR;
		d.winLose = 0;
		
		
		try 
		{
			d.saveRewards(datFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		roundNum++;
		
		if(roundNum % perRound == 0)
		{
			try {
				
				double winRatio = (double)d.culmWinLose/perRound;
				d.saveWins(winFile, winRatio, roundNum);
				
				d.culmWinLose = 0;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	
	}
	
	public void onWin(WinEvent e) 
	{
		
			
			
		double temp1 = (1-alpha)*lut.QTable.get((int)index).get(currentAction);
		double temp2 = alpha*(100);
		lut.QTable.get((int)index).set(currentAction, temp1+temp2);
		//rewFoo = 100;
		
		//trainNN(100);
		
		reward = 0;
		culmR += 100;
		rewards.add(culmR);
		
		
		////////////////////////////////////////////////
		
		
		
		//System.out.format("*****Culm R %f", culmR);
			
		d.thisRoundNum = roundNum;
		d.roundCulmR= culmR;
		d.winLose = 1;
		d.culmWinLose += 1;
		
		try 
		{
			
			d.saveRewards(datFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
		roundNum++;
	
		if(roundNum % perRound == 0)
		{
			double winRatio = (double)d.culmWinLose/perRound;
			try {
				d.saveWins(winFile, winRatio, roundNum);
				d.culmWinLose = 0;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
	}
	
	public void onBattleEnded(BattleEndedEvent event)
	{
		if(test== false)
		{

			lut.save(qFile);	
		}
		
		try {
			writeIndex("test.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public void writeIndex(String f) throws IOException
	{
		
		FileWriter fileWriter = new FileWriter(f, false);
		double offset = 0.5;
		double QMax = 300;
		double QMin = -100;
		double Q = 0;
		double a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0;
		
		double aVal = 1;
		fileWriter.write("counter,index,bearing,dist,life,x,Y,heat,a1,a2,a3,a4,a5,a6,a7,a8,a9,Q\n");
		for(int a=0; a<9; a++)
		{
			
			switch(a)
			{
			case 0:
				a1= aVal;a2= -aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 1:
				a1= -aVal;a2= aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 2:
				a1= -aVal;a2= -aVal;a3= aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 3:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 4:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 5:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 6:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 7:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=aVal;a9=-aVal;
				
				break;
				
			case 8:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=aVal;
				
				break;
				
			
				
				
			}
			
			int count = 0;
			for(int n=0; n < heatBin; n++)
			{
				for(int m=0; m < Ygrid; m++)
				{
					for(int l=0; l < Xgrid; l++)
					{
						for(int k=0; k < lifeBin; k++)
						{
							for(int j=0; j < 3; j++)
							{
								for(int i=0; i < bearingBin; i++)
								{
									int index = i + j * (bearingBin) + k * (3 * bearingBin) + l * (lifeBin*3 * bearingBin) + m * (Xgrid * lifeBin*3 * bearingBin ) +  n * (Ygrid * Xgrid * lifeBin*3 * bearingBin );
									String s;
									
									s = String.format("%d,", count);
									
									fileWriter.write(s);
									
									s = String.format("%d,", index);
									
									fileWriter.write(s);
								
									Q = lut.QTable.get(index).get(a);
									Q = (Q - QMin)/(QMax - QMin);
									
									s = String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n", 
											(double)i/bearingBin - offset,(double)j/3 - offset,(double)k/lifeBin - offset,
											(double)l/Xgrid - offset,(double)m/Ygrid - offset,(double)n/heatBin - offset,
											a1,a2,a3,a4,a5,a6,a7,a8,a9, Q);
									
									fileWriter.write(s);
									count++;
								}
							}
						}
					}
				}
			}
		}
		
		
		fileWriter.close();		
	}
	
	
	public double getNNMax()
	{
		double offset = 0.5,foo = -999999999;
		ArrayList<Double> nnIn = new ArrayList<Double>();
		if(dimRed)
		{
			double bearing = lut.unQuantize(absBearing, -Math.PI, 3*Math.PI, bearingBin-1) / 12.0 - offset;
			
			
			
			nnIn.add(bearing - offset);
		}
		else
		{
			nnIn.add((double)stateList.get(0)/bearingBin - offset);
		}
		
		nnIn.add((double)stateList.get(1)/3 - offset);
		nnIn.add((double)stateList.get(2)/lifeBin - offset);
		nnIn.add((double)stateList.get(3)/Xgrid - offset);
		nnIn.add((double)stateList.get(4)/Ygrid - offset);
		nnIn.add((double)stateList.get(5)/heatBin - offset);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		double a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0, aVal = 1;
		
		for(int a = 0; a < 9; a++)
		{
			
			switch(a)
			{
			case 0:
				a1= aVal;a2= -aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 1:
				a1= -aVal;a2= aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 2:
				a1= -aVal;a2= -aVal;a3= aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
				
				break;
				
			case 3:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 4:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 5:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=aVal;a7=-aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 6:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=aVal;a8=-aVal;a9=-aVal;
				
				break;
				
			case 7:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=aVal;a9=-aVal;
				
				break;
				
			case 8:
				a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=aVal;
				
				break;
			}
				
			nnIn.set(5+1,a1);
			nnIn.set(5+2,a2);
			nnIn.set(5+3,a3);
			nnIn.set(5+4,a4);
			nnIn.set(5+5,a5);
			nnIn.set(5+6,a6);
			nnIn.set(5+7,a7);
			nnIn.set(5+8,a8);
			nnIn.set(5+9,a9);
			
			//int maxIndex;
			net.feedForward(nnIn);
			double foo2 = net.net.get(inputs + hidden).computeOutput();
			
			if(foo2 > foo)
			{
				foo = foo2;
				maxIndex = a;
			}
			
		}
		
		return foo;
	}
	

	public void trainNN(double max)
	{
		double offset = 0.5;
		ArrayList<Double> nnIn = new ArrayList<Double>();
		
		if(dimRed)
		{
			double bearing = lut.unQuantize(absBearing, -Math.PI, Math.PI, bearingBin-1) / 12.0 - offset;
			
			nnIn.add(bearing - offset);
		}
		else
		{
			nnIn.add((double)prevStateList.get(0)/bearingBin - offset);
		}
		nnIn.add((double)prevStateList.get(1)/3 - offset);
		nnIn.add((double)prevStateList.get(2)/lifeBin - offset);
		nnIn.add((double)prevStateList.get(3)/Xgrid - offset);
		nnIn.add((double)prevStateList.get(4)/Ygrid - offset);
		nnIn.add((double)prevStateList.get(5)/heatBin - offset);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		nnIn.add(0.0);
		double a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0, aVal = 1;
		
		
			
		switch(prevAction)
		{
		case 0:
			a1= aVal;a2= -aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
			
			break;
			
		case 1:
			a1= -aVal;a2= aVal;a3= -aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
			
			break;
			
		case 2:
			a1= -aVal;a2= -aVal;a3= aVal;a4= -aVal;a5= -aVal;a6= -aVal;a7= -aVal;a8= -aVal;a9= -aVal;
			
			break;
			
		case 3:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
			
			break;
			
		case 4:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=-aVal;
			
			break;
			
		case 5:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=aVal;a7=-aVal;a8=-aVal;a9=-aVal;
			
			break;
			
		case 6:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=aVal;a8=-aVal;a9=-aVal;
			
			break;
			
		case 7:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=aVal;a9=-aVal;
			
			break;
			
		case 8:
			a1=-aVal;a2=-aVal;a3=-aVal;a4=-aVal;a5=-aVal;a6=-aVal;a7=-aVal;a8=-aVal;a9=aVal;
			
			break;
		}
				
			nnIn.set(5+1,a1);
			nnIn.set(5+2,a2);
			nnIn.set(5+3,a3);
			nnIn.set(5+4,a4);
			nnIn.set(5+5,a5);
			nnIn.set(5+6,a6);
			nnIn.set(5+7,a7);
			nnIn.set(5+8,a8);
			nnIn.set(5+9,a9);
			double rew = ((rewFoo + 150)/300 - 0.5) * 2;
			
			
			
			// normalize target value to [-1,1]
			double target = (rew + gamma*max)/4;
			
			boolean yolo = false;
			
			if(/*prevStateList.get(0) == 6 &&*/ prevStateList.get(1) == 2 &&  prevStateList.get(2) == 1 && 
					 /*prevStateList.get(3) == 1 && prevStateList.get(4) == 2 &&*/ prevStateList.get(5) == 0 && prevAction == 0)
			{
				yolo = true;	

				callCount++;
			}
			
			net.train2(target, nnIn, callCount,yolo, max);
			
			
			rewFoo = 0;
			prevStateList.clear();
	}

}

