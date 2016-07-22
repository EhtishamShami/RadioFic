package com.zhangxaochen.opencamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PopupView extends LinearLayout {
	private static final String TAG = "PopupView";

	private int picture_size_index = -1;
	private int timer_index = -1;

	private Map<String, View> popup_buttons = new Hashtable<String, View>();

	public PopupView(Context context) {
		super(context);

		this.setOrientation(LinearLayout.VERTICAL);

		final MainActivity main_activity = (MainActivity)this.getContext();
		final Preview preview = main_activity.getPreview();
        List<String> supported_flash_values = preview.getSupportedFlashValues();
    	addButtonOptionsToPopup(supported_flash_values, R.array.flash_icons, R.array.flash_values, getResources().getString(R.string.flash_mode), preview.getCurrentFlashValue(), new ButtonOptionsPopupListener() {
			@Override
			public void onClick(String option) {
				if( MyDebug.LOG )
					Log.d(TAG, "clicked flash: " + option);
				preview.updateFlash(option);
				main_activity.closePopup();
			}
		});
    	
		if( preview.isVideo() && preview.isTakingPhoto() ) {
    		// don't add any more options
    	}
    	else {
        	List<String> supported_focus_values = preview.getSupportedFocusValues();
        	addButtonOptionsToPopup(supported_focus_values, R.array.focus_mode_icons, R.array.focus_mode_values, getResources().getString(R.string.focus_mode), preview.getCurrentFocusValue(), new ButtonOptionsPopupListener() {
    			@Override
    			public void onClick(String option) {
    				if( MyDebug.LOG )
    					Log.d(TAG, "clicked focus: " + option);
    				preview.updateFocus(option, false, true);
    				main_activity.closePopup();
    			}
    		});
            
    		List<String> supported_isos = preview.getSupportedISOs();
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
    		String current_iso = sharedPreferences.getString(Preview.getISOPreferenceKey(), "auto");
    		// n.b., we hardcode the string "ISO" as we don't want it translated - firstly more consistent with the ISO values returned by the driver, secondly need to worry about the size of the buttons, so don't want risk of a translated string being too long
        	addButtonOptionsToPopup(supported_isos, -1, -1, "ISO", current_iso, new ButtonOptionsPopupListener() {
    			@Override
    			public void onClick(String option) {
    				if( MyDebug.LOG )
    					Log.d(TAG, "clicked iso: " + option);
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
    				SharedPreferences.Editor editor = sharedPreferences.edit();
    				editor.putString(Preview.getISOPreferenceKey(), option);
    				editor.apply();

    				main_activity.updateForSettings("ISO: " + option);
    				main_activity.closePopup();
    			}
    		});

        	List<String> supported_white_balances = preview.getSupportedWhiteBalances();
        	addRadioOptionsToPopup(supported_white_balances, getResources().getString(R.string.white_balance), Preview.getWhiteBalancePreferenceKey(), Camera.Parameters.WHITE_BALANCE_AUTO, "TEST_WHITE_BALANCE");

        	List<String> supported_scene_modes = preview.getSupportedSceneModes();
        	addRadioOptionsToPopup(supported_scene_modes, getResources().getString(R.string.scene_mode), Preview.getSceneModePreferenceKey(), Camera.Parameters.SCENE_MODE_AUTO, "TEST_SCENE_MODE");

        	List<String> supported_color_effects = preview.getSupportedColorEffects();
        	addRadioOptionsToPopup(supported_color_effects, getResources().getString(R.string.color_effect), Preview.getColorEffectPreferenceKey(), Camera.Parameters.EFFECT_NONE, "TEST_COLOR_EFFECT");
        	
        	if( main_activity.supportsAutoStabilise() ) {
        		CheckBox checkBox = new CheckBox(main_activity);
        		checkBox.setText(getResources().getString(R.string.preference_auto_stabilise));
        		checkBox.setTextColor(Color.WHITE);

        		boolean auto_stabilise = sharedPreferences.getBoolean("preference_auto_stabilise", false);
        		checkBox.setChecked(auto_stabilise);
        		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
	    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean("preference_auto_stabilise", isChecked);
						editor.apply();

						String message = getResources().getString(R.string.preference_auto_stabilise) + ": " + getResources().getString(isChecked ? R.string.on : R.string.off);
						preview.showToast(main_activity.changed_auto_stabilise_toast, message);
						main_activity.closePopup();
					}
        		});

        		this.addView(checkBox);
        	}

    		final List<Camera.Size> picture_sizes = preview.getSupportedPictureSizes();
    		picture_size_index = preview.getCurrentPictureSizeIndex();
    		final List<String> picture_size_strings = new ArrayList<String>();
    		for(Camera.Size picture_size : picture_sizes) {
    			String size_string = picture_size.width + " x " + picture_size.height;
    			picture_size_strings.add(size_string);
    		}
    		addArrayOptionsToPopup(picture_size_strings, getResources().getString(R.string.preference_resolution), picture_size_index, new ArrayOptionsPopupListener() {
    			private void update() {
    				if( picture_size_index == -1 )
    					return;
	                Camera.Size new_size = picture_sizes.get(picture_size_index);
	                String resolution_string = new_size.width + " " + new_size.height;
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(Preview.getResolutionPreferenceKey(preview.getCameraId()), resolution_string);
					editor.apply();
					main_activity.updateForSettings("");
    			}
				@Override
				public int onClickPrev() {
	        		if( picture_size_index != -1 && picture_size_index > 0 ) {
	        			picture_size_index--;
	        			update();
	    				return picture_size_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( picture_size_index != -1 && picture_size_index < picture_sizes.size()-1 ) {
	                	picture_size_index++;
	        			update();
	    				return picture_size_index;
	        		}
					return -1;
				}
    		});

        	final String [] timer_values = getResources().getStringArray(R.array.preference_timer_values);
        	String [] timer_entries = getResources().getStringArray(R.array.preference_timer_entries);
    		String timer_value = sharedPreferences.getString("preference_timer", "0");
    		timer_index = Arrays.asList(timer_values).indexOf(timer_value);
    		if( timer_index == -1 ) {
				if( MyDebug.LOG )
					Log.d(TAG, "can't find timer_value " + timer_value + " in timer_values!");
				timer_index = 0;
    		}
    		addArrayOptionsToPopup(Arrays.asList(timer_entries), getResources().getString(R.string.preference_timer), timer_index, new ArrayOptionsPopupListener() {
    			private void update() {
    				if( timer_index == -1 )
    					return;
    				String new_timer_value = timer_values[timer_index];
    				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("preference_timer", new_timer_value);
					editor.apply();
    			}
				@Override
				public int onClickPrev() {
	        		if( timer_index != -1 && timer_index > 0 ) {
	        			timer_index--;
	        			update();
	    				return timer_index;
	        		}
					return -1;
				}
				@Override
				public int onClickNext() {
	                if( timer_index != -1 && timer_index < timer_values.length-1 ) {
	                	timer_index++;
	        			update();
	    				return timer_index;
	        		}
					return -1;
				}
    		});
    	}
	}

    private abstract class ButtonOptionsPopupListener {
		public abstract void onClick(String option);
    }
    
    private void addButtonOptionsToPopup(List<String> supported_options, int icons_id, int values_id, String string, String current_value, final ButtonOptionsPopupListener listener) {
		if( MyDebug.LOG )
			Log.d(TAG, "addButtonOptionsToPopup");
    	if( supported_options != null ) {
	    	final long time_s = System.currentTimeMillis();
        	LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
			if( MyDebug.LOG )
				Log.d(TAG, "addButtonOptionsToPopup time 1: " + (System.currentTimeMillis() - time_s));
        	String [] icons = icons_id != -1 ? getResources().getStringArray(icons_id) : null;
        	String [] values = values_id != -1 ? getResources().getStringArray(values_id) : null;
			if( MyDebug.LOG )
				Log.d(TAG, "addButtonOptionsToPopup time 2: " + (System.currentTimeMillis() - time_s));
        	for(final String supported_option : supported_options) {
        		if( MyDebug.LOG )
        			Log.d(TAG, "supported_option: " + supported_option);
        		int resource = -1;
        		if( icons != null && values != null ) {
            		int index = -1;
            		for(int i=0;i<values.length && index==-1;i++) {
            			if( values[i].equals(supported_option) )
            				index = i;
            		}
            		if( MyDebug.LOG )
            			Log.d(TAG, "index: " + index);
            		if( index != -1 ) {
            			resource = getResources().getIdentifier(icons[index], null, this.getContext().getApplicationContext().getPackageName());
            		}
        		}
    			if( MyDebug.LOG )
    				Log.d(TAG, "addButtonOptionsToPopup time 2.1: " + (System.currentTimeMillis() - time_s));

        		View view = null;
    			final float scale = getResources().getDisplayMetrics().density;
        		if( resource != -1 ) {
        			ImageButton image_button = new ImageButton(this.getContext());
        			if( MyDebug.LOG )
        				Log.d(TAG, "addButtonOptionsToPopup time 2.11: " + (System.currentTimeMillis() - time_s));
        			view = image_button;
        			ll2.addView(view);
        			if( MyDebug.LOG )
        				Log.d(TAG, "addButtonOptionsToPopup time 2.12: " + (System.currentTimeMillis() - time_s));

        			//image_button.setImageResource(resource);
        			final MainActivity main_activity = (MainActivity)this.getContext();
        			Bitmap bm = main_activity.getPreloadedBitmap(resource);
        			if( bm != null )
        				image_button.setImageBitmap(bm);
        			else {
            			if( MyDebug.LOG )
            				Log.d(TAG, "failed to find bitmap for resource " + resource + "!");
        			}
        			if( MyDebug.LOG )
        				Log.d(TAG, "addButtonOptionsToPopup time 2.13: " + (System.currentTimeMillis() - time_s));
        			image_button.setScaleType(ScaleType.FIT_CENTER);
        			final int padding = (int) (10 * scale + 0.5f); // convert dps to pixels
        			view.setPadding(padding, padding, padding, padding);
        		}
        		else {
        			Button button = new Button(this.getContext());
        			view = button;
        			ll2.addView(view);

        			// hack for ISO mode ISO_HJR (e.g., on Samsung S5)
        			if( string.equalsIgnoreCase("ISO") && supported_option.length() >= 4 && supported_option.substring(0, 4).equalsIgnoreCase("ISO_") ) {
            			button.setText(string + "\n" + supported_option.substring(4));
        			}
        			else {
            			button.setText(string + "\n" + supported_option);
        			}
        			button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        			button.setTextColor(Color.WHITE);
        			// need 0 padding so we have enough room to display text for ISO buttons, when there are 6 ISO settings
        			final int padding = (int) (0 * scale + 0.5f); // convert dps to pixels
        			view.setPadding(padding, padding, padding, padding);
        		}
    			if( MyDebug.LOG )
    				Log.d(TAG, "addButtonOptionsToPopup time 2.2: " + (System.currentTimeMillis() - time_s));

    			int total_width = 280;
    			{
    				Activity activity = (Activity)this.getContext();
    			    Display display = activity.getWindowManager().getDefaultDisplay();
    			    DisplayMetrics outMetrics = new DisplayMetrics();
    			    display.getMetrics(outMetrics);

    			    // the height should limit the width, due to when held in portrait
    			    float density  = getResources().getDisplayMetrics().density;
    			    int dpHeight = (int)(outMetrics.heightPixels / density);
        			if( MyDebug.LOG )
        				Log.d(TAG, "dpHeight: " + dpHeight);
        			dpHeight -= 50; // allow space for the icons at top/right of screen
        			if( total_width > dpHeight )
        				total_width = dpHeight;
    			}
    			if( MyDebug.LOG )
    				Log.d(TAG, "total_width: " + total_width);
    			ViewGroup.LayoutParams params = view.getLayoutParams();
    			params.width = (int) ((total_width/supported_options.size()) * scale + 0.5f); // convert dps to pixels
    			params.height = (int) (50 * scale + 0.5f); // convert dps to pixels
    			view.setLayoutParams(params);

    			view.setContentDescription(string);
    			if( supported_option.equals(current_value) ) {
    				view.setAlpha(1.0f);
    			}
    			else {
    				view.setAlpha(0.6f);
    			}
    			if( MyDebug.LOG )
    				Log.d(TAG, "addButtonOptionsToPopup time 2.3: " + (System.currentTimeMillis() - time_s));
    			view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if( MyDebug.LOG )
							Log.d(TAG, "clicked: " + supported_option);
						listener.onClick(supported_option);
					}
    			});
    			this.popup_buttons.put(supported_option, view);
    			if( MyDebug.LOG )
    				Log.d(TAG, "addButtonOptionsToPopup time 2.4: " + (System.currentTimeMillis() - time_s));
    		}
			if( MyDebug.LOG )
				Log.d(TAG, "addButtonOptionsToPopup time 3: " + (System.currentTimeMillis() - time_s));
    		this.addView(ll2);
			if( MyDebug.LOG )
				Log.d(TAG, "addButtonOptionsToPopup time 4: " + (System.currentTimeMillis() - time_s));
        }
    }
    
    private void addRadioOptionsToPopup(List<String> supported_options, final String title, final String preference_key, final String default_option, final String test_key) {
		if( MyDebug.LOG )
			Log.d(TAG, "addOptionsToPopup: " + title);
    	if( supported_options != null ) {
    		final MainActivity main_activity = (MainActivity)this.getContext();

    		TextView text_view = new TextView(this.getContext());
    		text_view.setText(title);
    		text_view.setTextColor(Color.WHITE);
    		text_view.setGravity(Gravity.CENTER);
    		text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
        	this.addView(text_view);

    		RadioGroup rg = new RadioGroup(this.getContext()); 
        	rg.setOrientation(RadioGroup.VERTICAL);
        	this.popup_buttons.put(test_key, rg);

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
			String current_option = sharedPreferences.getString(preference_key, default_option);
        	for(final String supported_option : supported_options) {
        		if( MyDebug.LOG )
        			Log.d(TAG, "supported_option: " + supported_option);
        		//Button button = new Button(this);
        		RadioButton button = new RadioButton(this.getContext());
        		button.setText(supported_option);
        		button.setTextColor(Color.WHITE);
        		if( supported_option.equals(current_option) ) {
        			button.setChecked(true);
        		}
        		else {
        			button.setChecked(false);
        		}
    			//ll.addView(button);
    			rg.addView(button);
    			button.setContentDescription(supported_option);
    			button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if( MyDebug.LOG )
							Log.d(TAG, "clicked current_option: " + supported_option);
						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString(preference_key, supported_option);
						editor.apply();

						main_activity.updateForSettings(title + ": " + supported_option);
						main_activity.closePopup();
					}
    			});
    			this.popup_buttons.put(supported_option, button);
        	}
        	this.addView(rg);
        }
    }
    
    private abstract class ArrayOptionsPopupListener {
		public abstract int onClickPrev();
		public abstract int onClickNext();
    }
    
    private void addArrayOptionsToPopup(final List<String> supported_options, final String title, final int current_index, final ArrayOptionsPopupListener listener) {
		if( supported_options != null && current_index != -1 ) {
    		TextView text_view = new TextView(this.getContext());
    		text_view.setText(title);
    		text_view.setTextColor(Color.WHITE);
    		text_view.setGravity(Gravity.CENTER);
    		text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
        	this.addView(text_view);

        	LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
            
			final TextView resolution_text_view = new TextView(this.getContext());
			resolution_text_view.setText(supported_options.get(current_index));
			resolution_text_view.setTextColor(Color.WHITE);
			resolution_text_view.setGravity(Gravity.CENTER);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
			resolution_text_view.setLayoutParams(params);

			final float scale = getResources().getDisplayMetrics().density;
			final Button prev_button = new Button(this.getContext());
			ll2.addView(prev_button);
			prev_button.setText("<");
			prev_button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
			final int padding = (int) (0 * scale + 0.5f); // convert dps to pixels
			prev_button.setPadding(padding, padding, padding, padding);
			ViewGroup.LayoutParams vg_params = prev_button.getLayoutParams();
			vg_params.width = (int) (60 * scale + 0.5f); // convert dps to pixels
			vg_params.height = (int) (50 * scale + 0.5f); // convert dps to pixels
			prev_button.setLayoutParams(vg_params);
			prev_button.setVisibility( (current_index > 0) ? View.VISIBLE : View.INVISIBLE);

        	ll2.addView(resolution_text_view);

			final Button next_button = new Button(this.getContext());
			ll2.addView(next_button);
			next_button.setText(">");
			next_button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
			next_button.setPadding(padding, padding, padding, padding);
			vg_params = next_button.getLayoutParams();
			vg_params.width = (int) (60 * scale + 0.5f); // convert dps to pixels
			vg_params.height = (int) (50 * scale + 0.5f); // convert dps to pixels
			next_button.setLayoutParams(vg_params);
			next_button.setVisibility( (current_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);

			prev_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
        			int new_index = listener.onClickPrev();
        			if( new_index != -1 ) {
        				resolution_text_view.setText(supported_options.get(new_index));
	        			prev_button.setVisibility( (new_index > 0) ? View.VISIBLE : View.INVISIBLE);
	        			next_button.setVisibility( (new_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);
        			}
				}
			});
			next_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
        			int new_index = listener.onClickNext();
        			if( new_index != -1 ) {
        				resolution_text_view.setText(supported_options.get(new_index));
	        			prev_button.setVisibility( (new_index > 0) ? View.VISIBLE : View.INVISIBLE);
	        			next_button.setVisibility( (new_index < supported_options.size()-1) ? View.VISIBLE : View.INVISIBLE);
        			}
				}
			});

			this.addView(ll2);
    	}
    }

    void close() {
		if( MyDebug.LOG )
			Log.d(TAG, "close");
		popup_buttons.clear();
    }

    // for testing
    View getPopupButton(String key) {
    	return popup_buttons.get(key);
    }
}
