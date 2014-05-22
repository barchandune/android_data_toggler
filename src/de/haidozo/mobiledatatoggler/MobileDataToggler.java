package de.haidozo.mobiledatatoggler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.RemoteViews;

public class MobileDataToggler extends AppWidgetProvider {	
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		//Log.i("Custom", "Update");
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for(int widgetId : appWidgetIds) {
			String toggleAction = context.getResources().getString(R.string.toggle_action);
			Intent intent = new Intent(context, MobileDataToggler.class);
			intent.setAction(toggleAction);
	
			// Register an onclick listener for the imagebutton
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.imageButton1, pendingIntent);
			
			// Set icon according on mobile data state
			if(isEnabled(context)) {
				views.setImageViewResource(R.id.imageButton1, R.drawable.ic_mobile_data_on);
			} else {
				views.setImageViewResource(R.id.imageButton1, R.drawable.ic_mobile_data_off);
			}
		
			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.i("Custom", "Recieve");
		super.onReceive(context, intent);		
		String toggleAction = context.getResources().getString(R.string.toggle_action);
		if (intent.getAction().equals(toggleAction)) {			
				toggleMobileData(context);			
		}
	}
	
	private void toggleMobileData(Context context) {
		try {
			final ConnectivityManager cm = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> cmClass = Class.forName(cm.getClass().getName());
			final Field cmField = cmClass.getDeclaredField("mService");
			cmField.setAccessible(true);
			final Object icm = cmField.get(cm);
			final Class<?> icmClass =  Class.forName(icm.getClass().getName());
			
			final Method setMobileDataEnabledMethod = icmClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);		
			
			// toggle state and widget image
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			ComponentName thisWidget = new ComponentName(context.getApplicationContext(), MobileDataToggler.class);
			
			boolean oldState = isEnabled(context);
			setMobileDataEnabledMethod.invoke(icm, !oldState);
			
			// waiting for state change...
			while(isEnabled(context) == oldState)
				//Log.i("Custom", "Waiting for state change")
				;
			
			//... and then update the icon
			if(!oldState) {
				views.setImageViewResource(R.id.imageButton1, R.drawable.ic_mobile_data_on);
			} else {
				views.setImageViewResource(R.id.imageButton1, R.drawable.ic_mobile_data_off);
			}			
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(thisWidget, views);		
		} catch (ClassNotFoundException | NoSuchFieldException
				| IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}	
	
	private boolean isEnabled(Context context) {
		try {
			final ConnectivityManager cm = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> cmClass = Class.forName(cm.getClass().getName());
			final Field cmField = cmClass.getDeclaredField("mService");
			cmField.setAccessible(true);
			
			// get mobile data state		
			final Method isEnabled = cmClass.getDeclaredMethod("getMobileDataEnabled");
			isEnabled.setAccessible(true);
			
			return (Boolean)isEnabled.invoke(cm);
		} catch (ClassNotFoundException | NoSuchFieldException
				| IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			// off in case of exception
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
