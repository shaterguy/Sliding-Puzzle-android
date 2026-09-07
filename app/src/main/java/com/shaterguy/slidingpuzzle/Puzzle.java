package com.shaterguy.slidingpuzzle;

import java.util.ArrayList;
import java.util.Random;

/** Zero is the blank. All shuffles are random walks along legal edges. */
public final class Puzzle {
 public final int size;
 private final int[] tiles;
 public int moves;
 public Puzzle(int size) {
  if(size<3 || size>6) throw new IllegalArgumentException("size");
  this.size=size; tiles=new int[size*size];
  for(int i=0;i<tiles.length-1;i++) tiles[i]=i+1;
 }
 public int tile(int index) { return tiles[index]; }
 public int[] snapshot() { return tiles.clone(); }
 public int blank() { for(int i=0;i<tiles.length;i++) if(tiles[i]==0)return i; throw new IllegalStateException(); }
 public boolean adjacent(int index) {
  if(index<0 || index>=tiles.length) return false;
  int b=blank(); return Math.abs(index/size-b/size)+Math.abs(index%size-b%size)==1;
 }
 public boolean move(int index) {
  if(!adjacent(index))return false;
  int b=blank(); tiles[b]=tiles[index]; tiles[index]=0; moves++; return true;
 }
 public boolean solved() {
  for(int i=0;i<tiles.length-1;i++) if(tiles[i]!=i+1)return false;
  return tiles[tiles.length-1]==0;
 }
 public void shuffle(Random random) {
  int previous=-1;
  for(int k=0;k<size*size*60;k++) {
   int old=blank(); ArrayList<Integer> options=new ArrayList<>();
   for(int i=0;i<tiles.length;i++) if(i!=previous && adjacent(i))options.add(i);
   move(options.get(random.nextInt(options.size()))); previous=old;
  }
  if(solved())move(blank()-1);
  moves=0;
 }
 public String encode() {
  StringBuilder out=new StringBuilder();
  for(int t:tiles){ if(out.length()>0)out.append(',');out.append(t); }return out.toString();
 }
 public static Puzzle restore(int size,String encoded,int moves) {
  Puzzle p=new Puzzle(size);String[] parts=encoded.split(",");
  if(parts.length!=size*size || moves<0)throw new IllegalArgumentException("state");
  boolean[] seen=new boolean[parts.length];
  for(int i=0;i<parts.length;i++){int t=Integer.parseInt(parts[i]);if(t<0||t>=parts.length||seen[t])throw new IllegalArgumentException("tiles");seen[t]=true;p.tiles[i]=t;}
  if(!p.solvable())throw new IllegalArgumentException("parity");
  p.moves=moves;return p;
 }
 public boolean solvable() {
  int inversions=0;for(int i=0;i<tiles.length;i++)for(int j=i+1;j<tiles.length;j++)if(tiles[i]!=0&&tiles[j]!=0&&tiles[i]>tiles[j])inversions++;
  return size%2==1?inversions%2==0:(inversions+size-blank()/size)%2==1;
 }
}
