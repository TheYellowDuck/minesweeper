package MineSweeper;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Semaphore;

public class windowListener implements WindowListener {
	
	Semaphore sem = new Semaphore(0);
	
	public void windowOpened(WindowEvent e) {
    	
    }

    public void windowClosing(WindowEvent e) {
    	
    }
      
    public void windowClosed(WindowEvent e) {
    	sem.release();
    }

    public void windowIconified(WindowEvent e) {
     
    }

    public void windowDeiconified(WindowEvent e) {
    	  
    }

    public void windowActivated(WindowEvent e) {
    	  
    }

    public void windowDeactivated(WindowEvent e) {
    	  
    }

}
