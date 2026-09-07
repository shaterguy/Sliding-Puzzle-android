package com.shaterguy.slidingpuzzle;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

final class BoardView extends android.widget.FrameLayout {
 interface MoveListener {void move(int index);}
 private final Puzzle puzzle;private final Bitmap photo;private final MoveListener listener;private final boolean tap;
 private int edge; private final float density;
 BoardView(Context context,Puzzle puzzle,Bitmap photo,boolean tap,MoveListener listener){
  super(context);this.puzzle=puzzle;this.photo=photo;this.listener=listener;this.tap=tap;density=getResources().getDisplayMetrics().density;
  setId(200);setContentDescription("퍼즐 보드");setClipChildren(false);setClipToPadding(false);
  for(int i=0;i<puzzle.size*puzzle.size;i++){Tile t=new Tile(context,i);addView(t);}
 }
 protected void onMeasure(int w,int h){edge=Math.min(MeasureSpec.getSize(w),(int)(560*density));setMeasuredDimension(edge,edge);int cell=edge/puzzle.size;for(int i=0;i<getChildCount();i++)getChildAt(i).measure(MeasureSpec.makeMeasureSpec(cell,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(cell,MeasureSpec.EXACTLY));}
 protected void onLayout(boolean changed,int l,int t,int r,int b){int cell=edge/puzzle.size;for(int i=0;i<getChildCount();i++){int x=i%puzzle.size*cell,y=i/puzzle.size*cell;getChildAt(i).layout(x,y,x+cell,y+cell);}}
 void animateMove(int from,int blank,Runnable finished){
  View tile=getChildAt(from);float cell=edge/(float)puzzle.size;
  long duration=android.animation.ValueAnimator.areAnimatorsEnabled()?160:0;
  tile.setElevation(6*density);tile.animate().translationX((blank%puzzle.size-from%puzzle.size)*cell).translationY((blank/puzzle.size-from/puzzle.size)*cell).setDuration(duration).withEndAction(finished).start();
 }
 @Override protected void onDetachedFromWindow(){for(int i=0;i<getChildCount();i++)getChildAt(i).animate().cancel();super.onDetachedFromWindow();}
 final class Tile extends View {
  final int index,value;final Paint paint=new Paint(3);float sx,sy;boolean cancelled;
  Tile(Context context,int index){super(context);this.index=index;value=puzzle.tile(index);
   setId(1000+index);setFocusable(value!=0);setClickable(value!=0);setImportantForAccessibility(value==0?IMPORTANT_FOR_ACCESSIBILITY_NO:IMPORTANT_FOR_ACCESSIBILITY_YES);
   if(value!=0)setContentDescription(String.format(Locale.KOREAN,"%d번 조각, %d행 %d열%s",value,index/puzzle.size+1,index%puzzle.size+1,puzzle.adjacent(index)?", 빈칸으로 이동 가능":""));
  }
  protected void onDraw(Canvas c){
   float gap=photo==null?4*density:1.5f*density;RectF rect=new RectF(gap,gap,getWidth()-gap,getHeight()-gap);
   if(value==0&&(!puzzle.solved()||photo==null)){paint.setColor(Color.rgb(226,233,225));c.drawRoundRect(rect,12*density,12*density,paint);return;}
   int actual=value==0?puzzle.size*puzzle.size:value;
   if(photo!=null){
    int s=photo.getWidth();Rect source=new Rect((actual-1)%puzzle.size*s/puzzle.size,(actual-1)/puzzle.size*s/puzzle.size,((actual-1)%puzzle.size+1)*s/puzzle.size,((actual-1)/puzzle.size+1)*s/puzzle.size);
    c.save();Path clip=new Path();clip.addRoundRect(rect,7*density,7*density,Path.Direction.CW);c.clipPath(clip);c.drawBitmap(photo,source,rect,paint);c.restore();
   }else{
    paint.setColor(puzzle.adjacent(index)?Color.rgb(8,127,140):Color.rgb(255,185,143));c.drawRoundRect(rect,12*density,12*density,paint);
    paint.setColor(puzzle.adjacent(index)?Color.WHITE:Color.rgb(36,52,73));paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextSize(getWidth()*.38f);paint.setTextAlign(Paint.Align.CENTER);
    c.drawText(String.valueOf(actual),getWidth()/2f,getHeight()/2f-(paint.ascent()+paint.descent())/2,paint);
   }
  }
  public boolean onTouchEvent(MotionEvent e){
   if(value==0||puzzle.solved())return false;
   switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:sx=e.getX();sy=e.getY();cancelled=false;getParent().requestDisallowInterceptTouchEvent(true);return true;
    case MotionEvent.ACTION_CANCEL:cancelled=true;getParent().requestDisallowInterceptTouchEvent(false);return true;
    case MotionEvent.ACTION_UP:
     getParent().requestDisallowInterceptTouchEvent(false);if(cancelled)return true;
     float dx=e.getX()-sx,dy=e.getY()-sy;float distance=(float)Math.hypot(dx,dy);int blank=puzzle.blank();
     if(distance<12*density){if(tap)performClick();return true;}
     if(!puzzle.adjacent(index))return true;
     int row=blank/puzzle.size-index/puzzle.size,col=blank%puzzle.size-index%puzzle.size;
     if(distance>=24*density&&((col!=0&&Math.abs(dx)>Math.abs(dy)&&dx*col>0)||(row!=0&&Math.abs(dy)>Math.abs(dx)&&dy*row>0)))listener.move(index);
     return true;
    default:return true;
   }
  }
  public boolean performClick(){super.performClick();listener.move(index);return true;}
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.Button");if(value!=0&&puzzle.adjacent(index))info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK,"빈칸으로 이동"));}
  public boolean performAccessibilityAction(int action,Bundle args){if(action==AccessibilityNodeInfo.ACTION_CLICK&&value!=0){listener.move(index);return true;}return super.performAccessibilityAction(action,args);}
 }
}
