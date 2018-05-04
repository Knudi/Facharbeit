package me.knuth.path.drivecontroller;

import lejos.hardware.port.Port;

/**
 * Der PID-Controller welcher in Kombination mit der Kamera zur Navigation benutzt wird.
 * 
 * @author Alexander Knuth
 *
 */
public class PIDController 
{
	/*Die beiden Motoren des EV3 */
	private Motor leftMotor;
	private Motor rightMotor;
	
	/*Die Parameter des PI-Controllers*/
	private float kP;
	private float kI;
	private float kD;
	
	/*die Basisgeschwindigkeit*/
	private int speed;
	
	private float errorMod;
	
	/*Die integral und ableitenden Komponenten*/
	private int lastError;
	private int integral;
	private float integralDecay;
	
	/*Limitierung der Drehung*/
	private int highLimit;
	private int lowLimit;
	
	private boolean overshoot;
	
	public PIDController(Port portLeft, Port portRight, int speed, float errorMod, float kP, float kI, float kD, float integralDecay)
	{
		this.leftMotor = new Motor(portLeft);
		this.rightMotor = new Motor(portRight);
		this.speed = speed;
		this.kP = kP;
		this.kI = kI;
		this.kD = kD;
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
		this.lastError = 0;
		this.integral = 0;
		this.errorMod = errorMod;
		this.integralDecay = integralDecay;
		highLimit = (int) (this.speed * 1.5);
		lowLimit = -this.highLimit;
		this.overshoot = false;
	}
	
	public void startMoving()
	{
		this.leftMotor.run();
		this.rightMotor.run();
	}
	
	public void stopMoving()
	{
		this.leftMotor.setSpeed(0);
		this.rightMotor.setSpeed(0);
	}
	
	public Motor getLeftMotor()
	{
		return this.leftMotor;
	}
	
	public Motor getRightMotor()
	{
		return this.rightMotor;
	}
	
	/**
	 * Aktualisiert die Geschwindigkeit beider Motoren anhand des übergebenen Fehlers
	 * basierend auf dem PID-Prinzip
	 * 
	 * @param fError - der Error, welche im [-1; 1] liegt
	 */
	public void updatePID(float fError)
	{
		/*Der Fehler welcher von -1 bis eins führt wird verstärkt*/
		int error = (int) (fError * this.speed * this.errorMod);
		/*Der integralDecay sorgt dafür, dass das Intgral über einen Zeitraum kleiner wird.*/
		this.integral = Math.round(this.integral * this.integralDecay + this.errorMod);
		int derivative = error - this.lastError;
		
		/*hier wird die Summe der drei Komponenten berechnet*/
		int turn = Math.round(error * this.kP + this.integral * this.kI + derivative * this.kD);
		
		/*Dies dient dazu, dass kein zu übertriebenes Lenken hervorkommt, kann jedoch aktiviert werden*/
		if(turn > this.highLimit && !this.overshoot) turn = this.highLimit;
		else if(turn < this.lowLimit && !this.overshoot) turn = this.lowLimit;
		
		this.leftMotor.updateSpeed((int) (speed + turn));
		this.rightMotor.updateSpeed((int) (speed - turn));
		this.lastError = error;
		
	}
	
	public void setSpeed(int speed)
	{
		this.speed = speed;
	}
	
	public void setKP(float kP)
	{
		this.kP = kP;
	}
	
	public void setKI(float kI)
	{
		this.kI = kI;
	}
	
	public void setKD(float kD)
	{
		this.kD = kD;
	}
	
	public void allowOvershot(boolean b)
	{
		this.overshoot = b;
	}
}
