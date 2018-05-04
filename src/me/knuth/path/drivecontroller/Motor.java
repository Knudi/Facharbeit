package me.knuth.path.drivecontroller;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

/**
 * Eine Wrapper Klasse für die lejos Klasse EV3LargeRegulatedMotor,
 * damit dieser durch Eingabe eines neagitven Geschindigkeitswertes eine 
 * Entgegengesetze Motordrehung vollzieht  
 * 
 * @author Alexander Knuth
 *
 */
public class Motor 
{
	private EV3LargeRegulatedMotor motor;
	private int speed;
	private boolean stopped;
	
	public Motor(Port port)
	{
		this.motor = new EV3LargeRegulatedMotor(port);
		this.speed = 0;
		this.stopped = true;
	}
	
	public void setSpeed(int dgs)
	{
		this.speed = dgs;
		this.motor.setSpeed((int)Math.abs(dgs));
		if(speed == 0) this.stopped = true;
	}
	
	public void increaseSpeed(int dgs)
	{
		if(dgs == 0) return;
		this.speed += dgs;
		this.updateSpeed(this.speed);
	}
	
	public void updateSpeed(int dgs)
	{
		this.setSpeed(dgs);
		this.run();
	}
	
	public void run()
	{
		if(this.speed > 0) this.motor.forward();
		else if(this.speed < 0) this.motor.backward();
		else
		{
			this.motor.setSpeed(0);
			this.stopped = true;
			return;
		}
		this.stopped = false;
	}
	
	public void stop()
	{
		this.stopped = true;
		this.motor.stop();
	}
	
	public boolean isMoving()
	{
		return !this.stopped;
	}
	
	public void flt()
	{
		this.motor.flt();
	}
	
	public int getSpeed()
	{
		return this.speed;
	}
	
	public EV3LargeRegulatedMotor getMotor()
	{
		return this.motor;
	}
	
	public void forward()
	{
		this.speed = Math.abs(this.speed);
		motor.forward();
	}
	
	public void backward()
	{
		this.speed = -Math.abs(this.speed);
		this.motor.backward();
	}
}
