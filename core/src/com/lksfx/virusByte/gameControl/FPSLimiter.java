package com.lksfx.virusByte.gameControl;


public class FPSLimiter {
	private long diff, start = System.currentTimeMillis();
	private int fps;
	
	public FPSLimiter(int fps) {
		this.fps = fps;
	}
	
	public void delay() {
		if(fps>0){
	      diff = System.currentTimeMillis() - start;
	      long targetDelay = 1000 / fps;
	      if (diff < targetDelay) {
	        try{
	            Thread.sleep(targetDelay - diff);
	          } catch (InterruptedException e) {}
	        }   
	      start = System.currentTimeMillis();
	    }
	}
	
}
