package in.celest.xash3d;
//Created by Solexid
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import in.celest.xash3d.hl.R;

public class ModPicker extends Activity {
    private String currentMod;
    public static final int sdk = Integer.valueOf(Build.VERSION.SDK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( sdk >= 21 )
            super.setTheme( 0x01030224 );
        setContentView(R.layout.activity_modpicker);
        fill();
    }

    private void fill()
    {
		
    }
	
    public void onModClick(View v)
    {
        Toast.makeText(this, "Chosen mod : " + currentMod, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetMod", currentMod);
        setResult(RESULT_OK, intent);
        finish();
    }
}
