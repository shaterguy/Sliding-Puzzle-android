package com.shaterguy.slidingpuzzle;
import android.content.*;
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
 @Before public void reset(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();context.getSharedPreferences("puzzle",0).edit().clear().commit();}
 private void nearComplete(boolean tap){
  context.getSharedPreferences("puzzle",0).edit().putString("screen","game").putString("mode","number").putInt("size",3).putString("tiles","1,2,3,4,5,6,7,0,8").putInt("moves",4).putBoolean("tap",tap).commit();
 }
 private void idle(){InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
 private void waitForPhoto(ActivityScenario<MainActivity> scenario)throws Exception{
  long deadline=android.os.SystemClock.elapsedRealtime()+10000;boolean[] pending={true};
  while(pending[0]&&android.os.SystemClock.elapsedRealtime()<deadline){scenario.onActivity(a->pending[0]=a.s.pending);if(pending[0])Thread.sleep(100);}
  assertFalse("Photo import must finish",pending[0]);idle();
 }
 @Test public void defaultTapDoesNotMoveButSwipeCompletes()throws Exception{
  nearComplete(false);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   onView(withId(1008)).perform(click());scenario.onActivity(a->assertEquals(4,a.s.puzzle.moves));
   onView(withId(1008)).perform(new GeneralSwipeAction(Swipe.FAST,GeneralLocation.CENTER_RIGHT,GeneralLocation.CENTER_LEFT,Press.FINGER));
   onView(withId(202)).check(matches(withText("완성했어요!")));
   scenario.onActivity(a->{assertTrue(a.s.puzzle.solved());assertEquals(5,a.s.puzzle.moves);});
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
 @Test public void rotationAndRelaunchPreserveBoard(){
  nearComplete(false);
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.recreate();scenario.onActivity(a->{assertEquals("1,2,3,4,5,6,7,0,8",a.s.puzzle.encode());assertEquals(4,a.s.puzzle.moves);});
  }
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->assertEquals("1,2,3,4,5,6,7,0,8",a.s.puzzle.encode()));
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
   scenario.onActivity(a->{assertEquals("photo",a.s.mode);assertEquals(name[0],a.s.photo);assertNotNull(a.s.bitmap);});
  }
  PhotoStore store=new PhotoStore(context);assertTrue(store.file(name[0]).exists());store.delete(name[0]);assertFalse(store.file(name[0]).exists());
  try(java.io.InputStream in=context.getContentResolver().openInputStream(fixture)){assertNotNull(in);assertTrue(in.read()!=-1);}
 }
 @Test public void warmShareAndCorruptPhotoAreHandled()throws Exception{
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->a.onNewIntent(new Intent(a,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/photo"))));
   waitForPhoto(scenario);String[] before={""};scenario.onActivity(a->before[0]=a.s.photo);
   scenario.onActivity(a->a.onNewIntent(new Intent(a,MainActivity.class).setAction(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("content://com.shaterguy.slidingpuzzle.testphotos/broken"))));
   waitForPhoto(scenario);scenario.onActivity(a->{assertEquals(before[0],a.s.photo);assertNotNull(a.s.bitmap);assertEquals("prepare",a.s.screen);});
  }
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
