package com.shaterguy.slidingpuzzle;
import android.content.*;
import android.app.Activity;
import android.app.Instrumentation;
import android.provider.MediaStore;
import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.intent.matcher.IntentMatchers.*;
import android.net.Uri;
import android.view.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.action.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.File;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.action.ViewActions.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameUiTest {
 private Context context;
 @Before public void reset(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();context.getSharedPreferences("puzzle",0).edit().clear().commit();for(File f:new PhotoStore(context).list())f.delete();}
 private void nearComplete(boolean tap){
  context.getSharedPreferences("puzzle",0).edit().putString("screen","game").putString("mode","number").putInt("size",3).putString("tiles","1,2,3,4,5,6,7,0,8").putInt("moves",4).putInt("starBaseline",1).putBoolean("tap",tap).commit();
 }
 private void idle(){InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
 private void waitForPhoto(ActivityScenario<MainActivity> scenario)throws Exception{
  idle();long deadline=android.os.SystemClock.elapsedRealtime()+10000;boolean[] pending={true};
  while(pending[0]&&android.os.SystemClock.elapsedRealtime()<deadline){scenario.onActivity(a->pending[0]=a.s.pending);if(pending[0])Thread.sleep(100);}
  assertFalse("Photo import must finish",pending[0]);idle();
 }
 @Test public void defaultTapDoesNotMoveButSwipeCompletes()throws Exception{
  nearComplete(false);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   onView(withId(204)).check(matches(isDisplayed()));
   onView(withId(1008)).perform(click());scenario.onActivity(a->assertEquals(4,a.s.puzzle.moves));
   onView(withId(1008)).perform(swipeRight());scenario.onActivity(a->assertEquals(4,a.s.puzzle.moves));
   onView(withId(1008)).perform(swipeLeft());
   scenario.onActivity(a->{assertTrue("Inward swipe must move the tile",a.s.puzzle.solved());assertEquals(5,a.s.puzzle.moves);});
   onView(withId(202)).check(matches(withText("완성했어요!")));onView(withId(203)).check(matches(withText("★★★")));onView(withId(205)).check(matches(isDisplayed()));
   scenario.onActivity(a->{assertTrue(a.s.puzzle.solved());assertEquals(5,a.s.puzzle.moves);assertEquals(1,a.s.starBaseline);});
  }
 }
 @Test public void generousTwoStarBandShowsTwoLargeStars(){
  context.getSharedPreferences("puzzle",0).edit().putString("screen","game").putString("mode","number").putInt("size",3).putString("tiles","1,2,3,4,5,6,7,0,8").putInt("moves",20).putInt("starBaseline",1).putBoolean("tap",true).commit();
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   onView(withId(1008)).perform(click());onView(withId(203)).check(matches(withText("★★")));onView(withText("별 2개 획득!")).check(matches(isDisplayed()));onView(withId(205)).check(matches(isDisplayed()));
   scenario.onActivity(a->assertEquals("done",a.s.screen));
  }
 }
 @Test public void tapSettingPersistsAndCompletes(){
  nearComplete(false);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   onView(withId(111)).perform(click());onView(withId(400)).perform(click());onView(withText("완료")).perform(click());
   scenario.recreate();onView(withId(1008)).perform(click());onView(withId(202)).check(matches(isDisplayed()));
   assertTrue(context.getSharedPreferences("puzzle",0).getBoolean("tap",false));
  }
 }
 @Test public void rotationAndRelaunchPreserveBoardAndStarBaseline(){
  nearComplete(false);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.recreate();scenario.onActivity(a->{assertEquals("1,2,3,4,5,6,7,0,8",a.s.puzzle.encode());assertEquals(4,a.s.puzzle.moves);assertEquals(1,a.s.starBaseline);});
  }
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{assertEquals("1,2,3,4,5,6,7,0,8",a.s.puzzle.encode());assertEquals(1,a.s.starBaseline);});
  }
 }
 @Test public void coldShareImportsReusesAndDeletesOnlyCopy()throws Exception{
  Uri fixture=Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/photo");
  Intent share=new Intent(context,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,fixture).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  String[] name={""};
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(share)){
   waitForPhoto(scenario);onView(withId(106)).check(matches(isDisplayed()));
   scenario.onActivity(a->{assertEquals("prepare",a.s.screen);assertEquals(a.s.bitmap.getWidth(),a.s.bitmap.getHeight());name[0]=a.s.photo;});
   onView(withId(106)).perform(scrollTo(),click());scenario.recreate();
   scenario.onActivity(a->{assertEquals("photo",a.s.mode);assertEquals(name[0],a.s.photo);assertNotNull(a.s.bitmap);assertTrue(a.s.starBaseline>0);});
   onView(withId(110)).perform(click());onView(withId(102)).perform(click());
   onView(withText("이 사진 사용")).perform(scrollTo(),click());waitForPhoto(scenario);
   scenario.onActivity(a->assertEquals(name[0],a.s.photo));
   onView(withText("삭제")).perform(scrollTo(),click());onView(withId(android.R.id.button1)).perform(click());
   assertFalse(new PhotoStore(context).file(name[0]).exists());
  }
  try(java.io.InputStream in=context.getContentResolver().openInputStream(fixture)){assertNotNull(in);assertTrue(in.read()!=-1);}
 }
 @Test public void warmShareAndCorruptPhotoAreHandled()throws Exception{
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->a.startActivity(new Intent(a,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/photo"))));
   waitForPhoto(scenario);String[] before={""};scenario.onActivity(a->before[0]=a.s.photo);
   scenario.onActivity(a->a.onNewIntent(new Intent(a,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/broken"))));
   waitForPhoto(scenario);scenario.onActivity(a->{assertEquals(before[0],a.s.photo);assertNotNull(a.s.bitmap);assertEquals("prepare",a.s.screen);});
  }
 }
 @Test public void completedPhotoFillsLastTileAndLocksProgress()throws Exception{
  Intent share=new Intent(context,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/photo"));
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(share)){
   waitForPhoto(scenario);context.getSharedPreferences("puzzle",0).edit().putBoolean("tap",true).commit();
   scenario.onActivity(a->{a.s.puzzle=Puzzle.restore(3,"1,2,3,4,5,6,7,0,8",12);a.s.size=3;a.s.starBaseline=1;a.s.screen="game";});
   scenario.recreate();onView(withId(1008)).perform(click());onView(withId(202)).check(matches(isDisplayed()));
   long[] duration={0};
   scenario.onActivity(a->{
    assertTrue(a.s.puzzle.solved());assertEquals(13,a.s.puzzle.moves);duration[0]=a.s.elapsed;
    View last=a.findViewById(1008);android.graphics.Bitmap drawn=android.graphics.Bitmap.createBitmap(last.getWidth(),last.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
    last.draw(new android.graphics.Canvas(drawn));int actual=drawn.getPixel(last.getWidth()/2,last.getHeight()/2);
    int expected=a.s.bitmap.getPixel(a.s.bitmap.getWidth()*5/6,a.s.bitmap.getHeight()*5/6);
    assertEquals(expected,actual);drawn.recycle();
   });
   onView(withId(1000)).perform(click());scenario.recreate();
   scenario.onActivity(a->{assertEquals(13,a.s.puzzle.moves);assertEquals(duration[0],a.s.elapsed);assertEquals("done",a.s.screen);assertEquals(1,a.s.starBaseline);});
  }
 }
 @Test public void pickerButtonImportsReturnedPhoto()throws Exception{
  Intents.init();
  try{
   Intent result=new Intent().setData(Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/photo"));
   String action=android.os.Build.VERSION.SDK_INT>=33?MediaStore.ACTION_PICK_IMAGES:Intent.ACTION_OPEN_DOCUMENT;
   intending(hasAction(action)).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK,result));
   try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
    onView(withId(102)).perform(click());onView(withId(107)).perform(scrollTo(),click());
    intended(hasAction(action));waitForPhoto(scenario);onView(withId(106)).check(matches(isDisplayed()));
    scenario.onActivity(a->{assertEquals("photo",a.s.mode);assertNotNull(a.s.bitmap);});
   }
  }finally{Intents.release();}
 }
 @Test public void inaccessibleUriAndNonAdjacentMoveDoNotChangeGame()throws Exception{
  nearComplete(true);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   onView(withId(1000)).perform(click());scenario.onActivity(a->assertEquals(4,a.s.puzzle.moves));
   scenario.onActivity(a->{a.s.puzzle.moves=0;a.onNewIntent(new Intent(a,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("file:///etc/passwd")));});
   waitForPhoto(scenario);scenario.onActivity(a->{assertEquals("game",a.s.screen);assertEquals("1,2,3,4,5,6,7,0,8",a.s.puzzle.encode());});
  }
 }
}
