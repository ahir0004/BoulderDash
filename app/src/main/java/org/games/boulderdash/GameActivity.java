/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.games.boulderdash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.games.boulderdash.GameView.GraphicsThread;
import org.games.boulderdash.util.controlButton;

/**
 *
 * @author AH
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class GameActivity extends Activity implements SensorEventListener {

   private static final String TAG= "BoulderDash";

   private SensorManager sensorManager;
   private final controlButton keys[] = new controlButton[4];
   private GameView theGame;
   public TextView scoreBoard;
   private int width;
   private int height;
   MediaPlayer diamondMP;
   MediaPlayer explosionMP;

   GraphicsThread botThread;//Thread controlling bots and such



   /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

       diamondMP=MediaPlayer.create(this, R.raw.diamond_sound);
       diamondMP.setVolume(0.5f, 0.5f);

       explosionMP=MediaPlayer.create(this, R.raw.explosion_sound);
       explosionMP.setVolume(0.5f, 0.5f);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game);


       theGame =(GameView)findViewById(R.id.gameView);
       
       //textview øverst i skærmen der fungerer spil display
       scoreBoard=(TextView)findViewById(R.id.score_board);
       this.scoreBoard.setText(theGame.updateScore());

       //til at beregne hvornår spillen er uden for skærmen
       Display display = getWindowManager().getDefaultDisplay();  
       width = display.getWidth(); 
       height = display.getHeight();
       
       findViews(); //finding control buttons
       setListeners();
       setVolumeControlStream(AudioManager.STREAM_MUSIC);//controlling media sound level

       
    }
    //til at håndtere tastatur tryk
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean bool= super.onKeyDown(keyCode, event);
        int direction=0;
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_UP:
                direction=R.id.up;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                direction=R.id.down;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                direction=R.id.left;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                direction=R.id.right;
                break;

            default:
                return bool;
        }

        //delegerer videre til skærmknapperne
        theGame.controls(direction, width, height);
        return true;
    }



    void playSound(int SoundId){
        switch(SoundId){
            case R.raw.diamond_sound:
                diamondMP.start();
                break;
            case R.raw.explosion_sound:
                explosionMP.start();
                break;
        }

    }

  

    @Override
    protected void onPause() {
        super.onPause();
        try {
            
            botThread.setActive(false);//releasing runnable
            theGame.playerDead=false;
            sensorManager.unregisterListener(this);
            storeHighscore(theGame.getScore());
        } catch (IOException ex) {
            Logger.getLogger(GameActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


  
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        diamondMP.release();//releasing resources
        explosionMP.release();
    }

  

    @Override
    protected void onResume() {
      super.onResume();
      
      botThread = theGame.newThread();
      botThread.start();
      sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

      Sensor orienteringsSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
      sensorManager.registerListener(this, orienteringsSensor, SensorManager.SENSOR_DELAY_GAME);
    }




   private void findViews() {

      keys[0]= (controlButton)findViewById(R.id.up);
      keys[1] = (controlButton)findViewById(R.id.right);
      keys[2] = (controlButton)findViewById(R.id.left);
      keys[3] = (controlButton)findViewById(R.id.down);
/*
      if(Prefs.getControls(this))
       for(int i=0;i<keys.length;i++)
           keys[i].setAlphaValue(150);
*/

      
      
   }

   public void toggleControls(int buttonID){

       for(int i=0;i<keys.length;i++){

           if(keys[i].getId()==buttonID)
                keys[i].setAlphaValue(150);
           else
            keys[i].setAlphaValue(0);
       }



   }

   private void setListeners() {
      for (int i = 0; i < keys.length; i++) {
         final int t = i ;
      
         keys[i].setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

/*
                if( Prefs.getControls(GameActivity.this)){
                  theGame.controls(keys[t].getId(),width, height);
                  
                }
 */
            }});
      }
   }


   
	 
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensortype = event.sensor.getType();

                

                //if (Prefs.getSensor(this)){
			if (sensortype == Sensor.TYPE_ORIENTATION)  {
                        //Log.d("GAMEACTIVITY_onSensor", "X: "+event.values[1]+"    Y: "+event.values[2]);
                            

                             if(event.values[2] <-15 && event.values[2] > -20 ){
					toggleControls(R.id.right);
                                        theGame.controls(R.id.right, width, height);
                                        
                                        //keys[3].setAlphaValue(150);
                                        //Log.d("NANO: ", ":::"+event.timestamp);
                            }
                             if(event.values[2] >15 && event.values[2] <20){
					toggleControls(R.id.left);
                                        theGame.controls(R.id.left, width, height);
                                        
                            }
                             if(event.values[1] <-15 && event.values[1] > -20){
					toggleControls(R.id.down);
                                        theGame.controls(R.id.down, width, height);
                                        
                            }
                             if(event.values[1] >15 && event.values[1] <20){
					toggleControls(R.id.up);
                                        theGame.controls(R.id.up, width, height);
                                        
                            }
                             
                                        
                       
                         }
		}
	//}
   
   public void storeHighscore(String theScore) throws IOException {

        BufferedReader reader = null;
        FileWriter fw=null ;
        File privatMappe = this.getFilesDir();
        File theFile = new File(privatMappe, "HighScore.txt");

        try {
            reader = new BufferedReader(new FileReader(theFile));
        } catch (FileNotFoundException ex) {
            fw = new FileWriter(theFile);//if no file, make one
            Logger.getLogger(GameActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

        String tmp= "";
        String line = "";
        ArrayList<Integer> ar = new ArrayList<Integer>();

        if(reader!=null){
            while ((line = reader.readLine()) != null) {
                ar.add(Integer.parseInt(line));
            }
            reader.close();
        }

        int newScore=Integer.parseInt(theScore);

        if(newScore>0)
            ar.add(newScore);//Add score

        Collections.sort(ar);//sorting the collection
        Collections.reverse(ar);//Descending order

        if(ar.size()>5)//only 5 scores
            ar.remove(ar.size() - 1);//so remove last one

        if(fw==null)
            fw = new FileWriter(theFile);

        PrintWriter pw = new PrintWriter(fw);
        for (Integer val : ar) {//for each
            pw.println(val);
        }
        fw.close();
       
      }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //throw new UnsupportedOperationException("Not supported yet.");

    }
   
}
