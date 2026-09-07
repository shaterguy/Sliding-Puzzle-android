package com.shaterguy.slidingpuzzle;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.*;
/** Synthetic test-only content provider; never shipped in the game APK. */
public class FixtureProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 public String getType(Uri uri){return "image/png";}
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{
  File file=new File(getContext().getCacheDir(),"fixture-"+(uri.getPath().contains("broken")?"broken":"image"));
  try(FileOutputStream out=new FileOutputStream(file)){
   if(uri.getPath().contains("broken"))out.write(new byte[]{1,2,3});
   else {Bitmap b=Bitmap.createBitmap(600,400,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(Color.CYAN);Paint p=new Paint();p.setColor(Color.MAGENTA);c.drawRect(0,0,300,200,p);b.compress(Bitmap.CompressFormat.PNG,100,out);b.recycle();}
  }catch(IOException e){throw new FileNotFoundException(e.toString());}
  return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
 }
 public Cursor query(Uri u,String[] p,String s,String[] a,String o){return null;}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
 public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
