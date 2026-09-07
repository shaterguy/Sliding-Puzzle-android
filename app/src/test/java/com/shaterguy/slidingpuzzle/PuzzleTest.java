package com.shaterguy.slidingpuzzle;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;
public class PuzzleTest {
 @Test public void shuffleIsSolvablePermutationForEverySize(){
  for(int size=3;size<=6;size++)for(int seed=0;seed<80;seed++){
   Puzzle p=new Puzzle(size);p.shuffle(new Random(seed));assertFalse(p.solved());assertTrue(p.solvable());assertEquals(0,p.moves);
   boolean[] seen=new boolean[size*size];for(int t:p.snapshot()){assertFalse(seen[t]);seen[t]=true;}
   Puzzle saved=Puzzle.restore(size,p.encode(),p.moves);assertArrayEquals(p.snapshot(),saved.snapshot());
  }
 }
 @Test public void movesRespectEdgesAndCompletion(){
  Puzzle p=new Puzzle(3);assertFalse(p.move(0));assertFalse(p.move(6));assertTrue(p.move(7));assertFalse(p.solved());assertEquals(1,p.moves);
  assertTrue(p.move(8));assertTrue(p.solved());assertEquals(2,p.moves);
 }
 @Test(expected=IllegalArgumentException.class) public void rejectsUnsolvableState(){Puzzle.restore(3,"2,1,3,4,5,6,7,8,0",0);}
 @Test(expected=IllegalArgumentException.class) public void rejectsDuplicateState(){Puzzle.restore(3,"1,1,3,4,5,6,7,8,0",0);}
 @Test public void evenWidthParity(){Puzzle p=new Puzzle(4);assertTrue(p.solvable());assertTrue(p.move(11));assertTrue(p.solvable());assertTrue(p.move(10));assertTrue(p.solvable());}
}
