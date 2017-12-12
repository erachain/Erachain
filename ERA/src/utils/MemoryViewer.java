package utils;

import controller.Controller;

public class MemoryViewer extends Thread {
	
	public MemoryViewer(){
		
		setName("Memory manager");
		
	}
	public void run(){
		// if memory !Ok
		while(true){
		try {
			sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	if (Runtime.getRuntime().maxMemory()== Runtime.getRuntime().totalMemory()){
	//	System.out.println("########################### Free Memory:" + Runtime.getRuntime().freeMemory());
	 if(Runtime.getRuntime().freeMemory()< 1000000) Controller.getInstance().stopAll(99);
		}
		}
	}

}
