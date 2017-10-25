
package org.games.boulderdash;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
   // Option names and default values
   private static final String OPT_CONTROLS = "controls";
   private static final boolean OPT_CONTROLS_DEF = true;
   private static final String OPT_SENSOR = "sensor";
   private static final boolean OPT_SENSOR_DEF = true;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
   }

   /** Get the current value of the controls option */
   
   public static boolean getControls(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(OPT_CONTROLS, OPT_CONTROLS_DEF);
   }
   
   
   /** Get the current value of the sensor option */
   
   public static boolean getSensor(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(OPT_SENSOR, OPT_SENSOR_DEF);
   }
   
}
