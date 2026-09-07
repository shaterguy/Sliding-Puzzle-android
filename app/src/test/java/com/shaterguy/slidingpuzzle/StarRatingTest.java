package com.shaterguy.slidingpuzzle;

import org.junit.Test;
import static org.junit.Assert.*;

public class StarRatingTest {
 @Test public void exactMinimumIsUsedForThreeByThree(){
  Puzzle one=Puzzle.restore(3,"1,2,3,4,5,6,7,0,8",0);assertEquals(1,StarRating.minimumMoveBaseline(one));
  Puzzle two=Puzzle.restore(3,"1,2,3,4,5,6,0,7,8",0);assertEquals(2,StarRating.minimumMoveBaseline(two));
 }
 @Test public void largerBoardsUseMinimumMoveLowerBound(){
  Puzzle p=Puzzle.restore(4,"1,2,3,4,5,6,7,8,9,10,11,12,13,14,0,15",0);assertEquals(1,StarRating.minimumMoveBaseline(p));
 }
 @Test public void threeStarsHaveBufferAndTwoStarsAreGenerous(){
  StarRating r=StarRating.fromBaseline(4,40);assertTrue(r.threeStarLimit>40);assertTrue(r.twoStarLimit-r.threeStarLimit>=4*4*6);
  assertEquals(3,r.starsForMoves(r.threeStarLimit));assertEquals(2,r.starsForMoves(r.threeStarLimit+1));assertEquals(2,r.starsForMoves(r.twoStarLimit));assertEquals(1,r.starsForMoves(r.twoStarLimit+1));
 }
 @Test public void completionAlwaysGetsAtLeastOneStarAndMeterIsOrdered(){
  StarRating r=StarRating.fromBaseline(6,80);assertEquals(1,r.starsForMoves(Integer.MAX_VALUE));assertTrue(r.threeMarker()>0);assertTrue(r.threeMarker()<r.twoMarker());assertTrue(r.twoMarker()<1f);assertEquals(1f,r.progress(Integer.MAX_VALUE),0f);
 }
 @Test public void legacyGamesAreNotPenalizedByMissingBaseline(){
  Puzzle p=Puzzle.restore(3,"1,2,3,4,5,6,7,0,8",40);assertTrue(StarRating.legacyBaseline(p)>=40);
 }
}
