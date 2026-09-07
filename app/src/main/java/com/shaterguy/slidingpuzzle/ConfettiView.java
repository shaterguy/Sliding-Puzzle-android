package com.shaterguy.slidingpuzzle;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.animation.ValueAnimator;
import java.util.Random;

final class ConfettiView extends View {
 private final Paint paint=new Paint(3);private final float[] x=new float[80],speed=new float[80],angle=new float[80];private float progress;private ValueAnimator animator;
 private final int[] colors={0xff087F8C,0xffFFB98F,0xffFFD45C,0xff9BCDAD,0xffD993C3};
 ConfettiView(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);Random r=new Random(17);for(int i=0;i<x.length;i++){x[i]=r.nextFloat();speed[i]=.6f+r.nextFloat()*.8f;angle[i]=r.nextFloat()*360;}}
 void start(){if(!ValueAnimator.areAnimatorsEnabled()){progress=.45f;invalidate();return;}animator=ValueAnimator.ofFloat(0,1);animator.setDuration(2600);animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.start();}
 void stop(){if(animator!=null){animator.cancel();animator=null;}setVisibility(GONE);}
 protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
 protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density;for(int i=0;i<x.length;i++){paint.setColor(colors[i%colors.length]);paint.setAlpha((int)(255*Math.min(1,(1-progress)*3)));float px=x[i]*getWidth()+30*d*(float)Math.sin(progress*7+i),py=-60*d+progress*getHeight()*speed[i];c.save();c.translate(px,py);c.rotate(angle[i]+progress*400);c.drawRoundRect(-3*d,-6*d,3*d,6*d,2*d,2*d,paint);c.restore();}}
}
