package com.shaterguy.slidingpuzzle;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/** Move-based achievement thresholds. Threshold counts are intentionally not exposed by the UI. */
final class StarRating {
 final int size;
 final int baseline;
 final int threeStarLimit;
 final int twoStarLimit;
 final int visualCap;

 private StarRating(int size,int baseline){
  if(size<3||size>6||baseline<1)throw new IllegalArgumentException("rating");
  this.size=size;this.baseline=baseline;
  int threeBuffer=Math.max(size*size,(baseline+1)/2);
  threeStarLimit=baseline+threeBuffer;
  int generousTwoBuffer=Math.max(size*size*6,baseline*2);
  twoStarLimit=threeStarLimit+generousTwoBuffer;
  visualCap=twoStarLimit+Math.max(size*size*3,baseline);
 }

 static StarRating forPuzzle(Puzzle puzzle){return fromBaseline(puzzle.size,minimumMoveBaseline(puzzle));}
 static StarRating fromBaseline(int size,int baseline){return new StarRating(size,Math.max(1,baseline));}
 static int legacyBaseline(Puzzle puzzle){return Math.max(Math.max(1,puzzle.moves),minimumMoveBaseline(puzzle));}

 int starsForMoves(int moves){
  if(moves<=threeStarLimit)return 3;
  if(moves<=twoStarLimit)return 2;
  return 1;
 }
 float progress(int moves){return Math.min(1f,Math.max(0,moves)/(float)visualCap);}
 float threeMarker(){return threeStarLimit/(float)visualCap;}
 float twoMarker(){return twoStarLimit/(float)visualCap;}

 /** Exact optimal distance for 3x3; admissible Manhattan lower bound for larger boards. */
 static int minimumMoveBaseline(Puzzle puzzle){
  int[] tiles=puzzle.snapshot();int h=manhattan(puzzle.size,tiles);
  if(puzzle.size!=3)return Math.max(1,h);
  return Math.max(1,exactThreeByAStar(tiles,h));
 }

 private static int manhattan(int size,int[] tiles){
  int total=0;
  for(int pos=0;pos<tiles.length;pos++){
   int tile=tiles[pos];if(tile==0)continue;int goal=tile-1;
   total+=Math.abs(pos/size-goal/size)+Math.abs(pos%size-goal%size);
  }
  return total;
 }
 private static long encode(int[] tiles){long key=0;for(int i=0;i<tiles.length;i++)key|=((long)tiles[i])<<(i*4);return key;}
 private static int tile(long key,int pos){return (int)((key>>(pos*4))&0xFL);}
 private static long moveBlank(long key,int blank,int next){
  long value=(long)tile(key,next);long clearBlank=~(0xFL<<(blank*4));long clearNext=~(0xFL<<(next*4));
  return (key&clearBlank&clearNext)|(value<<(blank*4));
 }
 private static int manhattan3(long key){
  int total=0;for(int pos=0;pos<9;pos++){int value=tile(key,pos);if(value==0)continue;int goal=value-1;total+=Math.abs(pos/3-goal/3)+Math.abs(pos%3-goal%3);}return total;
 }
 private static int exactThreeByAStar(int[] start,int initialH){
  long startKey=encode(start);long goal=encode(new int[]{1,2,3,4,5,6,7,8,0});if(startKey==goal)return 0;
  int blank=0;while(start[blank]!=0)blank++;
  PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingInt(Node::f).thenComparingInt(n->n.h));
  HashMap<Long,Integer> best=new HashMap<>();open.add(new Node(startKey,blank,0,initialH));best.put(startKey,0);
  while(!open.isEmpty()){
   Node node=open.poll();Integer known=best.get(node.key);if(known==null||known!=node.g)continue;if(node.key==goal)return node.g;
   int row=node.blank/3,col=node.blank%3;
   if(row>0)offer(open,best,node,node.blank-3);
   if(row<2)offer(open,best,node,node.blank+3);
   if(col>0)offer(open,best,node,node.blank-1);
   if(col<2)offer(open,best,node,node.blank+1);
  }
  return initialH;
 }
 private static void offer(PriorityQueue<Node> open,HashMap<Long,Integer> best,Node node,int nextBlank){
  long next=moveBlank(node.key,node.blank,nextBlank);int g=node.g+1;Integer old=best.get(next);if(old!=null&&old<=g)return;
  best.put(next,g);open.add(new Node(next,nextBlank,g,manhattan3(next)));
 }
 private static final class Node{
  final long key;final int blank,g,h;Node(long key,int blank,int g,int h){this.key=key;this.blank=blank;this.g=g;this.h=h;}int f(){return g+h;}
 }
}
