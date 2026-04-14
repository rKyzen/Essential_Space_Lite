package com.betterglyph;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class VisualizerTileService extends TileService {
    @Override public void onStartListening() { super.onStartListening(); refresh(); }
    @Override public void onClick() {
        super.onClick();
        if (AudioCaptureService.isRunning()) {
            stopService(new Intent(this,AudioCaptureService.class));
            refresh(false);
        } else {
            unlockAndRun(()->{
                Intent i=new Intent(this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityAndCollapse(i);
            });
        }
    }
    private void refresh() { refresh(AudioCaptureService.isRunning()); }
    private void refresh(boolean on) {
        Tile t=getQsTile(); if(t==null) return;
        t.setState(on?Tile.STATE_ACTIVE:Tile.STATE_INACTIVE);
        t.setLabel(on?"Glyph Viz ON":"Glyph Visualizer");
        t.setSubtitle(on?"Tap to stop":"Tap to open");
        t.setIcon(Icon.createWithResource(this,android.R.drawable.ic_media_play));
        t.updateTile();
    }
}
