package com.shaterguy.slidingpuzzle;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.animation.*;
import java.util.Random;

final class ConfettiView extends View {
 private static final int GOLD=0xffF1B51D,INK=0xff243449;
 private final Paint paint=new Paint(3);private final float[] x=new float[80],speed=new float[80],angle=new float[80];private final int stars;private float progress;private ValueAnimator animator;
 private final int[] colors={0xff087F8C,0xffFFB98F,0xffFFD45C,0xff9BCDAD,0xffD993C3};
 ConfettiView(Context c,int stars){super(c);this.stars=Math.max(1,Math.min(3,stars));setId(205);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);Random r=new Random(17);for(int i=0;i<x.length;i++){x[i]=r.nextFloat();speed[i]=.6f+r.nextFloat()*.8f;angle[i]=r.nextFloat()*360;}}
 void start(){
  if(!ValueAnimator.areAnimatorsEnabled()){progress=.35f;invalidate();postDelayed(this::stop,1800);return;}
  animator=ValueAnimator.ofFloat(0,1);animator.setDuration(2800);animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.addListener(new AnimatorListenerAdapter(){@Override public void onAnimationEnd(Animator a){postDelayed(()->{if(animator==a)stop();},120);}});animator.start();
 }
 void stop(){if(animator!=null){animator.cancel();animator=null;}setVisibility(GONE);}
 protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
 private String stars(){StringBuilder out=new StringBuilder();for(int i=0;i<stars;i++)out.append('★');return out.toString();}
 protected void onDraw(Canvas c){
  float d=getResources().getDisplayMetrics().density;
  for(int i=0;i<x.length;i++){paint.setStyle(Paint.Style.FILL);paint.setColor(colors[i%colors.length]);paint.setAlpha((int)(255*Math.min(1,(1-progress)*3)));float px=x[i]*getWidth()+30*d*(float)Math.sin(progress*7+i),py=-60*d+progress*getHeight()*speed[i];c.save();c.translate(px,py);c.rotate(angle[i]+progress*400);c.drawRoundRect(-3*d,-6*d,3*d,6*d,2*d,2*d,paint);c.restore();}
  float fade=progress<.70f?1f:Math.max(0,(1-progress)/.30f);int alpha=(int)(255*fade);float center=getWidth()/2f,y=Math.max(110*d,getHeight()*.24f);
  paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(64*d);paint.setStrokeWidth(7*d);paint.setStyle(Paint.Style.STROKE);paint.setColor(INK);paint.setAlpha(alpha);c.drawText(stars(),center,y,paint);
  paint.setStyle(Paint.Style.FILL);paint.setColor(GOLD);paint.setAlpha(alpha);c.drawText(stars(),center,y,paint);
  paint.setTextSize(24*d);paint.setColor(INK);paint.setAlpha(alpha);c.drawText("별 "+stars+"개 획득!",center,y+40*d,paint);
 }
}
