package com.melchor629.musicote.actualizador;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Actualizador extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Intent intent = new Intent(arg0, servicio.class);
        arg0.startService(intent);
	}

}
