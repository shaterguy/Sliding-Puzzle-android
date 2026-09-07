package com.shaterguy.slidingpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Visual-only star budget meter. Numeric move thresholds are deliberately hidden. */
final class StarMeterView extends View {
 private static final int INK=0xff243449,TEAL=0xff087F8C,GOLD=0xffE5A500,LOST=0xffAAA39A,TRACK=0xffE7E0D4;
 private final Paint paint=new Paint(3);private final float density;private final StarRating rating;private int moves;
 StarMeterView(Context context,StarRating rating,int moves){
  super(context);this.rating=rating;this.moves=Math.max(0,moves);density=getResources().getDisplayMetrics().density;setId(204);updateAccessibility();
 }
 void setMoves(int moves){this.moves=Math.max(0,moves);updateAccessibility();invalidate();}
 private void updateAccessibility(){int stars=rating.starsForMoves(moves);setContentDescription(stars==1?"현재 별 1개 보장 구간":"현재 별 "+stars+"개 획득 가능 구간");}
 @Override protected void onMeasure(int widthSpec,int heightSpec){int width=MeasureSpec.getSize(widthSpec);if(width==0)width=(int)(320*density);setMeasuredDimension(width,(int)(78*density));}
 @Override protected void onDraw(Canvas canvas){
  super.onDraw(canvas);float left=10*density,right=getWidth()-10*density,trackY=48*density,trackH=11*density,width=right-left;
  float x3=left+width*rating.threeMarker(),x2=left+width*rating.twoMarker(),x1=right;
  int eligible=rating.starsForMoves(moves);drawLabel(canvas,"★★★",x3,eligible==3?GOLD:LOST);drawLabel(canvas,"★★",x2,eligible==2?GOLD:(eligible<2?LOST:INK));drawLabel(canvas,"★",x1,eligible==1?GOLD:INK);
  paint.setStyle(Paint.Style.FILL);paint.setColor(TRACK);RectF track=new RectF(left,trackY,right,trackY+trackH);canvas.drawRoundRect(track,trackH/2,trackH/2,paint);
  float fill=left+width*rating.progress(moves);paint.setColor(TEAL);canvas.drawRoundRect(new RectF(left,trackY,Math.max(left,fill),trackY+trackH),trackH/2,trackH/2,paint);
  drawTick(canvas,x3,trackY,trackH);drawTick(canvas,x2,trackY,trackH);drawTick(canvas,x1,trackY,trackH);
  paint.setColor(INK);paint.setStyle(Paint.Style.FILL);canvas.drawCircle(Math.min(right,fill),trackY+trackH/2,5*density,paint);
 }
 private void drawTick(Canvas canvas,float x,float y,float h){paint.setColor(INK);paint.setStrokeWidth(1.5f*density);canvas.drawLine(x,y-4*density,x,y+h+4*density,paint);}
 private void drawLabel(Canvas canvas,String text,float center,int color){
  paint.setStyle(Paint.Style.FILL);paint.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));paint.setTextSize(18*density);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(color);
  float half=paint.measureText(text)/2;float x=Math.max(half+2*density,Math.min(getWidth()-half-2*density,center));canvas.drawText(text,x,24*density,paint);
 }
}
