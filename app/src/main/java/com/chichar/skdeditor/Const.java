package com.chichar.skdeditor;

import android.content.Context;
import android.content.SharedPreferences;

public final class Const {
	public static String pkg = "com.ChillyRoom.DungeonShooter";

	public static void load(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE);
		pkg = prefs.getString("pkg", "com.ChillyRoom.DungeonShooter");
	}

	public static void save(Context ctx, String newPkg) {
		SharedPreferences.Editor editor = ctx.getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).edit();
		editor.putString("pkg", newPkg);
		editor.apply();
		pkg = newPkg;
	}
}
